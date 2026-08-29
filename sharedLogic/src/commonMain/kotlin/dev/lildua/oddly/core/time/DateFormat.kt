package dev.lildua.oddly.core.time

import dev.lildua.oddly.domain.model.AppLanguage
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Date formatting for the app's two languages.
 *
 * Hand-rolled rather than using platform locale APIs so the output is identical
 * on Android and iOS, and so it follows the in-app language setting rather than
 * the device locale — the user can read the app in English on a Vietnamese
 * phone (spec §S15).
 */
object DateFormat {

    private val shortWeekdaysVi = mapOf(
        DayOfWeek.MONDAY to "T2",
        DayOfWeek.TUESDAY to "T3",
        DayOfWeek.WEDNESDAY to "T4",
        DayOfWeek.THURSDAY to "T5",
        DayOfWeek.FRIDAY to "T6",
        DayOfWeek.SATURDAY to "T7",
        DayOfWeek.SUNDAY to "CN",
    )

    private val shortWeekdaysEn = mapOf(
        DayOfWeek.MONDAY to "Mon",
        DayOfWeek.TUESDAY to "Tue",
        DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu",
        DayOfWeek.FRIDAY to "Fri",
        DayOfWeek.SATURDAY to "Sat",
        DayOfWeek.SUNDAY to "Sun",
    )

    private val fullWeekdaysVi = mapOf(
        DayOfWeek.MONDAY to "Thứ Hai",
        DayOfWeek.TUESDAY to "Thứ Ba",
        DayOfWeek.WEDNESDAY to "Thứ Tư",
        DayOfWeek.THURSDAY to "Thứ Năm",
        DayOfWeek.FRIDAY to "Thứ Sáu",
        DayOfWeek.SATURDAY to "Thứ Bảy",
        DayOfWeek.SUNDAY to "Chủ Nhật",
    )

    private val fullWeekdaysEn = mapOf(
        DayOfWeek.MONDAY to "Monday",
        DayOfWeek.TUESDAY to "Tuesday",
        DayOfWeek.WEDNESDAY to "Wednesday",
        DayOfWeek.THURSDAY to "Thursday",
        DayOfWeek.FRIDAY to "Friday",
        DayOfWeek.SATURDAY to "Saturday",
        DayOfWeek.SUNDAY to "Sunday",
    )

    private val monthsEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    /** Weekday labels for a Monday-first calendar grid. */
    fun weekdayHeaders(language: AppLanguage): List<String> =
        DayOfWeek.entries.map { shortWeekdays(language).getValue(it) }

    fun shortWeekday(date: LocalDate, language: AppLanguage): String =
        shortWeekdays(language).getValue(date.dayOfWeek)

    fun fullWeekday(date: LocalDate, language: AppLanguage): String =
        when (language) {
            AppLanguage.VIETNAMESE -> fullWeekdaysVi
            AppLanguage.ENGLISH -> fullWeekdaysEn
        }.getValue(date.dayOfWeek)

    /** "Thứ Bảy, 08 tháng 8" · "Saturday, 8 August" */
    fun dayAndMonth(date: LocalDate, language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE ->
            "${fullWeekday(date, language)}, ${pad(date.dayOfMonth)} tháng ${date.monthNumber}"
        AppLanguage.ENGLISH ->
            "${fullWeekday(date, language)}, ${date.dayOfMonth} ${monthsEn[date.monthNumber - 1]}"
    }

    /** "Tháng 8, 2026" · "August 2026" */
    fun monthAndYear(date: LocalDate, language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Tháng ${date.monthNumber}, ${date.year}"
        AppLanguage.ENGLISH -> "${monthsEn[date.monthNumber - 1]} ${date.year}"
    }

    /**
     * "08/08/2026" in both languages: day-first is unambiguous for Vietnamese
     * readers and still parses correctly for English ones, whereas month-first
     * would be misread by half the audience.
     */
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

    private fun shortWeekdays(language: AppLanguage) = when (language) {
        AppLanguage.VIETNAMESE -> shortWeekdaysVi
        AppLanguage.ENGLISH -> shortWeekdaysEn
    }

    private fun pad(value: Int): String = if (value < 10) "0$value" else value.toString()
}
