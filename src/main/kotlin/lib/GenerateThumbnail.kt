package com.marcoshier.lib

import com.marcoshier.isProduction
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {  }

fun generateThumbnails(folderName: String, mediaName: String): List<File> {
    val inputFile = File("converted/$folderName/$mediaName")

    if (!inputFile.exists()) {
        logger.error { "Input file does not exist: ${inputFile.absolutePath}" }
        return emptyList()
    }

    if (inputFile.isImageFile) {
        val thumbnailPath = "thumbnails/$folderName/$mediaName-128.png"
        val outputFile = File(thumbnailPath)

        println("full path: $thumbnailPath")

        if (outputFile.exists()) {
            return listOf(outputFile)
        }

        outputFile.parentFile?.mkdirs()

        val retVal = ProcessBuilder(
            if (isProduction) {
                "convert"
            } else {
                "thirdparty/im/ImageMagick-7.1.1-38-portable-Q16-x64/magick.exe"
            },
            inputFile.absolutePath,
            "-resize",
            "128x128^",
            "-gravity",
            "center",
            "-extent",
            "128x128",
            outputFile.absolutePath
        ).redirectError(File("magick.error.txt"))
            .start().waitFor()

        if (retVal != 0) {
            logger.warn { "thumbnail generation failed (${inputFile.path} -> ${outputFile.path}) $retVal" }
        } else {
            logger.info { "generated thumbnail" }
        }

        return listOf(outputFile)
    } else {
        val outputFiles = mutableListOf<File>()

        for (i in 0 until 10) {
            val thumbnailPath = "thumbnails/$folderName/$mediaName-128-$i.png"
            val outputFile = File(thumbnailPath)

            if (outputFile.exists()) {
                outputFiles.add(outputFile)
                continue
            }

            outputFile.parentFile?.mkdirs()

            val retVal = ProcessBuilder(
                if (isProduction) {
                    "ffmpeg"
                } else {
                    "thirdparty/ffmpeg/ffmpeg-7.1-essentials_build/ffmpeg.exe"
                },
                "-y",
                "-i", inputFile.absolutePath,
                "-vf", "select='gte(n\\,${i * 10})',scale=128:128:force_original_aspect_ratio=increase,crop=128:128",
                "-frames:v", "1",
                outputFile.absolutePath
            ).redirectErrorStream(true)
                .redirectOutput(File("ffmpeg.log.txt"))
                .start().waitFor()

            if (retVal != 0) {
                logger.warn { "thumbnail generation failed for index $i (${inputFile.path} -> ${outputFile.path}) $retVal" }
            } else {
                outputFiles.add(outputFile)
            }
        }

        return outputFiles
    }
}