package dev.lildua.oddly.core.time

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * The current date in the device's own time zone.
 *
 * Exists so neither app has to reach for `Clock.System` directly — Swift cannot
 * name it without colliding with its own `Clock` protocol — and so there is one
 * place to intercept "what day is it" when tests need to pin it.
 */
object Today {
    fun local(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
