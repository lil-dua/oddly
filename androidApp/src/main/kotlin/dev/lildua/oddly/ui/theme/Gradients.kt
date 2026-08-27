package dev.lildua.oddly.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The gradient vocabulary of the app. Kept in one place so the pink→purple→blue
 * sweep stays identical across the logo, CTAs, progress bars and share card.
 */
object OddlyGradients {

    val brandStops = listOf(
        OddlyColors.Pink,
        OddlyColors.Magenta,
        OddlyColors.Purple,
        OddlyColors.Indigo,
    )

    /** Primary CTA fill — horizontal pink → purple. */
    val primaryButton = Brush.horizontalGradient(
        listOf(OddlyColors.Pink, OddlyColors.Magenta, OddlyColors.Purple),
    )

    /** Logo / headline text fill. */
    val brandText = Brush.horizontalGradient(
        listOf(OddlyColors.Pink, OddlyColors.Purple, OddlyColors.Blue),
    )

    /** Level and XP progress bars. */
    val progress = Brush.horizontalGradient(
        listOf(OddlyColors.Warning, OddlyColors.Pink, OddlyColors.Purple),
    )

    /** Streak / fire accents. */
    val flame = Brush.horizontalGradient(
        listOf(OddlyColors.FlameBright, OddlyColors.Flame),
    )

    /** Full-bleed sweep used by the splash ring and the streak halo. */
    val brandSweep = Brush.sweepGradient(
        brandStops + OddlyColors.Pink,
    )

    /** A soft category-tinted wash for category cards and chips. */
    fun categoryWash(color: Color) = Brush.verticalGradient(
        listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.06f)),
    )

    /** Radial glow behind hero elements. */
    fun glow(color: Color, alpha: Float = 0.35f) = Brush.radialGradient(
        listOf(color.copy(alpha = alpha), Color.Transparent),
    )
}
