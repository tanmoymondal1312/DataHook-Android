package com.mediaghor.datahook.update

import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig

/**
 * Update decision, driven purely by the backend `enabled` + `mode` fields —
 * the version strings are display-only and never gate the prompt.
 *
 *   disabled or mode 0 (or unknown mode) → nothing
 *   mode 1 (optional) → prompt, user can skip
 *   mode 2 (mandatory) → prompt, cannot be dismissed
 */
fun decideUpdate(config: AppUpdateConfig): UpdatePrompt? {
    if (!config.enabled) return null
    return when (config.mode) {
        1 -> UpdatePrompt(config, mandatory = false)
        2 -> UpdatePrompt(config, mandatory = true)
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