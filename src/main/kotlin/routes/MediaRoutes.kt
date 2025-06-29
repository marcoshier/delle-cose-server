package com.marcoshier.routes

import com.marcoshier.auth.requireAuth
import com.marcoshier.components.logger
import com.marcoshier.data.DataService
import com.marcoshier.lib.findMatch
import com.marcoshier.media.gallery
import com.marcoshier.media.image
import com.marcoshier.media.mediaManifest
import com.marcoshier.media.streamVideo
import com.marcoshier.services.MediaService
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
import org.koin.ktor.ext.getKoin
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun Route.mediaRoutes() {
    val dataService = application.getKoin().get<DataService>()
    val mediaService = application.getKoin().get<MediaService>()

    val mediaPath = run {
        val f = File("media/")

        if (!f.exists()) {
            f.mkdir()
        }

        f
    }

    val mediaFolders = mediaPath.listFiles().filter { it.isDirectory }

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

    get("/media-manifest/{query}") {
        val projectName = call.parameters["query"]
        val project = projectName?.let { dataService.getProject(projectName) }

        if (project == null) {
            call.respond("Non ho trovato una cartella media con nome simile a $projectName")
        } else {
            val dirName = findMatch(mediaFolders.map { it.nameWithoutExtension }, project.name)

            if (dirName == null) {
                call.respond("Cartella foto non trovata con nome simile a ${project.name}")
                return@get
            }

            mediaManifest("/media/$dirName")
        }
    }

    get("/images/{query}") {
        val projectName = call.parameters["query"]
        val project = projectName?.let { dataService.getProject(projectName) }

        if (project == null) {
            call.respond("Non ho trovato una progetto con nome simile a $projectName")
        } else {
            val dirName = findMatch(mediaFolders.map { it.nameWithoutExtension }, project.name)

            if (dirName == null) {
                call.respond("Non ho trovato una cartella foto con nome simile a ${project.name}")
                return@get
            }

            gallery(project.name, "/media/$dirName", photos = true, videos = false)
        }
    }

    get("/videos/{query}") {
        val projectName = call.parameters["query"]
        val project = projectName?.let { dataService.getProject(projectName) }

        if (project == null) {
            call.respond("Non ho trovato un progetto con nome simile a $projectName")
        } else {
            val dirName = findMatch(mediaFolders.map { it.nameWithoutExtension }, project.name)

            if (dirName == null) {
                call.respond("Non ho trovato una cartella foto con nome simile a ${project.name}")
                return@get
            }

            gallery(project.name, "/media/$dirName", photos = false, videos = true)
        }
    }



    get("/video/{folderName}/{fileName}") {
        val folderName = call.parameters["folderName"]?.let {
            URLDecoder.decode(it, StandardCharsets.UTF_8)
        } ?: return@get call.respond(HttpStatusCode.BadRequest)

        val fileName = call.parameters["fileName"]?.let {
            URLDecoder.decode(it, StandardCharsets.UTF_8)
        } ?: return@get call.respond(HttpStatusCode.BadRequest)

        val convertedVideo = mediaService.getConvertedVideo(folderName, fileName)

        if (convertedVideo != null && convertedVideo.exists()) {
            streamVideo(convertedVideo.path)
        } else {
            call.respondRedirect("/404")
        }
    }

    get("/image/{folderName}/{fileName}") {

        val folderName = call.parameters["folderName"]?.let {
            URLDecoder.decode(it, StandardCharsets.UTF_8)
        } ?: return@get call.respond(HttpStatusCode.BadRequest)

        val fileName = call.parameters["fileName"]?.let {
            URLDecoder.decode(it, StandardCharsets.UTF_8)
        } ?: return@get call.respond(HttpStatusCode.BadRequest)

        val convertedImage = mediaService.getConvertedImage(folderName, fileName)

        if (convertedImage != null && convertedImage.exists()) {
            image(convertedImage.path)
        } else {
            call.respondRedirect("/404")
        }
    }

    post("/update-caption") {
        call.requireAuth {
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
        call.requireAuth {
            val multipart = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 500)
            val result = mediaService.upload(multipart)
            call.respond(result)

            dataService.updateWithMedia()
        }
    }

    delete("/delete-media") {
        call.requireAuth {
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