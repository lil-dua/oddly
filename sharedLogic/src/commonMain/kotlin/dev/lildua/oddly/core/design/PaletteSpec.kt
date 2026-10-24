package dev.lildua.oddly.core.design

/**
 * Surface and text colours that flip between the dark and light themes, as 0xAARRGGBB.
 *
 * Lives in shared so both platforms build their palettes from one set of values and a unit test
 * can measure contrast on the real numbers instead of trusting two hand-copied hex lists.
 * Every text token (textPrimary..danger) must reach WCAG AA 4.5:1 on all four surfaces of its own
 * theme. Brand neon (Purple, Pink, category colours) is deliberately not here: it is a branding
 * choice, and on the light theme it still needs a darker text variant.
 */
data class PaletteSpec(
    val background: Long,
    val surface: Long,
    val surfaceElevated: Long,
    val surfaceHighest: Long,
    val outline: Long,
    val textPrimary: Long,
    val textSecondary: Long,
    val textTertiary: Long,
    val flame: Long,
    val success: Long,
    val warning: Long,
    val danger: Long,
)

object PaletteSpecs {
    val dark = PaletteSpec(
        background = 0xFF07070C,
        surface = 0xFF12121B,
        surfaceElevated = 0xFF1A1A26,
        surfaceHighest = 0xFF23232F,
        outline = 0xFF2C2C3A,
        textPrimary = 0xFFF4F4F7,
        textSecondary = 0xFFA0A0B0,
        // Was 6C6C7E (3.0-3.9:1); 8A8A9C clears 4.5 on every dark surface yet stays dimmer than secondary.
        textTertiary = 0xFF8A8A9C,
        flame = 0xFFFF7A45,
        success = 0xFF4ADE80,
        warning = 0xFFFBBF24,
        danger = 0xFFF87171,
    )

    val light = PaletteSpec(
        background = 0xFFF7F7FB,
        surface = 0xFFFFFFFF,
        surfaceElevated = 0xFFF0F0F6,
        surfaceHighest = 0xFFE6E6F0,
        outline = 0xFFE0E0EA,
        textPrimary = 0xFF14141C,
        textSecondary = 0xFF5A5A6E,
        textTertiary = 0xFF666678,
        // Deeper hues of the neon family: the neon itself is 1.4-2.8:1 on light surfaces.
        flame = 0xFFAA340A,
        success = 0xFF14713A,
        warning = 0xFFA14A0A,
        danger = 0xFFB91C1C,
    )
}
