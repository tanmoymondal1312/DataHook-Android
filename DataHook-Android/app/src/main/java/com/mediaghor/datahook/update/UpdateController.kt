package com.mediaghor.datahook.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mediaghor.datahook.data.repository.UpdateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Process-wide, lifecycle-safe update check.
 *
 * - Runs once per process ([check] is idempotent), so Activity recreation,
 *   rotation and background/foreground cycles never produce a second dialog.
 * - Runs on its own [CoroutineScope] that survives configuration changes and
 *   never blocks startup: the app UI renders immediately and the dialog
 *   appears only if/when the backend says an update is needed.
 * - Every failure (timeout, HTTP error, malformed JSON, no network) is
 *   swallowed: the app simply continues without any update UI.
 */
class UpdateController(
    private val repository: UpdateRepository,
    private val currentVersionCode: Int,
) {

    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow<UpdatePrompt?>(null)
    val state: StateFlow<UpdatePrompt?> = _state.asStateFlow()

    /** Starts the background update check; safe to call any number of times. */
    fun check() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            val config = repository.fetch().getOrNull() ?: return@launch
            val prompt = decideUpdate(config, currentVersionCode)
            if (prompt != null) _state.value = prompt
        }
    }

    /** User chose "Later" on an optional update — hide the prompt. */
    fun dismiss() {
        _state.value = null
    }

    /**
     * Opens the Play Store listing. Uses the backend-provided URL; when that
     * is missing or nothing can handle it, falls back to the Play Store app
     * (`market://`) and finally the Play Store web page in the browser.
     */
    fun openPlayStore(context: Context) {
        val prompt = _state.value ?: return
        val packageName = context.packageName
        val configuredUrl = prompt.config.playStoreUrl.trim()
            .ifBlank { "https://play.google.com/store/apps/details?id=$packageName" }

        val opened = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(configuredUrl)))
        }.isSuccess

        if (!opened) {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
                )
            }.onFailure {
                runCatching {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                        ),
                    )
                }
            }
        }
    }
}