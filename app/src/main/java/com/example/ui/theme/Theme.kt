package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary = AmberAccent,
    onTertiary = AmberOnAccent,
    tertiaryContainer = AmberAccentContainer,
    onTertiaryContainer = AmberOnAccentContainer,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AppOutline,
    outlineVariant = AppOutlineVariant,
    error = StatusDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = Color.White,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E4032),
    onSecondaryContainer = Color.White,
    tertiary = AmberAccent,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5D2800),
    onTertiaryContainer = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFE2E3E0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E3E0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC3C8C2),
    outline = Color(0xFF8D938D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
