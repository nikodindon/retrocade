package com.nikodindon.retrocade.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object RetroColors {
    val Background = Color(0xFF070A12)
    val BackgroundRaised = Color(0xFF0C1220)
    val Surface = Color(0xFF111A2B)
    val SurfaceAlt = Color(0xFF18243A)
    val Grid = Color(0xFF1F3350)
    val NeonGreen = Color(0xFF39FFB6)
    val NeonCyan = Color(0xFF3FE9FF)
    val NeonMagenta = Color(0xFFFF3FA4)
    val Amber = Color(0xFFFFC93F)
    val Danger = Color(0xFFFF5C6C)
    val TextPrimary = Color(0xFFEAF7F2)
    val TextMuted = Color(0xFF8297AB)
    val TextDim = Color(0xFF53687C)
    val Black = Color(0xFF02040A)
}

private val RetroTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        letterSpacing = 2.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = 1.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 1.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        letterSpacing = 1.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.6.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp,
    ),
)

private val RetroColorScheme = darkColorScheme(
    primary = RetroColors.NeonGreen,
    onPrimary = RetroColors.Black,
    secondary = RetroColors.NeonCyan,
    onSecondary = RetroColors.Black,
    tertiary = RetroColors.NeonMagenta,
    onTertiary = RetroColors.Black,
    background = RetroColors.Background,
    onBackground = RetroColors.TextPrimary,
    surface = RetroColors.Surface,
    onSurface = RetroColors.TextPrimary,
    surfaceVariant = RetroColors.SurfaceAlt,
    onSurfaceVariant = RetroColors.TextMuted,
    outline = RetroColors.Grid,
    error = RetroColors.Danger,
)

@Composable
fun RetrocadeTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        content = content,
    )
}
