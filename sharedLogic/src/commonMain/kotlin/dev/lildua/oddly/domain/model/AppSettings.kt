package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalTime

enum class ThemeMode(val title: String) {
    SYSTEM("Hệ thống"),
    LIGHT("Sáng"),
    DARK("Tối"),
}

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
