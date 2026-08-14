package com.mediaghor.datahook.util

private val NON_KEY_CHARS = Regex("[^a-z0-9]+")
private val VALID_KEY = Regex("^[a-z][a-z0-9_]*$")

/**
 * Turns a human label into a safe snake_case key.
 * e.g. "Phone Number" -> "phone_number", "E-mail!!" -> "e_mail".
 */
fun slugify(label: String): String =
    label.trim().lowercase()
        .replace(NON_KEY_CHARS, "_")
        .trim('_')

/** A key is valid if it's lowercase, starts with a letter, and has no spaces/symbols. */
fun isValidKey(key: String): Boolean = key.isNotBlank() && VALID_KEY.matches(key)
