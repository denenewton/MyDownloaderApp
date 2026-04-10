package com.example.boilerplate.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Apple Music Red Color
val AppleRed = Color(0xFFFA243C)

/**
 * Light color scheme based on Apple Music's clean white aesthetic.
 */
val AppleLightColorScheme = lightColorScheme(
    primary = AppleRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF0F2),
    onPrimaryContainer = AppleRed,
    secondary = AppleRed,
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

/**
 * Dark color scheme based on Apple Music's dark mode.
 */
val AppleDarkColorScheme = darkColorScheme(
    primary = AppleRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D000A),
    onPrimaryContainer = AppleRed,
    secondary = AppleRed,
    onSecondary = Color.White,
    background = Color.Black,
    surface = Color(0xFF1C1C1E),
    onBackground = Color.White,
    onSurface = Color.White,
)

/**
 * Custom Typography to simulate the Apple/iTunes style.
 * Uses SansSerif as base for better cross-platform compatibility.
 */
@Composable
fun appTypography(): Typography {
    val baseFont = FontFamily.SansSerif
    return Typography(
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = baseFont, fontWeight = FontWeight.Bold),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = baseFont, fontWeight = FontWeight.Bold),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = baseFont, fontWeight = FontWeight.SemiBold),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = baseFont, fontWeight = FontWeight.SemiBold),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = baseFont),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = baseFont),
        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = baseFont, fontWeight = FontWeight.Medium)
    )
}
