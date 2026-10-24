package dev.lildua.oddly.ui.theme

import androidx.compose.ui.graphics.Color
import dev.lildua.oddly.core.design.PaletteSpecs

/**
 * Dark-first palette (spec §8). The app is a deep near-black space backdrop with
 * a small number of saturated neon accents used sparingly for emphasis.
 */
object OddlyColors {
    // Surfaces — near-black with a cool blue cast so the neon reads warm against it.
    val Background = Color(PaletteSpecs.dark.background)
    val Surface = Color(PaletteSpecs.dark.surface)
    val SurfaceElevated = Color(PaletteSpecs.dark.surfaceElevated)
    val SurfaceHighest = Color(PaletteSpecs.dark.surfaceHighest)
    val Outline = Color(PaletteSpecs.dark.outline)

    // Text
    val TextPrimary = Color(PaletteSpecs.dark.textPrimary)
    val TextSecondary = Color(PaletteSpecs.dark.textSecondary)
    val TextTertiary = Color(PaletteSpecs.dark.textTertiary)

    // Brand gradient stops (the "1%" wordmark and primary CTAs)
    val Pink = Color(0xFFFF7EB3)
    val Magenta = Color(0xFFE879F9)
    val Purple = Color(0xFFA78BFA)
    val Indigo = Color(0xFF818CF8)
    val Blue = Color(0xFF60A5FA)

    // Semantic accents
    val Flame = Color(PaletteSpecs.dark.flame)
    val FlameBright = Color(0xFFFFB067)
    val Success = Color(PaletteSpecs.dark.success)
    val Warning = Color(PaletteSpecs.dark.warning)
    val Danger = Color(PaletteSpecs.dark.danger)

    // Light theme surfaces — the spec lists Light as optional from MVP, so the
    // scheme exists but the app defaults to dark.
    val LightBackground = Color(PaletteSpecs.light.background)
    val LightSurface = Color(PaletteSpecs.light.surface)
    val LightSurfaceElevated = Color(PaletteSpecs.light.surfaceElevated)
    val LightTextPrimary = Color(PaletteSpecs.light.textPrimary)
    val LightTextSecondary = Color(PaletteSpecs.light.textSecondary)
    val LightSurfaceHighest = Color(PaletteSpecs.light.surfaceHighest)
    val LightTextTertiary = Color(PaletteSpecs.light.textTertiary)
    val LightFlame = Color(PaletteSpecs.light.flame)
    val LightSuccess = Color(PaletteSpecs.light.success)
    val LightWarning = Color(PaletteSpecs.light.warning)
    val LightDanger = Color(PaletteSpecs.light.danger)
    val LightOutline = Color(PaletteSpecs.light.outline)
}
