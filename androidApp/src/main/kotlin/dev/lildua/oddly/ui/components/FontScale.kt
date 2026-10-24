package dev.lildua.oddly.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** Font scale above which layouts switch from compact (fixed-shape) to content-sized variants. */
private const val LARGE_FONT_SCALE = 1.3f

/**
 * Limits the font scale of [content] to [max]. Used for pieces whose box size is fixed (tab bar,
 * calendar cells, streak ring, chart axis) and for the share card, whose exported image must not
 * depend on the user's system font size. Body content is never capped.
 */
@Composable
fun CappedFontScale(max: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val capped = Density(density.density, fontScale = minOf(density.fontScale, max))
    CompositionLocalProvider(LocalDensity provides capped, content = content)
}

/** Pins the font scale of [content] to exactly [scale], ignoring the system setting in both directions. */
@Composable
fun FixedFontScale(scale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = scale), content = content)
}

/** True once the user's font scale is big enough that fixed-shape layouts should relax. */
@Composable
fun isLargeFontScale(): Boolean = LocalDensity.current.fontScale > LARGE_FONT_SCALE
