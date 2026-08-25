package dev.lildua.oddly.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.ui.components.ChallengeRow
import dev.lildua.oddly.ui.components.EmptyState
import dev.lildua.oddly.ui.components.OddlyChip
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * S20 — the full challenge library with a category filter. Search is P1; the
 * spec notes filters matter more than search at this catalogue size.
 */
@Composable
fun AllChallengesScreen(
    state: OddlyAppState,
    onBack: () -> Unit,
    onSelect: (Challenge) -> Unit,
) {
    val palette = OddlyTheme.palette
    var filter by remember { mutableStateOf<Category?>(null) }

    val challenges = remember(filter) {
        if (filter == null) ChallengeSeed.all else ChallengeSeed.byCategory(filter!!)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        OddlyTopBar(title = "Tất cả thử thách", onBack = onBack)

        // Category filter strip
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OddlyChip(
                text = "Tất cả",
                selected = filter == null,
                accent = OddlyColors.Purple,
                onClick = { filter = null },
            )
            Category.entries.forEach { category ->
                OddlyChip(
                    text = category.title,
                    leadingEmoji = category.emoji,
                    selected = filter == category,
                    accent = category.color,
                    onClick = { filter = category },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${challenges.size} thử thách",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textTertiary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(12.dp))

        if (challenges.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                EmptyState(
                    title = "Không tìm thấy thử thách nào",
                    subtitle = "Thử chọn một chủ đề khác.",
                    actionText = "Xem tất cả",
                    onAction = { filter = null },
                )
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(challenges, key = { it.id }) { challenge ->
                    ChallengeRow(
                        challenge = challenge,
                        trailingText = if (state.isCompleted(challenge.id)) "✓" else "+${challenge.humanityPercent}%",
                        onClick = { onSelect(challenge) },
                    )
                }
            }
        }
    }
}
