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

    // --- Published ids: completions and favourites store ids, so these must never go missing. ---

    @Test
    fun `every published challenge id is still in the seed`() {
        val missing = PUBLISHED_CHALLENGE_CATEGORIES.keys.filter { ChallengeSeed.byId(it) == null }

        assertTrue(missing.isEmpty(), "published challenges missing from the seed: $missing")
    }

    @Test
    fun `published challenges keep their category`() {
        // Stats group completions by category, so moving a challenge rewrites history.
        for ((id, category) in PUBLISHED_CHALLENGE_CATEGORIES) {
            assertEquals(category, ChallengeSeed.byId(id)?.category, "$id changed category")
        }
    }

    @Test
    fun `every published quote id is still in the seed`() {
        val ids = QuoteSeed.all.map { it.id }.toSet()
        val missing = PUBLISHED_QUOTE_IDS.filter { it !in ids }

        assertTrue(missing.isEmpty(), "published quotes missing from the seed: $missing")
    }

    @Test
    fun `published quotes keep their position`() {
        // The quotes screen walks `all` in order and favourites sort by it.
        assertEquals(PUBLISHED_QUOTE_IDS, QuoteSeed.all.take(PUBLISHED_QUOTE_IDS.size).map { it.id })
    }

    @Test
    fun `challenge ids follow the category prefix`() {
        val prefixes = mapOf(
            Category.HEALTH to "health_",
            Category.RELATIONSHIPS to "rel_",
            Category.SELF_GROWTH to "growth_",
            Category.CREATIVITY to "create_",
            Category.FINANCE to "fin_",
            Category.LIFE_EXPERIENCE to "life_",
        )
        val shape = Regex("^[a-z0-9_]+$")
        for (challenge in ChallengeSeed.all) {
            assertTrue(shape.matches(challenge.id), "${challenge.id}: id must be lowercase snake_case")
            val prefix = prefixes.getValue(challenge.category)
            assertTrue(challenge.id.startsWith(prefix), "${challenge.id}: expected prefix $prefix")
        }
    }

    @Test
    fun `challenge titles are not duplicated`() {
        for (language in listOf("vi", "en")) {
            val duplicates = ChallengeSeed.all
                .groupBy { (if (language == "vi") it.title.vi else it.title.en).trim().lowercase() }
                .filterValues { it.size > 1 }
                .mapValues { (_, list) -> list.map { it.id } }

            assertTrue(duplicates.isEmpty(), "duplicate $language titles: $duplicates")
        }
    }

    // --- Quotes ---

    @Test
    fun `there are enough quotes to avoid repeating within two months`() {
        assertTrue(QuoteSeed.all.size >= 60, "only ${QuoteSeed.all.size} quotes")
    }

    @Test
    fun `quote ids follow the q prefix`() {
        val shape = Regex("^q_[a-z0-9_]+$")
        for (quote in QuoteSeed.all) {
            assertTrue(shape.matches(quote.id), "${quote.id}: id must be q_ + lowercase snake_case")
        }
    }

    @Test
    fun `quote texts are not duplicated`() {
        for (language in listOf("vi", "en")) {
            val duplicates = QuoteSeed.all
                .groupBy { (if (language == "vi") it.text.vi else it.text.en).trim().lowercase() }
                .filterValues { it.size > 1 }
                .mapValues { (_, list) -> list.map { it.id } }

            assertTrue(duplicates.isEmpty(), "duplicate $language quotes: $duplicates")
        }
    }

    @Test
    fun `quotes fit on the daily card`() {
        // The Home card has no maxLines, so a long quote makes the card grow.
        for (quote in QuoteSeed.all) {
            for ((name, text, limit) in listOf(
                Triple("text.vi", quote.text.vi, 160),
                Triple("text.en", quote.text.en, 160),
                Triple("author.vi", quote.author.vi, 40),
                Triple("author.en", quote.author.en, 40),
            )) {
                assertTrue(text.length <= limit, "${quote.id}: $name is ${text.length} chars (max $limit)")
            }
        }
    }

    @Test
    fun `quote text is not wrapped in quotation marks`() {
        // The UI adds the curly quotes itself; seed text that carries them shows them twice.
        val marks = setOf('"', '\u201C', '\u201D')
        for (quote in QuoteSeed.all) {
            for (text in listOf(quote.text.vi, quote.text.en)) {
                assertTrue(
                    text.first() !in marks && text.last() !in marks,
                    "${quote.id}: text is wrapped in quotation marks",
                )
            }
        }
    }

    @Test
    fun `every quote author is on the approved list`() {
        val unapproved = QuoteSeed.all.filter { it.author.en !in APPROVED_QUOTE_AUTHORS + LEGACY_QUOTE_AUTHORS }
            .map { "${it.id} (${it.author.en})" }

        assertTrue(unapproved.isEmpty(), "authors not on the approved list: $unapproved")
    }

    @Test
    fun `legacy anonymous labels are used only by the two shipped quotes`() {
        val legacy = QuoteSeed.all.filter { it.author.en in LEGACY_QUOTE_AUTHORS }.map { it.id }.toSet()

        assertEquals(LEGACY_QUOTE_IDS, legacy, "new quotes must not use Proverb or Unknown")
    }

    @Test
    fun `the quote of the day visits every quote once per cycle`() {
        val size = QuoteSeed.all.size

        assertEquals(size, (0 until size).map { QuoteSeed.forDayIndex(it).id }.toSet().size)
        // A date before the epoch yields a negative day index and must still resolve.
        assertNotNull(QuoteSeed.forDayIndex(-1))
    }
}

/**
 * Authors allowed in `QuoteSeed`. Adding a name here is a deliberate act: it means the author died
 * before 1956 and the line was traced to a specific work (PLAN T-26), or the label is a proverb,
 * a folk verse, or the app's own voice. "Proverb" and "Unknown" are legacy labels of two shipped
 * quotes (backlog T-27), kept apart in [LEGACY_QUOTE_AUTHORS] and pinned to those ids by a test.
 */
private val APPROVED_QUOTE_AUTHORS = setOf(
    "1% HUMAN",
    "Vietnamese proverb",
    "Vietnamese folk verse",
    "Aristotle",
    "Lao Tzu",
    "Confucius",
    "Xunzi",
    "Seneca",
    "Marcus Aurelius",
    "Epictetus",
    "Ralph Waldo Emerson",
    "Henry David Thoreau",
    "Benjamin Franklin",
    "Mark Twain",
    "Leo Tolstoy",
    "Johann Wolfgang von Goethe",
    "Michel de Montaigne",
    "Louis Pasteur",
)

/** Old anonymous labels. Only the quotes in [LEGACY_QUOTE_IDS] may carry them (PLAN decision 5). */
private val LEGACY_QUOTE_AUTHORS = setOf("Proverb", "Unknown")
private val LEGACY_QUOTE_IDS = setOf("q_tree_time", "q_progress_perfection")
