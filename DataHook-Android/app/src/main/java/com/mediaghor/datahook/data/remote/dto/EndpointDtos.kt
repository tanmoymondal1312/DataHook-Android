package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** List item shape returned by GET /api/endpoints/. */
@Serializable
data class Endpoint(
    val id: Int,
    val name: String = "",
    val description: String? = null,
    @SerialName("submission_count") val submissionCount: Int = 0,
    @SerialName("attribute_count") val attributeCount: Int = 0,
    @SerialName("notify_on_submit") val notifyOnSubmit: Boolean = true,
    @SerialName("notify_title") val notifyTitle: String = "",
    @SerialName("notify_logo_url") val notifyLogoUrl: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class Snippets(
    @SerialName("js_fetch") val jsFetch: String = "",
    val curl: String = "",
    @SerialName("html_form") val htmlForm: String = "",
)

/** Detail shape returned by GET/POST/PATCH /api/endpoints/{id}/. */
@Serializable
data class EndpointDetail(
    val id: Int,
    val name: String = "",
    val description: String? = null,
    @SerialName("api_key") val apiKey: String = "",
    @SerialName("ingest_url") val ingestUrl: String = "",
    @SerialName("notify_on_submit") val notifyOnSubmit: Boolean = true,
    @SerialName("notify_title") val notifyTitle: String = "",
    @SerialName("notify_logo_url") val notifyLogoUrl: String = "",
    @SerialName("submission_count") val submissionCount: Int = 0,
    @SerialName("attribute_count") val attributeCount: Int = 0,
    val snippets: Snippets = Snippets(),
    val attributes: List<Attribute> = emptyList(),
)

@Serializable
data class CreateEndpointRequest(
    val name: String,
    val description: String? = null,
)

/**
 * PATCH body. Only non-null fields are serialized (explicitNulls = false), so this
 * doubles as a partial update — toggle notify, rename, etc.
 */
@Serializable
data class UpdateEndpointRequest(
    val name: String? = null,
    val description: String? = null,
    @SerialName("notify_on_submit") val notifyOnSubmit: Boolean? = null,
    @SerialName("notify_title") val notifyTitle: String? = null,
)

@Serializable
data class RotateKeyResponse(
    @SerialName("api_key") val apiKey: String? = null,
    @SerialName("ingest_url") val ingestUrl: String? = null,
    val snippets: Snippets? = null,
)
