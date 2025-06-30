package com.marcoshier.media

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentRange
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.ranges
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import java.io.File
import java.io.FileInputStream

private val logger = KotlinLogging.logger {  }

private fun getVideoFile(file: File): File? {
    if (!file.exists()) {
        logger.info { "Video does not exist" }
        return null
    } else {
        return file
    }
}

suspend fun RoutingContext.streamVideo(path: String) {

    val file = File(path)
    val video = getVideoFile(file)

    if (video == null) {
        call.respondRedirect("/404")
    }

    val rangesSpecifier = call.request.ranges()
    if (rangesSpecifier == null) {
        call.respondFile(file)
        return
    }

    val range = rangesSpecifier.ranges.first()
    val fileLength = file.length()

    val (rangeStart, rangeEnd) = when (range) {
        is ContentRange.Bounded -> range.from to range.to
        is ContentRange.TailFrom -> range.from to (fileLength - 1)
        is ContentRange.Suffix -> (fileLength - range.lastCount) to (fileLength - 1)
    }
    val contentLength = rangeEnd - rangeStart + 1

    call.response.header(HttpHeaders.AcceptRanges, "bytes")
    call.response.header(
        HttpHeaders.ContentRange,
        "bytes $rangeStart-$rangeEnd/$fileLength"
    )
    call.response.header(HttpHeaders.ContentLength, contentLength.toString())
    call.response.status(HttpStatusCode.PartialContent)

    FileInputStream(file).use { input ->
        call.respondOutputStream {
            input.skip(rangeStart)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRemaining = contentLength

            while (bytesRemaining > 0) {
                val bytesRead = input.read(buffer, 0, minOf(buffer.size, bytesRemaining.toInt()))
                if (bytesRead == -1) break

                write(buffer, 0, bytesRead)
                bytesRemaining -= bytesRead
                flush()
            }
        }
    }
}