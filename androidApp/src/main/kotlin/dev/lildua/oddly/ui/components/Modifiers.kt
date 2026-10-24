package dev.lildua.oddly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * Tap handling without the Material ripple.
 *
 * The design leans on gradients and glows, and a ripple washes out over them —
 * interactive surfaces animate their own press scale instead.
 *
 * [role] defaults to Button; selectable/checkable elements must use
 * [selectableNoRipple] / [toggleableNoRipple] instead so they never announce as a plain button.
 */
@Composable
fun Modifier.clickableNoRipple(
    onClickLabel: String? = null,
    role: Role? = Role.Button,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick,
)

/** Single-choice item (tab / radio). [role] null keeps only the selected state (e.g. calendar cells). */
@Composable
fun Modifier.selectableNoRipple(
    selected: Boolean,
    role: Role?,
    onClick: () -> Unit,
): Modifier = selectable(
    selected = selected,
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    role = role,
    onClick = onClick,
)

/** Two-state item (checkbox / switch row): the whole row is one toggle target. */
@Composable
fun Modifier.toggleableNoRipple(
    value: Boolean,
    role: Role,
    onValueChange: (Boolean) -> Unit,
): Modifier = toggleable(
    value = value,
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    role = role,
    onValueChange = onValueChange,
)

fun Modifier.a11yHeading(): Modifier = semantics { heading() }

/** Decorative content (emoji next to a name, icons beside text) that a screen reader should skip. */
fun Modifier.a11yHidden(): Modifier = semantics { hideFromAccessibility() }

/** Announces changes to this node's text without moving focus. */
fun Modifier.a11yLiveRegion(): Modifier = semantics { liveRegion = LiveRegionMode.Polite }
