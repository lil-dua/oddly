package dev.lildua.oddly.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.data.seed.QuoteSeed
import dev.lildua.oddly.ui.components.OddlyCard
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.StatTile
import dev.lildua.oddly.ui.components.TodayChallengeCard
import dev.lildua.oddly.ui.components.WeekStrip
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.localized
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * S05 — the default screen on every launch after the first. Today's challenge is
 * the visual hero; everything else supports it.
 */
@Composable
fun HomeScreen(
    state: OddlyAppState,
    onOpenChallenge: () -> Unit,
    onStartChallenge: () -> Unit,
    onAnotherChallenge: () -> Unit,
    onOpenStreak: () -> Unit,
    onOpenQuotes: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val streak = state.streak

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = DateFormat.dayAndMonth(state.today, strings.language),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = strings.homeHeadline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textPrimary,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        TodayChallengeCard(
            challenge = state.todayChallenge.localized(),
            completed = state.isCompletedToday(state.todayChallenge.id),
            onClick = onOpenChallenge,
            onStart = onStartChallenge,
        )

        Spacer(Modifier.height(16.dp))

        // Streak card
        OddlyCard(onClick = onOpenStreak) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(strings.streak)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${strings.streakDays(streak.current)} 🔥",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.flame,
                )
                Spacer(Modifier.width(6.dp))
                OddlyIcon(OddlyIcon.ChevronRight, size = 14.dp, tint = palette.textTertiary)
            }

            Spacer(Modifier.height(16.dp))

            WeekStrip(days = state.weekActivity)
        }

        Spacer(Modifier.height(16.dp))

        // Quick stats
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = "${state.totalCompleted}",
                label = strings.challengesCompletedLabel,
                accent = OddlyColors.Purple,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = "Lv.${state.profile.level}",
                label = strings.xpProgress(state.profile.xpInLevel, state.profile.xpForNextLevel),
                accent = OddlyColors.Pink,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Quote of the day
        val quote = QuoteSeed.forDayIndex(state.today.toEpochDays()).localized()
        OddlyCard(onClick = onOpenQuotes) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(strings.dailyInspiration)
                Spacer(Modifier.weight(1f))
                OddlyIcon(OddlyIcon.ChevronRight, size = 14.dp, tint = palette.textTertiary)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "– ${quote.author}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary,
            )
        }

        Spacer(Modifier.height(16.dp))

        // Explore another challenge
        OddlyCard(onClick = onAnotherChallenge) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = strings.wantAnotherChallenge,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = strings.wantAnotherChallengeBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textTertiary,
                        )
                    }
                }
                OddlyIcon(OddlyIcon.Dice, size = 28.dp, tint = OddlyColors.Purple)
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
