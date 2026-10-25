package dev.lildua.oddly.ui.screens.journey

import androidx.compose.ui.platform.testTag
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.ui.components.CappedFontScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.ui.components.ChallengeRow
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.IconCircleButton
import dev.lildua.oddly.ui.components.a11yHeading
import dev.lildua.oddly.ui.components.a11yHidden
import dev.lildua.oddly.ui.components.a11yLiveRegion
import dev.lildua.oddly.ui.components.selectableNoRipple
import dev.lildua.oddly.core.text.AccessibilityText
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyGradients
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.localized
import dev.lildua.oddly.ui.theme.OddlyTheme
import kotlinx.datetime.LocalDate

/**
 * S10 — completion history by day. An empty day is neutral, never framed as a
 * miss (spec §S10).
 */
@Composable
fun CalendarScreen(
    state: OddlyAppState,
    onBack: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    var visibleMonth by remember { mutableStateOf(LocalDate(state.today.year, state.today.monthNumber, 1)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(state.today) }

    val completedDays = remember(state.completions) {
        state.completions.map { it.date.toEpochDays() }.toSet()
    }

    Column(
        Modifier
            .testTag(UiTestTags.CALENDAR_SCREEN)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        OddlyTopBar(title = strings.calendar, onBack = onBack)

        Column(Modifier.padding(horizontal = 20.dp)) {

            // Month switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = DateFormat.monthAndYear(visibleMonth, strings.language),
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                    // Live region so paging months announces the new month name.
                    modifier = Modifier.weight(1f).a11yHeading().a11yLiveRegion(),
                )
                IconCircleButton(
                    icon = OddlyIcon.ChevronLeft,
                    contentDescription = strings.a11yPreviousMonth,
                    onClick = { visibleMonth = shiftMonth(visibleMonth, -1) },
                    diameter = 34.dp,
                )
                Spacer(Modifier.width(8.dp))
                IconCircleButton(
                    icon = OddlyIcon.ChevronRight,
                    contentDescription = strings.a11yNextMonth,
                    onClick = { visibleMonth = shiftMonth(visibleMonth, 1) },
                    diameter = 34.dp,
                )
            }

            Spacer(Modifier.height(20.dp))

            // Weekday headers
            // Each day cell already speaks its full weekday, so the header row is redundant.
            // Cells are width-bound; past 1.5x two-digit days and weekday labels overflow them.
            CappedFontScale(1.5f) {
            Row(Modifier.fillMaxWidth().a11yHidden()) {
                DateFormat.weekdayHeaders(strings.language).forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            }

            Spacer(Modifier.height(10.dp))

            CappedFontScale(1.5f) {
                MonthGrid(
                    month = visibleMonth,
                    today = state.today,
                    selected = selectedDate,
                    completedDays = completedDays,
                    onSelect = { selectedDate = it },
                )
            }

            Spacer(Modifier.height(24.dp))

            // Selected day detail
            val date = selectedDate
            if (date != null) {
                val dayCompletions = state.completionsOn(date)
                SectionLabel(DateFormat.numeric(date))
                Spacer(Modifier.height(12.dp))

                if (dayCompletions.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surfaceElevated)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🌙", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.a11yHidden())
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = strings.noChallengeThisDay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    dayCompletions.forEach { completion ->
                        state.challengeOf(completion)?.let { challenge ->
                            ChallengeRow(
                                challenge = challenge.localized(),
                                trailingText = "+${completion.humanityPercent}%",
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Recent history
            SectionLabel(strings.recent)
            Spacer(Modifier.height(12.dp))
            state.completions
                .sortedByDescending { it.date.toEpochDays() }
                .take(6)
                .forEach { completion ->
                    state.challengeOf(completion)?.let { challenge ->
                        Text(
                            text = DateFormat.numeric(completion.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textTertiary,
                        )
                        Spacer(Modifier.height(6.dp))
                        ChallengeRow(
                            challenge = challenge.localized(),
                            trailingText = "+${completion.humanityPercent}%",
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MonthGrid(
    month: LocalDate,
    today: LocalDate,
    selected: LocalDate?,
    completedDays: Set<Int>,
    onSelect: (LocalDate) -> Unit,
) {
    val palette = OddlyTheme.palette
    val daysInMonth = DateFormat.daysInMonth(month)
    val leadingBlanks = DateFormat.weekdayIndex(month)

    // Pad the head so the 1st lands under the right weekday, and the tail so the
    // final row is a complete week.
    val cells = buildList<LocalDate?> {
        repeat(leadingBlanks) { add(null) }
        for (day in 1..daysInMonth) {
            add(LocalDate(month.year, month.monthNumber, day))
        }
        while (size % 7 != 0) add(null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                isToday = date == today,
                                isSelected = date == selected,
                                isCompleted = date.toEpochDays() in completedDays,
                                onClick = { onSelect(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val description = AccessibilityText.calendarDay(date, isCompleted, isToday, strings)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .then(
                when {
                    isCompleted -> Modifier.background(OddlyGradients.flame)
                    isSelected -> Modifier.background(OddlyColors.Purple.copy(alpha = 0.2f))
                    else -> Modifier
                },
            )
            .then(
                when {
                    isSelected && !isCompleted -> Modifier.border(1.5.dp, OddlyColors.Purple, CircleShape)
                    isToday && !isCompleted -> Modifier.border(1.dp, palette.textTertiary, CircleShape)
                    else -> Modifier
                },
            )
            // No role: the state is "selected", the label already carries date and completion.
            .selectableNoRipple(isSelected, role = null, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${date.dayOfMonth}",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isCompleted -> androidx.compose.ui.graphics.Color(0xFF2B1206)
                isToday || isSelected -> palette.textPrimary
                else -> palette.textSecondary
            },
        )
    }
}

/** Move [date] by [delta] months, keeping the day at the 1st. */
private fun shiftMonth(date: LocalDate, delta: Int): LocalDate {
    val zeroBased = (date.year * 12 + (date.monthNumber - 1)) + delta
    return LocalDate(zeroBased / 12, zeroBased % 12 + 1, 1)
}
