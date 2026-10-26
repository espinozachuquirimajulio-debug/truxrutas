package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF00296B),
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = TruxAmberSecondary,
    onSecondary = Color(0xFF261900),
    secondaryContainer = Color(0xFF523800),
    onSecondaryContainer = TruxAmberContainer,
    tertiary = Color(0xFFFFAB91),
    onTertiary = Color(0xFF4E1600),
    tertiaryContainer = TruxOrangeTertiary,
    onTertiaryContainer = TruxOrangeContainer,
    background = TruxBackgroundDark,
    onBackground = Color(0xFFE4E9F2),
    surface = TruxSurfaceDark,
    onSurface = Color(0xFFE4E9F2),
    surfaceVariant = TruxSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB8C4D9)
)

private val LightColorScheme = lightColorScheme(
    primary = TruxBluePrimary,
    onPrimary = Color.White,
    primaryContainer = TruxBlueContainer,
    onPrimaryContainer = OnTruxBlueContainer,
    secondary = TruxAmberSecondary,
    onSecondary = Color(0xFF1F1500),
    secondaryContainer = TruxAmberContainer,
    onSecondaryContainer = OnTruxAmberContainer,
    tertiary = TruxOrangeTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TruxOrangeContainer,
    onTertiaryContainer = OnTruxOrangeContainer,
    background = TruxBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = TruxSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = TruxSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
