package com.mediaghor.datahook.data.remote

import com.mediaghor.datahook.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer <access>` to every request except the auth
 * endpoints (login/register/refresh), which must stay unauthenticated.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val skipAuth = path.endsWith("/api/auth/login/") ||
            path.endsWith("/api/auth/register/") ||
            path.endsWith("/api/auth/refresh/")

        if (skipAuth) return chain.proceed(request)

        val token = runBlocking { tokenManager.accessToken.first() }
        val authed = if (!token.isNullOrEmpty()) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(authed)
    }
}
