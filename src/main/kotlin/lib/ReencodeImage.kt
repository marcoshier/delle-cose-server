package com.marcoshier.lib

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import java.io.File

private val logger = KotlinLogging.logger {  }

fun reencodeImage(folderName: String, imageName: String, maxHeight: Int): File {

    val fullSizePath = "media/$folderName/$imageName"
    val convertedPath = "converted/$folderName/$imageName"

    if (File(convertedPath).exists()) {
        logger.info { "image already exist, skipping resize" }
        return File(convertedPath)
    }

    val executable = "convert"

    val retVal =
        ProcessBuilder()
            .command(listOf(
                executable,
                "\"${File(fullSizePath).path}\"",
                "-geometry",
                "x$maxHeight",
                "\"${File(convertedPath).path}\""
            )
            )
            .redirectError(File("convert.error.txt"))
            .start().waitFor()

    if (retVal != 0) {
        logger.warn { "conversion failed ($fullSizePath -> $convertedPath $maxHeight) $retVal" }
    } else {
        logger.info { "done reencoding" }
        }


    return File(convertedPath)
}