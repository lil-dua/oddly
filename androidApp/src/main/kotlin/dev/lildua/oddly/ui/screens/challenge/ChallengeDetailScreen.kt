package dev.lildua.oddly.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.model.LocalizedChallenge
import dev.lildua.oddly.domain.usecase.ChallengeAvailability
import dev.lildua.oddly.ui.components.GlowOrb
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.OddlyChip
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.TextAction
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * S06 — the full brief for a challenge: why it matters, how to do it, and what
 * it pays. One dominant CTA that follows [availability]: start (today's pick),
 * make it today's (library entry), or a banner when today is already done.
 * Reroll is a secondary text action, only while today's pick can still be done.
 */
@Composable
fun ChallengeDetailScreen(
    challenge: LocalizedChallenge,
    availability: ChallengeAvailability,
    /** False when swaps ran out; hides the swap actions and says why. */
    canSwap: Boolean,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onAdopt: () -> Unit,
    onAnother: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val accent = challenge.category.color

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 45, seed = 23)

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OddlyTopBar(title = "", onBack = onBack)

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    GlowOrb(accent, Modifier.size(150.dp), alpha = 0.35f)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(accent.copy(alpha = 0.16f))
                            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(challenge.category.emoji, style = MaterialTheme.typography.displaySmall)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OddlyChip(
                        challenge.categoryTitle,
                        leadingEmoji = challenge.category.emoji,
                        accent = accent,
                    )
                    OddlyChip(challenge.difficultyTitle)
                    OddlyChip(strings.minutes(challenge.estimatedMinutes))
                }

                Spacer(Modifier.height(28.dp))
            }

            // Why it matters
            InfoBlock(
                label = strings.whyItMatters,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(
                    text = challenge.whyItMatters,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            // How to do it
            InfoBlock(
                label = strings.howToDoIt,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                challenge.howToDoIt.forEachIndexed { index, step ->
                    if (index > 0) Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "✦",
                            style = MaterialTheme.typography.bodyMedium,
                            color = accent,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Reward
            InfoBlock(
                label = strings.reward,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${challenge.humanityPercent}% Humanity",
                        style = MaterialTheme.typography.titleMedium,
                        color = OddlyColors.Success,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "+${challenge.rewardXp} XP",
                        style = MaterialTheme.typography.titleMedium,
                        color = OddlyColors.Purple,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(Modifier.padding(horizontal = 24.dp)) {
                when (availability) {
                    ChallengeAvailability.DONE_THIS_TODAY -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(percent = 50))
                            .background(OddlyColors.Success.copy(alpha = 0.14f))
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OddlyIcon(
                            OddlyIcon.Check,
                            size = 18.dp,
                            tint = OddlyColors.Success,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = strings.alreadyCompletedThis,
                            style = MaterialTheme.typography.labelLarge,
                            color = OddlyColors.Success,
                        )
                    }

                    ChallengeAvailability.DAY_ALREADY_DONE ->
                        NeutralNotice(strings.todayDoneComeBackTomorrow)

                    ChallengeAvailability.NOT_TODAYS ->
                        if (canSwap) {
                            GradientButton(text = strings.setAsTodaysChallenge, onClick = onAdopt)
                        } else {
                            NeutralNotice(strings.noSwapsLeftAdopt)
                        }

                    ChallengeAvailability.CAN_COMPLETE -> {
                        GradientButton(text = strings.begin, onClick = onComplete)

                        Spacer(Modifier.height(12.dp))

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (canSwap) {
                                TextAction(strings.seeAnotherChallenge, onAnother)
                            } else {
                                Text(
                                    text = strings.noSwapsLeftToday,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textTertiary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Neutral rather than green: this is not a win for *this* challenge. */
@Composable
private fun NeutralNotice(text: String) {
    val palette = OddlyTheme.palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surfaceElevated)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InfoBlock(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = OddlyTheme.palette
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surfaceElevated)
            .padding(18.dp),
    ) {
        SectionLabel(label)
        Spacer(Modifier.height(12.dp))
        content()
    }
}
