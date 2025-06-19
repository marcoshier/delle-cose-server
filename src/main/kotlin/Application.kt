package com.marcoshier

import com.marcoshier.data.DataService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    val dataService = DataService()

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
                dataService.updateData()
                call.respond("Aggiornato alle ${LocalDateTime.now()}")
            } catch (e: Throwable) {
                call.respond("")
            }
        }
    }

}
