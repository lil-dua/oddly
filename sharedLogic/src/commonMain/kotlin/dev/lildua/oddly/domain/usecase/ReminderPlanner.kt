package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.core.text.ReminderContent
import dev.lildua.oddly.core.text.ReminderSeed
import dev.lildua.oddly.core.text.Strings
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.ChallengeCompletion
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Decides whether a reminder firing on a given day should show, and with what
 * copy. It reads completions so a user who already did today's challenge is not
 * nagged, and lives in shared code so Android and iOS cannot drift apart.
 *
 * The streak is counted up to the day *before* the fire date: that is the run
 * which breaks if the fire date is skipped, and it stays computable for
 * reminders scheduled days ahead (no completions exist for the future, so those
 * simply fall back to the normal copy instead of overstating a streak).
 *
 * Priority: already done -> none; last scheduled -> lapsed; streak >= min ->
 * streak copy; otherwise the rotating [ReminderSeed] copy.
 */
object ReminderPlanner {

    const val STREAK_COPY_MIN_DAYS = 3

    /** Consecutive days with a completion ending on `date - 1`; later dates are ignored. */
    fun streakBefore(completions: List<ChallengeCompletion>, date: LocalDate): Int {
        val days = completions.map { it.date }.filter { it < date }.toSet()
        var cursor = date.minus(1, DateTimeUnit.DAY)
        var count = 0
        while (cursor in days) {
            count++
            cursor = cursor.minus(1, DateTimeUnit.DAY)
        }
        return count
    }

    /** The reminder for [fireDate], or null when it should be skipped. */
    fun content(
        completions: List<ChallengeCompletion>,
        fireDate: LocalDate,
        language: AppLanguage,
        isLastScheduled: Boolean,
    ): ReminderContent? {
        if (DailyChallengeRules.isDayDone(completions, fireDate)) return null
        val strings = Strings.of(language)
        if (isLastScheduled) {
            return ReminderContent(strings.reminderLapsedTitle, strings.reminderLapsedBody)
        }
        val streak = streakBefore(completions, fireDate)
        if (streak >= STREAK_COPY_MIN_DAYS) {
            return ReminderContent(strings.reminderStreakTitle(streak), strings.reminderStreakBody)
        }
        return ReminderSeed.content(fireDate.toEpochDays(), language)
    }
}
