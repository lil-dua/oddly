package dev.lildua.oddly.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.SecondaryButton
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyGradients
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * S09 — picking a category narrows the randomisation pool. Each tile shows how
 * far the user has got in that category so the grid doubles as progress.
 */
@Composable
fun ChooseCategoryScreen(
    state: OddlyAppState,
    onBack: () -> Unit,
    onPick: (Category) -> Unit,
) {
    val palette = OddlyTheme.palette

    // Count distinct challenges cleared, not completions: the same challenge can
    // be done on several days, and "12 / 10 đã làm" would be nonsense.
    val completedPerCategory = state.completions
        .map { it.challengeId }
        .distinct()
        .mapNotNull { ChallengeSeed.byId(it)?.category }
        .groupingBy { it }
        .eachCount()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 45, seed = 37)

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OddlyTopBar(title = "", onBack = onBack)

            Column(Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Chọn chủ đề bạn muốn\nthử thách",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(28.dp))

                // Two-column grid, laid out manually so the whole page scrolls
                // as one surface instead of nesting a scrollable grid.
                Category.entries.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        rowCategories.forEach { category ->
                            CategoryTile(
                                category = category,
                                completedCount = completedPerCategory[category] ?: 0,
                                totalCount = ChallengeSeed.byCategory(category).size,
                                onClick = { onPick(category) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // Keep the last row aligned if the count is odd.
                        if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(14.dp))
                }

                Spacer(Modifier.height(16.dp))

                SecondaryButton(text = "Quay lại", onClick = onBack)

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: Category,
    completedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OddlyTheme.palette
    val accent = category.color

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(OddlyGradients.categoryWash(accent))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickableNoRipple(onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(category.emoji, style = MaterialTheme.typography.displaySmall)

        Column {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$completedCount / $totalCount đã làm",
                style = MaterialTheme.typography.bodySmall,
                color = accent,
            )
        }
    }
}
