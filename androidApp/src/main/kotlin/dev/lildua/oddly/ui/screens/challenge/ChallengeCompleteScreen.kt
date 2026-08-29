package dev.lildua.oddly.ui.screens.challenge

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.model.LocalizedChallenge
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.GradientText
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.SecondaryButton
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.TextAction
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * S07 — the reward moment. Per spec §8.1 the celebration must not slow the user
 * down, so the animation is short and every action stays tappable throughout.
 */
@Composable
fun ChallengeCompleteScreen(
    challenge: LocalizedChallenge,
    reward: OddlyAppState.Reward?,
    streakDays: Int,
    quote: String,
    onShare: () -> Unit,
    onAnother: () -> Unit,
    onDone: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    var played by remember { mutableStateOf(false) }
    val burst by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(1900),
        label = "confetti",
    )
    val popScale by animateFloatAsState(
        targetValue = if (played) 1f else 0.4f,
        animationSpec = tween(500),
        label = "pop",
    )

    LaunchedEffect(Unit) { played = true }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 60, seed = 29)
        Confetti(progress = burst, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextAction(strings.done, onDone)
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "🥳",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.scale(popScale),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = strings.celebrationTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = palette.textPrimary,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = strings.celebrationBody,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            GradientText(
                text = "+${reward?.humanityPercent ?: challenge.humanityPercent}% Humanity",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RewardPill("+${reward?.xp ?: challenge.rewardXp} XP", OddlyColors.Purple)
                RewardPill("🔥 ${strings.streakDays(streakDays)}", OddlyColors.Flame)
            }

            if (reward?.leveledUp == true) {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(OddlyColors.Warning.copy(alpha = 0.16f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    OddlyIcon(OddlyIcon.Sparkle, size = 16.dp, tint = OddlyColors.Warning)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${strings.levelUp} ${reward.newLevel}!",
                        style = MaterialTheme.typography.labelMedium,
                        color = OddlyColors.Warning,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceElevated)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "“$quote”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.weight(1f))

            GradientButton(text = strings.share, onClick = onShare, leadingIcon = OddlyIcon.Share)

            Spacer(Modifier.height(12.dp))

            SecondaryButton(text = strings.seeAnotherChallenge, onClick = onAnother)

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RewardPill(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

private data class Particle(
    val angle: Float,
    val distance: Float,
    val color: Color,
    val size: Float,
    val spin: Float,
)

/**
 * A one-shot confetti burst driven by a single 0..1 [progress] value, so the
 * whole effect is one animation rather than dozens.
 */
@Composable
private fun Confetti(progress: Float, modifier: Modifier = Modifier) {
    val colors = listOf(
        OddlyColors.Pink,
        OddlyColors.Purple,
        OddlyColors.Blue,
        OddlyColors.Warning,
        OddlyColors.Success,
    )
    val particles = remember {
        val random = Random(1)
        List(56) {
            Particle(
                angle = random.nextFloat() * 360f,
                distance = 0.25f + random.nextFloat() * 0.75f,
                color = colors[random.nextInt(colors.size)],
                size = 4f + random.nextFloat() * 6f,
                spin = random.nextFloat() * 360f,
            )
        }
    }

    Canvas(modifier) {
        if (progress <= 0f) return@Canvas
        val origin = Offset(size.width / 2f, size.height * 0.38f)
        val reach = size.minDimension * 0.55f
        // Ease out, then let gravity pull the pieces down.
        val eased = 1f - (1f - progress) * (1f - progress)
        // Hold full opacity through the spread, then fade over the last third
        // so the burst is actually legible rather than gone on arrival.
        val fade = ((1f - progress) / 0.35f).coerceIn(0f, 1f)

        particles.forEach { particle ->
            val radians = particle.angle * (kotlin.math.PI / 180f).toFloat()
            val travel = particle.distance * reach * eased
            val gravity = size.height * 0.25f * progress * progress
            val position = Offset(
                origin.x + cos(radians) * travel,
                origin.y + sin(radians) * travel + gravity,
            )
            rotate(degrees = particle.spin * progress, pivot = position) {
                drawRect(
                    color = particle.color.copy(alpha = fade),
                    topLeft = Offset(position.x - particle.size / 2f, position.y - particle.size / 2f),
                    size = Size(particle.size, particle.size * 1.6f),
                )
            }
        }
    }
}
