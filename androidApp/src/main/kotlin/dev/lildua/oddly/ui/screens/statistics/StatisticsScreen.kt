package dev.lildua.oddly.ui.screens.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.usecase.DayBar
import dev.lildua.oddly.domain.usecase.StatsCalculator
import dev.lildua.oddly.domain.usecase.StatsRange
import dev.lildua.oddly.ui.components.EmptyState
import dev.lildua.oddly.ui.components.GradientProgressBar
import dev.lildua.oddly.ui.components.OddlyCard
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.StatTile
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyGradients
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * S12 — completion counts, trend and category mix across four time ranges.
 */
@Composable
fun StatisticsScreen(
    state: OddlyAppState,
    onStartFirstChallenge: () -> Unit,
) {
    val palette = OddlyTheme.palette
    var range by remember { mutableStateOf(StatsRange.WEEK) }
    val summary = StatsCalculator.summarize(state.completions, state.today, range)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Thống kê",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.textPrimary,
        )

        Spacer(Modifier.height(20.dp))

        if (state.completions.isEmpty()) {
            EmptyState(
                title = "Chưa có dữ liệu để thống kê",
                subtitle = "Hoàn thành thử thách đầu tiên\nđể bắt đầu theo dõi tiến trình.",
                actionText = "Khám phá thử thách",
                onAction = onStartFirstChallenge,
            )
            return@Column
        }

        RangeTabs(selected = range, onSelect = { range = it })

        Spacer(Modifier.height(20.dp))

        OddlyCard {
            SectionLabel("Tổng thử thách đã hoàn thành")
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${summary.totalCompleted}",
                    style = MaterialTheme.typography.displaySmall,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.width(12.dp))
                if (range != StatsRange.ALL) {
                    val delta = summary.deltaVsPreviousPeriod
                    Text(
                        text = if (delta >= 0) "+$delta so với kỳ trước" else "$delta so với kỳ trước",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (delta >= 0) OddlyColors.Success else palette.textTertiary,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            BarChart(bars = summary.bars)
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = summary.mostActiveCategory?.emoji ?: "–",
                label = summary.mostActiveCategory?.title ?: "Chưa có dữ liệu",
                accent = summary.mostActiveCategory?.color ?: palette.textTertiary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = "${state.completionRatePercent}%",
                label = "Tỷ lệ hoàn thành",
                accent = OddlyColors.Success,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        OddlyCard {
            SectionLabel("Phân bổ chủ đề")
            Spacer(Modifier.height(14.dp))

            if (summary.distribution.isEmpty()) {
                Text(
                    text = "Chưa có thử thách nào trong khoảng thời gian này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textTertiary,
                )
            } else {
                summary.distribution.forEach { slice ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(slice.category.emoji, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = slice.category.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${(slice.fraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = slice.category.color,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    GradientProgressBar(
                        progress = slice.fraction,
                        height = 6.dp,
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(slice.category.color, slice.category.color.copy(alpha = 0.5f)),
                        ),
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun RangeTabs(selected: StatsRange, onSelect: (StatsRange) -> Unit) {
    val palette = OddlyTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StatsRange.entries.forEach { entry ->
            val active = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) OddlyColors.Purple.copy(alpha = 0.22f) else Color.Transparent)
                    .clickableNoRipple { onSelect(entry) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) OddlyColors.Purple else palette.textTertiary,
                )
            }
        }
    }
}

/**
 * Simple vertical bar chart. Bars are scaled against the busiest bucket so the
 * shape stays readable whatever the absolute counts are.
 */
@Composable
private fun BarChart(bars: List<DayBar>) {
    val palette = OddlyTheme.palette
    val max = (bars.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            val fraction by animateFloatAsState(
                targetValue = bar.count.toFloat() / max,
                animationSpec = tween(500),
                label = "bar-${bar.label}",
            )

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = if (bar.count > 0) "${bar.count}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Keep a sliver visible for empty buckets so the
                            // axis still reads as a chart.
                            .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (bar.count > 0) OddlyGradients.progress
                                else androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(palette.surfaceHighest, palette.surfaceHighest),
                                ),
                            ),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (bar.isToday) palette.textPrimary else palette.textTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
