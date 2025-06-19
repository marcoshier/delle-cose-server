package com.marcoshier

import com.marcoshier.data.DataService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
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
