package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class Submission(
    val id: Int,
    /** Arbitrary key -> value payload the external caller POSTed. */
    val data: Map<String, JsonElement> = emptyMap(),
    /** URL of the endpoint's data-header image for this submission, or "". */
    @SerialName("header_image") val headerImage: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

/** DRF-style paginated response (page size 20). */
@Serializable
data class PaginatedSubmissions(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<Submission> = emptyList(),
)

/** Renders a JSON payload value as a readable string for the UI. */
fun JsonElement.displayValue(): String = when (this) {
    is JsonNull -> "—"
    is JsonPrimitive -> content
    else -> toString()
}
