package dev.lildua.oddly.core.time

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Vietnamese date formatting.
 *
 * Hand-rolled rather than using platform locale APIs so the output is identical
 * on Android and iOS and needs no expect/actual. When English is added (spec
 * §8), branch here on the selected [dev.lildua.oddly.domain.model.AppLanguage].
 */
object DateFormat {

    private val shortWeekdays = mapOf(
        DayOfWeek.MONDAY to "T2",
        DayOfWeek.TUESDAY to "T3",
        DayOfWeek.WEDNESDAY to "T4",
        DayOfWeek.THURSDAY to "T5",
        DayOfWeek.FRIDAY to "T6",
        DayOfWeek.SATURDAY to "T7",
        DayOfWeek.SUNDAY to "CN",
    )

    private val fullWeekdays = mapOf(
        DayOfWeek.MONDAY to "Thứ Hai",
        DayOfWeek.TUESDAY to "Thứ Ba",
        DayOfWeek.WEDNESDAY to "Thứ Tư",
        DayOfWeek.THURSDAY to "Thứ Năm",
        DayOfWeek.FRIDAY to "Thứ Sáu",
        DayOfWeek.SATURDAY to "Thứ Bảy",
        DayOfWeek.SUNDAY to "Chủ Nhật",
    )

    /** Weekday labels for a Monday-first calendar grid. */
    val weekdayHeaders: List<String> = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    fun shortWeekday(date: LocalDate): String = shortWeekdays.getValue(date.dayOfWeek)

    fun fullWeekday(date: LocalDate): String = fullWeekdays.getValue(date.dayOfWeek)

    /** "Thứ Bảy, 08 tháng 8" */
    fun dayAndMonth(date: LocalDate): String =
        "${fullWeekday(date)}, ${pad(date.dayOfMonth)} tháng ${date.monthNumber}"

    /** "Tháng 8, 2026" */
    fun monthAndYear(date: LocalDate): String = "Tháng ${date.monthNumber}, ${date.year}"

    /** "08/08/2026" */
    fun numeric(date: LocalDate): String =
        "${pad(date.dayOfMonth)}/${pad(date.monthNumber)}/${date.year}"

    /** "09:41" */
    fun time(time: LocalTime): String = "${pad(time.hour)}:${pad(time.minute)}"

    /** Monday-first index, 0..6. */
    fun weekdayIndex(date: LocalDate): Int = date.dayOfWeek.ordinal

    /** Number of days in the month containing [date]. */
    fun daysInMonth(date: LocalDate): Int {
        val firstOfThis = LocalDate(date.year, date.monthNumber, 1)
        val firstOfNext = if (date.monthNumber == 12) {
            LocalDate(date.year + 1, 1, 1)
        } else {
            LocalDate(date.year, date.monthNumber + 1, 1)
        }
        return firstOfNext.toEpochDays() - firstOfThis.toEpochDays()
    }

    private fun pad(value: Int): String = if (value < 10) "0$value" else value.toString()
}
