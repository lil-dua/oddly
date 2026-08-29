package dev.lildua.oddly.ui.screens.journey

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.components.GlowOrb
import dev.lildua.oddly.ui.components.OddlyCard
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.StatTile
import dev.lildua.oddly.ui.components.WeekStrip
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * S13 — the streak. MVP keeps this encouraging: there is no punishment
 * mechanic, only the current run, the record, and a nudge to keep going.
 */
@Composable
fun StreakScreen(
    state: OddlyAppState,
    onBack: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val streak = state.streak

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        OddlyTopBar(title = strings.streakTitle, onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))

            Box(contentAlignment = Alignment.Center) {
                StarField(Modifier.size(280.dp), starCount = 30, seed = 43)
                GlowOrb(OddlyColors.Flame, Modifier.size(260.dp), alpha = 0.4f)

                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(OddlyColors.Flame.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${streak.current}",
                            style = MaterialTheme.typography.displayLarge,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = strings.days,
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    value = "${streak.best}",
                    label = strings.personalBest,
                    accent = OddlyColors.Warning,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "${state.totalCompleted}",
                    label = strings.totalDone,
                    accent = OddlyColors.Purple,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            OddlyCard {
                SectionLabel(strings.lastSevenDays)
                Spacer(Modifier.height(16.dp))
                WeekStrip(days = state.weekActivity)
            }

            Spacer(Modifier.height(16.dp))

            // Encouragement, not punishment.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OddlyColors.Flame.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔥", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = strings.keepStreakTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.completedToday) {
                            strings.keepStreakDoneToday
                        } else {
                            strings.keepStreakPending
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OddlyCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = strings.reminderLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = strings.reminderToggleBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textTertiary,
                        )
                    }
                    Switch(
                        checked = state.settings.reminderEnabled,
                        onCheckedChange = {
                            state.settings = state.settings.copy(reminderEnabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = OddlyColors.Success,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = strings.streakNoPenalty,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
