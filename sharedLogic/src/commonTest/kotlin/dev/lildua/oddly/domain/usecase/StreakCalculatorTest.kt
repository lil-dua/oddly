package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.completion
import dev.lildua.oddly.completionsOn
import dev.lildua.oddly.daysAgo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StreakCalculatorTest {

    @Test
    fun `no completions means no streak`() {
        val streak = StreakCalculator.calculate(emptyList(), TODAY)

        assertEquals(0, streak.current)
        assertEquals(0, streak.best)
        assertNull(streak.lastCompletedDate)
    }

    @Test
    fun `a single completion today is a streak of one`() {
        val streak = StreakCalculator.calculate(completionsOn(0), TODAY)

        assertEquals(1, streak.current)
        assertEquals(1, streak.best)
        assertEquals(TODAY, streak.lastCompletedDate)
    }

    @Test
    fun `consecutive days ending today all count`() {
        val streak = StreakCalculator.calculate(completionsOn(4, 3, 2, 1, 0), TODAY)

        assertEquals(5, streak.current)
        assertEquals(5, streak.best)
    }

    /**
     * The rule that makes the app livable: at 8am today you have done nothing
     * yet, and the streak you built must not already be gone.
     */
    @Test
    fun `a run ending yesterday is still the current streak`() {
        val streak = StreakCalculator.calculate(completionsOn(3, 2, 1), TODAY)

        assertEquals(3, streak.current)
        assertEquals(daysAgo(1), streak.lastCompletedDate)
    }

    @Test
    fun `missing a whole day breaks the current streak`() {
        val streak = StreakCalculator.calculate(completionsOn(4, 3, 2), TODAY)

        assertEquals(0, streak.current)
        assertEquals(3, streak.best)
    }

    @Test
    fun `the best streak survives a break`() {
        // A five-day run a fortnight ago, then a two-day run ending today.
        val streak = StreakCalculator.calculate(completionsOn(17, 16, 15, 14, 13, 1, 0), TODAY)

        assertEquals(2, streak.current)
        assertEquals(5, streak.best)
    }

    @Test
    fun `two completions on the same day count as one`() {
        val twiceToday = listOf(
            completion(TODAY, challengeId = "a"),
            completion(TODAY, challengeId = "b"),
            completion(daysAgo(1), challengeId = "c"),
        )

        val streak = StreakCalculator.calculate(twiceToday, TODAY)

        assertEquals(2, streak.current)
        assertEquals(2, streak.best)
    }

    @Test
    fun `order of the completion log does not matter`() {
        val shuffled = completionsOn(0, 3, 1, 2)

        val streak = StreakCalculator.calculate(shuffled, TODAY)

        assertEquals(4, streak.current)
    }

    @Test
    fun `the streak spans a month boundary`() {
        // 2026-09-09 back to 2026-08-30: crosses the end of August.
        val streak = StreakCalculator.calculate(completionsOn(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0), TODAY)

        assertEquals(11, streak.current)
    }

    @Test
    fun `recent activity is seven days ending today - oldest first`() {
        val week = StreakCalculator.recentActivity(completionsOn(6, 2, 0), TODAY)

        assertEquals(7, week.size)
        assertEquals(daysAgo(6), week.first().date)
        assertEquals(TODAY, week.last().date)
        assertTrue(week.last().isToday)
        assertEquals(1, week.count { it.isToday })
        assertEquals(listOf(true, false, false, false, true, false, true), week.map { it.completed })
    }

    @Test
    fun `completions older than the window do not appear in recent activity`() {
        val week = StreakCalculator.recentActivity(completionsOn(7), TODAY)

        assertTrue(week.none { it.completed })
    }
}
