package dev.lildua.oddly.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.lildua.oddly.core.text.Strings
import dev.lildua.oddly.core.text.VietnameseStrings
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.LocalizedChallenge
import dev.lildua.oddly.domain.model.LocalizedQuote
import dev.lildua.oddly.domain.model.LocalizedText
import dev.lildua.oddly.domain.model.Quote

/**
 * The UI string table for the language the user picked in Settings.
 *
 * Static rather than dynamic because switching language re-composes the whole
 * tree anyway — there is no partial-update case worth optimising for.
 */
val LocalStrings = staticCompositionLocalOf<Strings> { VietnameseStrings }

/** Shorthand for the current language's side of a piece of content text. */
@Composable
@ReadOnlyComposable
fun LocalizedText.current(): String = of(LocalStrings.current.language)

/** Resolves a challenge's content into the language currently on screen. */
@Composable
@ReadOnlyComposable
fun Challenge.localized(): LocalizedChallenge = localized(LocalStrings.current.language)

/** Resolves a quote's content into the language currently on screen. */
@Composable
@ReadOnlyComposable
fun Quote.localized(): LocalizedQuote = localized(LocalStrings.current.language)
