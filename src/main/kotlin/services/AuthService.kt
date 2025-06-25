package com.marcoshier.services

import com.marcoshier.auth.UserSession
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.util.UUID
import kotlin.getValue

private val logger = KotlinLogging.logger {  }

val PASS = "popipopi"

class AuthService: KoinComponent {
    private val refreshService by inject<RefreshService>()

    private val sessions = mutableMapOf<String, UserSession>()


    fun createSession(): UserSession {
        val sessionId = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plusSeconds(4 * 3600).epochSecond

        val session = UserSession(sessionId, expiresAt)
        sessions[sessionId] = session

        logger.info { "Created session with ID: ${sessionId.take(4)}" }

        return session
    }


    fun authenticateSession(sessionId: String, password: String): Boolean {

        logger.info { "Attempting authentication of: ${sessionId.take(4)}" }

        val session = sessions[sessionId]

        if (session == null) {
            logger.info { "Session not found with provided id: ${sessionId.take(4)}" }
            return false
        }

        if (isSessionExpired(session)) {
            logger.info { "Session expired. Please log in again." }
            sessions.remove(sessionId)
            return false
        }

        if (password == PASS) {
            sessions[sessionId] = session.copy(isAuthenticated = true)
            return true
        }

        return false

    }


    fun isSessionAuthenticated(sessionId: String): Boolean {
        val session = sessions[sessionId] ?: return false

        if (isSessionExpired(session)) {
            sessions.remove(sessionId)
            return false
        }

        return session.isAuthenticated
    }

    fun logout(sessionId: String) {
        sessions.remove(sessionId)
    }



    private fun isSessionExpired(session: UserSession): Boolean {
        return Instant.now().epochSecond > session.expiresAt
    }

    fun cleanupExpiredSessions() {
        val now = Instant.now().epochSecond
        sessions.entries.removeIf { it.value.expiresAt < now }
    }

    fun sessionExists(sessionId: String): Boolean {
        return sessions.containsKey(sessionId)
    }

    init {
        refreshService.add(::cleanupExpiredSessions)
    }

}