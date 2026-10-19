package dev.lildua.oddly.domain.model

/** An offline motivational quote (spec §S14). One is surfaced per day. */
data class Quote(
    val id: String,
    val text: LocalizedText,
    val author: LocalizedText,
) {
    fun localized(language: AppLanguage): LocalizedQuote = LocalizedQuote(
        id = id,
        text = text.of(language),
        author = author.of(language),
    )
}

/** A [Quote] with both strings already resolved to one language. */
data class LocalizedQuote(
    val id: String,
    val text: String,
    val author: String,
)
