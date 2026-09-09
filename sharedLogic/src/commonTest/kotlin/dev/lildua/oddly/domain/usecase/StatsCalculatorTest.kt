package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.completion
import dev.lildua.oddly.completionsOn
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StatsCalculatorTest {

    private fun summarize(
        completions: List<dev.lildua.oddly.domain.model.ChallengeCompletion>,
        range: StatsRange,
    ) = StatsCalculator.summarize(completions, TODAY, range, AppLanguage.ENGLISH)

    // MARK: - Windowing

    @Test
    fun `the week window covers today and the six days before it`() {
        val kept = StatsCalculator.filterTo(completionsOn(0, 6), TODAY, StatsRange.WEEK)

        assertEquals(2, kept.size)
    }

    @Test
    fun `the day before the week window is excluded`() {
        val kept = StatsCalculator.filterTo(completionsOn(7), TODAY, StatsRange.WEEK)

        assertTrue(kept.isEmpty())
    }

    @Test
    fun `the all-time range keeps everything`() {
        val kept = StatsCalculator.filterTo(completionsOn(0, 400, 900), TODAY, StatsRange.ALL)

        assertEquals(3, kept.size)
    }

    @Test
    fun `the month window is thirty days ending today`() {
        assertEquals(1, StatsCalculator.filterTo(completionsOn(29), TODAY, StatsRange.MONTH).size)
        assertEquals(0, StatsCalculator.filterTo(completionsOn(30), TODAY, StatsRange.MONTH).size)
    }

    // MARK: - Period comparison

    @Test
    fun `the delta compares this week against the one before it`() {
        // Three this week, one in the week before.
        val summary = summarize(completionsOn(0, 1, 2, 8), StatsRange.WEEK)

        assertEquals(3, summary.totalCompleted)
        assertEquals(2, summary.deltaVsPreviousPeriod)
    }

    @Test
    fun `the delta is negative when this period is quieter`() {
        val summary = summarize(completionsOn(0, 8, 9, 10), StatsRange.WEEK)

        assertEquals(-2, summary.deltaVsPreviousPeriod)
    }

    @Test
    fun `the all-time range has no previous period to compare against`() {
        val summary = summarize(completionsOn(0, 100), StatsRange.ALL)

        assertEquals(2, summary.totalCompleted)
        assertEquals(2, summary.deltaVsPreviousPeriod)
    }

    // MARK: - Bars

    @Test
    fun `the week chart has one bar per day ending today`() {
        val summary = summarize(completionsOn(0, 0, 3), StatsRange.WEEK)

        assertEquals(7, summary.bars.size)
        assertTrue(summary.bars.last().isToday)
        assertEquals(1, summary.bars.count { it.isToday })
        assertEquals(2, summary.bars.last().count)
        assertEquals(1, summary.bars[3].count)
    }

    /**
     * The headline number and the chart are read side by side, so bars that do
     * not add up to the total look like a bug even when neither is wrong on its
     * own. Every range buckets a window ending today, so they must agree.
     */
    @Test
    fun `bars always sum to the headline total`() {
        val spread = completionsOn(0, 1, 3, 6, 9, 14, 20, 29, 40, 90, 200, 300)

        for (range in StatsRange.entries) {
            val summary = summarize(spread, range)
            assertEquals(
                summary.totalCompleted,
                summary.bars.sumOf { it.count },
                "bars disagree with the total for $range",
            )
        }
    }

    @Test
    fun `longer ranges still draw seven buckets`() {
        for (range in StatsRange.entries) {
            assertEquals(7, summarize(completionsOn(0, 50, 200), range).bars.size)
        }
    }

    @Test
    fun `an empty log produces an empty chart rather than no chart`() {
        val summary = summarize(emptyList(), StatsRange.WEEK)

        assertEquals(0, summary.totalCompleted)
        assertEquals(7, summary.bars.size)
        assertTrue(summary.bars.all { it.count == 0 })
        assertTrue(summary.distribution.isEmpty())
        assertNull(summary.mostActiveCategory)
    }

    // MARK: - Category distribution

    @Test
    fun `the distribution counts real seed challenges by category`() {
        val health = ChallengeSeed.all.first { it.category == Category.HEALTH }
        val finance = ChallengeSeed.all.first { it.category == Category.FINANCE }

        val distribution = StatsCalculator.distribution(
            listOf(
                completion(daysAgo(0), challengeId = health.id),
                completion(daysAgo(1), challengeId = health.id),
                completion(daysAgo(2), challengeId = finance.id),
            ),
        )

        assertEquals(2, distribution.size)
        assertEquals(Category.HEALTH, distribution.first().category)
        assertEquals(2, distribution.first().count)
    }

    @Test
    fun `the distribution fractions add up to one`() {
        val ids = ChallengeSeed.all.take(5).map { it.id }
        val completions = ids.mapIndexed { index, id -> completion(daysAgo(index), challengeId = id) }

        val total = StatsCalculator.distribution(completions).sumOf { it.fraction.toDouble() }

        assertTrue(total in 0.999..1.001, "fractions summed to $total")
    }

    @Test
    fun `a completion whose challenge no longer exists is left out`() {
        // Content can be removed between releases; the log still refers to it.
        val distribution = StatsCalculator.distribution(
            listOf(completion(daysAgo(0), challengeId = "removed-in-a-later-release")),
        )

        assertTrue(distribution.isEmpty())
    }

    @Test
    fun `the most active category is the one with the most completions`() {
        val health = ChallengeSeed.all.first { it.category == Category.HEALTH }
        val finance = ChallengeSeed.all.first { it.category == Category.FINANCE }

        val summary = summarize(
            listOf(
                completion(daysAgo(0), challengeId = finance.id),
                completion(daysAgo(1), challengeId = health.id),
                completion(daysAgo(2), challengeId = health.id),
            ),
            StatsRange.WEEK,
        )

        assertEquals(Category.HEALTH, summary.mostActiveCategory)
    }
}
