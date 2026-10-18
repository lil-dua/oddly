package dev.lildua.oddly.core.time

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The current date and time in the device's own time zone.
 *
 * Exists so neither app has to reach for `Clock.System` directly — Swift cannot
 * name it without colliding with its own `Clock` protocol — and so there is one
 * place to intercept "what day is it" when tests need to pin it.
 */
object Today {
    /** Wall-clock now, used to stamp completions with the time they happened. */
    fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    // Derived from [now] rather than read separately, so the date and time a
    // caller sees can never straddle midnight.
    fun local(): LocalDate = now().date
}
