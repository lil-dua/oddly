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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.theme.OddlyGradients
import dev.lildua.oddly.ui.theme.OddlyTheme

/** The standard content container: rounded, slightly lifted, hairline border. */
@Composable
fun OddlyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    background: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    contentPadding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && onClick != null) 0.985f else 1f, label = "card-scale")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(background)
            .border(BorderStroke(1.dp, borderColor), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        content = content,
    )
}

/** Card variant filled with a brush — used for hero and category surfaces. */
@Composable
fun GradientCard(
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = Color.Transparent,
    contentPadding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush)
            .border(BorderStroke(1.dp, borderColor), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        content = content,
    )
}

/** Text painted with a gradient — the "1%" wordmark and hero numbers. */
@Composable
fun GradientText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    brush: Brush = OddlyGradients.brandText,
) {
    Text(text = text, style = style.copy(brush = brush), modifier = modifier)
}

/** Small uppercase label that heads a section, e.g. "GỢI Ý", "PHẦN THƯỞNG". */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = OddlyTheme.palette.textTertiary,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier,
    )
}

/** A rounded progress track filled with a gradient. */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    brush: Brush = OddlyGradients.progress,
    trackColor: Color = OddlyTheme.palette.surfaceHighest,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "progress")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(RoundedCornerShape(percent = 50))
                .background(brush),
        )
    }
}

/** Screen header with an optional back affordance and trailing slot. */
@Composable
fun OddlyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = OddlyTheme.palette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(palette.surfaceElevated)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                OddlyIcon(OddlyIcon.ChevronLeft, size = 20.dp, tint = palette.textPrimary)
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** A pill-shaped tag: difficulty, category, estimated time. */
@Composable
fun OddlyChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = OddlyTheme.palette.textSecondary,
    leadingEmoji: String? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val palette = OddlyTheme.palette
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) accent.copy(alpha = 0.2f) else palette.surfaceElevated)
            .border(
                BorderStroke(1.dp, if (selected) accent.copy(alpha = 0.7f) else Color.Transparent),
                RoundedCornerShape(percent = 50),
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leadingEmoji?.let {
            Text(it, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else palette.textSecondary,
        )
    }
}
