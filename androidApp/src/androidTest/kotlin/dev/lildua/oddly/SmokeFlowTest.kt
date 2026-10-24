package dev.lildua.oddly

import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.core.text.VietnameseStrings
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.clickTag
import dev.lildua.oddly.support.completeTodaysChallenge
import dev.lildua.oddly.support.textOf
import dev.lildua.oddly.support.waitForTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SmokeFlowTest {
    @get:Rule
    val app = OddlyAppRule()
    private val compose get() = app.compose

    @Test
    fun onboardingToRewardToJourney() {
        app.start("2026-03-10T09:00:00")

        compose.clickTag(UiTestTags.ONBOARDING_START)
        compose.clickTag(UiTestTags.interest(Category.HEALTH))
        compose.onNodeWithTag(UiTestTags.interest(Category.HEALTH)).assertIsOn()
        compose.clickTag(UiTestTags.INTERESTS_CONTINUE)
        compose.clickTag(UiTestTags.PERMISSION_LATER)

        compose.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        completeTodaysChallenge(compose)

        val stored = app.storedState()
        val completion = stored.completions.single()
        compose.onNodeWithTag(UiTestTags.COMPLETE_XP).assertTextEquals("+${completion.xpEarned} XP")
        compose.onNodeWithTag(UiTestTags.COMPLETE_STREAK)
            .assertTextContains(VietnameseStrings.streakDays(1), substring = true)
        compose.clickTag(UiTestTags.COMPLETE_DONE)

        compose.clickTag(UiTestTags.TAB_JOURNEY)
        compose.waitForTag(UiTestTags.JOURNEY_LEVEL)
        val profile = app.storedState().profile
        assertEquals(1, profile.level)
        assertEquals(completion.xpEarned, profile.xpInLevel)
        compose.onNodeWithTag(UiTestTags.JOURNEY_LEVEL).assertTextContains("Level 1", substring = true)
        compose.onNodeWithTag(UiTestTags.JOURNEY_LEVEL).assertTextContains(
            VietnameseStrings.xpProgress(profile.xpInLevel, profile.xpForNextLevel),
            substring = true,
        )

        assertTrue(stored.hasCompletedOnboarding)
        assertFalse(stored.settings.reminderEnabled)
        assertTrue(Category.HEALTH in stored.profile.interests)
    }
}
