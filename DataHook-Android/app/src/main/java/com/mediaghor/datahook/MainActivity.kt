package com.mediaghor.datahook

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediaghor.datahook.fcm.NotificationKeys
import com.mediaghor.datahook.ui.navigation.DataHookRoot
import com.mediaghor.datahook.ui.theme.DataHookTheme
import com.mediaghor.datahook.ui.update.UpdateDialog

class MainActivity : ComponentActivity() {

    private val deepLinkEndpointId: MutableState<Int?> = mutableStateOf(null)
    private val deepLinkSubmissionPayload: MutableState<String?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        deepLinkEndpointId.value = intent.readEndpointId()
        deepLinkSubmissionPayload.value = intent.readSubmissionPayload()

        val app = application as DataHookApp

        setContent {
            DataHookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    val updateController = app.container.updateController

                    // Non-blocking: kicks off the background check once per
                    // process; the UI below renders immediately regardless.
                    LaunchedEffect(Unit) { updateController.check() }

                    val updatePrompt by updateController.state.collectAsStateWithLifecycle()

                    RequestNotificationPermission()
                    DataHookRoot(
                        authRepository = app.container.authRepository,
                        deepLinkEndpointId = deepLinkEndpointId.value,
                        deepLinkSubmissionPayload = deepLinkSubmissionPayload.value,
                        onDeepLinkConsumed = {
                            deepLinkEndpointId.value = null
                            deepLinkSubmissionPayload.value = null
                        },
                    )

                    updatePrompt?.let { prompt ->
                        UpdateDialog(
                            prompt = prompt,
                            onLater = updateController::dismiss,
                            onUpdate = { updateController.openPlayStore(this) },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.readEndpointId()?.let { deepLinkEndpointId.value = it }
        intent.readSubmissionPayload()?.let { deepLinkSubmissionPayload.value = it }
    }

    private fun Intent?.readEndpointId(): Int? {
        val id = this?.getIntExtra(NotificationKeys.EXTRA_ENDPOINT_ID, -1) ?: -1
        return if (id > 0) id else null
    }

    private fun Intent?.readSubmissionPayload(): String? =
        this?.getStringExtra(NotificationKeys.EXTRA_SUBMISSION_PAYLOAD)?.ifBlank { null }
}

/** Asks for POST_NOTIFICATIONS on Android 13+ once, on first composition. */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* best-effort; app still works without it */ },
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
