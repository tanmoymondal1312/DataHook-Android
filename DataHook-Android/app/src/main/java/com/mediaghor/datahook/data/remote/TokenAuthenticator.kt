package com.mediaghor.datahook.data.remote

import com.mediaghor.datahook.data.local.TokenManager
import com.mediaghor.datahook.data.remote.dto.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * On a 401, transparently refreshes the access token via /api/auth/refresh/ and
 * retries the original request once. If refresh fails, the session is cleared,
 * which flips [TokenManager.isLoggedIn] to false and bounces the user to Auth.
 *
 * [refreshApiProvider] is a lazy provider to break the construction cycle
 * (the authed client depends on this, this depends on a bare client).
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshApiProvider: () -> RefreshApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Give up after we've already retried once (avoid infinite loops).
        if (responseCount(response) >= 2) return null

        val refresh = runBlocking { tokenManager.refreshToken.first() }
        if (refresh.isNullOrEmpty()) return null

        val newAccess: String? = synchronized(this) {
            // Another thread may have refreshed while we waited on the lock.
            val current = runBlocking { tokenManager.accessToken.first() }
            val failedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")?.trim()

            if (!current.isNullOrEmpty() && current != failedToken) {
                current
            } else {
                requestFreshToken(refresh)
            }
        }

        if (newAccess.isNullOrEmpty()) return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun requestFreshToken(refresh: String): String? {
        return try {
            val resp = refreshApiProvider().refresh(RefreshRequest(refresh)).execute()
            val access = resp.body()?.access
            if (resp.isSuccessful && !access.isNullOrEmpty()) {
                runBlocking { tokenManager.updateAccessToken(access) }
                access
            } else {
                // Refresh token is invalid/expired -> force logout.
                runBlocking { tokenManager.clearSession() }
                null
            }
        } catch (e: Exception) {
            null // network error; leave session intact so a later retry can succeed
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
