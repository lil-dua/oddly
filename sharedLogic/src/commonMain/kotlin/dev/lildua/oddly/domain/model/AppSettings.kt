package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalTime

enum class ThemeMode(val title: LocalizedText) {
    SYSTEM(LocalizedText(vi = "Hệ thống", en = "System")),
    LIGHT(LocalizedText(vi = "Sáng", en = "Light")),
    DARK(LocalizedText(vi = "Tối", en = "Dark")),
}

/**
 * [title] is deliberately not localised: a language picker lists each language
 * in its own language, so the option you are looking for is legible whichever
 * one is currently selected.
 */
enum class AppLanguage(val title: String, val tag: String) {
    VIETNAMESE("Tiếng Việt", "vi"),
    ENGLISH("English", "en"),
}

/** User configuration surfaced on the Settings screen (spec §S15). */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val reminderEnabled: Boolean = true,
    val reminderTime: LocalTime = LocalTime(9, 0),
    val language: AppLanguage = AppLanguage.VIETNAMESE,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
)
