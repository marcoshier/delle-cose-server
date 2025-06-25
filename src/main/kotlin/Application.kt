package com.marcoshier

import com.marcoshier.auth.UserSession
import com.marcoshier.data.DataService
import com.marcoshier.data.GoogleSheetsService
import com.marcoshier.data.LocalService
import com.marcoshier.routes.routes
import com.marcoshier.services.AuthService
import com.marcoshier.services.MediaService
import com.marcoshier.services.RefreshService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import org.koin.dsl.module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    install(Sessions) {
        cookie<UserSession>("SESSION_ID") {
            cookie.path = "data/cookies"
            cookie.maxAgeInSeconds = 4 * 3600
            cookie.httpOnly = true
            cookie.secure = false
        }
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
            },
            module {
                single { RefreshService() }
            },
            module {
                single { AuthService() }
            }
        )
    }

    getKoin().get<DataService>()
    getKoin().get<AuthService>()

    routes()

}
