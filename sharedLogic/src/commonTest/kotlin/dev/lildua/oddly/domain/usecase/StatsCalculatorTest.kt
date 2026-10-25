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
import kotlin.test.assertFalse
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

    // MARK: - Bar date spans (screen reader)

    @Test
    fun `week bars are single days ending today`() {
        val bars = summarize(emptyList(), StatsRange.WEEK).bars

        assertEquals(7, bars.size)
        assertTrue(bars.all { it.startDate == it.endDate })
        assertEquals(TODAY, bars.last().endDate)
        assertTrue(bars.last().isToday)
        assertEquals(daysAgo(6), bars.first().startDate)
    }

    @Test
    fun `month bars are contiguous and end today`() {
        val bars = summarize(completionsOn(0, 10, 29), StatsRange.MONTH).bars

        assertEquals(TODAY, bars.last().endDate)
        assertEquals(daysAgo(29), bars.first().startDate)
        // span 30 / bucket 5 leaves the oldest column wholly outside the window: it is
        // an empty column pinned to the first day, so contiguity holds from the 2nd bar.
        assertEquals(0, bars.first().count)
        bars.drop(1).zipWithNext().forEach { (a, b) ->
            assertEquals(a.endDate.toEpochDays() + 1, b.startDate.toEpochDays())
        }
        assertTrue(bars.all { it.startDate <= it.endDate })
    }

    @Test
    fun `all range bars never reach before the earliest completion window`() {
        val bars = summarize(completionsOn(0, 20), StatsRange.ALL).bars

        assertEquals(daysAgo(20), bars.first().startDate)
        assertEquals(TODAY, bars.last().endDate)
        assertEquals(2, bars.sumOf { it.count })
    }

    @Test
    fun `all range with no completions still spans seven days ending today`() {
        val bars = summarize(emptyList(), StatsRange.ALL).bars

        assertEquals(7, bars.size)
        assertEquals(daysAgo(6), bars.first().startDate)
        assertEquals(TODAY, bars.last().endDate)
    }

    // MARK: - Empty oldest column (bucketSize * 7 > span)

    /** The pre-fix count rule (HEAD): sum of days start..end, 0 when the clamped range is inverted. */
    private fun legacyCounts(
        completions: List<dev.lildua.oddly.domain.model.ChallengeCompletion>,
        range: StatsRange,
    ): List<Int> {
        val done = completions.groupingBy { it.date.toEpochDays() }.eachCount()
        val today = TODAY.toEpochDays()
        val span = range.days ?: (today - (completions.minOfOrNull { it.date.toEpochDays() } ?: today) + 1).coerceAtLeast(7)
        val windowStart = today - span + 1
        val bucket = (span + 6) / 7
        return (6 downTo 0).map { i ->
            val end = today - i * bucket
            val start = (end - bucket + 1).coerceAtLeast(windowStart)
            if (start > end) 0 else (start..end).sumOf { done[it] ?: 0 }
        }
    }

    @Test
    fun `every range keeps seven bars with start not after end and the last ending today`() {
        val data = completionsOn(0, 1, 3, 10, 29, 30, 100, 364, 365, 500)
        StatsRange.entries.forEach { range ->
            val bars = summarize(data, range).bars
            assertEquals(7, bars.size, "$range")
            assertTrue(bars.all { it.startDate <= it.endDate }, "$range start<=end")
            assertEquals(TODAY, bars.last().endDate, "$range")
        }
    }

    @Test
    fun `bar counts match the pre-fix counts for month year and all`() {
        val data = completionsOn(0, 1, 3, 5, 10, 29, 30, 31, 100, 200, 364, 365, 500)
        listOf(StatsRange.MONTH, StatsRange.YEAR, StatsRange.ALL).forEach { range ->
            assertEquals(legacyCounts(data, range), summarize(data, range).bars.map { it.count }, "$range")
        }
    }

    @Test
    fun `all range bar counts match the pre-fix counts for every earliest day`() {
        for (earliest in 0..60) {
            val data = completionsOn(0, earliest)
            val bars = summarize(data, StatsRange.ALL).bars
            assertEquals(legacyCounts(data, StatsRange.ALL), bars.map { it.count }, "earliest=$earliest")
            assertEquals(data.size, bars.sumOf { it.count }, "sum earliest=$earliest")
            assertTrue(bars.all { it.startDate <= it.endDate }, "earliest=$earliest")
        }
    }

    @Test
    fun `month bar total equals the headline total and the oldest column is empty`() {
        val data = completionsOn(0, 10, 29, 30)
        val summary = summarize(data, StatsRange.MONTH)

        assertEquals(StatsCalculator.filterTo(data, TODAY, StatsRange.MONTH).size, summary.bars.sumOf { it.count })
        assertEquals(0, summary.bars.first().count)
        assertEquals(daysAgo(29), summary.bars.first().startDate)
        assertEquals(daysAgo(29), summary.bars.first().endDate)
    }

    @Test
    fun `an empty oldest column is padding and is not read in either language`() {
        val bars = summarize(completionsOn(0), StatsRange.MONTH).bars
        assertTrue(bars.first().isPadding)
        assertTrue(bars.drop(1).none { it.isPadding })
        listOf(dev.lildua.oddly.core.text.VietnameseStrings, dev.lildua.oddly.core.text.EnglishStrings).forEach { s ->
            val text = dev.lildua.oddly.core.text.AccessibilityText.chart(bars, s)
            assertEquals(bars.size - 1, text.split(". ").size - 1, text)
        }
    }

    @Test
    fun `all range with 8 days of history does not read the window start date repeatedly`() {
        val bars = summarize(completionsOn(0, 7), StatsRange.ALL).bars
        listOf(dev.lildua.oddly.core.text.VietnameseStrings, dev.lildua.oddly.core.text.EnglishStrings).forEach { s ->
            val text = dev.lildua.oddly.core.text.AccessibilityText.chart(bars, s)
            val weekday = dev.lildua.oddly.core.time.DateFormat.fullWeekday(daysAgo(7), s.language)
            assertTrue(text.split(weekday).size - 1 <= 1, text)
        }
    }

    @Test
    fun `a single day column older than 7 days is not read with a bare weekday`() {
        val bars = summarize(completionsOn(0, 8), StatsRange.ALL).bars
        val old = bars.first { it.startDate == it.endDate && it.startDate == daysAgo(8) }
        listOf(dev.lildua.oddly.core.text.VietnameseStrings, dev.lildua.oddly.core.text.EnglishStrings).forEach { s ->
            val text = dev.lildua.oddly.core.text.AccessibilityText.chartBar(old, s, TODAY)
            assertEquals(
                s.a11yChartDay(dev.lildua.oddly.core.time.DateFormat.dayAndMonth(old.startDate, s.language), old.count),
                text,
            )
        }
    }

    @Test
    fun `chart sentences are unique skip padding and never name an old day by bare weekday for every range and span`() {
        val langs = listOf(dev.lildua.oddly.core.text.VietnameseStrings, dev.lildua.oddly.core.text.EnglishStrings)
        val cases = (0..59).map { StatsRange.ALL to it } + listOf(StatsRange.WEEK to 0, StatsRange.MONTH to 0, StatsRange.YEAR to 0)
        for ((range, earliest) in cases) {
            val data = completionsOn(0, earliest, 3)
            val bars = summarize(data, range).bars
            val tag = "$range earliest=$earliest"
            // data columns are never padding; padding is always empty
            assertTrue(bars.none { it.isPadding && it.count > 0 }, tag)
            assertEquals(summarize(data, range).bars.sumOf { it.count }, bars.filterNot { it.isPadding }.sumOf { it.count }, tag)
            langs.forEach { s ->
                val spoken = bars.filterNot { it.isPadding }.map {
                    dev.lildua.oddly.core.text.AccessibilityText.chartBar(it, s, TODAY)
                }
                val expected = (listOf(s.a11yChartTitle) + spoken).joinToString(". ")
                assertEquals(expected, dev.lildua.oddly.core.text.AccessibilityText.chart(bars, s), tag)
                assertEquals(spoken.size, spoken.toSet().size, "$tag duplicate sentence: $spoken")
                bars.filterNot { it.isPadding }.forEach { b ->
                    if (b.startDate == b.endDate && !b.isToday && TODAY.toEpochDays() - b.startDate.toEpochDays() > 6) {
                        val bare = s.a11yChartDay(dev.lildua.oddly.core.time.DateFormat.fullWeekday(b.startDate, s.language), b.count)
                        assertTrue(dev.lildua.oddly.core.text.AccessibilityText.chartBar(b, s, TODAY) != bare, tag)
                    }
                }
            }
        }
    }

    @Test
    fun `all range with 9 days of history keeps the earliest completion column and reads its date`() {
        val data = completionsOn(0, 8)
        val bars = summarize(data, StatsRange.ALL).bars
        val col = bars.first { it.count > 0 && it.startDate <= daysAgo(8) }
        assertFalse(col.isPadding)
        assertEquals(legacyCounts(data, StatsRange.ALL), bars.map { it.count })
        listOf(
            dev.lildua.oddly.core.text.VietnameseStrings,
            dev.lildua.oddly.core.text.EnglishStrings,
        ).forEach { s ->
            val text = dev.lildua.oddly.core.text.AccessibilityText.chart(bars, s)
            assertTrue(text.contains(dev.lildua.oddly.core.time.DateFormat.dayAndMonth(daysAgo(8), s.language)), text)
        }
    }

    @Test
    fun `vietnamese old day format is weekday comma day thang month`() {
        assertEquals(
            "Thứ Sáu, 16 tháng 10",
            dev.lildua.oddly.core.time.DateFormat.dayAndMonth(
                kotlinx.datetime.LocalDate(2026, 10, 16),
                AppLanguage.VIETNAMESE,
            ),
        )
    }

    @Test
    fun `summarize distribution matches distribution of the window`() {
        val ids = ChallengeSeed.all.take(6).map { it.id }
        val log = ids.mapIndexed { i, id -> completion(daysAgo(i * 3), challengeId = id) }
        for (range in StatsRange.entries) {
            val summary = summarize(log, range)
            val windowed = StatsCalculator.filterTo(log, TODAY, range)
            assertEquals(StatsCalculator.distribution(windowed), summary.distribution)
            assertEquals(windowed.size, summary.totalCompleted)
        }
    }

    @Test
    fun `distribution skips completions whose challenge id is unknown`() {
        val log = listOf(
            completion(TODAY, challengeId = ChallengeSeed.all.first().id),
            completion(daysAgo(1), challengeId = "no_such_challenge"),
        )
        assertEquals(1, StatsCalculator.distribution(log).sumOf { it.count })
    }
}
