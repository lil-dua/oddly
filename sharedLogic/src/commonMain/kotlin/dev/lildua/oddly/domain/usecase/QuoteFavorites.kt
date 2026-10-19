package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.data.seed.QuoteSeed
import dev.lildua.oddly.domain.model.Quote

/** Rules for the hearted quotes, shared so both platforms behave the same. */
object QuoteFavorites {

    /**
     * Flips [quoteId] in the saved favorites. An id that is not in [library] is
     * ignored, so a stray call can never put junk into the saved document.
     */
    fun toggle(
        state: PersistedState,
        quoteId: String,
        library: List<Quote> = QuoteSeed.all,
    ): PersistedState {
        if (library.none { it.id == quoteId }) return state
        val ids = state.favoriteQuoteIds
        return state.copy(favoriteQuoteIds = if (quoteId in ids) ids - quoteId else ids + quoteId)
    }

    /**
     * The hearted quotes in [library] order. A Swift `Set` has no order, so the
     * library order is what keeps both platforms listing them identically. Ids no
     * longer in the library are skipped here but stay saved.
     */
    fun favorites(ids: Set<String>, library: List<Quote> = QuoteSeed.all): List<Quote> =
        library.filter { it.id in ids }
}
