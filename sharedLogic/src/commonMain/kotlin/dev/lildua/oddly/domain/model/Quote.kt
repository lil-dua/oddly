package dev.lildua.oddly.domain.model

/** An offline motivational quote (spec §S14). One is surfaced per day. */
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val isFavorite: Boolean = false,
)
