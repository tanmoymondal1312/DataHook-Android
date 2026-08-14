package com.mediaghor.datahook.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val OUTPUT = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a", Locale.getDefault())

/** Formats an ISO-8601 timestamp for display, falling back to the raw string. */
fun formatTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val zoned = runCatching {
        OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault())
    }.recoverCatching {
        Instant.parse(raw).atZone(ZoneId.systemDefault())
    }.getOrNull()
    return zoned?.format(OUTPUT) ?: raw
}
