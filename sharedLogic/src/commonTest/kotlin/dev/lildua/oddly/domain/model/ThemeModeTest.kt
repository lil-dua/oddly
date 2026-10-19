package dev.lildua.oddly.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeModeTest {

    @Test
    fun `light is never dark`() {
        assertFalse(ThemeMode.LIGHT.isDark(systemIsDark = true))
        assertFalse(ThemeMode.LIGHT.isDark(systemIsDark = false))
    }

    @Test
    fun `dark is always dark`() {
        assertTrue(ThemeMode.DARK.isDark(systemIsDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemIsDark = false))
    }

    @Test
    fun `system follows the device setting`() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemIsDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemIsDark = false))
    }

    @Test
    fun `every mode is covered`() {
        // A new ThemeMode must get its own isDark test above.
        assertEquals(
            setOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
            ThemeMode.entries.toSet(),
        )
        assertEquals(3, ThemeMode.entries.size)
    }
}
