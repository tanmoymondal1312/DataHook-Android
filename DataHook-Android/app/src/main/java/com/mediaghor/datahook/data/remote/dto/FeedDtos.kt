package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** One item in the cross-endpoint home feed (GET /api/submissions/). */
@Serializable
data class FeedItem(
    val id: Int,
    @SerialName("endpoint_id") val endpointId: Int = 0,
    @SerialName("endpoint_name") val endpointName: String = "",
    val data: Map<String, JsonElement> = emptyMap(),
    @SerialName("header_image") val headerImage: String = "",
    @SerialName("created_at") val createdAt: String? = null,
) {
    /** The plain submission view used by the detail screen. */
    fun toSubmission(): Submission =
        Submission(id = id, data = data, headerImage = headerImage, createdAt = createdAt)
}

@Serializable
data class PaginatedFeed(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<FeedItem> = emptyList(),
)
