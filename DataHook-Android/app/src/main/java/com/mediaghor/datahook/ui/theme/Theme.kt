package com.mediaghor.datahook.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = dh_primaryLight,
    onPrimary = dh_onPrimaryLight,
    primaryContainer = dh_secondarySurfaceLight,
    onPrimaryContainer = dh_primaryLight,
    secondary = dh_secondaryLight,
    onSecondary = dh_onSecondaryLight,
    secondaryContainer = dh_secondarySurfaceLight,
    onSecondaryContainer = dh_onSecondaryLight,
    tertiary = dh_secondaryLight,
    onTertiary = dh_onSecondaryLight,
    background = dh_backgroundLight,
    onBackground = dh_mainTextLight,
    surface = dh_surfaceLight,
    onSurface = dh_mainTextLight,
    surfaceVariant = dh_secondarySurfaceLight,
    onSurfaceVariant = dh_secondaryTextLight,
    surfaceContainer = dh_secondarySurfaceLight,
    surfaceContainerHigh = dh_secondarySurfaceLight,
    outline = dh_outlineLight,
    outlineVariant = dh_outlineLight,
    error = dh_errorLight,
    onError = dh_onPrimaryLight,
    errorContainer = dh_errorLight.copy(alpha = 0.10f),
    onErrorContainer = dh_errorLight,
)

private val DarkColors = darkColorScheme(
    primary = dh_primaryDark,
    onPrimary = dh_onPrimaryDark,
    primaryContainer = dh_secondarySurfaceDark,
    onPrimaryContainer = dh_primaryDark,
    secondary = dh_secondaryDark,
    onSecondary = dh_onSecondaryDark,
    secondaryContainer = dh_secondarySurfaceDark,
    onSecondaryContainer = dh_onSecondaryDark,
    tertiary = dh_secondaryDark,
    onTertiary = dh_onSecondaryDark,
    background = dh_backgroundDark,
    onBackground = dh_mainTextDark,
    surface = dh_surfaceDark,
    onSurface = dh_mainTextDark,
    surfaceVariant = dh_secondarySurfaceDark,
    onSurfaceVariant = dh_secondaryTextDark,
    surfaceContainer = dh_secondarySurfaceDark,
    surfaceContainerHigh = dh_secondarySurfaceDark,
    outline = dh_outlineDark,
    outlineVariant = dh_outlineDark,
    error = dh_errorDark,
    onError = dh_onPrimaryDark,
    errorContainer = dh_errorDark.copy(alpha = 0.10f),
    onErrorContainer = dh_errorDark,
)

@Composable
fun DataHookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Material You (wallpaper colors) exists only on Android 12+; older
    // devices always get the branded scheme below.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DataHookTypography,
        content = content,
    )
}
