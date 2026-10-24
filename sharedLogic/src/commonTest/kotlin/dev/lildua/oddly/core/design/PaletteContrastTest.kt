package dev.lildua.oddly.core.design

import dev.lildua.oddly.domain.model.Category
import kotlin.test.Test
import kotlin.test.assertTrue

class PaletteContrastTest {
    private val aa = 4.5

    private fun surfaces(s: PaletteSpec) = listOf(s.background, s.surface, s.surfaceElevated, s.surfaceHighest)

    private fun texts(s: PaletteSpec) = mapOf(
        "textPrimary" to s.textPrimary,
        "textSecondary" to s.textSecondary,
        "textTertiary" to s.textTertiary,
        "flame" to s.flame,
        "success" to s.success,
        "warning" to s.warning,
        "danger" to s.danger,
    )

    private fun assertAllPass(s: PaletteSpec) {
        for ((name, fg) in texts(s)) for (bg in surfaces(s)) {
            val r = contrastRatio(fg, bg)
            assertTrue(r >= aa, "$name ${fg.toString(16)} on ${bg.toString(16)} = $r")
        }
    }

    @Test
    fun `every text token passes AA on every dark surface`() = assertAllPass(PaletteSpecs.dark)

    @Test
    fun `every text token passes AA on every light surface`() = assertAllPass(PaletteSpecs.light)

    @Test
    fun `tertiary text stays dimmer than secondary in both themes`() {
        val d = PaletteSpecs.dark
        val l = PaletteSpecs.light
        assertTrue(relativeLuminance(d.textTertiary) < relativeLuminance(d.textSecondary))
        assertTrue(relativeLuminance(l.textTertiary) > relativeLuminance(l.textSecondary))
    }

    @Test
    fun `category colours pass AA as text on dark surfaces`() {
        val cats = Category.entries
        assertTrue(cats.size == 6)
        for (c in cats) for (bg in surfaces(PaletteSpecs.dark)) {
            val r = contrastRatio(c.colorHex, bg)
            assertTrue(r >= aa, "${c.name} on ${bg.toString(16)} = $r")
        }
    }

    @Test
    fun `light tints keep semantic text readable`() {
        val l = PaletteSpecs.light
        val neonSuccess = PaletteSpecs.dark.success
        val neonWarning = PaletteSpecs.dark.warning
        for (bg in surfaces(l)) {
            val s = contrastRatio(l.success, composite(neonSuccess, 0.14, bg))
            val w12 = contrastRatio(l.warning, composite(neonWarning, 0.12, bg))
            val w16 = contrastRatio(l.warning, composite(neonWarning, 0.16, bg))
            assertTrue(s >= aa, "success tint on ${bg.toString(16)} = $s")
            assertTrue(w12 >= aa, "warning 12% tint on ${bg.toString(16)} = $w12")
            assertTrue(w16 >= aa, "warning 16% tint on ${bg.toString(16)} = $w16")
            val f = contrastRatio(l.flame, composite(PaletteSpecs.dark.flame, 0.14, bg))
            assertTrue(f >= aa, "flame tint on ${bg.toString(16)} = $f")
        }
    }

    @Test
    fun `dark tints keep semantic text readable`() {
        val d = PaletteSpecs.dark
        for (bg in surfaces(d)) {
            assertTrue(contrastRatio(d.success, composite(d.success, 0.14, bg)) >= aa)
            assertTrue(contrastRatio(d.warning, composite(d.warning, 0.16, bg)) >= aa)
        }
    }
}
