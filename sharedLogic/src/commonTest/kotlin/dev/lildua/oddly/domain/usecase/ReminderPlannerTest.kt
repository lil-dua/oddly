package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.completion
import dev.lildua.oddly.completionsOn
import dev.lildua.oddly.core.text.ReminderSeed
import dev.lildua.oddly.core.text.Strings
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.domain.model.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReminderPlannerTest {

    private val vi = AppLanguage.VIETNAMESE
    private val en = AppLanguage.ENGLISH

    @Test
    fun `streakBefore is zero without completions`() {
        assertEquals(0, ReminderPlanner.streakBefore(emptyList(), TODAY))
    }

    @Test
    fun `streakBefore is zero when yesterday is empty`() {
        assertEquals(0, ReminderPlanner.streakBefore(completionsOn(2, 3), TODAY))
    }

    @Test
    fun `streakBefore counts three consecutive days ending yesterday`() {
        assertEquals(3, ReminderPlanner.streakBefore(completionsOn(1, 2, 3), TODAY))
    }

    @Test
    fun `streakBefore stops at a gap`() {
        assertEquals(2, ReminderPlanner.streakBefore(completionsOn(1, 2, 4), TODAY))
    }

    @Test
    fun `streakBefore ignores the fire date and later days`() {
        val completions = completionsOn(0, -1, -2, 1, 2)
        assertEquals(2, ReminderPlanner.streakBefore(completions, TODAY))
    }

    @Test
    fun `streakBefore counts duplicate days once`() {
        val completions = completionsOn(1, 2) + completion(daysAgo(1), challengeId = "dup")
        assertEquals(2, ReminderPlanner.streakBefore(completions, TODAY))
    }

    @Test
    fun `content is null when the fire date is already done`() {
        assertNull(ReminderPlanner.content(completionsOn(0, 1, 2, 3), TODAY, vi, isLastScheduled = false))
        assertNull(ReminderPlanner.content(completionsOn(0), TODAY, en, isLastScheduled = true))
    }

    @Test
    fun `content for a done yesterday still shows today`() {
        assertNotNull(ReminderPlanner.content(completionsOn(1), TODAY, vi, isLastScheduled = false))
    }

    @Test
    fun `content uses the rotating seed copy at streak two`() {
        for (lang in listOf(vi, en)) {
            val result = ReminderPlanner.content(completionsOn(1, 2), TODAY, lang, isLastScheduled = false)
            assertEquals(ReminderSeed.content(TODAY.toEpochDays(), lang), result)
        }
    }

    @Test
    fun `content uses streak copy at streak three in both languages`() {
        for (lang in listOf(vi, en)) {
            val strings = Strings.of(lang)
            val result = ReminderPlanner.content(completionsOn(1, 2, 3), TODAY, lang, isLastScheduled = false)
            assertNotNull(result)
            assertEquals(strings.reminderStreakTitle(3), result.title)
            assertEquals(strings.reminderStreakBody, result.body)
        }
    }

    @Test
    fun `content streak title carries the longer count`() {
        val result = ReminderPlanner.content(completionsOn(1, 2, 3, 4, 5), TODAY, en, isLastScheduled = false)
        assertEquals(Strings.of(en).reminderStreakTitle(5), result?.title)
    }

    @Test
    fun `content uses lapsed copy on the last scheduled day over streak`() {
        for (lang in listOf(vi, en)) {
            val strings = Strings.of(lang)
            val result = ReminderPlanner.content(completionsOn(1, 2, 3), TODAY, lang, isLastScheduled = true)
            assertNotNull(result)
            assertEquals(strings.reminderLapsedTitle, result.title)
            assertEquals(strings.reminderLapsedBody, result.body)
        }
    }

    @Test
    fun `streak and lapsed copy differ between Vietnamese and English`() {
        val v = Strings.of(vi)
        val e = Strings.of(en)
        assertNotEquals(v.reminderStreakTitle(3), e.reminderStreakTitle(3))
        assertNotEquals(v.reminderStreakBody, e.reminderStreakBody)
        assertNotEquals(v.reminderLapsedTitle, e.reminderLapsedTitle)
        assertNotEquals(v.reminderLapsedBody, e.reminderLapsedBody)
    }
}
