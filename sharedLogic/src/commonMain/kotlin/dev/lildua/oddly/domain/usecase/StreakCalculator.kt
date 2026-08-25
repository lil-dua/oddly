package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.StreakInfo
import kotlinx.datetime.LocalDate

/** One day of the activity strip. */
data class DayActivity(
    val date: LocalDate,
    val completed: Boolean,
    val isToday: Boolean,
)

/**
 * Derives streak state from the completion log (spec §6.2).
 *
 * A streak is unbroken as long as there is a completion on every calendar day.
 * Today not being done yet does *not* break the streak — the user still has the
 * rest of the day, so a run ending yesterday still counts as current.
 */
object StreakCalculator {

    fun calculate(completions: List<ChallengeCompletion>, today: LocalDate): StreakInfo {
        val days = completions.map { it.date }.distinct().sorted()
        if (days.isEmpty()) return StreakInfo(current = 0, best = 0, lastCompletedDate = null)

        var best = 1
        var run = 1
        for (i in 1 until days.size) {
            run = if (days[i].toEpochDays() == days[i - 1].toEpochDays() + 1) run + 1 else 1
            if (run > best) best = run
        }

        val last = days.last()
        val daysSinceLast = today.toEpochDays() - last.toEpochDays()
        val current = if (daysSinceLast > 1) {
            0
        } else {
            var count = 1
            var cursor = days.size - 1
            while (cursor > 0 && days[cursor].toEpochDays() == days[cursor - 1].toEpochDays() + 1) {
                count++
                cursor--
            }
            count
        }

        return StreakInfo(current = current, best = best, lastCompletedDate = last)
    }

    /**
     * The [days] calendar days ending at [today], oldest first.
     *
     * Each entry carries its real date so the UI can label it with the correct
     * weekday — the window is a rolling one, not a Monday-to-Sunday week.
     */
    fun recentActivity(
        completions: List<ChallengeCompletion>,
        today: LocalDate,
        days: Int = 7,
    ): List<DayActivity> {
        val done = completions.map { it.date.toEpochDays() }.toSet()
        return ((days - 1) downTo 0).map { offset ->
            val epochDay = today.toEpochDays() - offset
            DayActivity(
                date = LocalDate.fromEpochDays(epochDay),
                completed = epochDay in done,
                isToday = offset == 0,
            )
        }
    }
}
