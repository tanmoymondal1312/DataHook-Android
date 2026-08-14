package com.mediaghor.datahook.update

import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig

/**
 * Version-based update decision, shared between the controller and tests.
 *
 *   mode 0 (or disabled / unknown mode) → nothing
 *   mode 1 (optional) → prompt when current < latest_version
 *   mode 2 (mandatory) → prompt when current < minimum_version
 */
fun decideUpdate(config: AppUpdateConfig, currentVersionCode: Int): UpdatePrompt? {
    if (!config.enabled) return null
    return when (config.mode) {
        1 -> if (currentVersionCode < config.latestVersion) {
            UpdatePrompt(config, mandatory = false)
        } else {
            null
        }
        2 -> if (currentVersionCode < config.minimumVersion) {
            UpdatePrompt(config, mandatory = true)
        } else {
            null
        }
        else -> null
    }
}

/** A resolved prompt to show, derived from the backend configuration. */
data class UpdatePrompt(
    val config: AppUpdateConfig,
    val mandatory: Boolean,
) {
    val title: String get() = if (mandatory) "Update Required" else "Update Available"
    val message: String
        get() = config.message.ifBlank {
            if (mandatory) {
                "A new version of the app is required to continue. " +
                    "Please update the app to keep using it."
            } else {
                "A new version of the app is available. Update now to get " +
                    "the latest features and improvements."
            }
        }
}