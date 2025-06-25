package com.marcoshier.auth

import com.marcoshier.services.AuthService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.koin.ktor.ext.getKoin

suspend fun ApplicationCall.requireAuth(block: suspend () -> Unit) {
    val authService = getKoin().get<AuthService>()
    val session = sessions.get<UserSession>()

    if (session == null || !authService.isSessionAuthenticated(session.sessionId)) {
        respondRedirect("/login")
        return
    }

    block()
}