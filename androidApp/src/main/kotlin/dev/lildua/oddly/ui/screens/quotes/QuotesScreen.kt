package dev.lildua.oddly.ui.screens.quotes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.data.seed.QuoteSeed
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.localized
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * S14 — one quote a day, from the bundled offline database. Swiping through the
 * archive and favouriting are P1 niceties already wired up here.
 */
@Composable
fun QuotesScreen(
    state: OddlyAppState,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val quotes = QuoteSeed.all
    val todayIndex = remember { ((state.today.toEpochDays() % quotes.size) + quotes.size) % quotes.size }
    var index by remember { mutableIntStateOf(todayIndex) }
    val favorites = remember { mutableStateOf(setOf<String>()) }

    val quote = quotes[index].localized()
    val isFavorite = quote.id in favorites.value

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 55, seed = 47)

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            OddlyTopBar(title = "", onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = strings.quotesTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textPrimary,
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFavorite) OddlyColors.Pink.copy(alpha = 0.18f)
                            else palette.surfaceElevated,
                        )
                        .clickableNoRipple {
                            favorites.value = if (isFavorite) {
                                favorites.value - quote.id
                            } else {
                                favorites.value + quote.id
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    OddlyIcon(
                        OddlyIcon.Heart,
                        size = 20.dp,
                        tint = if (isFavorite) OddlyColors.Pink else palette.textTertiary,
                    )
                }

                Spacer(Modifier.weight(1f))

                // Quote card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    OddlyColors.Purple.copy(alpha = 0.16f),
                                    palette.surfaceElevated,
                                ),
                            ),
                        )
                        .padding(28.dp),
                ) {
                    Text(
                        text = "“${quote.text}”",
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "– ${quote.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavArrow(OddlyIcon.ChevronLeft) {
                        index = (index - 1 + quotes.size) % quotes.size
                    }
                    Spacer(Modifier.size(20.dp))
                    Text(
                        text = "${index + 1} / ${quotes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textTertiary,
                    )
                    Spacer(Modifier.size(20.dp))
                    NavArrow(OddlyIcon.ChevronRight) {
                        index = (index + 1) % quotes.size
                    }
                }

                Spacer(Modifier.weight(1f))

                GradientButton(text = strings.share, onClick = onShare, leadingIcon = OddlyIcon.Share)

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = strings.reminderLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = strings.reminderToggleBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textTertiary,
                        )
                    }
                    Switch(
                        checked = state.settings.reminderEnabled,
                        onCheckedChange = {
                            state.settings = state.settings.copy(reminderEnabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = OddlyColors.Success,
                        ),
                    )
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun NavArrow(icon: OddlyIcon, onClick: () -> Unit) {
    val palette = OddlyTheme.palette
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(palette.surfaceElevated)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        OddlyIcon(icon, size = 16.dp, tint = palette.textSecondary)
    }
}
