package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberBlack,
    secondary = CyberPurple,
    onSecondary = TextPrimary,
    tertiary = CyberGold,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberCardBorder,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
