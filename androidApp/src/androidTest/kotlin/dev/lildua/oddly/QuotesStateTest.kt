package dev.lildua.oddly

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.ComposeTestRule
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.core.text.EnglishStrings
import dev.lildua.oddly.core.text.VietnameseStrings
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.skipOnboarding
import dev.lildua.oddly.support.waitForTag
import org.junit.Rule
import org.junit.Test

/** S14 keeps its Favorites filter across Share and Back, and starts fresh when reopened from Home. */
class QuotesStateTest {
    @get:Rule
    val app = OddlyAppRule()
    private val compose get() = app.compose

    private fun either(vi: String, en: String): SemanticsMatcher =
        hasText(vi, ignoreCase = true) or hasText(en, ignoreCase = true)

    private fun ComposeTestRule.waitFor(m: SemanticsMatcher) =
        waitUntil(10_000) { onAllNodes(m).fetchSemanticsNodes().isNotEmpty() }

    private fun ComposeTestRule.tap(m: SemanticsMatcher) {
        waitFor(m)
        onAllNodes(m).onFirst().performClick()
    }

    private val inspiration = either(VietnameseStrings.dailyInspiration, EnglishStrings.dailyInspiration)
    private val allChip = either(VietnameseStrings.quotesFilterAll, EnglishStrings.quotesFilterAll)
    private val favChip = either(VietnameseStrings.quotesFilterFavorites, EnglishStrings.quotesFilterFavorites)
    private val share = either(VietnameseStrings.share, EnglishStrings.share)
    private val addFav = hasContentDescription(VietnameseStrings.a11yAddFavorite) or
        hasContentDescription(EnglishStrings.a11yAddFavorite)

    @Test
    fun favoritesFilterSurvivesShareAndResetsOnReopen() {
        app.start("2026-03-10T09:00:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)

        compose.tap(inspiration)
        compose.tap(addFav)
        compose.tap(favChip)
        compose.onAllNodes(favChip).onFirst().assertIsSelected()

        compose.tap(share)
        // On the share screen the filter chips are gone.
        compose.waitUntil(10_000) { compose.onAllNodes(allChip).fetchSemanticsNodes().isEmpty() }

        app.pressBack()
        compose.waitFor(favChip)
        compose.onAllNodes(favChip).onFirst().assertIsSelected()

        // Leave S14 for Home, let the 500 ms cleanup run, reopen: back to "All".
        app.pressBack()
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        // The cleanup delay runs on the test clock, so advance it instead of sleeping.
        compose.mainClock.advanceTimeBy(1000)
        compose.tap(inspiration)
        compose.waitFor(allChip)
        compose.onAllNodes(allChip).onFirst().assertIsSelected()
    }

    @Test
    fun favoritesFilterDoesNotSurviveActivityRecreation() {
        app.start("2026-03-10T09:00:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)

        compose.tap(inspiration)
        compose.tap(addFav)
        compose.tap(favChip)
        compose.onAllNodes(favChip).onFirst().assertIsSelected()

        // The navigator restarts at Home, so S14 must not inherit the restored slot.
        app.recreate()
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        compose.tap(inspiration)
        compose.waitFor(allChip)
        compose.onAllNodes(allChip).onFirst().assertIsSelected()
    }
}
