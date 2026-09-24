package com.marcoshier.lib

import com.marcoshier.isProduction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import java.io.File

private val logger = KotlinLogging.logger {  }

fun reencodeImage(
    folderName: String, imageName: String, maxHeight: Int,
    onProgress: ((Int) -> Unit)? = null,
    control: ConversionControl? = null
): File {
    val convertedPath = "converted/$folderName/$imageName"
    val outputFile = File(convertedPath)

    if (outputFile.exists()) {
        onProgress?.invoke(100)
        return outputFile
    }

    outputFile.parentFile?.mkdirs()

    val inputFile = File("media/$folderName/$imageName")
    if (!inputFile.exists()) {
        logger.error { "Input file does not exist: ${inputFile.absolutePath}" }
        return outputFile
    }

    val process = ProcessBuilder(
        if (isProduction) {
            "convert"
        } else {
            "thirdparty/im/ImageMagick-7.1.1-38-portable-Q16-x64/magick.exe"
        },
        inputFile.absolutePath,
        "-geometry",
        "x$maxHeight",
        outputFile.absolutePath
    ).redirectError(File("magick.error.txt"))
        .start()

    control?.attach(process)
    val retVal = process.waitFor()
    control?.detach(process)

    if (control?.isCancelled == true) {
        outputFile.delete()
        return outputFile
    }

    if (retVal != 0) {
        logger.warn { "conversion failed (${inputFile.path} -> ${outputFile.path} $maxHeight) $retVal" }
    } else onProgress?.invoke(100)

    return outputFile
}