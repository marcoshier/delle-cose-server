package com.marcoshier.services

import com.marcoshier.lib.findMatch
import com.marcoshier.lib.reencodeImage
import com.marcoshier.lib.reencodeVideo
import com.marcoshier.media.isImageFile
import com.marcoshier.media.isVideoFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import kotlin.math.PI

private val logger = KotlinLogging.logger {  }

fun String.sanitize(): String {
    return this.replace(Regex("[<>:\"/\\\\|?*.]"), "")
}

class MediaService() {

    var fullSizePath = "media"
    var convertedPath = "converted"

    fun reencodeAllMediaForProject(projectName: String) {
        val allFolders = File(fullSizePath).listFiles()!!.filter { it.isDirectory }
        val folderName = findMatch(allFolders.map { it.nameWithoutExtension }, projectName)

        var nameRef = folderName?.sanitize()

        if (folderName == null) {
            File("$fullSizePath/${projectName.sanitize()}").mkdirs()
            nameRef = projectName.sanitize()
        }

        val outputFolder = File("$convertedPath/$nameRef")
        outputFolder.mkdirs()

        val mediaFolder = File("$fullSizePath/$nameRef")
        val files = mediaFolder.listFiles().filter { it.isFile }

        for (file in files) {
            if (file.isVideoFile()) {
                reencodeVideo(nameRef!!, file.name, 1080)
            } else {
                reencodeImage(nameRef!!, file.name, 1080)
            }
        }
    }

    fun getConvertedImage(folderName: String, imageName: String): File? {
        val targetFolder = File("$convertedPath/$folderName")

        if (!targetFolder.exists()) {
            targetFolder.mkdir()
        }

        val reencoded = reencodeImage(folderName, imageName, 1080)

        return if (reencoded.exists()) {
            reencoded
        } else null
    }

    fun getConvertedVideo(folderName: String, videoName: String): File? {
        val targetFolder = File("$convertedPath/$folderName")

        if (!targetFolder.exists()) {
            targetFolder.mkdir()
        }

        val reencoded = reencodeVideo(folderName, videoName, 1080)

        return if (reencoded.exists()) {
            reencoded
        } else null
    }

}