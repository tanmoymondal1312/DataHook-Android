package com.mediaghor.datahook.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mediaghor.datahook.DataHookApp
import com.mediaghor.datahook.MainActivity
import com.mediaghor.datahook.R
import com.mediaghor.datahook.data.remote.ApiConfig
import com.mediaghor.datahook.data.remote.dto.Submission
import com.mediaghor.datahook.util.SubmissionCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DataHookMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val container = (application as DataHookApp).container
        scope.launch {
            container.tokenManager.saveFcmToken(token)
            val access = container.tokenManager.accessToken.first()
            if (!access.isNullOrEmpty()) {
                container.authRepository.registerDevice(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val name = data["endpoint_name"]
            ?: data["name"]
            ?: message.notification?.title
            ?: "your endpoint"
        val body = data["body"]
            ?: message.notification?.body
            ?: "You have a new submission."
        // The backend composes the whole notification (custom title, the body
        // built from the chosen attributes, subtitle, picture and logo). The
        // `notification?.…` fallbacks only matter for older payloads.
        val title = data["title"]
            ?: message.notification?.title
            ?: "New submission · $name"
        val endpointId = (data["endpoint_id"] ?: data["endpointId"])?.toIntOrNull()
        val submissionPayload = encodeSubmission(data["submission_json"])

        // Images have to be fetched before the notification can be posted, so
        // this hops off the main thread; everything else is already in hand.
        scope.launch {
            showNotification(
                title = title,
                body = body,
                subtitle = data["subtitle"]?.ifBlank { null },
                picture = loadBitmap(data["image_url"]),
                logo = loadBitmap(data["logo_url"]),
                endpointId = endpointId,
                submissionPayload = submissionPayload,
            )
        }
    }

    /** Downloads a notification image, or returns null (never throws). */
    private suspend fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        val request = ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false) // notifications need a software bitmap
            .build()
        val result = ImageLoader(this).execute(request)
        return (result.drawable as? BitmapDrawable)?.bitmap
    }

    /** Turns the backend's raw submission JSON into the app's Base64 nav payload. */
    private fun encodeSubmission(submissionJson: String?): String? {
        if (submissionJson.isNullOrBlank()) return null
        return runCatching {
            val submission = ApiConfig.json.decodeFromString(Submission.serializer(), submissionJson)
            SubmissionCodec.encode(submission)
        }.getOrNull()
    }

    private fun showNotification(
        title: String,
        body: String,
        subtitle: String?,
        picture: Bitmap?,
        logo: Bitmap?,
        endpointId: Int?,
        submissionPayload: String?,
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (endpointId != null) {
                putExtra(NotificationKeys.EXTRA_ENDPOINT_ID, endpointId)
            }
            if (submissionPayload != null) {
                putExtra(NotificationKeys.EXTRA_SUBMISSION_PAYLOAD, submissionPayload)
            }
        }

        val notificationId = nextNotificationId()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(
            this,
            getString(R.string.default_notification_channel_id),
        )
            // The small icon must be a local drawable — Android renders it as a
            // flat silhouette, so an uploaded logo can only be the large icon.
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (logo != null) builder.setLargeIcon(logo)

        if (picture != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(picture)
                    // Collapsing the shade would otherwise leave the logo
                    // replaced by the picture's thumbnail.
                    .bigLargeIcon(logo)
                    .setSummaryText(body),
            )
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        val notification = builder.build()

        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private var counter = 1000
        private fun nextNotificationId(): Int = counter++
    }
}
