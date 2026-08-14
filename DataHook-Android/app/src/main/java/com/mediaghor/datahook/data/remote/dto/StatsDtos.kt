package com.mediaghor.datahook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyCount(
    val date: String = "",
    val count: Int = 0,
)

/** GET /api/endpoints/{id}/stats/ */
@Serializable
data class EndpointStats(
    val total: Int = 0,
    val today: Int = 0,
    @SerialName("last_7_days") val last7Days: Int = 0,
    val daily: List<DailyCount> = emptyList(),
)
