package com.marcoshier.services

import com.marcoshier.components.logger
import com.marcoshier.data.MediaItem
import com.marcoshier.data.MediaItems
import com.marcoshier.lib.reencodeImage
import com.marcoshier.lib.reencodeVideo
import com.marcoshier.media.isImageFile
import com.marcoshier.media.isVideoFile
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.serialization.json.Json
import java.io.File

private val logger = KotlinLogging.logger {  }

fun String.sanitize(): String {
    return this.replace(Regex("[<>:\"/\\\\|?*.]"), "")
}

class MediaService() {

    var fullSizePath = "media"
    var convertedPath = "converted"

    fun reencodeAllMediaForProject(projectName: String) {
        val allFolders = File(fullSizePath).listFiles()!!.filter { it.isDirectory }
        val folderName = allFolders.find { it.nameWithoutExtension == projectName.sanitize() }?.nameWithoutExtension

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

    fun loadMediaInfo(folderName: String): MediaItems {
        val mediaInfoFile = File("converted/$folderName/${folderName.sanitize()}.json")

        return if(!mediaInfoFile.exists()) {
            val mediaItemsMap = mutableMapOf<String, MediaItem>()
            val mediaInfoFolder = File("converted/$folderName")

            println("converted/$folderName")

            val mediaFiles = mediaInfoFolder.listFiles()!!.filter { it.isFile }

            for (media in mediaFiles) {
                if (media.isImageFile() || media.isVideoFile()) {
                    mediaItemsMap[media.name] = MediaItem(
                        media.name,
                        "",
                        media.extension
                    )
                }
            }

            val mediaItems = MediaItems(mediaItemsMap)

            val json = Json.encodeToString(mediaItems)
            mediaInfoFile.writeText(json)
            mediaItems
        } else {
            Json.decodeFromString<MediaItems>(mediaInfoFile.readText())
        }
    }

    suspend fun upload(multipart: MultiPartData): Map<String, Any> {
        val uploadedFiles = mutableListOf<File>()

        try {
            var folderName: String? = null
            val successfulUploads = mutableListOf<String>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "folderName") {
                            folderName = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name == "files") {
                            val fileName = part.originalFileName ?: "unknown"
                            val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "mov", "avi", "mkv")
                            val fileExtension = fileName.substringAfterLast('.', "").lowercase()

                            if (fileExtension in allowedExtensions) {
                                val targetFolder = File("media/${folderName?.sanitize()}")
                                targetFolder.mkdirs()

                                val targetFile = File(targetFolder, fileName.sanitize())
                                uploadedFiles.add(targetFile)

                                try {
                                    val tempFile = File(targetFile.parent, "${targetFile.name}.tmp")

                                    val channel = part.provider()
                                    tempFile.outputStream().use { output ->
                                        channel.copyTo(output)
                                    }

                                    if (tempFile.renameTo(targetFile)) {
                                        successfulUploads.add(fileName)
                                        uploadedFiles.remove(targetFile)
                                        logger.info { "Successfully uploaded: $fileName" }
                                    } else {
                                        logger.error { "Failed to rename temp file for: $fileName" }
                                        tempFile.delete()
                                    }

                                } catch (e: Exception) {
                                    logger.error(e) { "Failed to upload file: $fileName" }
                                }
                            }
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val result = mapOf(
                "success" to true,
                "message" to "Uploaded ${successfulUploads.size} files successfully",
                "files" to successfulUploads
            )
            return result

        } catch (e: Exception) {
            logger.error(e) { "Upload interrupted or failed" }

            return mapOf(
                "error" to "Upload failed: ${e.message}"
            )
        } finally {
            uploadedFiles.forEach { file ->
                try {
                    if (file.exists()) {
                        file.delete()
                        logger.info { "Cleaned up partial upload: ${file.name}" }
                    }

                    val tempFile = File(file.parent, "${file.name}.tmp")
                    if (tempFile.exists()) {
                        tempFile.delete()
                        logger.info { "Cleaned up temp file: ${tempFile.name}" }
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to cleanup file: ${file.name}" }
                }
            }
        }
    }

    fun updateCaption(folderName: String, fileName: String, newCaption: String): Boolean {
        val mediaInfoFile = File("converted/${folderName}/$folderName.json")

        if (!mediaInfoFile.exists()) {
            logger.info { "$mediaInfoFile not found" }
            return false
        } else {
            val json = Json.decodeFromString<MediaItems>(mediaInfoFile.readText())
            val mediaInfoItem = json.items[fileName]!!

            json.items.replace(fileName, mediaInfoItem.copy(caption = newCaption))

            val newJson = Json.encodeToString(json)
            mediaInfoFile.writeText(newJson)
            return true
        }
    }


}