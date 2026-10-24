package dev.lildua.oddly.core.design

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class WcagContrastTest {
    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 0.01) =
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")

    @Test
    fun `black on white is 21`() = assertNear(21.0, contrastRatio(0xFF000000, 0xFFFFFFFF))

    @Test
    fun `a colour against itself is 1`() = assertNear(1.0, contrastRatio(0xFF8A8A9C, 0xFF8A8A9C))

    @Test
    fun `ratio is symmetric`() =
        assertNear(contrastRatio(0xFF14713A, 0xFFF7F7FB), contrastRatio(0xFFF7F7FB, 0xFF14713A), 1e-9)

    @Test
    fun `777777 on white matches the published 4_48`() =
        assertNear(4.48, contrastRatio(0xFF777777, 0xFFFFFFFF))

    @Test
    fun `composite at zero alpha keeps the background and at full alpha the foreground`() {
        assertTrue(composite(0xFF4ADE80, 0.0, 0xFFF7F7FB) == 0xFFF7F7FB)
        assertTrue(composite(0xFF4ADE80, 1.0, 0xFFF7F7FB) == 0xFF4ADE80)
    }
}
