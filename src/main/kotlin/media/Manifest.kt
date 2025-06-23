package com.marcoshier.media

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.math.log

private val logger = KotlinLogging.logger { }

@Serializable
data class MediaFile(
    val path: String,
    val type: String,
    val caption: String = "",
)

@Serializable
data class MediaFolderResponse(
    val folder: String,
    val totalFiles: Int,
    val images: List<MediaFile>,
    val videos: List<MediaFile>
)

fun File.getMediaType(): String? {
    return when {
        isImageFile() -> "image"
        isVideoFile() -> "video"
        else -> null
    }
}

suspend fun RoutingContext.mediaManifest(folderPath: String) {
    val folder = File(folderPath)

    if (!folder.exists() || !folder.isDirectory) {
        logger.info { "Folder does not exist or is not a directory: $folderPath" }
        call.respondRedirect("/404")
        return
    }

    try {
        val mediaFiles = folder.listFiles()
            ?.filter { file ->
                file.isFile && (file.isImageFile() || file.isVideoFile())
            }
            ?.map { file ->
                MediaFile(
                    path = file.absolutePath,
                    type = file.getMediaType() ?: "unknown"
                )
            } ?: emptyList()

        val images = mediaFiles.filter { it.type == "image" }
        val videos = mediaFiles.filter { it.type == "video" }

        val response = MediaFolderResponse(
            folder = folder.name,
            totalFiles = mediaFiles.size,
            images = images,
            videos = videos
        )

        call.respond(HttpStatusCode.OK, response)

    } catch (e: Exception) {
        logger.error(e) { "Error reading media folder: $folderPath" }
        call.respond(HttpStatusCode.InternalServerError, "Error reading media folder")
    }
}