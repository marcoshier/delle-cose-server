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

        authRoutes()

        apiRoutes()

        mediaRoutes()


        get("/") {
            call.respond(dataService.data)
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

    }
}