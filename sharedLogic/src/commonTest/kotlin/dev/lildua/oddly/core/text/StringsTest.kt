package dev.lildua.oddly.core.text

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue
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

    @Test
    fun `shareCaption ends with the website url in both languages`() {
        all.forEach {
            listOf(1, 5).forEach { n -> assertTrue(it.shareCaption(n).endsWith(Brand.WEBSITE_URL)) }
        }
    }

    @Test
    fun `supportEmailSubject includes version and platform in both languages`() {
        all.forEach {
            val s = it.supportEmailSubject(version, "Android")
            assertContains(s, version)
            assertContains(s, "Android")
            assertContains(s, Brand.NAME)
        }
    }

    @Test
    fun `new support strings are not blank in both languages`() {
        all.forEach {
            listOf(
                it.sectionSupport, it.rateApp, it.contactSupport, it.privacyPolicy, it.linkFallbackTitle,
                it.emailFallbackBody, it.browserFallbackBody, it.copy, it.addressCopied,
                it.a11yOpensBrowser, it.a11yOpensEmail, it.a11yOpensStore,
            ).forEach { s -> assertTrue(s.isNotBlank()) }
        }
    }
}
