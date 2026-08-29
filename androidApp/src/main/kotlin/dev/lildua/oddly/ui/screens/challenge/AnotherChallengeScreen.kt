package dev.lildua.oddly.ui.screens.challenge

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.components.GlowOrb
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.SecondaryButton
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * S08 — random challenge. The dice roll is a short piece of anticipation before
 * the reroll actually happens.
 */
@Composable
fun AnotherChallengeScreen(
    onBack: () -> Unit,
    onSurpriseMe: () -> Unit,
    onChooseCategory: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var rolls by remember { mutableIntStateOf(0) }
    val spin by animateFloatAsState(
        targetValue = rolls * 360f,
        animationSpec = tween(700),
        label = "dice-spin",
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 50, seed = 31)

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            OddlyTopBar(title = "", onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = strings.anotherChallengeTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                Box(contentAlignment = Alignment.Center) {
                    GlowOrb(OddlyColors.Purple, Modifier.size(240.dp), alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(palette.surfaceElevated)
                            .border(
                                1.dp,
                                OddlyColors.Purple.copy(alpha = 0.35f),
                                RoundedCornerShape(32.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        OddlyIcon(
                            OddlyIcon.Dice,
                            size = 72.dp,
                            tint = palette.textPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.rotate(spin),
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = strings.anotherChallengeBody,
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                GradientButton(
                    text = strings.surpriseMe,
                    onClick = {
                        rolls += 2
                        scope.launch {
                            delay(700)
                            onSurpriseMe()
                        }
                    },
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = strings.or,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textTertiary,
                )

                Spacer(Modifier.height(20.dp))

                SecondaryButton(text = strings.chooseAnotherCategory, onClick = onChooseCategory)

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
