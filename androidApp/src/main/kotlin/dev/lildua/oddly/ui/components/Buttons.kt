package dev.lildua.oddly.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.theme.OddlyGradients
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * The dominant call-to-action. Per spec §8.1 there is at most one of these on
 * screen at a time — everything else is a [SecondaryButton] or a plain
 * [TextAction].
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    brush: Brush = OddlyGradients.primaryButton,
    height: Dp = 54.dp,
    leadingIcon: OddlyIcon? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "cta-scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.4f)
            .background(brush, RoundedCornerShape(percent = 50))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.let {
                OddlyIcon(it, size = 18.dp, tint = Color(0xFF1B0A25))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                // Deep plum rather than pure black — reads softer on the neon fill.
                color = Color(0xFF1B0A25),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Outlined alternative for the non-dominant action on a screen. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    leadingIcon: OddlyIcon? = null,
) {
    val palette = OddlyTheme.palette
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "secondary-scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                RoundedCornerShape(percent = 50),
            )
            .background(palette.surfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(percent = 50))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.let { OddlyIcon(it, size = 18.dp, tint = palette.textPrimary) }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textPrimary,
            )
        }
    }
}

/** Low-emphasis inline action, e.g. "Để sau" or "Bỏ qua". */
@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = OddlyTheme.palette.textSecondary,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
    )
}
