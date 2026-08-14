package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attribute(
    val id: Int,
    val label: String = "",
    val key: String = "",
    val type: String = "text",
    val required: Boolean = false,
    val order: Int = 0,
    @SerialName("show_in_notification") val showInNotification: Boolean = false,
    @SerialName("show_as_subtitle") val showAsSubtitle: Boolean = false,
    @SerialName("show_as_data_header") val showAsDataHeader: Boolean = false,
)

@Serializable
data class AttributeRequest(
    val label: String,
    val key: String,
    val type: String,
    val required: Boolean,
    val order: Int,
    @SerialName("show_in_notification") val showInNotification: Boolean = false,
    @SerialName("show_as_subtitle") val showAsSubtitle: Boolean = false,
    @SerialName("show_as_data_header") val showAsDataHeader: Boolean = false,
)

/** Partial update; only non-null fields serialized. */
@Serializable
data class AttributeUpdateRequest(
    val label: String? = null,
    val key: String? = null,
    val type: String? = null,
    val required: Boolean? = null,
    val order: Int? = null,
    @SerialName("show_in_notification") val showInNotification: Boolean? = null,
    @SerialName("show_as_subtitle") val showAsSubtitle: Boolean? = null,
    @SerialName("show_as_data_header") val showAsDataHeader: Boolean? = null,
)
