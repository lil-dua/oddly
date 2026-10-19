package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.completionsOn
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.seed.QuoteSeed
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class QuoteFavoritesTest {

    private val fresh = OddlyRepository.freshState(TODAY)
    private val first = QuoteSeed.all[0].id
    private val second = QuoteSeed.all[1].id
    private val third = QuoteSeed.all[2].id

    @Test
    fun `toggling a quote that is not saved saves it`() {
        val result = QuoteFavorites.toggle(fresh, first)

        assertEquals(setOf(first), result.favoriteQuoteIds)
    }

    @Test
    fun `toggling a saved quote removes it`() {
        val saved = fresh.copy(favoriteQuoteIds = setOf(first, second))

        val result = QuoteFavorites.toggle(saved, first)

        assertEquals(setOf(second), result.favoriteQuoteIds)
    }

    @Test
    fun `toggling twice returns to the start`() {
        val result = QuoteFavorites.toggle(QuoteFavorites.toggle(fresh, first), first)

        assertTrue(result.favoriteQuoteIds.isEmpty())
    }

    @Test
    fun `toggling an unknown quote changes nothing`() {
        val saved = fresh.copy(favoriteQuoteIds = setOf(first))

        val result = QuoteFavorites.toggle(saved, "q_not_in_seed")

        assertSame(saved, result)
    }

    @Test
    fun `toggling leaves the rest of the state alone`() {
        val state = fresh.copy(
            completions = completionsOn(0, 1),
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = "health_stairs", swapsUsed = 2),
            settings = fresh.settings.copy(themeMode = ThemeMode.LIGHT),
            skippedCount = 3,
            hasCompletedOnboarding = true,
        )

        val result = QuoteFavorites.toggle(state, first)

        assertEquals(state.copy(favoriteQuoteIds = setOf(first)), result)
    }

    @Test
    fun `toggling an id from a custom library works and ignores the seed`() {
        val library = QuoteSeed.all.take(2)

        assertSame(fresh, QuoteFavorites.toggle(fresh, third, library))
        assertEquals(setOf(second), QuoteFavorites.toggle(fresh, second, library).favoriteQuoteIds)
    }

    @Test
    fun `favorites are listed in library order`() {
        val result = QuoteFavorites.favorites(setOf(third, first, second))

        assertEquals(listOf(first, second, third), result.map { it.id })
    }

    @Test
    fun `favorites skip ids no longer in the library`() {
        val result = QuoteFavorites.favorites(setOf(second, "q_gone_from_seed"))

        assertEquals(listOf(second), result.map { it.id })
    }

    @Test
    fun `no favorites gives an empty list`() {
        assertTrue(QuoteFavorites.favorites(emptySet()).isEmpty())
    }
}
