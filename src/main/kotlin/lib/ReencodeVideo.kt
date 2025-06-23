package com.marcoshier.lib

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import java.io.File

private val logger = KotlinLogging.logger {  }

fun reencodeVideo(inputVideo: String, outputVideo: String, maxHeight: Int): String? {
    if (File(outputVideo).exists()) {
        logger.info { "video $outputVideo already exist, skipping resize" }
        return null
    }

    val executable = "ffmpeg"

    logger.info { "reencoding '${inputVideo}' to '${outputVideo}' $maxHeight " }

    val retVal =
        ProcessBuilder().command(listOf(executable, "-i", inputVideo, "-vf", "scale=-2:$maxHeight", outputVideo))
            .redirectOutput(File("ffmpeg.out.txt")).redirectError(File("error.txt")).start().waitFor()

    if (retVal != 0) {
        logger.warn {  "conversion failed ($inputVideo -> $outputVideo $maxHeight) $retVal" }
    }
    logger.info { "done reencoding" }

    return outputVideo
}