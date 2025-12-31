package com.marcoshier

import com.marcoshier.auth.UserSession
import com.marcoshier.data.DataService
import com.marcoshier.data.GoogleSheetsService
import com.marcoshier.data.LocalService
import com.marcoshier.routes.routes
import com.marcoshier.services.AuthService
import com.marcoshier.services.MediaLookupService
import com.marcoshier.services.MediaProcessingService
import com.marcoshier.services.MediaService
import com.marcoshier.services.RefreshService
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import org.koin.dsl.module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import io.ktor.http.HttpHeaders

val isProduction = System.getenv("prod") != null

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    install(Sessions) {
        cookie<UserSession>("SESSION_ID") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 4 * 3600
            cookie.httpOnly = true
            cookie.secure = isProduction
        }
    }

    install(CORS) {
        if (isProduction) {
            allowHost(
                host = "dellecose-frontend.netlify.app/",
                schemes = listOf("https")
            )
        } else {
            anyHost()
        }

        allowCredentials = true

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { DataService() }
                single { MediaService() }
                single { MediaProcessingService() }
                single { MediaLookupService() }
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
