package com.mediaghor.datahook.util

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Fetches the current FCM registration token, or null if Firebase isn't
 * configured yet / the fetch fails. Never throws, so callers can treat push
 * registration as best-effort.
 */
suspend fun fetchFcmToken(): String? = suspendCancellableCoroutine { cont ->
    try {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> cont.resume(token) }
            .addOnFailureListener { cont.resume(null) }
    } catch (e: Exception) {
        cont.resume(null)
    }
}
