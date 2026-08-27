package dev.lildua.oddly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.usecase.DayActivity
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyGradients
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * The visual hero of Home (spec §8.1). A gradient-edged card carrying today's
 * challenge and the single dominant CTA.
 */
@Composable
fun TodayChallengeCard(
    challenge: Challenge,
    completed: Boolean,
    onClick: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OddlyTheme.palette
    val accent = challenge.category.color

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.16f),
                        palette.surfaceElevated,
                        palette.surfaceElevated,
                    ),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
    ) {
        StarField(Modifier.fillMaxWidth().height(200.dp), starCount = 22, seed = 3)

        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Thử thách hôm nay", color = palette.textSecondary)
                Spacer(Modifier.weight(1f))
                OddlyIcon(
                    OddlyIcon.ChevronRight,
                    size = 18.dp,
                    tint = palette.textTertiary,
                    modifier = Modifier.clickableNoRipple(onClick),
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                CompletionBadge(completed = completed, accent = accent)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = challenge.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OddlyChip(challenge.category.title, leadingEmoji = challenge.category.emoji, accent = accent)
                OddlyChip("${challenge.estimatedMinutes} phút")
            }

            Spacer(Modifier.height(20.dp))

            if (completed) {
                SecondaryButton(
                    text = "Đã hoàn thành hôm nay",
                    onClick = onClick,
                    leadingIcon = OddlyIcon.Check,
                )
            } else {
                GradientButton(text = "Tôi sẽ làm!", onClick = onStart)
            }
        }
    }
}

@Composable
private fun CompletionBadge(completed: Boolean, accent: Color) {
    val palette = OddlyTheme.palette
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (completed) OddlyColors.Success else Color.Transparent)
            .border(
                1.5.dp,
                if (completed) OddlyColors.Success else accent.copy(alpha = 0.5f),
                RoundedCornerShape(percent = 50),
            ),
        contentAlignment = Alignment.Center,
    ) {
        OddlyIcon(
            OddlyIcon.Check,
            size = 16.dp,
            tint = if (completed) Color(0xFF08240F) else palette.textTertiary,
            strokeWidth = 2.dp,
        )
    }
}

/**
 * Compact challenge row used by the challenge library, calendar day lists and
 * history. [trailing] carries the reward badge or a timestamp.
 */
@Composable
fun ChallengeRow(
    challenge: Challenge,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val palette = OddlyTheme.palette
    val accent = challenge.category.color

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceElevated)
            .then(if (onClick != null) Modifier.clickableNoRipple(onClick) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryBadge(challenge.category)

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${challenge.category.title} · ${challenge.estimatedMinutes} phút",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary,
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = trailingText ?: "+${challenge.humanityPercent}%",
            style = MaterialTheme.typography.labelMedium,
            color = accent,
        )
    }
}

/** Rounded square holding a category's emoji, tinted with its colour. */
@Composable
fun CategoryBadge(
    category: Category,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(category.color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(category.emoji, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Rolling activity strip (Home and Streak screens).
 *
 * The window ends today rather than on a fixed week boundary, so each cell is
 * labelled from its own date instead of a hardcoded Mon–Sun sequence.
 */
@Composable
fun WeekStrip(
    days: List<DayActivity>,
    modifier: Modifier = Modifier,
) {
    val palette = OddlyTheme.palette
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DateFormat.shortWeekday(day.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) palette.textPrimary else palette.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (day.completed) {
                                OddlyGradients.flame
                            } else {
                                Brush.linearGradient(
                                    listOf(palette.surfaceHighest, palette.surfaceHighest),
                                )
                            },
                        )
                        .then(
                            if (day.isToday) {
                                Modifier.border(
                                    1.5.dp,
                                    OddlyColors.Purple,
                                    RoundedCornerShape(percent = 50),
                                )
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (day.completed) {
                        Text("🔥", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/** Big-number tile used across Journey and Statistics. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = OddlyColors.Purple,
) {
    val palette = OddlyTheme.palette
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surfaceElevated)
            .padding(16.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accent,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textTertiary,
        )
    }
}

/**
 * Friendly zero-data state (spec §S19). Empty is framed as the start of a
 * journey, never as a failure.
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val palette = OddlyTheme.palette
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StarryBox(Modifier.fillMaxWidth().height(180.dp), starCount = 30, seed = 21) {
            Box(Modifier.align(Alignment.Center)) {
                Astronaut(size = 140.dp)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
        )

        subtitle?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            GradientButton(text = actionText, onClick = onAction)
        }
    }
}

/** Settings-style row: leading icon, title, optional value, trailing slot. */
@Composable
fun SettingsRow(
    icon: OddlyIcon,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    tint: Color = OddlyTheme.palette.textSecondary,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = OddlyTheme.palette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickableNoRipple(onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OddlyIcon(icon, size = 20.dp, tint = tint)
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tint == palette.textSecondary) palette.textPrimary else tint,
            modifier = Modifier.weight(1f),
        )
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textTertiary,
            )
            Spacer(Modifier.width(8.dp))
        }
        trailing?.invoke()
        if (showChevron && trailing == null) {
            Spacer(Modifier.width(4.dp))
            OddlyIcon(OddlyIcon.ChevronRight, size = 16.dp, tint = palette.textTertiary)
        }
    }
}
