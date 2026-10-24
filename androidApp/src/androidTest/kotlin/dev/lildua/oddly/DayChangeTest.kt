package dev.lildua.oddly

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.core.text.VietnameseStrings
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.clickTag
import dev.lildua.oddly.support.completeTodaysChallenge
import dev.lildua.oddly.support.hasTag
import dev.lildua.oddly.support.skipOnboarding
import dev.lildua.oddly.support.textOf
import dev.lildua.oddly.support.waitForTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

/** T-02: crossing midnight shows a new day, and a completion started before it is refused. */
class DayChangeTest {
    @get:Rule
    val app = OddlyAppRule()
    private val compose get() = app.compose

    @Test
    fun newDayShowsBannerAndNewChallenge() {
        app.start("2026-03-10T09:00:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        completeTodaysChallenge(compose)
        compose.clickTag(UiTestTags.COMPLETE_DONE)
        compose.waitForTag(UiTestTags.HOME_TODAY_DONE)
        val yesterdayTitle = compose.textOf(UiTestTags.HOME_TODAY_TITLE)

        app.resumeAt("2026-03-11T09:00:00")

        // The banner clears itself after 4 s, so assert it before anything else waits.
        compose.waitForTag(UiTestTags.HOME_DAY_BANNER)
        compose.onNodeWithTag(UiTestTags.HOME_DAY_BANNER)
            .assertTextContains(VietnameseStrings.newDayNotice, substring = true)
        compose.waitForTag(UiTestTags.HOME_TODAY_START)
        assertFalse(compose.hasTag(UiTestTags.HOME_TODAY_DONE))
        assertNotEquals(yesterdayTitle, compose.textOf(UiTestTags.HOME_TODAY_TITLE))
        compose.onNodeWithText(
            DateFormat.dayAndMonth(LocalDate(2026, 3, 11), AppLanguage.VIETNAMESE),
        ).assertExists()
    }

    @Test
    fun completionAcrossMidnightIsRejected() {
        app.start("2026-03-10T23:59:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        compose.clickTag(UiTestTags.HOME_TODAY_START)
        compose.waitForTag(UiTestTags.DETAIL_BEGIN)

        app.jumpTo("2026-03-11T00:01:00")
        compose.clickTag(UiTestTags.DETAIL_BEGIN)

        compose.waitForTag(UiTestTags.HOME_DAY_BANNER)
        compose.onNodeWithTag(UiTestTags.HOME_DAY_BANNER)
            .assertTextContains(VietnameseStrings.dayChangedCompletionNotice, substring = true)
        assertFalse(compose.hasTag(UiTestTags.COMPLETE_DONE))
        assertTrue(app.storedState().completions.isEmpty())

        compose.clickTag(UiTestTags.TAB_JOURNEY)
        compose.waitForTag(UiTestTags.JOURNEY_EMPTY)
        assertEquals(0, app.storedState().completions.size)
    }
}
