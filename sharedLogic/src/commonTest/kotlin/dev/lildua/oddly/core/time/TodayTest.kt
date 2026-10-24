package dev.lildua.oddly.core.time

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class TodayTest {
    private val tz get() = TimeZone.currentSystemDefault()

    @AfterTest
    fun unpin() = Today.pinForTesting(null)

    private fun gapSeconds(a: LocalDateTime, b: LocalDateTime): Long =
        (a.toInstant(tz) - b.toInstant(tz)).inWholeSeconds

    @Test
    fun `pinned now starts at the given time`() {
        Today.pinForTesting("2026-03-10T09:00:00")
        val gap = gapSeconds(Today.now(), LocalDateTime.parse("2026-03-10T09:00:00"))
        assertTrue(gap in 0..2, "gap was $gap")
    }

    @Test
    fun `pinned local returns the pinned date`() {
        Today.pinForTesting("2026-03-10T09:00:00")
        assertEquals(LocalDate(2026, 3, 10), Today.local())
    }

    @Test
    fun `pinning again jumps to the new day`() {
        Today.pinForTesting("2026-03-10T09:00:00")
        Today.pinForTesting("2026-03-11T09:00:00")
        assertEquals(LocalDate(2026, 3, 11), Today.local())
    }

    @Test
    fun `null pin restores the real clock`() {
        Today.pinForTesting("2026-03-10T09:00:00")
        Today.pinForTesting(null)
        val real = Clock.System.now()
        assertTrue((Today.now().toInstant(tz) - real).absoluteValue < 2.seconds)
    }
}
