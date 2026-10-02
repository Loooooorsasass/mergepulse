package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightStudioColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFFD97706),
    background = StudioBg,
    surface = StudioSurface,
    surfaceVariant = StudioSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = DangerRed
)

@Composable
fun MergePulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightStudioColorScheme,
        typography = Typography,
        content = content
    )
}
