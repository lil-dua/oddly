package dev.lildua.oddly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Tap handling without the Material ripple.
 *
 * The design leans on gradients and glows, and a ripple washes out over them —
 * interactive surfaces animate their own press scale instead.
 */
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick,
)
