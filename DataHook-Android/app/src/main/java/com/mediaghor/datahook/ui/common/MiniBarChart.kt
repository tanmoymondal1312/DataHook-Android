package com.mediaghor.datahook.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A lightweight single-series bar chart (e.g. daily submission counts). Bars use
 * the accent color and scale to the series max; a faint track hints the full height.
 */
@Composable
fun MiniBarChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)

    Canvas(modifier) {
        val n = values.size.coerceAtLeast(1)
        val slot = size.width / n
        val barWidth = (slot * 0.62f).coerceIn(1.5.dp.toPx(), slot)
        val corner = CornerRadius(barWidth / 3f, barWidth / 3f)

        values.forEachIndexed { index, value ->
            val left = index * slot + (slot - barWidth) / 2f
            // Faint full-height track.
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = corner,
                alpha = 0.4f,
            )
            // Value bar.
            val barHeight = (value.toFloat() / max) * size.height
            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner,
                )
            }
        }
    }
}
