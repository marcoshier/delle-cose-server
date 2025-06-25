package com.marcoshier.routes

import com.marcoshier.data.DataService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import kotlin.getValue

fun Application.routes() {

    val dataService by inject<DataService>()

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

        get("/update") {
            try {
                dataService.updateWithMedia()
                call.respond("Aggiornato alle ${LocalDateTime.now()}")
            } catch (e: Throwable) {
                call.respond("")
            }
        }


        get("/404") {
            call.respond(HttpStatusCode.NotFound)
        }


        authRoutes()

        mediaRoutes()



    }
}