package dev.lildua.oddly

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.core.text.Brand
import dev.lildua.oddly.core.text.EnglishStrings
import dev.lildua.oddly.core.text.VietnameseStrings
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.clickTag
import dev.lildua.oddly.support.skipOnboarding
import dev.lildua.oddly.support.waitForTag
import org.junit.Rule
import org.junit.Test

/** T-12: the "Support & legal" rows exist in Settings. Links are not tapped (leaving the app is flaky). */
class SettingsSupportTest {
    @get:Rule
    val app = OddlyAppRule()
    private val compose get() = app.compose

    @Test
    fun settingsShowsSupportAndLegalRows() {
        app.start("2026-03-10T09:00:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        compose.clickTag(UiTestTags.TAB_SETTINGS)
        compose.waitForTag(UiTestTags.SETTINGS_PRIVACY)

        listOf(
            UiTestTags.SETTINGS_RATE,
            UiTestTags.SETTINGS_CONTACT,
            UiTestTags.SETTINGS_PRIVACY,
            UiTestTags.SETTINGS_ABOUT,
        ).forEach { compose.onNodeWithTag(it).performScrollTo().assertIsDisplayed() }

        // The email is the row's subtitle, merged into the row's semantics.
        compose.onNodeWithTag(UiTestTags.SETTINGS_CONTACT)
            .assertTextContains(Brand.SUPPORT_EMAIL, substring = true)

        // TalkBack speaks the click label ("double tap to ..."); the app language is
        // not fixed in this test, so accept either translation.
        compose.onNodeWithTag(UiTestTags.SETTINGS_CONTACT)
            .assert(clickLabelIn(VietnameseStrings.a11yOpensEmail, EnglishStrings.a11yOpensEmail))
        compose.onNodeWithTag(UiTestTags.SETTINGS_PRIVACY)
            .assert(clickLabelIn(VietnameseStrings.a11yOpensBrowser, EnglishStrings.a11yOpensBrowser))
    }

    private fun clickLabelIn(vararg labels: String) =
        SemanticsMatcher("OnClick label in ${labels.toList()}") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label in labels
        }
}
