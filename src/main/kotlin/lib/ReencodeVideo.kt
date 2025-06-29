package com.marcoshier.lib

import com.marcoshier.isProduction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import java.io.File

private val logger = KotlinLogging.logger {  }

fun reencodeVideo(folderName: String, videoName: String, maxHeight: Int): File {
    val convertedPath = "converted/$folderName/$videoName"
    val outputFile = File(convertedPath)

    if (outputFile.exists()) {
        logger.info { "video already exist, skipping resize" }
        return outputFile
    }

    outputFile.parentFile?.mkdirs()

    logger.info { "reencoding '$videoName' to '${outputFile.path}' $maxHeight" }

    val inputFile = File("media/$folderName/$videoName")
    if (!inputFile.exists()) {
        logger.error { "Input file does not exist: ${inputFile.absolutePath}" }
        return outputFile
    }

    val retVal = ProcessBuilder(
        if (isProduction) {
            "ffmpeg"
        } else {
            "thirdparty/ffmpeg/ffmpeg-7.1-essentials_build/ffmpeg.exe"
        },
        "-y",
        "-i", inputFile.absolutePath,
        "-vf", "scale=-2:$maxHeight",
        outputFile.absolutePath
    ).redirectErrorStream(true)
        .redirectOutput(File("ffmpeg.log.txt"))
        .start().waitFor()

    if (retVal != 0) {
        logger.warn { "conversion failed (${inputFile.path} -> ${outputFile.path} $maxHeight) $retVal" }
    } else {
        logger.info { "done reencoding" }
    }

    return outputFile
}