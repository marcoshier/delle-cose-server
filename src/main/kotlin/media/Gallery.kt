package com.marcoshier.media

import com.marcoshier.components.galleryComponent
import com.marcoshier.components.mediaComponent
import com.marcoshier.components.noMediaComponent
import com.marcoshier.services.MediaService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import org.koin.ktor.ext.getKoin
import java.io.File

private val logger = KotlinLogging.logger { }


private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff")
private val videoExtensions = setOf("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v", "3gp")

fun File.isImageFile(): Boolean {
    return this.extension.lowercase() in imageExtensions
}

fun File.isVideoFile(): Boolean {
    return this.extension.lowercase() in videoExtensions
}

suspend fun RoutingContext.gallery(projectName: String, folderPath: String, photos: Boolean = true, videos: Boolean = true) {
    val mediaService = call.application.getKoin().get<MediaService>()

    val folder = File(folderPath.drop(1))

    if (!folder.exists() || !folder.isDirectory) {
        logger.info { "Gallery: Folder does not exist or is not a directory: $folderPath" }
        call.respondRedirect("/404")
        return
    }

    try {
        val mediaFiles = folder.listFiles()
            ?.filter { file -> file.isFile && ((photos && file.isImageFile()) || (videos && file.isVideoFile())) }
            ?.sortedBy { it.name }
            ?: emptyList()

        val imageCount = mediaFiles.count { it.isImageFile() }
        val videoCount = mediaFiles.count { it.isVideoFile() }

        val mediaInfo = mediaService.loadMediaInfo(folder.name)


        val sortedMediaItems = mediaInfo.items.map {
            it.key to it.value
        }.sortedByDescending { it.second.updatedAt }

        val mediaComponents = sortedMediaItems.joinToString("\n") { (filename, mediaInfoItem) ->

            val convertedFile = File("converted/${folder.name}/$filename")

            if (!convertedFile.exists()) {
                noMediaComponent(filename)
            } else {
                mediaComponent(
                    File("converted/${folder.name}/$filename"),
                    folder.name,
                    mediaInfoItem
                )
            }


        }

        call.respondText(
            galleryComponent(
                projectName,
                mediaFiles,
                imageCount,
                videoCount,
                mediaComponents
            ), ContentType.Text.Html)

    } catch (e: Exception) {
        logger.error(e) { "Error reading media folder: $folderPath" }
        call.respondText(
            """
            <!DOCTYPE html>
            <html>
            <head><title>Error</title></head>
            <body>
                <h1>Error</h1>
                <p>Unable to load media folder. -${e.printStackTrace()}</p>
            </body>
            </html>
            """.trimIndent(),
            ContentType.Text.Html,
            HttpStatusCode.InternalServerError
        )
    }
}
