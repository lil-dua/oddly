package dev.lildua.oddly.core.text

import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.domain.usecase.DayActivity
import dev.lildua.oddly.domain.usecase.DayBar
import kotlinx.datetime.LocalDate

/**
 * Sentences read by TalkBack / VoiceOver that are assembled from data. They live
 * in shared code so both platforms announce exactly the same words; the
 * platform UI only attaches the result as a label.
 */
object AccessibilityText {
    /** "Thứ Hai, 20 tháng 10, đã hoàn thành, hôm nay" */
    fun calendarDay(date: LocalDate, completed: Boolean, isToday: Boolean, strings: Strings): String =
        buildString {
            append(DateFormat.dayAndMonth(date, strings.language))
            append(", ")
            append(if (completed) strings.a11yCompleted else strings.a11yNotCompleted)
            if (isToday) append(", ").append(strings.a11yToday)
        }

    fun weekStrip(days: List<DayActivity>, strings: Strings): String {
        val done = days.filter { it.completed }.map {
            if (it.isToday) strings.a11yToday else DateFormat.fullWeekday(it.date, strings.language)
        }
        return strings.a11yWeekStrip(days.size, done)
    }

    /**
     * [today] is the newest column's end date. A single-day column is only named by a
     * bare weekday when it lies within the last 7 days (where the weekday is
     * unambiguous); older ones, e.g. the oldest ALL bucket of a 9-day history, get the
     * full date so "Thứ Sáu" is never mistaken for this week's Friday.
     */
    fun chartBar(bar: DayBar, strings: Strings, today: LocalDate? = null): String =
        if (bar.startDate == bar.endDate) {
            val recent = today == null || today.toEpochDays() - bar.startDate.toEpochDays() in 0..6
            val day = when {
                bar.isToday -> strings.a11yToday
                recent -> DateFormat.fullWeekday(bar.startDate, strings.language)
                else -> DateFormat.dayAndMonth(bar.startDate, strings.language)
            }
            strings.a11yChartDay(day, bar.count)
        } else {
            strings.a11yChartRange(
                DateFormat.numeric(bar.startDate),
                DateFormat.numeric(bar.endDate),
                bar.count,
            )
        }

    /** Padding columns (wholly before the window, always 0) carry no information, so they are skipped. */
    fun chart(bars: List<DayBar>, strings: Strings): String {
        val today = bars.lastOrNull()?.endDate
        val spoken = bars.filterNot { it.isPadding }.map { chartBar(it, strings, today) }
        return (listOf(strings.a11yChartTitle) + spoken).joinToString(". ")
    }
}
