package com.mediaghor.datahook.util

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

/** The field types the backend accepts for an attribute. */
enum class AttributeType(val apiValue: String, val label: String) {
    TEXT("text", "Text"),
    EMAIL("email", "Email"),
    NUMBER("number", "Number"),
    PHONE("phone", "Phone"),
    DATE("date", "Date"),
    BOOLEAN("boolean", "Boolean"),

    /** Holds an image URL — can drive the notification picture and data header. */
    IMAGE("image", "Image URL");

    val keyboardOptions: KeyboardOptions
        get() = KeyboardOptions(
            keyboardType = when (this) {
                EMAIL -> KeyboardType.Email
                NUMBER -> KeyboardType.Number
                PHONE -> KeyboardType.Phone
                IMAGE -> KeyboardType.Uri
                else -> KeyboardType.Text
            }
        )

    companion object {
        fun fromApi(value: String): AttributeType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: TEXT
    }
}
