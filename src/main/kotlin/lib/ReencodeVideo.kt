package com.marcoshier.lib

import com.marcoshier.isProduction
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import kotlinx.io.IOException
import java.io.File

private val logger = KotlinLogging.logger {  }
fun reencodeVideo(
    folderName: String,
    videoName: String,
    maxHeight: Int,
    onProgress: ((Int) -> Unit)? = null,
    control: ConversionControl? = null
): File {
    val convertedPath = "converted/$folderName/${videoName.dropLast(3)}mp4"
    val outputFile = File(convertedPath)

    if (outputFile.exists()) { onProgress?.invoke(100); return outputFile }
    outputFile.parentFile?.mkdirs()

    val inputFile = File("media/$folderName/$videoName")
    if (!inputFile.exists()) {
        logger.error { "Input file does not exist: ${inputFile.absolutePath}" }
        return outputFile
    }

    val process = ProcessBuilder(
        if (isProduction) "ffmpeg"
        else "thirdparty/ffmpeg/ffmpeg-7.1-essentials_build/ffmpeg.exe",
        "-y", "-i", inputFile.absolutePath,
        "-vf", "scale=-2:$maxHeight",
        "-c:v", "libx264",
        "-preset", "veryfast",
        "-crf", "24",
        "-c:a", "aac",
        "-b:a", "128k",
        "-progress", "pipe:1",
        outputFile.absolutePath
    ).redirectErrorStream(true).start()

    control?.attach(process)

    var durationSeconds = 0.0
    val durationRegex = Regex("""Duration:\s*(\d+):(\d+):(\d+(?:\.\d+)?)""")
    val outTimeRegex = Regex("""out_time=(\d+):(\d+):(\d+(?:\.\d+)?)""")
    val tail = ArrayDeque<String>()

    try {
        process.inputStream.bufferedReader().useLines { lines ->
            for (line in lines) {
                tail.addLast(line); if (tail.size > 50) tail.removeFirst()
                if (durationSeconds <= 0.0) durationRegex.find(line)?.let { m ->
                    val (h, mn, s) = m.destructured
                    durationSeconds = h.toInt() * 3600 + mn.toInt() * 60 + s.toDouble()
                }
                if (durationSeconds > 0.0) outTimeRegex.find(line)?.let { m ->
                    val (h, mn, s) = m.destructured
                    val cur = h.toInt() * 3600 + mn.toInt() * 60 + s.toDouble()
                    onProgress?.invoke(((cur / durationSeconds) * 100).toInt().coerceIn(0, 99))
                }
            }
        }
    } catch (e: IOException) {
        if (control?.isCancelled != true) throw e
    }

    val retVal = process.waitFor()
    control?.detach(process)

    if (control?.isCancelled == true) {
        outputFile.delete()
        logger.info { "reencode cancelled: $videoName" }
        return outputFile
    }

    if (retVal != 0) {
        logger.warn { "conversion failed (${inputFile.path} -> ${outputFile.path} $maxHeight) $retVal" }
        runCatching {
            File(outputFile.parentFile, "${outputFile.nameWithoutExtension}.ffmpeg.log")
                .writeText(tail.joinToString("\n"))
        }
    } else onProgress?.invoke(100)

    return outputFile
}