package dev.lildua.oddly.ui.theme

import androidx.compose.ui.graphics.Color
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Difficulty

/** Bridges the platform-agnostic colour on [Category] into a Compose [Color]. */
val Category.color: Color get() = Color(colorHex)

val Difficulty.color: Color
    get() = when (this) {
        Difficulty.EASY -> OddlyColors.Success
        Difficulty.MEDIUM -> OddlyColors.Warning
        Difficulty.HARD -> OddlyColors.Flame
    }
