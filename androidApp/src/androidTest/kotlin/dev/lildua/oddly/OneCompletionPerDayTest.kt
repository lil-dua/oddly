package dev.lildua.oddly

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.clickTag
import dev.lildua.oddly.support.completeTodaysChallenge
import dev.lildua.oddly.support.hasTag
import dev.lildua.oddly.support.skipOnboarding
import dev.lildua.oddly.support.tagStartsWith
import dev.lildua.oddly.support.waitForTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/** T-03: one scoring completion a day. */
class OneCompletionPerDayTest {
    @get:Rule
    val app = OddlyAppRule()
    private val compose get() = app.compose

    private fun finishTodayAndReturnHome() {
        app.start("2026-03-10T09:00:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        completeTodaysChallenge(compose)
        compose.clickTag(UiTestTags.COMPLETE_DONE)
        compose.waitForTag(UiTestTags.HOME_TODAY_DONE)
    }

    @Test
    fun secondCompletionSameDayIsBlocked() {
        finishTodayAndReturnHome()
        val before = app.storedState()
        val doneId = before.completions.single().challengeId

        // Today's card is now "done": its detail shows the notice, never Begin.
        compose.clickTag(UiTestTags.HOME_TODAY_DONE)
        compose.waitForTag(UiTestTags.DETAIL_ALREADY_DONE)
        assertFalse(compose.hasTag(UiTestTags.DETAIL_BEGIN))
        app.pressBack()

        // A different challenge from the library: the day is done, so no Begin or Adopt.
        compose.clickTag(UiTestTags.TAB_JOURNEY)
        compose.clickTag(UiTestTags.JOURNEY_ALL_CHALLENGES)
        val doneRow = UiTestTags.libraryRow(doneId)
        compose.waitUntil(10_000) {
            compose.onAllNodes(tagStartsWith("library.")).fetchSemanticsNodes().isNotEmpty()
        }
        val other = compose.onAllNodes(tagStartsWith("library."))
            .fetchSemanticsNodes()
            .map { it.config[SemanticsProperties.TestTag] }
            .first { it != doneRow }
        compose.onNodeWithTag(other).performClick()

        compose.waitForTag(UiTestTags.DETAIL_DAY_DONE)
        assertFalse(compose.hasTag(UiTestTags.DETAIL_BEGIN))
        assertFalse(compose.hasTag(UiTestTags.DETAIL_ADOPT))

        val after = app.storedState()
        assertEquals(1, after.completions.size)
        assertEquals(before.profile.xpInLevel, after.profile.xpInLevel)
        assertEquals(before.profile.level, after.profile.level)
    }

    @Test
    fun relaunchSameDayKeepsDoneState() {
        finishTodayAndReturnHome()

        app.relaunch()

        compose.waitForTag(UiTestTags.HOME_TODAY_DONE)
        assertFalse(compose.hasTag(UiTestTags.HOME_TODAY_START))
        compose.clickTag(UiTestTags.HOME_TODAY_DONE)
        compose.waitForTag(UiTestTags.DETAIL_ALREADY_DONE)
        assertFalse(compose.hasTag(UiTestTags.DETAIL_BEGIN))
    }
}
