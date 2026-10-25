package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.LocalizedText
import kotlinx.datetime.LocalDate

/** Time buckets offered by the Statistics screen (spec §S12). */
enum class StatsRange(val title: LocalizedText, val days: Int?) {
    WEEK(LocalizedText(vi = "Tuần", en = "Week"), 7),
    MONTH(LocalizedText(vi = "Tháng", en = "Month"), 30),
    YEAR(LocalizedText(vi = "Năm", en = "Year"), 365),
    ALL(LocalizedText(vi = "Tất cả", en = "All"), null),
}

data class CategorySlice(
    val category: Category,
    val count: Int,
    val fraction: Float,
)

data class DayBar(
    val label: String,
    val count: Int,
    val isToday: Boolean,
    /** First day the column covers; equals [endDate] for single-day (week) columns. Lets screen readers name the span. */
    val startDate: LocalDate,
    val endDate: LocalDate,
    /** True for an empty column lying wholly before the window; kept for layout, skipped by screen readers. */
    val isPadding: Boolean = false,
)

data class StatsSummary(
    val totalCompleted: Int,
    val deltaVsPreviousPeriod: Int,
    val bars: List<DayBar>,
    val distribution: List<CategorySlice>,
    val mostActiveCategory: Category?,
)

/** Aggregates the completion log into everything the progress screens display. */
object StatsCalculator {

    fun summarize(
        completions: List<ChallengeCompletion>,
        today: LocalDate,
        range: StatsRange,
        language: AppLanguage,
    ): StatsSummary {
        val windowed = filterTo(completions, today, range)
        val previous = previousPeriod(completions, today, range)
        val distribution = distribution(windowed)

        return StatsSummary(
            totalCompleted = windowed.size,
            deltaVsPreviousPeriod = windowed.size - previous.size,
            bars = buildBars(completions, today, range, language),
            distribution = distribution,
            mostActiveCategory = distribution.maxByOrNull { it.count }?.category,
        )
    }

    fun filterTo(
        completions: List<ChallengeCompletion>,
        today: LocalDate,
        range: StatsRange,
    ): List<ChallengeCompletion> {
        val days = range.days ?: return completions
        val cutoff = today.toEpochDays() - days + 1
        return completions.filter { it.date.toEpochDays() >= cutoff }
    }

    private fun previousPeriod(
        completions: List<ChallengeCompletion>,
        today: LocalDate,
        range: StatsRange,
    ): List<ChallengeCompletion> {
        val days = range.days ?: return emptyList()
        val end = today.toEpochDays() - days
        val start = end - days + 1
        return completions.filter { it.date.toEpochDays() in start..end }
    }

    fun distribution(completions: List<ChallengeCompletion>): List<CategorySlice> {
        if (completions.isEmpty()) return emptyList()
        val counts = completions
            .mapNotNull { ChallengeSeed.byId(it.challengeId)?.category }
            .groupingBy { it }
            .eachCount()
        val total = counts.values.sum().takeIf { it > 0 } ?: return emptyList()
        return counts.entries
            .sortedByDescending { it.value }
            .map { (category, count) ->
                CategorySlice(category, count, count.toFloat() / total)
            }
    }

    /**
     * Bars for the chart.
     *
     * Every range uses a window that *ends today*, matching [filterTo], so the
     * bars always sum to the headline total. Week shows one bar per day; longer
     * ranges bucket into seven equal slices to keep the shape readable.
     */
    private fun buildBars(
        completions: List<ChallengeCompletion>,
        today: LocalDate,
        range: StatsRange,
        language: AppLanguage,
    ): List<DayBar> {
        val done = completions.groupingBy { it.date.toEpochDays() }.eachCount()

        if (range == StatsRange.WEEK) {
            return (6 downTo 0).map { offset ->
                val epochDay = today.toEpochDays() - offset
                val date = LocalDate.fromEpochDays(epochDay)
                DayBar(
                    label = DateFormat.shortWeekday(date, language),
                    count = done[epochDay] ?: 0,
                    isToday = offset == 0,
                    startDate = date,
                    endDate = date,
                )
            }
        }

        val span = range.days ?: run {
            val earliest = completions.minOfOrNull { it.date.toEpochDays() } ?: today.toEpochDays()
            (today.toEpochDays() - earliest + 1).coerceAtLeast(7)
        }
        val windowStart = today.toEpochDays() - span + 1
        val bucketSize = (span + 6) / 7

        return (6 downTo 0).map { bucketIndex ->
            val end = today.toEpochDays() - bucketIndex * bucketSize
            // Clamp so the oldest bucket never reaches past the window and
            // double-counts days the headline total excluded.
            val start = (end - bucketSize + 1).coerceAtLeast(windowStart)
            // When bucketSize * 7 > span the oldest bucket lies wholly before the
            // window (end < windowStart). It stays an empty column (count 0, so the
            // other counts and "-N" labels are unchanged) but its range is pinned to
            // windowStart so startDate <= endDate for accessibility text.
            val empty = end < windowStart
            val count = if (empty) 0 else (start..end).sumOf { done[it] ?: 0 }
            val shownEnd = if (empty) start else end
            DayBar(
                label = if (bucketIndex == 0) {
                    when (language) {
                        AppLanguage.VIETNAMESE -> "Nay"
                        AppLanguage.ENGLISH -> "Now"
                    }
                } else {
                    "-${bucketIndex * bucketSize}"
                },
                count = count,
                isToday = bucketIndex == 0,
                startDate = LocalDate.fromEpochDays(start),
                endDate = LocalDate.fromEpochDays(shownEnd),
                isPadding = empty,
            )
        }
    }
}
