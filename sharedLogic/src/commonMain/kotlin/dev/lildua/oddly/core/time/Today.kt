package dev.lildua.oddly.core.time

import kotlin.concurrent.Volatile
import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * The current date and time in the device's own time zone.
 *
 * Exists so neither app has to reach for `Clock.System` directly — Swift cannot
 * name it without colliding with its own `Clock` protocol — and so there is one
 * place to intercept "what day is it" when tests need to pin it.
 */
object Today {
    // An offset from the real clock rather than a frozen instant: the pinned
    // clock keeps ticking, so a UI test running in another process (XCUITest)
    // can still cross midnight by launching at 23:59:xx and waiting.
    @Volatile
    private var offset: Duration = Duration.ZERO

    /** Wall-clock now, used to stamp completions with the time they happened. */
    fun now(): LocalDateTime =
        (Clock.System.now() + offset).toLocalDateTime(TimeZone.currentSystemDefault())

    // Derived from [now] rather than read separately, so the date and time a
    // caller sees can never straddle midnight.
    fun local(): LocalDate = now().date

    /**
     * Makes [now] read [start] (ISO `LocalDateTime`, e.g. `2026-03-10T09:00:00`)
     * as of this call, then keep running. `null` restores the real clock.
     *
     * Only UI tests and debug-only hooks call this; nothing in the shipped app
     * does, so the default offset of zero means real time. A string rather than
     * a `LocalDateTime` so Swift needs no parse call with default arguments.
     */
    fun pinForTesting(start: String?) {
        offset = if (start == null) {
            Duration.ZERO
        } else {
            val tz = TimeZone.currentSystemDefault()
            LocalDateTime.parse(start).toInstant(tz) - Clock.System.now()
        }
    }
}
