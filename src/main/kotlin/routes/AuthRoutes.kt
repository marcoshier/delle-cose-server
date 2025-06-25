package com.marcoshier.routes

import com.marcoshier.auth.UserSession
import com.marcoshier.components.loginComponent
import com.marcoshier.services.AuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.koin
import kotlin.math.log

private val logger = KotlinLogging.logger {  }

fun Route.authRoutes() {
    val authService = application.getKoin().get<AuthService>()

    get("/login") {
        val session = call.sessions.get<UserSession>()

        if (session != null && authService.isSessionAuthenticated(session.sessionId)) {
            call.respondRedirect("/media") // TODO last visited page
            return@get
        }

        call.respondText(loginComponent(), ContentType.Text.Html)
    }

    post("/login") {
        val session = call.sessions.get<UserSession>() ?: authService.createSession()
        val formParams = call.receiveParameters()
        val password = formParams["password"]

        if (password.isNullOrBlank()) {
            logger.info { "Entered password is null or blank" }
            return@post
        }

        if (authService.authenticateSession(session.sessionId, password)) {
            val authenticatedSession = session.copy(isAuthenticated = true)
            call.sessions.set(authenticatedSession)
            call.respondRedirect("/media") // TODO last visited page
        } else {
            call.sessions.set(session)
            call.respondRedirect("/login?error=invalid")
        }
    }

    post("/logout") {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            authService.logout(session.sessionId)
        }
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }

}