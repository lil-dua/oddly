package dev.lildua.oddly.core.text

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.domain.usecase.DayActivity
import dev.lildua.oddly.domain.usecase.DayBar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccessibilityTextTest {

    private val both = listOf<Strings>(VietnameseStrings, EnglishStrings)

    private fun week(vararg doneOffsets: Int): List<DayActivity> =
        (6 downTo 0).map { DayActivity(daysAgo(it), it in doneOffsets, it == 0) }

    private fun bar(offset: Int, count: Int, today: Boolean = false) =
        DayBar("x", count, today, daysAgo(offset), daysAgo(offset))

    // MARK: - calendarDay

    @Test
    fun `calendarDay names the date and says completed in both languages`() {
        for (s in both) {
            val text = AccessibilityText.calendarDay(daysAgo(1), true, false, s)
            assertTrue(text.contains(DateFormat.dayAndMonth(daysAgo(1), s.language)))
            assertTrue(text.contains(s.a11yCompleted))
            assertFalse(text.contains(s.a11yNotCompleted))
            assertFalse(text.contains(s.a11yToday))
        }
    }

    @Test
    fun `calendarDay says not completed for a missed day`() {
        for (s in both) {
            val text = AccessibilityText.calendarDay(daysAgo(2), false, false, s)
            assertTrue(text.contains(s.a11yNotCompleted))
            assertFalse(text.contains(s.a11yToday))
        }
    }

    @Test
    fun `calendarDay adds today only for today`() {
        for (s in both) {
            val text = AccessibilityText.calendarDay(TODAY, true, true, s)
            assertTrue(text.endsWith(s.a11yToday))
            assertTrue(text.contains(s.a11yCompleted))
        }
    }

    @Test
    fun `calendarDay for a future day reads as not completed and not today`() {
        for (s in both) {
            val text = AccessibilityText.calendarDay(daysAgo(-3), false, false, s)
            assertTrue(text.contains(DateFormat.dayAndMonth(daysAgo(-3), s.language)))
            assertTrue(text.contains(s.a11yNotCompleted))
            assertFalse(text.contains(s.a11yToday))
        }
    }

    @Test
    fun `calendarDay uses the vietnamese weekday wording`() {
        // TODAY is a Wednesday
        val text = AccessibilityText.calendarDay(TODAY, false, false, VietnameseStrings)
        assertTrue(text.startsWith("Thứ Tư"), text)
        val en = AccessibilityText.calendarDay(TODAY, false, false, EnglishStrings)
        assertTrue(en.startsWith("Wednesday"), en)
    }

    // MARK: - weekStrip

    @Test
    fun `weekStrip with nothing done says nothing completed`() {
        assertEquals(
            "7 ngày gần nhất: chưa hoàn thành ngày nào",
            AccessibilityText.weekStrip(week(), VietnameseStrings),
        )
        assertEquals(
            "Last 7 days: nothing completed yet",
            AccessibilityText.weekStrip(week(), EnglishStrings),
        )
    }

    @Test
    fun `weekStrip lists three done days oldest first`() {
        for (s in both) {
            val text = AccessibilityText.weekStrip(week(5, 3, 1), s)
            val names = listOf(5, 3, 1).map { DateFormat.fullWeekday(daysAgo(it), s.language) }
            val positions = names.map { text.indexOf(it) }
            assertTrue(positions.all { it >= 0 }, text)
            assertEquals(positions.sorted(), positions, text)
            assertFalse(text.contains(s.a11yToday))
        }
        assertTrue(AccessibilityText.weekStrip(week(5, 3, 1), VietnameseStrings).contains("hoàn thành 3 ngày"))
        assertTrue(AccessibilityText.weekStrip(week(5, 3, 1), EnglishStrings).contains("3 days"))
    }

    @Test
    fun `weekStrip uses the word today when today is done`() {
        for (s in both) {
            val text = AccessibilityText.weekStrip(week(0), s)
            assertTrue(text.contains(s.a11yToday), text)
            assertFalse(text.contains(DateFormat.fullWeekday(TODAY, s.language)), text)
        }
    }

    @Test
    fun `weekStrip uses singular day in english for one done day`() {
        val text = AccessibilityText.weekStrip(week(2), EnglishStrings)
        assertTrue(text.contains("completed on 1 day "), text)
        assertFalse(text.contains("1 days"), text)
    }

    @Test
    fun `weekStrip reports the number of days passed`() {
        assertTrue(AccessibilityText.weekStrip(week().take(3), EnglishStrings).contains("Last 3 days"))
    }

    // MARK: - chartBar

    @Test
    fun `chartBar single day uses the full weekday not the short label`() {
        for (s in both) {
            val text = AccessibilityText.chartBar(bar(2, 4), s)
            assertTrue(text.startsWith(DateFormat.fullWeekday(daysAgo(2), s.language)), text)
            assertTrue(text.contains(s.challengeCount(4)), text)
        }
    }

    @Test
    fun `chartBar today uses the word today`() {
        for (s in both) {
            val text = AccessibilityText.chartBar(bar(0, 1, today = true), s)
            assertTrue(text.startsWith(s.a11yToday), text)
        }
    }

    @Test
    fun `chartBar range names both numeric dates and the count`() {
        for (s in both) {
            val b = DayBar("-12", 5, false, daysAgo(12), daysAgo(6))
            val text = AccessibilityText.chartBar(b, s)
            assertTrue(text.contains(DateFormat.numeric(daysAgo(12))), text)
            assertTrue(text.contains(DateFormat.numeric(daysAgo(6))), text)
            assertTrue(text.contains(s.challengeCount(5)), text)
        }
        assertTrue(
            AccessibilityText.chartBar(DayBar("a", 0, false, daysAgo(9), daysAgo(3)), VietnameseStrings)
                .startsWith("Từ "),
        )
    }

    @Test
    fun `chartBar zero count still reads a count`() {
        assertTrue(AccessibilityText.chartBar(bar(1, 0), EnglishStrings).contains(EnglishStrings.challengeCount(0)))
    }

    // MARK: - chart

    @Test
    fun `chart starts with the title and includes all seven bars`() {
        for (s in both) {
            val bars = (6 downTo 0).map { bar(it, it, today = it == 0) }
            val text = AccessibilityText.chart(bars, s)
            assertTrue(text.startsWith(s.a11yChartTitle))
            assertEquals(8, text.split(". ").size, text)
            bars.forEach { assertTrue(text.contains(AccessibilityText.chartBar(it, s))) }
        }
    }

    @Test
    fun `chart with no bars is just the title`() {
        for (s in both) {
            assertEquals(s.a11yChartTitle, AccessibilityText.chart(emptyList(), s))
        }
    }

    @Test
    fun `chart with all zero bars still lists every column`() {
        val bars = (6 downTo 0).map { bar(it, 0, today = it == 0) }
        val text = AccessibilityText.chart(bars, EnglishStrings)
        assertEquals(7, Regex("0 challenges|0 challenge").findAll(text).count(), text)
    }

    // MARK: - Strings with arguments

    @Test
    fun `parameterised a11y strings contain their arguments in both languages`() {
        for (s in both) {
            assertTrue(s.a11yQuotePosition(3, 60).let { it.contains("3") && it.contains("60") })
            assertTrue(s.a11yLevel(7).contains("7"))
            assertTrue(s.a11yChartDay("Mon", 2).contains("Mon"))
            assertTrue(s.a11yChartRange("A", "B", 2).let { it.contains("A") && it.contains("B") })
            assertTrue(s.a11yWeekStrip(7, listOf("Mon", "Tue")).let { it.contains("Mon") && it.contains("Tue") })
        }
    }
}
