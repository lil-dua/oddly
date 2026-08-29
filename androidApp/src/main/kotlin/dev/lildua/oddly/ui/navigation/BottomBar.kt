package dev.lildua.oddly.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme

private val TabDestination.icon: OddlyIcon
    get() = when (this) {
        TabDestination.HOME -> OddlyIcon.Sparkle
        TabDestination.JOURNEY -> OddlyIcon.Journey
        TabDestination.STATISTICS -> OddlyIcon.Stats
        TabDestination.SETTINGS -> OddlyIcon.Settings
    }

/**
 * Four-destination bottom navigation (spec §5). Everything deeper is a child
 * route, so this bar never grows.
 */
@Composable
fun OddlyBottomBar(
    selected: TabDestination?,
    onSelect: (TabDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabDestination.entries.forEach { tab ->
            val active = tab == selected
            val tint by animateColorAsState(
                if (active) OddlyColors.Purple else palette.textTertiary,
                label = "tab-tint",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickableNoRipple { onSelect(tab) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (active) OddlyColors.Purple.copy(alpha = 0.16f) else Color.Transparent,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    OddlyIcon(tab.icon, size = 20.dp, tint = tint)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tab.label(strings),
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                )
            }
        }
    }
}

/** A hairline divider above the bar so content scrolling under it stays legible. */
@Composable
fun BottomBarDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    )
}
