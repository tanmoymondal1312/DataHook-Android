package com.mediaghor.datahook.ui.common

import android.content.ClipData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.toClipEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** App-wide snackbar access so any composable can surface a short message. */
class SnackbarController(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    fun show(message: String) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message)
        }
    }
}

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("No SnackbarController provided")
}

/**
 * Returns a function that copies [text] to the clipboard, gives a light
 * haptic tick so the tap feels acknowledged, and shows a "Copied" snackbar.
 * The clipboard write is suspending on the modern [LocalClipboard] API, so
 * it is launched on the caller's scope rather than run inline.
 */
@Composable
fun rememberCopyAction(): (String) -> Unit {
    val clipboard = LocalClipboard.current
    val snackbar = LocalSnackbarController.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    return { text ->
        scope.launch {
            clipboard.setClipEntry(ClipData.newPlainText("DataHook", text).toClipEntry())
        }
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        snackbar.show("Copied")
    }
}
