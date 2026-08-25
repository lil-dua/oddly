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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.usecase.StatsCalculator
import dev.lildua.oddly.ui.components.Astronaut
import dev.lildua.oddly.ui.components.EmptyState
import dev.lildua.oddly.ui.components.GradientProgressBar
import dev.lildua.oddly.ui.components.OddlyCard
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.StatTile
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * S11 — the long-term view: level, totals, streak and where the user spends
 * their effort. Entry point to Calendar, Streak and the full library.
 */
@Composable
fun JourneyScreen(
    state: OddlyAppState,
    onOpenCalendar: () -> Unit,
    onOpenStreak: () -> Unit,
    onOpenAllChallenges: () -> Unit,
    onStartFirstChallenge: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val profile = state.profile
    val distribution = StatsCalculator.distribution(state.completions)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Hành trình của bạn",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.textPrimary,
        )

        Spacer(Modifier.height(20.dp))

        if (state.completions.isEmpty()) {
            EmptyState(
                title = "Bạn chưa có thử thách nào",
                subtitle = "Hãy bắt đầu hành trình\n1% tốt hơn mỗi ngày.",
                actionText = "Khám phá thử thách",
                onAction = onStartFirstChallenge,
            )
            return@Column
        }

        // Level card
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(palette.surfaceElevated),
        ) {
            StarField(Modifier.fillMaxWidth().height(150.dp), starCount = 20, seed = 41)
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    SectionLabel("Cấp độ hiện tại")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Level ${profile.level}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${profile.xpInLevel} / ${profile.xpForNextLevel} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    GradientProgressBar(progress = profile.levelProgress)
                }
                Spacer(Modifier.width(12.dp))
                Astronaut(size = 96.dp)
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionLabel("Thống kê nhanh")

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = "${state.totalCompleted}",
                label = "Thử thách\nđã hoàn thành",
                accent = OddlyColors.Purple,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = "${state.streak.current}",
                label = "Ngày liên tiếp",
                accent = OddlyColors.Flame,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                value = "${state.exploredCategoryCount}",
                label = "Chủ đề\nđã khám phá",
                accent = OddlyColors.Blue,
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

        // Category distribution
        OddlyCard {
            SectionLabel("Phân bổ chủ đề")
            Spacer(Modifier.height(14.dp))
            distribution.forEach { slice ->
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

        Spacer(Modifier.height(16.dp))

        NavigationRow("Lịch hoàn thành", OddlyIcon.Calendar, onOpenCalendar)
        Spacer(Modifier.height(10.dp))
        NavigationRow("Chuỗi ngày liên tiếp", OddlyIcon.Flame, onOpenStreak)
        Spacer(Modifier.height(10.dp))
        NavigationRow("Tất cả thử thách", OddlyIcon.Target, onOpenAllChallenges)

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun NavigationRow(title: String, icon: OddlyIcon, onClick: () -> Unit) {
    val palette = OddlyTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceElevated)
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OddlyIcon(icon, size = 20.dp, tint = OddlyColors.Purple)
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
        )
        OddlyIcon(OddlyIcon.ChevronRight, size = 16.dp, tint = palette.textTertiary)
    }
}
