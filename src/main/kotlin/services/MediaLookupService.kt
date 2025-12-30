package com.marcoshier.services

import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.KoinComponent
import java.io.File

private val logger = KotlinLogging.logger {  }

class MediaLookupService: KoinComponent {

    fun getConvertedMedia(folderName: String, fileName: String): File? {
        val file = File("converted/$folderName/$fileName")

        return if (file.exists()) {
            file
        } else {
            logger.info { "Could not find media for folder $folderName, file $fileName" }
            null
        }
    }

    fun getThumbnail(folderName: String, fileName: String): File? {
        val file = File("thumbnails/$folderName/$fileName")

        return if (file.exists()) {
            file
        } else {
            logger.info { "Could not find thumbnail for folder $folderName, file $fileName" }
            null
        }
    }


}