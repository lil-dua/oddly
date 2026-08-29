package dev.lildua.oddly.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.TextAction
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.color

/**
 * S03 — interest selection. Choosing nothing is allowed and simply means "use
 * every category", so this screen never blocks onboarding.
 */
@Composable
fun ChooseInterestScreen(
    selected: Set<Category>,
    onToggle: (Category) -> Unit,
    onContinue: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 50, seed = 13)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(28.dp))

            Text(
                text = strings.interestsTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = strings.interestsSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary,
            )

            Spacer(Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Category.entries.forEach { category ->
                    InterestRow(
                        category = category,
                        selected = category in selected,
                        onClick = { onToggle(category) },
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = strings.interestsContinue,
                onClick = onContinue,
            )

            Spacer(Modifier.height(8.dp))

            TextAction(
                text = if (selected.isEmpty()) {
                    strings.interestsUseAll
                } else {
                    strings.topicsSelected(selected.size)
                },
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun InterestRow(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val accent = category.color
    val border by animateColorAsState(
        if (selected) accent.copy(alpha = 0.8f) else Color.Transparent,
        label = "interest-border",
    )
    val fill by animateColorAsState(
        if (selected) accent.copy(alpha = 0.12f) else palette.surfaceElevated,
        label = "interest-fill",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(category.emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.size(12.dp))
        Text(
            text = category.title.of(strings.language),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                OddlyIcon(
                    OddlyIcon.Check,
                    size = 14.dp,
                    tint = Color(0xFF0B0B12),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
