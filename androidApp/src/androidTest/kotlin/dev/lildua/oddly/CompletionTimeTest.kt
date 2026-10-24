package dev.lildua.oddly

import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.clickTag
import dev.lildua.oddly.support.completeTodaysChallenge
import dev.lildua.oddly.support.skipOnboarding
import dev.lildua.oddly.support.waitForTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

/** T-01: the stored completion time is the real clock time, not midnight. */
class CompletionTimeTest {
    @get:Rule
    val app = OddlyAppRule()
    private val compose get() = app.compose

    @Test
    fun completionStoresRealClockTime() {
        app.start("2026-03-10T09:41:00")
        skipOnboarding(compose)
        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        completeTodaysChallenge(compose)

        val completion = app.storedState().completions.single()
        assertEquals(LocalDate(2026, 3, 10), completion.date)
        assertEquals(9, completion.completedAt.hour)
        assertTrue("was ${completion.completedAt}", completion.completedAt.minute in 41..42)

        app.relaunch()
        compose.clickTag(UiTestTags.TAB_JOURNEY)
        compose.waitForTag(UiTestTags.JOURNEY_LEVEL)
        assertEquals(1, app.storedState().completions.size)
    }
}
