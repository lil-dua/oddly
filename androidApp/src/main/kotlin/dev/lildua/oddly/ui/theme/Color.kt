package dev.lildua.oddly.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Dark-first palette (spec §8). The app is a deep near-black space backdrop with
 * a small number of saturated neon accents used sparingly for emphasis.
 */
object OddlyColors {
    // Surfaces — near-black with a cool blue cast so the neon reads warm against it.
    val Background = Color(0xFF07070C)
    val Surface = Color(0xFF12121B)
    val SurfaceElevated = Color(0xFF1A1A26)
    val SurfaceHighest = Color(0xFF23232F)
    val Outline = Color(0xFF2C2C3A)

    // Text
    val TextPrimary = Color(0xFFF4F4F7)
    val TextSecondary = Color(0xFFA0A0B0)
    val TextTertiary = Color(0xFF6C6C7E)

    // Brand gradient stops (the "1%" wordmark and primary CTAs)
    val Pink = Color(0xFFFF7EB3)
    val Magenta = Color(0xFFE879F9)
    val Purple = Color(0xFFA78BFA)
    val Indigo = Color(0xFF818CF8)
    val Blue = Color(0xFF60A5FA)

    // Semantic accents
    val Flame = Color(0xFFFF7A45)
    val FlameBright = Color(0xFFFFB067)
    val Success = Color(0xFF4ADE80)
    val Warning = Color(0xFFFBBF24)
    val Danger = Color(0xFFF87171)

    // Light theme surfaces — the spec lists Light as optional from MVP, so the
    // scheme exists but the app defaults to dark.
    val LightBackground = Color(0xFFF7F7FB)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceElevated = Color(0xFFF0F0F6)
    val LightTextPrimary = Color(0xFF14141C)
    val LightTextSecondary = Color(0xFF5A5A6E)
    val LightOutline = Color(0xFFE0E0EA)
}
