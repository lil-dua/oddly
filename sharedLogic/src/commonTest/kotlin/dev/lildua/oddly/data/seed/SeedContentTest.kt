package dev.lildua.oddly.data.seed

import dev.lildua.oddly.domain.model.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integrity checks on the shipped content.
 *
 * The library is hand-written data, and the failure modes are the boring ones:
 * a duplicated id silently shadows a challenge, a forgotten `en` string shows
 * Vietnamese to an English user, an empty category makes the "another one from
 * this category" action return nothing. None of that shows up until it is in
 * front of a user, so it is asserted here instead.
 */
class SeedContentTest {

    @Test
    fun `challenge ids are unique`() {
        val duplicates = ChallengeSeed.all
            .groupingBy { it.id }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        assertTrue(duplicates.isEmpty(), "duplicate challenge ids: $duplicates")
    }

    @Test
    fun `every challenge is reachable by id`() {
        for (challenge in ChallengeSeed.all) {
            assertEquals(challenge, ChallengeSeed.byId(challenge.id))
        }
    }

    @Test
    fun `an unknown id resolves to nothing rather than the wrong challenge`() {
        assertEquals(null, ChallengeSeed.byId("no-such-challenge"))
    }

    @Test
    fun `every challenge is written in both languages`() {
        for (challenge in ChallengeSeed.all) {
            val fields = listOf(
                "title" to challenge.title,
                "shortDescription" to challenge.shortDescription,
                "whyItMatters" to challenge.whyItMatters,
            )
            for ((name, text) in fields) {
                assertTrue(text.vi.isNotBlank(), "${challenge.id}: $name is missing Vietnamese")
                assertTrue(text.en.isNotBlank(), "${challenge.id}: $name is missing English")
            }
        }
    }

    @Test
    fun `every challenge explains how to do it in both languages`() {
        for (challenge in ChallengeSeed.all) {
            assertTrue(
                challenge.howToDoIt.isNotEmpty(),
                "${challenge.id}: no steps — the detail screen would render an empty section",
            )
            for ((index, step) in challenge.howToDoIt.withIndex()) {
                assertTrue(step.vi.isNotBlank(), "${challenge.id}: step $index is missing Vietnamese")
                assertTrue(step.en.isNotBlank(), "${challenge.id}: step $index is missing English")
            }
        }
    }

    @Test
    fun `every challenge has a believable time estimate`() {
        for (challenge in ChallengeSeed.all) {
            assertTrue(
                challenge.estimatedMinutes in 1..120,
                "${challenge.id}: ${challenge.estimatedMinutes} minutes",
            )
        }
    }

    /**
     * Rerolling inside a category, and choosing that category as an interest,
     * both need somewhere to land — the cooldown alone can exhaust a thin
     * category within a month.
     */
    @Test
    fun `every category has enough challenges to reroll within`() {
        for (category in Category.entries) {
            val count = ChallengeSeed.all.count { it.category == category }
            assertTrue(count >= 5, "${category.id} has only $count challenges")
        }
    }

    @Test
    fun `quote ids are unique`() {
        val duplicates = QuoteSeed.all
            .groupingBy { it.id }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        assertTrue(duplicates.isEmpty(), "duplicate quote ids: $duplicates")
    }

    @Test
    fun `every quote is written in both languages and attributed`() {
        assertTrue(QuoteSeed.all.isNotEmpty())
        for (quote in QuoteSeed.all) {
            assertTrue(quote.text.vi.isNotBlank(), "${quote.id}: missing Vietnamese")
            assertTrue(quote.text.en.isNotBlank(), "${quote.id}: missing English")
            assertTrue(quote.author.vi.isNotBlank(), "${quote.id}: missing Vietnamese attribution")
            assertTrue(quote.author.en.isNotBlank(), "${quote.id}: missing English attribution")
        }
    }

    @Test
    fun `categories carry an icon and a colour`() {
        for (category in Category.entries) {
            assertTrue(category.emoji.isNotBlank(), "${category.id} has no emoji")
            assertTrue(category.title.vi.isNotBlank() && category.title.en.isNotBlank())
            // Opaque ARGB: a colour with a zero alpha channel renders as nothing.
            assertTrue(category.colorHex ushr 24 == 0xFFL, "${category.id} colour is not opaque")
            assertNotNull(Category.fromId(category.id))
        }
    }
}
