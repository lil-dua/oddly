package dev.lildua.oddly.core.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrandTest {
    private val links = listOf(
        Brand.WEBSITE_DISPLAY, Brand.WEBSITE_URL, Brand.PRIVACY_POLICY_URL, Brand.SUPPORT_EMAIL, Brand.DEVELOPER,
    )

    @Test
    fun `website url is the https form of the displayed link`() {
        assertEquals("https://" + Brand.WEBSITE_DISPLAY + "/", Brand.WEBSITE_URL)
    }

    @Test
    fun `privacy policy lives under the website`() {
        assertTrue(Brand.PRIVACY_POLICY_URL.startsWith(Brand.WEBSITE_URL))
        assertTrue(Brand.PRIVACY_POLICY_URL.endsWith(".html"))
    }

    @Test
    fun `support email looks like an address`() {
        assertEquals(1, Brand.SUPPORT_EMAIL.count { it == '@' })
        assertFalse(Brand.SUPPORT_EMAIL.any { it.isWhitespace() })
    }

    @Test
    fun `brand links contain no placeholder`() {
        links.forEach {
            assertTrue(it.isNotBlank())
            assertFalse(it.contains("1percenthuman"), it)
            assertFalse(it.contains("["), it)
            assertFalse(it.contains("example"), it)
        }
    }

    @Test
    fun `app store id when set is numeric`() {
        Brand.APP_STORE_ID?.let { id -> assertTrue(id.isNotEmpty() && id.all { it.isDigit() }) }
    }
}
