package dev.lildua.oddly.core.text

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class StringsTest {
    private val version = "9.8.7"
    private val all: List<Strings> = listOf(VietnameseStrings, EnglishStrings)

    @Test
    fun `settingsFooter includes the given version in both languages`() {
        all.forEach { assertContains(it.settingsFooter(version), version) }
    }

    @Test
    fun `aboutBody includes the given version in both languages`() {
        all.forEach { assertContains(it.aboutBody(version), version) }
    }

    @Test
    fun `version strings no longer hardcode 1 point 0`() {
        all.forEach {
            assertFalse(it.settingsFooter(version).contains("1.0"))
            assertFalse(it.aboutBody(version).contains("1.0"))
        }
    }
}
