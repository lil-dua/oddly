package dev.lildua.oddly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.model.ThemeMode

/** Rounded cards, 16–24dp radius (spec §8). */
val OddlyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Colours the app needs that Material's [androidx.compose.material3.ColorScheme]
 * has no slot for. Exposed through [LocalOddlyPalette] so screens read them the
 * same way they read `MaterialTheme.colorScheme`.
 */
@Immutable
data class OddlyPalette(
    val isDark: Boolean,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val surfaceElevated: Color,
    val surfaceHighest: Color,
    val flame: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
)

private val DarkPalette = OddlyPalette(
    isDark = true,
    textPrimary = OddlyColors.TextPrimary,
    textSecondary = OddlyColors.TextSecondary,
    textTertiary = OddlyColors.TextTertiary,
    surfaceElevated = OddlyColors.SurfaceElevated,
    surfaceHighest = OddlyColors.SurfaceHighest,
    flame = OddlyColors.Flame,
    success = OddlyColors.Success,
    warning = OddlyColors.Warning,
    danger = OddlyColors.Danger,
)

private val LightPalette = OddlyPalette(
    isDark = false,
    textPrimary = OddlyColors.LightTextPrimary,
    textSecondary = OddlyColors.LightTextSecondary,
    textTertiary = OddlyColors.LightTextSecondary.copy(alpha = 0.7f),
    surfaceElevated = OddlyColors.LightSurfaceElevated,
    surfaceHighest = Color(0xFFE6E6F0),
    flame = OddlyColors.Flame,
    success = Color(0xFF16A34A),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
)

val LocalOddlyPalette = staticCompositionLocalOf { DarkPalette }

private val DarkColors = darkColorScheme(
    primary = OddlyColors.Purple,
    onPrimary = Color(0xFF16091F),
    primaryContainer = OddlyColors.Purple.copy(alpha = 0.18f),
    onPrimaryContainer = OddlyColors.Purple,
    secondary = OddlyColors.Pink,
    onSecondary = Color(0xFF2A0A16),
    tertiary = OddlyColors.Blue,
    background = OddlyColors.Background,
    onBackground = OddlyColors.TextPrimary,
    surface = OddlyColors.Surface,
    onSurface = OddlyColors.TextPrimary,
    surfaceVariant = OddlyColors.SurfaceElevated,
    onSurfaceVariant = OddlyColors.TextSecondary,
    outline = OddlyColors.Outline,
    outlineVariant = OddlyColors.Outline.copy(alpha = 0.6f),
    error = OddlyColors.Danger,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4FF),
    onPrimaryContainer = Color(0xFF3B1E75),
    secondary = Color(0xFFDB2777),
    onSecondary = Color.White,
    tertiary = Color(0xFF2563EB),
    background = OddlyColors.LightBackground,
    onBackground = OddlyColors.LightTextPrimary,
    surface = OddlyColors.LightSurface,
    onSurface = OddlyColors.LightTextPrimary,
    surfaceVariant = OddlyColors.LightSurfaceElevated,
    onSurfaceVariant = OddlyColors.LightTextSecondary,
    outline = OddlyColors.LightOutline,
    error = Color(0xFFDC2626),
)

@Composable
fun OddlyTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    CompositionLocalProvider(LocalOddlyPalette provides if (dark) DarkPalette else LightPalette) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = OddlyTypography,
            shapes = OddlyShapes,
            content = content,
        )
    }
}

/** Shorthand for the extended palette, mirroring `MaterialTheme.colorScheme`. */
object OddlyTheme {
    val palette: OddlyPalette
        @Composable get() = LocalOddlyPalette.current
}
