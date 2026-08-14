package com.mediaghor.datahook.data.repository

import com.mediaghor.datahook.data.local.TokenManager
import com.mediaghor.datahook.data.remote.DataHookApi
import com.mediaghor.datahook.data.remote.apiCall
import com.mediaghor.datahook.data.remote.dto.AuthResponse
import com.mediaghor.datahook.data.remote.dto.DeviceRequest
import com.mediaghor.datahook.data.remote.dto.GoogleAuthRequest
import com.mediaghor.datahook.data.remote.dto.LoginRequest
import com.mediaghor.datahook.data.remote.dto.RegisterRequest
import com.mediaghor.datahook.data.remote.dto.User
import com.mediaghor.datahook.data.remote.unwrapUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val api: DataHookApi,
    private val tokenManager: TokenManager,
) {
    val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedIn
    val userEmail: Flow<String?> = tokenManager.userEmail

    suspend fun login(email: String, password: String): Result<User> {
        val result = apiCall { api.login(LoginRequest(email.trim(), password)) }
        result.getOrNull()?.let { persist(it, email) }
        return result.map { it.user }
    }

    suspend fun register(name: String, email: String, password: String): Result<User> {
        val result = apiCall {
            api.register(RegisterRequest(email.trim(), name.trim(), password))
        }
        result.getOrNull()?.let { persist(it, email) }
        return result.map { it.user }
    }

    /**
     * Exchanges a Google ID token for a DataHook session. The backend matches
     * on the verified email, so signing in with Google lands in the same
     * account as an existing password login.
     */
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        val result = apiCall { api.googleLogin(GoogleAuthRequest(idToken)) }
        result.getOrNull()?.let { persist(it, fallbackEmail = "") }
        return result.map { it.user }
    }

    private suspend fun persist(resp: AuthResponse, fallbackEmail: String) {
        tokenManager.saveTokens(resp.access, resp.refresh)
        tokenManager.saveEmail(resp.user.email.ifBlank { fallbackEmail.trim() })
    }

    /** Registers this device's FCM token with the backend and caches it. */
    suspend fun registerDevice(fcmToken: String): Result<Unit> {
        val result = apiCall { api.registerDevice(DeviceRequest(fcmToken)) }.unwrapUnit()
        if (result.isSuccess) tokenManager.saveFcmToken(fcmToken)
        return result
    }

    /**
     * Permanently deletes the account server-side, then clears the local
     * session. The session is cleared only on success — a failed call must
     * leave the user signed in rather than stranded with no way back.
     */
    suspend fun deleteAccount(): Result<Unit> {
        val result = apiCall { api.deleteAccount() }.unwrapUnit()
        if (result.isSuccess) tokenManager.clearSession()
        return result
    }

    /** Best-effort device de-registration, then always clears the local session. */
    suspend fun logout() {
        val fcm = tokenManager.fcmToken.first()
        if (!fcm.isNullOrEmpty()) {
            runCatching { api.deleteDevice(DeviceRequest(fcm)) }
        }
        tokenManager.clearSession()
    }
}
