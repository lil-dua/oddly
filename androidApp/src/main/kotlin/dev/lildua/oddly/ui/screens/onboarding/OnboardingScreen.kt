package dev.lildua.oddly.ui.screens.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.components.Astronaut
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.GradientText
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.TextAction
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme

private data class Benefit(val icon: OddlyIcon, val label: String, val tint: androidx.compose.ui.graphics.Color)

/**
 * S02 — explains the 1% concept in two sentences and three benefits. Kept to a
 * single page; the spec caps onboarding at 2–3 screens total.
 */
@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onSkip: () -> Unit,
) {
    val palette = OddlyTheme.palette

    val benefits = listOf(
        Benefit(OddlyIcon.Sparkle, "Thử thách\nmỗi ngày", OddlyColors.Warning),
        Benefit(OddlyIcon.Heart, "Dễ thực hiện\nnhưng ý nghĩa", OddlyColors.Pink),
        Benefit(OddlyIcon.Refresh, "Thay đổi\ncuộc sống", OddlyColors.Blue),
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 70, seed = 9)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextAction("Bỏ qua", onSkip)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Chào mừng bạn đến với",
                style = MaterialTheme.typography.headlineSmall,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
            GradientText(
                text = "1% HUMAN",
                style = MaterialTheme.typography.displaySmall,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Mỗi ngày chúng tôi sẽ giao cho\nbạn một thử thách nhỏ.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            Astronaut(size = 210.dp)

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                benefits.forEach { benefit ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        OddlyIcon(benefit.icon, size = 24.dp, tint = benefit.tint)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = benefit.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            GradientButton(text = "Bắt đầu", onClick = onStart)

            Spacer(Modifier.height(20.dp))

            PageDots(count = 3, selected = 0)
        }
    }
}

@Composable
private fun PageDots(count: Int, selected: Int) {
    val palette = OddlyTheme.palette
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == selected) 8.dp else 6.dp)
                    .background(
                        color = if (index == selected) OddlyColors.Purple else palette.textTertiary,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
