package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Public app-update configuration served by `GET /api/app-update/`.
 *
 * All fields default so a malformed or incomplete response can never crash
 * the app — [ApiConfig.json] additionally coerces wrong types to the defaults.
 */
@Serializable
data class AppUpdateConfig(
    val enabled: Boolean = false,
    /** 0 = no update, 1 = optional, 2 = mandatory. Anything else = no update. */
    val mode: Int = 0,
    @SerialName("latest_version") val latestVersion: Int = 0,
    @SerialName("minimum_version") val minimumVersion: Int = 0,
    val message: String = "",
    @SerialName("play_store_url") val playStoreUrl: String = "",
)