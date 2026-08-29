package dev.lildua.oddly.domain.model

/**
 * A string in every language the app ships.
 *
 * Localisation lives in the shared layer rather than in `strings.xml` and
 * `Localizable.strings` because the language is an in-app setting (spec §S15),
 * not the system locale: the user can read the app in English on a Vietnamese
 * phone. Platform resource lookup follows the device, so it cannot express
 * that on its own — and duplicating the content library in two resource
 * formats would leave the two apps free to drift.
 */
data class LocalizedText(
    val vi: String,
    val en: String,
) {
    fun of(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> vi
        AppLanguage.ENGLISH -> en
    }
}
