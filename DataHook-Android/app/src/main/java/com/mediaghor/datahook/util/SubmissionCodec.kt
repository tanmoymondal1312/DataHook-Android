package com.mediaghor.datahook.util

import android.util.Base64
import com.mediaghor.datahook.data.remote.ApiConfig
import com.mediaghor.datahook.data.remote.dto.Submission

/**
 * Encodes a [Submission] as a URL-safe Base64 JSON string so it can travel as a
 * navigation argument (and later, inside an FCM payload) without escaping issues.
 */
object SubmissionCodec {

    private const val FLAGS = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun encode(submission: Submission): String {
        val json = ApiConfig.json.encodeToString(Submission.serializer(), submission)
        return Base64.encodeToString(json.toByteArray(Charsets.UTF_8), FLAGS)
    }

    fun decode(payload: String): Submission? = runCatching {
        val json = String(Base64.decode(payload, FLAGS), Charsets.UTF_8)
        ApiConfig.json.decodeFromString(Submission.serializer(), json)
    }.getOrNull()
}
