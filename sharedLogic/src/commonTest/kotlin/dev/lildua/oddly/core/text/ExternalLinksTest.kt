package dev.lildua.oddly.core.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalLinksTest {
    @Test
    fun `percentEncode keeps unreserved characters`() {
        assertEquals("abcXYZ019-._~", ExternalLinks.percentEncode("abcXYZ019-._~"))
    }

    @Test
    fun `percentEncode escapes percent and space`() {
        assertEquals("1%25%20HUMAN", ExternalLinks.percentEncode("1% HUMAN"))
    }

    @Test
    fun `percentEncode encodes Vietnamese as UTF-8`() {
        assertEquals("G%C3%B3p%20%C3%BD", ExternalLinks.percentEncode("Góp ý"))
    }

    @Test
    fun `percentEncode escapes reserved characters`() {
        val out = ExternalLinks.percentEncode("a&b=c?d#e/f(g)")
        for (c in "&=?#/()") assertFalse(out.contains(c), "raw '$c' left in $out")
        assertEquals("a%26b%3Dc%3Fd%23e%2Ff%28g%29", out)
    }

    @Test
    fun `percentEncode never emits a plus for space`() {
        assertEquals("a%20b%2Bc", ExternalLinks.percentEncode("a b+c"))
    }

    @Test
    fun `percentEncode of an empty string is empty`() {
        assertEquals("", ExternalLinks.percentEncode(""))
    }

    @Test
    fun `percentEncode encodes four byte characters`() {
        assertEquals("%F0%9F%98%80", ExternalLinks.percentEncode("😀"))
    }

    @Test
    fun `mailto keeps the address and encodes the subject`() {
        assertEquals(
            "mailto:a@b.c?subject=1%25%20HUMAN%201.0%20%28iOS%29",
            ExternalLinks.mailto("a@b.c", "1% HUMAN 1.0 (iOS)"),
        )
    }

    @Test
    fun `mailto subject with ampersand and question mark cannot break the query`() {
        val url = ExternalLinks.mailto("a@b.c", "x&cc=evil?y")
        assertEquals("mailto:a@b.c?subject=x%26cc%3Devil%3Fy", url)
        assertEquals(1, url.count { it == '?' })
        assertFalse(url.contains('&'))
    }

    @Test
    fun `mailto for the support email carries the Vietnamese subject`() {
        val subject = VietnameseStrings.supportEmailSubject("1.0", "Android")
        val url = ExternalLinks.mailto(Brand.SUPPORT_EMAIL, subject)
        assertTrue(url.startsWith("mailto:${Brand.SUPPORT_EMAIL}?subject="))
        assertEquals("mailto:huuthom.it@gmail.com?subject=G%C3%B3p%20%C3%BD%201%25%20HUMAN%201.0%20%28Android%29", url)
    }

    @Test
    fun `play store links use the package id`() {
        assertEquals("market://details?id=dev.lildua.oddly", ExternalLinks.playStoreApp("dev.lildua.oddly"))
        assertEquals(
            "https://play.google.com/store/apps/details?id=dev.lildua.oddly",
            ExternalLinks.playStoreWeb("dev.lildua.oddly"),
        )
    }

    @Test
    fun `app store review link uses the numeric id`() {
        assertEquals(
            "https://apps.apple.com/app/id123?action=write-review",
            ExternalLinks.appStoreReview("123"),
        )
    }

    @Test
    fun `app store review url follows the configured id`() {
        assertEquals(Brand.APP_STORE_ID?.let { ExternalLinks.appStoreReview(it) }, ExternalLinks.appStoreReviewUrl)
    }
}
