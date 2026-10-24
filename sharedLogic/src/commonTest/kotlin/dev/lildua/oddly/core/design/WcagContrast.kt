package dev.lildua.oddly.core.design

import kotlin.math.pow

/** WCAG 2.x contrast helpers over 0xAARRGGBB colours; alpha is ignored except in [composite]. */
fun relativeLuminance(argb: Long): Double {
    fun channel(shift: Int): Double {
        val c = ((argb shr shift) and 0xFF).toDouble() / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}

fun contrastRatio(fg: Long, bg: Long): Double {
    val a = relativeLuminance(fg)
    val b = relativeLuminance(bg)
    return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
}

/** Opaque result of painting [fg] at [alpha] over opaque [bg], like a tinted banner background. */
fun composite(fg: Long, alpha: Double, bg: Long): Long {
    fun mix(shift: Int): Long {
        val f = ((fg shr shift) and 0xFF).toDouble()
        val b = ((bg shr shift) and 0xFF).toDouble()
        return (f * alpha + b * (1 - alpha) + 0.5).toLong().coerceIn(0, 255)
    }
    return 0xFF000000 or (mix(16) shl 16) or (mix(8) shl 8) or mix(0)
}
