package com.marcoshier.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val sessionId: String,
    val expiresAt: Long,
    val isAuthenticated: Boolean = false
)