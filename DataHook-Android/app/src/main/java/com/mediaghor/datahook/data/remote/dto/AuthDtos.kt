package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val email: String = "",
    val name: String = "",
)

@Serializable
data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class GoogleAuthRequest(
    @SerialName("id_token") val idToken: String,
)

/** Response for /register/, /login/ and /auth/google/. */
@Serializable
data class AuthResponse(
    val user: User = User(),
    val access: String = "",
    val refresh: String = "",
    /** Only sent by /auth/google/: true when the account was just created. */
    val created: Boolean = false,
)

@Serializable
data class RefreshRequest(
    val refresh: String,
)

@Serializable
data class RefreshResponse(
    val access: String = "",
)
