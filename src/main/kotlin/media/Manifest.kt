package com.marcoshier.media

import com.marcoshier.data.MediaFile
import com.marcoshier.data.MediaFolderResponse
import com.marcoshier.lib.isImageFile
import com.marcoshier.lib.isVideoFile
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.Serializable
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.log

private val logger = KotlinLogging.logger { }

fun File.getMediaType(): String? {
    return when {
        isImageFile -> "image"
        isVideoFile -> "video"
        else -> null
    }
}

suspend fun RoutingContext.mediaManifest(folderPath: String) {
    val folder = File(folderPath.drop(1))

    if (!folder.exists() || !folder.isDirectory) {
        logger.info { "Manifest: Folder does not exist or is not a directory: $folderPath" }
        call.respondRedirect("/404")
        return
    }

    try {
        val mediaFiles = folder.listFiles()
            ?.filter { file ->
                file.isFile && (file.isImageFile || file.isVideoFile)
            }
            ?.map { file ->

                val encodedFileName = URLEncoder.encode(file.name, StandardCharsets.UTF_8)
                val encodedFolderName = URLEncoder.encode(folder.name, StandardCharsets.UTF_8)
                val fullUrl = "$encodedFolderName/$encodedFileName"

                MediaFile(
                    path = fullUrl,
                    type = file.getMediaType() ?: "unknown"
                )
            } ?: emptyList()

        val images = mediaFiles.filter { it.type == "image" }
        val videos = mediaFiles.filter { it.type == "video" }

        val response = MediaFolderResponse(
            folder = folder.name,
            totalFiles = mediaFiles.size,
            images = images,
            videos = videos
        )

        call.respond(HttpStatusCode.OK, response)

    } catch (e: Exception) {
        logger.error(e) { "Error reading media folder: $folderPath" }
        call.respond(HttpStatusCode.InternalServerError, "Error reading media folder")
    }
}