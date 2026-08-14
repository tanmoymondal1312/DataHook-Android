package com.mediaghor.datahook.data.remote

import kotlinx.serialization.json.Json

/**
 * Single source of truth for the backend base URL and the shared JSON config.
 * Change [BASE_URL] here if you point the app at a different deployment.
 */
object ApiConfig {
    // NOTE: must end with a trailing slash for Retrofit relative paths to resolve.
    const val BASE_URL = "https://datahook.mediaghor.com/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        isLenient = true
    }
}
