package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.challenge
import dev.lildua.oddly.completion
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.Difficulty
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChallengeSelectorTest {

    private val health = challenge("health-1", Category.HEALTH)
    private val health2 = challenge("health-2", Category.HEALTH)
    private val finance = challenge("finance-1", Category.FINANCE)

    private fun select(
        library: List<Challenge>,
        interests: Set<Category> = emptySet(),
        completions: List<ChallengeCompletion> = emptyList(),
        level: Int = 1,
        restrictTo: Category? = null,
        exclude: String? = null,
        seed: Int = 7,
    ) = ChallengeSelector.select(
        library = library,
        interests = interests,
        completions = completions,
        level = level,
        today = TODAY,
        restrictTo = restrictTo,
        excludeChallengeId = exclude,
        random = Random(seed),
    )

    @Test
    fun `an empty library yields nothing`() {
        assertNull(select(emptyList()))
    }

    @Test
    fun `the only challenge is the one picked`() {
        assertEquals(health, select(listOf(health)))
    }

    @Test
    fun `the excluded challenge is never returned`() {
        assertEquals(health2, select(listOf(health, health2), exclude = health.id))
    }

    @Test
    fun `excluding the only challenge yields nothing`() {
        assertNull(select(listOf(health), exclude = health.id))
    }

    @Test
    fun `a challenge done inside the cooldown is skipped`() {
        val picked = select(
            library = listOf(health, health2),
            completions = listOf(completion(daysAgo(29), challengeId = health.id)),
        )

        assertEquals(health2, picked)
    }

    @Test
    fun `a challenge comes back once the cooldown has passed`() {
        // 31 days is outside the 30-day window, so both are eligible again and
        // the pick has to be one of them rather than forced.
        val picked = select(
            library = listOf(health),
            completions = listOf(completion(daysAgo(31), challengeId = health.id)),
        )

        assertEquals(health, picked)
    }

    /** Better to repeat a challenge than to show the user an empty screen. */
    @Test
    fun `cooldown is relaxed when every challenge is on it`() {
        val picked = select(
            library = listOf(health),
            completions = listOf(completion(TODAY, challengeId = health.id)),
        )

        assertEquals(health, picked)
    }

    @Test
    fun `chosen interests are preferred`() {
        repeat(20) { seed ->
            val picked = select(
                library = listOf(health, health2, finance),
                interests = setOf(Category.FINANCE),
                seed = seed,
            )
            assertEquals(finance, picked, "seed $seed leaked outside the chosen interest")
        }
    }

    @Test
    fun `the full library is used when the chosen interests are exhausted`() {
        val picked = select(
            library = listOf(health, finance),
            interests = setOf(Category.FINANCE),
            completions = listOf(completion(daysAgo(1), challengeId = finance.id)),
        )

        assertEquals(health, picked)
    }

    @Test
    fun `an explicit category overrides the chosen interests`() {
        val picked = select(
            library = listOf(health, health2, finance),
            interests = setOf(Category.HEALTH),
            restrictTo = Category.FINANCE,
        )

        assertEquals(finance, picked)
    }

    @Test
    fun `an explicit category with no challenges yields nothing`() {
        assertNull(select(listOf(health, health2), restrictTo = Category.CREATIVITY))
    }

    @Test
    fun `the same seed always picks the same challenge`() {
        val library = listOf(health, health2, finance)

        val first = select(library, seed = 99)
        val second = select(library, seed = 99)

        assertNotNull(first)
        assertEquals(first, second)
    }

    /**
     * Difficulty is a weighting, not a filter, so this asserts the bias rather
     * than a single outcome. With a fixed seed the counts are reproducible.
     */
    @Test
    fun `difficulty near the users level is favoured`() {
        val easy = challenge("easy", Category.HEALTH, Difficulty.EASY)
        val hard = challenge("hard", Category.HEALTH, Difficulty.HARD)
        val random = Random(42)

        var easyCount = 0
        repeat(1000) {
            val picked = ChallengeSelector.select(
                library = listOf(easy, hard),
                interests = emptySet(),
                completions = emptyList(),
                level = 1,
                today = TODAY,
                random = random,
            )
            if (picked == easy) easyCount++
        }

        // The easy weighting is 1.6x, so roughly 615 of 1000 — well clear of
        // the 500 an unweighted picker would produce.
        assertTrue(easyCount in 550..680, "easy was picked $easyCount times in 1000")
    }

    /**
     * Levels 9 and up prefer hard challenges; the same library must skew the
     * other way, or the difficulty ramp does nothing.
     */
    @Test
    fun `a high level favours hard challenges`() {
        val easy = challenge("easy", Category.HEALTH, Difficulty.EASY)
        val hard = challenge("hard", Category.HEALTH, Difficulty.HARD)
        val random = Random(42)

        var hardCount = 0
        repeat(1000) {
            val picked = ChallengeSelector.select(
                library = listOf(easy, hard),
                interests = emptySet(),
                completions = emptyList(),
                level = 12,
                today = TODAY,
                random = random,
            )
            if (picked == hard) hardCount++
        }

        assertTrue(hardCount in 550..680, "hard was picked $hardCount times in 1000")
    }

    /**
     * Variety weighting: a category untouched for a fortnight gets more pull.
     *
     * `health-1` is on cooldown from those completions, but `health-2` is not —
     * so the two candidates differ only in how busy their category has been,
     * which is exactly what this weighting is for.
     */
    @Test
    fun `a neglected category is favoured over a recently used one`() {
        val random = Random(2026)
        var financeCount = 0
        repeat(1000) {
            val picked = ChallengeSelector.select(
                library = listOf(health, health2, finance),
                interests = emptySet(),
                completions = listOf(
                    completion(daysAgo(2), challengeId = health.id),
                    completion(daysAgo(4), challengeId = health.id),
                    completion(daysAgo(6), challengeId = health.id),
                    completion(daysAgo(8), challengeId = health.id),
                ),
                level = 1,
                today = TODAY,
                random = random,
            )
            if (picked == finance) financeCount++
        }

        // Finance carries four unused-category steps of 0.25, so 2.0 against
        // health-2's 1.0 — about two thirds of the picks.
        assertTrue(financeCount in 600..730, "finance was picked $financeCount times in 1000")
    }
}
