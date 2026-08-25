package dev.lildua.oddly.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.components.BrandRing
import dev.lildua.oddly.ui.components.GradientText
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.theme.OddlyTheme
import kotlinx.coroutines.delay

/**
 * S01 — branding while the local database initialises. Deliberately brief; the
 * spec calls for a branding moment, not a held splash.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val palette = OddlyTheme.palette
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(700), label = "splash-alpha")
    val scale by animateFloatAsState(if (visible) 1f else 0.9f, tween(700), label = "splash-scale")

    LaunchedEffect(Unit) {
        visible = true
        delay(1900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 80, seed = 5)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha).scale(scale),
        ) {
            Box(contentAlignment = Alignment.Center) {
                BrandRing(size = 190.dp, strokeWidth = 7.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GradientText(text = "1%", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = "HUMAN",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(56.dp))

            Text(
                text = "Every tiny step",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "makes a huge change.",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 72.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val palette = OddlyTheme.palette
    var step by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            step = (step + 1) % 3
        }
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            Box(
                Modifier
                    .size(7.dp)
                    .background(
                        color = if (index == step) palette.textPrimary else palette.textTertiary,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
