package com.mediaghor.datahook.util

enum class ValueKind { PLAIN, URL, EMAIL, PHONE }

private val EMAIL_RE = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val URL_RE = Regex("^(https?://|www\\.)\\S+$", RegexOption.IGNORE_CASE)
private val PHONE_RE = Regex("^\\+?[0-9][0-9\\s\\-().]{6,}$")

/** Detects whether a submission value is a link, email, phone, or plain text. */
fun classifyValue(raw: String): ValueKind {
    val v = raw.trim()
    if (v.isEmpty()) return ValueKind.PLAIN
    return when {
        EMAIL_RE.matches(v) -> ValueKind.EMAIL
        URL_RE.matches(v) -> ValueKind.URL
        PHONE_RE.matches(v) && v.count { it.isDigit() } in 7..15 -> ValueKind.PHONE
        else -> ValueKind.PLAIN
    }
}

/** Ensures a URL has a scheme so the browser can open it. */
fun normalizedUrl(raw: String): String {
    val v = raw.trim()
    return if (v.startsWith("http://", true) || v.startsWith("https://", true)) v else "https://$v"
}

/** Strips a phone value down to digits and a leading '+' for tel: intents. */
fun telNumber(raw: String): String = buildString {
    raw.trim().forEachIndexed { index, c ->
        if (c.isDigit() || (c == '+' && index == 0)) append(c)
    }
}
