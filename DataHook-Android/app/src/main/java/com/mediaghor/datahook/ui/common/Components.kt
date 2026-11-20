package com.mediaghor.datahook.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mediaghor.datahook.ui.theme.MonoTextStyle
import com.mediaghor.datahook.ui.theme.*

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Shapes & Spacing (mission spec: 4/8/12/16/20/24dp; shapes 8/12/16/20dp)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

val ShapeSmall = RoundedCornerShape(8.dp)
val ShapeInput = RoundedCornerShape(12.dp)
val ShapeCard = RoundedCornerShape(16.dp)
val ShapeDialog = RoundedCornerShape(20.dp)

val Spacing4 = 4.dp
val Spacing8 = 8.dp
val Spacing12 = 12.dp
val Spacing16 = 16.dp
val Spacing20 = 20.dp
val Spacing24 = 24.dp

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Buttons
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/** Primary CTA button — branded primary blue, white text, 50dp height. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        ),
        shape = ShapeCard,
        modifier = modifier.height(50.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Secondary button — transparent/surface background, primary text, subtle outline. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        shape = ShapeCard,
        modifier = modifier.height(50.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Destructive button — red outline; filled red only when [filled] is true. */
@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
) {
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = dh_errorLight,
                contentColor = dh_onPrimaryLight,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            ),
            shape = ShapeCard,
            modifier = modifier.height(50.dp),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = dh_errorLight,
            ),
            shape = ShapeCard,
            modifier = modifier.height(50.dp),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Compact text button for secondary actions. */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        ),
        modifier = modifier,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Icon button with minimum 48dp touch target and content description. */
@Composable
fun DataHookIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String,
    icon: ImageVector,
    tint: Color? = null,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .padding(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = tint ?: Color.Unspecified,
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Cards & Containers
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/** Standard card container — subtle outline, 16dp radius, no heavy shadow. */
@Composable
fun DataHookCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/** Section heading — title + optional subtitle. */
@Composable
fun SectionHeading(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Compact statistic card — large value, small label. */
@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = dh_secondarySurfaceLight,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Text Fields
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/** Standard outlined text field with label, optional error and helper text. */
@Composable
fun DataHookTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    helperText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Unspecified,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation? = null,
    trailingIcon: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val showPasswordState = remember { mutableStateOf(false) }
    val effectiveTransformation = if (visualTransformation != null) visualTransformation else if (showPasswordState.value) {
        androidx.compose.ui.text.input.VisualTransformation.None
    } else {
        androidx.compose.ui.text.input.PasswordVisualTransformation()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { placeholder?.let { Text(it) } },
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        supportingText = {
            if (errorText != null) {
                Text(errorText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            } else if (helperText != null) {
                Text(helperText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        visualTransformation = effectiveTransformation,
        trailingIcon = {
            if (visualTransformation != null && trailingIcon != null) {
                IconButton(onClick = trailingIcon) {
                    Icon(
                        imageVector = if (showPasswordState.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPasswordState.value) "Hide password" else "Show password",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Chips & Badges
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/** Status chip — small, rounded, colored by role. */
@Composable
fun StatusChip(
    text: String,
    role: ChipRole = ChipRole.Secondary,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when (role) {
        ChipRole.Primary -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ChipRole.Secondary -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
        ChipRole.Success -> Pair(
            dh_successLight.copy(alpha = 0.10f),
            dh_successLight,
        )
        ChipRole.Warning -> Pair(
            dh_warningLight.copy(alpha = 0.10f),
            dh_warningLight,
        )
        ChipRole.Error -> Pair(
            dh_errorLight.copy(alpha = 0.10f),
            dh_errorLight,
        )
    }
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

enum class ChipRole { Primary, Secondary, Success, Warning, Error }

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CopyableField & CodeBlock (improved)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * A labelled monospace value (URL / API key) with a copy button.
 * The value scrolls horizontally instead of wrapping.
 * [onShare] optionally adds a share button (non-secret values only).
 */
@Composable
fun CopyableField(
    label: String,
    value: String,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShare: (() -> Unit)? = null,
    mask: Boolean = false,
) {
    val displayedValue = if (mask && value.isNotBlank()) "●".repeat(value.length) else value
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            color = dh_secondarySurfaceLight,
            shape = ShapeInput,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayedValue.ifBlank { "—" },
                    style = MonoTextStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 14.dp, top = 14.dp, bottom = 14.dp),
                )
                IconButton(onClick = { onCopy(value) }, enabled = value.isNotBlank()) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copy $label",
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (onShare != null) {
                    IconButton(onClick = onShare, enabled = value.isNotBlank()) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share $label",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Horizontally-scrollable monospace code block with copy button. */
@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = dh_secondarySurfaceLight,
        shape = ShapeInput,
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (onCopy != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = { onCopy() }) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copy code",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = code.ifBlank { "// No snippet available" },
                style = MonoTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp),
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Existing reusable components kept for compatibility
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/** Centered loading spinner filling the available space. */
@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Friendly empty state with an icon, title, subtitle, and optional action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Inline error surface used under forms and network actions. */
@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = ShapeCard,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

/** Small rounded chip, e.g. for attribute type (legacy name = Pill). */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}
