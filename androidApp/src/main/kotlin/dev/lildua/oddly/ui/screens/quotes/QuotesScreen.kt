package dev.lildua.oddly.ui.screens.quotes

import androidx.compose.foundation.layout.PaddingValues
import dev.lildua.oddly.ui.components.FillHeightScrollColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.data.seed.QuoteSeed
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.OddlyChip
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.OddlyTopBar
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.lildua.oddly.ui.components.IconCircleButton
import dev.lildua.oddly.ui.components.a11yHeading
import dev.lildua.oddly.ui.components.a11yLiveRegion
import dev.lildua.oddly.ui.components.toggleableNoRipple
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.localized
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * S14 — one quote a day, from the bundled offline database. Swiping through the
 * archive is a P1 nicety. Hearts live in [OddlyAppState] so they survive leaving
 * the screen; the Favorites filter is a snapshot taken when the chip is tapped,
 * so un-hearting a card does not make it vanish under the reader.
 */
@Composable
fun QuotesScreen(
    state: OddlyAppState,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val allQuotes = QuoteSeed.all
    val todayIndex = remember { ((state.today.toEpochDays() % allQuotes.size) + allQuotes.size) % allQuotes.size }
    // Saveable because OddlyApp keeps this screen's saved state across Share and
    // Back. The snapshot is kept as ids (Bundle-friendly) and mapped back to quotes.
    var showFavorites by rememberSaveable { mutableStateOf(false) }
    var favoriteSnapshotIds by rememberSaveable { mutableStateOf(ArrayList<String>()) }
    var index by rememberSaveable { mutableIntStateOf(todayIndex) }

    val favoriteSnapshot = remember(favoriteSnapshotIds) {
        val byId = allQuotes.associateBy { it.id }
        favoriteSnapshotIds.mapNotNull { byId[it] }
    }
    val quotes = if (showFavorites) favoriteSnapshot else allQuotes
    val quote = quotes.getOrNull(index)?.localized()
    val isFavorite = quote != null && state.isFavorite(quote.id)

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

            FillHeightScrollColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = strings.quotesTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textPrimary,
                    modifier = Modifier.a11yHeading(),
                )

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OddlyChip(
                        text = strings.quotesFilterAll,
                        accent = OddlyColors.Purple,
                        selected = !showFavorites,
                        onClick = {
                            showFavorites = false
                            index = todayIndex
                        },
                    )
                    OddlyChip(
                        text = strings.quotesFilterFavorites,
                        accent = OddlyColors.Pink,
                        selected = showFavorites,
                        onClick = {
                            favoriteSnapshotIds = ArrayList(state.favoriteQuotes.map { it.id })
                            showFavorites = true
                            index = 0
                        },
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (quote == null) {
                    Spacer(Modifier.weight(1f))
                    QuoteCard {
                        Text(
                            text = strings.quotesFavoritesEmptyTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = strings.quotesFavoritesEmptyBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                } else {
                    // Label names the action, so it flips after each tap ("Add" -> "Remove").
                    val favoriteLabel =
                        if (isFavorite) strings.a11yRemoveFavorite else strings.a11yAddFavorite
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFavorite) OddlyColors.Pink.copy(alpha = 0.18f)
                                else palette.surfaceElevated,
                            )
                            .clickableNoRipple { state.toggleFavorite(quote.id) }
                            .semantics { contentDescription = favoriteLabel },
                        contentAlignment = Alignment.Center,
                    ) {
                        OddlyIcon(
                            OddlyIcon.Heart,
                            size = 20.dp,
                            tint = if (isFavorite) OddlyColors.Pink else palette.textTertiary,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    QuoteCard {
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

                    val positionLabel = strings.a11yQuotePosition(index + 1, quotes.size)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconCircleButton(
                            icon = OddlyIcon.ChevronLeft,
                            contentDescription = strings.a11yPreviousQuote,
                            onClick = { index = (index - 1 + quotes.size) % quotes.size },
                        )
                        Spacer(Modifier.size(20.dp))
                        Text(
                            text = "${index + 1} / ${quotes.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textTertiary,
                            modifier = Modifier.semantics {
                                contentDescription = positionLabel
                            },
                        )
                        Spacer(Modifier.size(20.dp))
                        IconCircleButton(
                            icon = OddlyIcon.ChevronRight,
                            contentDescription = strings.a11yNextQuote,
                            onClick = { index = (index + 1) % quotes.size },
                        )
                    }

                    Spacer(Modifier.weight(1f))
                }

                GradientButton(text = strings.share, onClick = onShare, leadingIcon = OddlyIcon.Share)

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surfaceElevated)
                        .toggleableNoRipple(state.settings.reminderEnabled, Role.Switch) {
                            state.settings = state.settings.copy(reminderEnabled = it)
                        }
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
                        // The row is the toggle target; the Switch only draws state.
                        onCheckedChange = null,
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
private fun QuoteCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = OddlyTheme.palette
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
            // One merged, polite live region: changing quote reads the new text once.
            .semantics(mergeDescendants = true) {}
            .a11yLiveRegion()
            .padding(28.dp),
        content = content,
    )
}
