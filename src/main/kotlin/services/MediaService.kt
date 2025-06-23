package com.marcoshier.services

import com.marcoshier.lib.reencodeImage
import com.marcoshier.lib.reencodeVideo
import com.marcoshier.media.isImageFile
import com.marcoshier.media.isVideoFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import kotlin.math.PI

private val logger = KotlinLogging.logger {  }

class MediaService() {

    var fullSizePath = "media"
    var convertedPath = "converted"

    fun reencodeAllMediaForProject(projectName: String) {

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
        return null
    }

}