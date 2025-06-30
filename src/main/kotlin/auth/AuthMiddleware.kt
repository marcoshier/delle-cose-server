package com.marcoshier.auth

import com.marcoshier.services.AuthService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.koin.ktor.ext.getKoin

suspend fun requireAuth(call: ApplicationCall, block: suspend () -> Unit) {
    val authService = call.getKoin().get<AuthService>()
    val session = call.sessions.get<UserSession>()

    if (session == null || !authService.isSessionAuthenticated(session.sessionId)) {
        call.respondRedirect("/login")
        return
    }

    block()
}