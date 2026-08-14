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
    /** Display-only free text (e.g. "2.4"); never used for gating. */
    @SerialName("current_version") val currentVersion: String = "",
    /** Display-only free text (e.g. "3.89") shown in the dialog when set. */
    @SerialName("new_version") val newVersion: String = "",
    val message: String = "",
    @SerialName("play_store_url") val playStoreUrl: String = "",
)