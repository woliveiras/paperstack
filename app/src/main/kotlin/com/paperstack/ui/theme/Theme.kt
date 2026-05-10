package com.paperstack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo500.copy(alpha = 0.12f),
    onPrimaryContainer = Indigo700,
    secondary = Indigo700,
    onSecondary = Color.White,
    tertiary = Indigo500,
    onTertiary = Color.White,
    surface = SurfaceWarm,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    background = SurfaceWarm,
    onBackground = OnSurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo700,
    onPrimaryContainer = Color.White,
    secondary = Indigo500,
    onSecondary = Color.White,
    tertiary = Indigo500,
    onTertiary = Color.White,
    surface = NavyDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    background = NavyDark,
    onBackground = OnSurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = Color.Black,
)

@Composable
fun PaperStackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaperStackTypography,
        shapes = PaperStackShapes,
        content = content,
    )
}
