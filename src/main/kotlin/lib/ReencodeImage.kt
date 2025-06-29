package com.marcoshier.lib

import com.marcoshier.isProduction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import java.io.File

private val logger = KotlinLogging.logger {  }
fun reencodeImage(folderName: String, imageName: String, maxHeight: Int): File {
    val convertedPath = "converted/$folderName/$imageName"
    val outputFile = File(convertedPath)

    if (outputFile.exists()) {
        //logger.info { "image already exist, skipping resize" }
        return outputFile
    }

    outputFile.parentFile?.mkdirs()

    val inputFile = File("media/$folderName/$imageName")
    if (!inputFile.exists()) {
        logger.error { "Input file does not exist: ${inputFile.absolutePath}" }
        return outputFile
    }

    val retVal = ProcessBuilder(
        if (isProduction) {
            "magick"
        } else {
            "thirdparty/im/ImageMagick-7.1.1-38-portable-Q16-x64/magick.exe"
        },
        inputFile.absolutePath,
        "-geometry",
        "x$maxHeight",
        outputFile.absolutePath
    ).redirectError(File("magick.error.txt"))
        .start().waitFor()

    if (retVal != 0) {
        logger.warn { "conversion failed (${inputFile.path} -> ${outputFile.path} $maxHeight) $retVal" }
    } else {
        logger.info { "done reencoding" }
    }

    return outputFile
}