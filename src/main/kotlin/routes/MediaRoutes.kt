package com.marcoshier.routes

import com.marcoshier.auth.requireAuth
import com.marcoshier.data.DataService
import com.marcoshier.lib.findMatch
import com.marcoshier.media.gallery
import com.marcoshier.media.image
import com.marcoshier.media.mediaManifest
import com.marcoshier.media.streamVideo
import com.marcoshier.services.MediaProcessingService
import com.marcoshier.services.MediaProgressService
import com.marcoshier.services.MediaService
import com.marcoshier.services.ProjectProgress
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.utils.io.jvm.javaio.copyTo
import org.koin.core.component.inject
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import kotlin.getValue

private val logger = KotlinLogging.logger {  }

fun Route.mediaRoutes() {
    val dataService = application.getKoin().get<DataService>()
    val mediaService = application.getKoin().get<MediaService>()
    val mediaProcessingService = application.getKoin().get<MediaProcessingService>()
    val mediaProgressService = application.getKoin().get<MediaProgressService>()

    val mediaFolders = File("media/").listFiles().filter { it.isDirectory }

    get("/media/{query}") {
        val projectName = call.parameters["query"]
        val project = projectName?.let { dataService.getProject(projectName) }

        if (project == null) {
            call.respond("Non ho trovato una cartella media con nome simile a $projectName")
        } else {
            val dirName = findMatch(mediaFolders.map { it.nameWithoutExtension }, project.name)

            if (dirName == null) {
                call.respond("Cartella media non trovata con nome simile a ${project.name}")
                return@get
            }

            gallery(project.name, "/media/$dirName")
        }
    }


    post("/update-caption") {
        requireAuth(call) {
            val formParams = call.receiveParameters()
            val fileName = formParams["filename"]
            val folderName = formParams["foldername"]
            val caption = formParams["caption"]

            if (caption == null || folderName == null || fileName == null) {
                logger.info { "incomplete data received $fileName, $folderName, $caption" }
                return@requireAuth
            }

            val success = mediaService.updateCaption(folderName, fileName, caption)

            if (success) {
                val referer = call.request.headers["Referer"] ?: "/"
                call.respondRedirect(referer)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update caption")
            }
        }
    }

    post("/upload-media") {
        requireAuth(call) {
            val multipart = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 500)
            val result = mediaService.upload(multipart)
            call.respond(result.response)

            val folder = result.folder
            if (!folder.isNullOrBlank() && result.batch.isNotEmpty()) {
                mediaProcessingService.processMedia(folder, result.batch)
            }
        }
    }

    get("/media-processing-status") {
        requireAuth(call) {
            val processingProjects: List<String> = mediaProcessingService.getAllProcessingProjects()
            call.respond(processingProjects)
        }
    }

    get("/media-processing-status/{projectName}") {
        requireAuth(call) {
            val name = call.parameters["projectName"]
            val snapshot = name?.let { mediaProgressService.snapshot(it) }
                ?: ProjectProgress(name ?: "", active = false, overallPercent = 0, files = emptyList())
            call.respond(snapshot)
        }
    }

    get("/media/reprocess") {
        try {
            for (project in dataService.data.projects) {
                mediaProcessingService.processMedia(project.name)
            }
            call.respond("Reprocessing started at ${LocalDateTime.now()}")
        } catch (e: Throwable) {
            call.respond(HttpStatusCode.InternalServerError, "")
        }
    }

    delete("/delete-media") {
        requireAuth(call) {
            try {
                val multipart = call.receiveMultipart()
                var folderName: String? = null
                var filename: String? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FormItem) {
                        when (part.name) {
                            "folderName" -> folderName = part.value
                            "filename" -> filename = part.value
                        }
                    }
                    part.dispose()
                }

                if (folderName == null || filename == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf(
                            "success" to "false",
                            "error" to "Missing folderName or filename"
                        )
                    )
                    return@requireAuth
                }

                val result = mediaService.deleteMedia(folderName, filename)

                if (result["success"] == "true") {
                    call.respond(HttpStatusCode.OK, result)
                } else {
                    call.respond(HttpStatusCode.BadRequest, result)
                }

            } catch (e: Exception) {
                logger.error(e) { "Delete route failed" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "success" to "false",
                        "message" to "Server error",
                        "error" to (e.message ?: "Unknown server error")
                    )
                )
            }
        }
    }

}