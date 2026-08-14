package com.mediaghor.datahook.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.mediaghor.datahook.util.ValueKind
import com.mediaghor.datahook.util.classifyValue
import com.mediaghor.datahook.util.normalizedUrl
import com.mediaghor.datahook.util.telNumber

/**
 * Renders a submission value. If it looks like a URL / email / phone number it
 * becomes a tappable link that opens the browser / mail app / dialer.
 */
@Composable
fun LinkifiedValue(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val trimmed = value.trim()
    val kind = remember(trimmed) { classifyValue(trimmed) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    if (kind == ValueKind.PLAIN) {
        Text(text = value, style = style, modifier = modifier)
        return
    }

    Text(
        text = value,
        style = style.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = modifier.clickable {
            when (kind) {
                ValueKind.URL -> runCatching { uriHandler.openUri(normalizedUrl(trimmed)) }
                ValueKind.EMAIL -> context.launch(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")))
                ValueKind.PHONE -> context.launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telNumber(trimmed)}")))
                ValueKind.PLAIN -> Unit
            }
        },
    )
}

private fun Context.launch(intent: Intent) {
    runCatching { startActivity(intent) }
}
