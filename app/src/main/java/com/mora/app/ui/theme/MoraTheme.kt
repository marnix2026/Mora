package com.mora.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MoraLight = lightColorScheme(
    primary = Color(0xFF5B3FD0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF1B0A5C),
    secondary = Color(0xFF00897B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDEFEA),
    onSecondaryContainer = Color(0xFF00201C),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1C1B22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B22),
    surfaceVariant = Color(0xFFEEEAF7),
    onSurfaceVariant = Color(0xFF48454F)
)

private val MoraDark = darkColorScheme(
    primary = Color(0xFFC9BEFF),
    onPrimary = Color(0xFF2E1A8F),
    primaryContainer = Color(0xFF4529B7),
    onPrimaryContainer = Color(0xFFE6DEFF),
    secondary = Color(0xFF7ED8CC),
    onSecondary = Color(0xFF00372F),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Color(0xFFCDEFEA),
    background = Color(0xFF121016),
    onBackground = Color(0xFFE6E1EB),
    surface = Color(0xFF1B1920),
    onSurface = Color(0xFFE6E1EB),
    surfaceVariant = Color(0xFF2A2731),
    onSurfaceVariant = Color(0xFFCAC4D3)
)

@Composable
fun MoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MoraDark else MoraLight,
        content = content
    )
}
