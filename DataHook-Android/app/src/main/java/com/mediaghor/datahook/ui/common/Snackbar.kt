package com.mediaghor.datahook.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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

/** Returns a function that copies [text] to the clipboard and shows a "Copied" snackbar. */
@Composable
fun rememberCopyAction(): (String) -> Unit {
    val clipboard = LocalClipboardManager.current
    val snackbar = LocalSnackbarController.current
    return { text ->
        clipboard.setText(AnnotatedString(text))
        snackbar.show("Copied")
    }
}
