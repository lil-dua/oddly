package dev.lildua.oddly.ui.theme

import androidx.compose.ui.graphics.Color
import dev.lildua.oddly.domain.model.Category

/** Bridges the platform-agnostic colour on [Category] into a Compose [Color]. */
val Category.color: Color get() = Color(colorHex)
