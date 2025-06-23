package com.marcoshier

import com.marcoshier.data.DataService
import com.marcoshier.lib.findMatch
import com.marcoshier.media.gallery
import com.marcoshier.media.image
import com.marcoshier.media.mediaManifest
import com.marcoshier.media.streamVideo
import com.marcoshier.services.MediaService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    val mediaService = MediaService()
    val dataService = DataService(mediaService)

    routing {
        get("/") {
            call.respond(dataService.data)
        }

        get("/progetti") {
            call.respond(dataService.projects)
        }

        get("/categorie") {
            call.respond(dataService.categories)
        }

        get("/autori") {
            call.respond(dataService.authors)
        }

        get("/progetto/{query}") {
            val query = call.parameters["query"]
            val project = query?.let { dataService.getProject(query) }

            if (project == null) {
                call.respond("Non ho trovato un progetto con nome simile a $query")
            } else {
                call.respond(project)
            }
        }

        get("/autore/{query}") {
            val query = call.parameters["query"]
            val author = query?.let { dataService.getAuthor(query) }

            if (author == null) {
                call.respond("Non ho trovato un autore con nome simile a $query")
            } else {
                call.respond(author)
            }
        }


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


        get("/update") {
            try {
                dataService.update()
                call.respond("Aggiornato alle ${LocalDateTime.now()}")
            } catch (e: Throwable) {
                call.respond("")
            }
        }

        get("/404") {
            call.respond(HttpStatusCode.NotFound)
        }
    }

}
