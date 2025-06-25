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

private val logger = KotlinLogging.logger {  }

fun Route.authRoutes() {
    val authService = application.getKoin().get<AuthService>()

    get("/login") {
        val session = call.sessions.get<UserSession>()

        if (session != null && authService.isSessionAuthenticated(session.sessionId)) {
            val referer = call.request.headers["Referer"]
            val destination = if (referer != null && !referer.contains("/login")) {
                referer
            } else {
                "/"
            }

            call.respondRedirect(destination)
            return@get
        }

        val referer = call.request.headers["Referer"]
        val redirectUrl = if (referer != null && !referer.contains("/login")) referer else null

        call.respondText(loginComponent(redirectUrl), ContentType.Text.Html)
    }

    post("/login") {
        var session = call.sessions.get<UserSession>()

        if (session == null || !authService.sessionExists(session.sessionId)) {
            logger.info { "No valid session found, creating new one..." }
            session = authService.createSession()
            call.sessions.set(session)
        }

        val formParams = call.receiveParameters()
        val userInput = formParams["password"]
        val redirectUrl = formParams["redirect"] // Get from form data instead of referer

        if (userInput.isNullOrBlank()) {
            logger.info { "Entered password is null or blank" }
            return@post
        }

        if (authService.authenticateSession(session.sessionId, userInput)) {
            logger.info { "Password correct, redirecting.." }

            val authenticatedSession = session.copy(isAuthenticated = true)
            call.sessions.set(authenticatedSession)

            val destination = redirectUrl ?: "/"

            logger.info { "Redirecting to: '$destination'" }
            call.respondRedirect(destination)
        } else {
            logger.info { "Input invalid password" }
            call.sessions.set(session)
            call.respondRedirect("/login?error=invalid")
        }
    }

    get("/logout") {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            authService.logout(session.sessionId)
        }
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }

}