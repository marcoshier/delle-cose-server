package com.marcoshier

import com.marcoshier.data.DataService
import com.marcoshier.data.GoogleSheetsService
import com.marcoshier.data.LocalService
import com.marcoshier.routes.mediaRoutes
import com.marcoshier.routes.userRoutes
import com.marcoshier.services.MediaService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.time.LocalDateTime

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { DataService() }
                single { MediaService() }
            },
            module {
                single { GoogleSheetsService() }
                single { LocalService() }
            }
        )
    }

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



        mediaRoutes()

        userRoutes()


    }

}
