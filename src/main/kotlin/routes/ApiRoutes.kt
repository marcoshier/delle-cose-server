package com.marcoshier.routes

import com.marcoshier.data.DataService
import com.marcoshier.lib.findMatch
import com.marcoshier.media.image
import com.marcoshier.media.mediaManifest
import com.marcoshier.media.streamVideo
import com.marcoshier.services.AuthService
import com.marcoshier.services.MediaService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.getValue

fun Route.apiRoutes() {
    val dataService = application.getKoin().get<DataService>()
    val mediaService = application.getKoin().get<MediaService>()

    get("/api/projects") {
        call.respond(dataService.projects)
    }

    get("/api/categories") {
        call.respond(dataService.categories)
    }

    get("/api/authors") {
        call.respond(dataService.authors)
    }


    get("/api/project/{query}") {
        val query = call.parameters["query"]
        val project = query?.let { dataService.getProject(query) }

        if (project == null) {
            call.respond("Non ho trovato un progetto con nome simile a $query")
        } else {
            call.respond(project)
        }
    }

    get("/api/author/{query}") {
        val query = call.parameters["query"]
        val author = query?.let { dataService.getAuthor(query) }

        if (author == null) {
            call.respond("Non ho trovato un autore con nome simile a $query")
        } else {
            call.respond(author)
        }
    }


    val mediaFolders = File("media/").listFiles().filter { it.isDirectory }

    get("/api/media/{query}") {
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

    get("/api/video/{folderName}/{fileName}") {
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

    get("/api/image/{folderName}/{fileName}") {

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

}