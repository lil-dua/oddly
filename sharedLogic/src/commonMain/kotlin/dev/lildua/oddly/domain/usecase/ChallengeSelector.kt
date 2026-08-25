package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.Difficulty
import kotlinx.datetime.LocalDate
import kotlin.random.Random

/**
 * Weighted-random challenge picker (spec §6.1).
 *
 * Plain random produces streaks of the same category and repeats challenges the
 * user just did, so each candidate gets a score instead:
 *  - challenges completed inside [COOLDOWN_DAYS] are excluded outright,
 *  - categories the user has neglected lately are boosted, for variety,
 *  - difficulty near the user's level is boosted, so the ramp stays comfortable.
 */
object ChallengeSelector {

    private const val COOLDOWN_DAYS = 30
    private const val RECENT_WINDOW_DAYS = 14

    fun select(
        library: List<Challenge>,
        interests: Set<Category>,
        completions: List<ChallengeCompletion>,
        level: Int,
        today: LocalDate,
        restrictTo: Category? = null,
        excludeChallengeId: String? = null,
        random: Random = Random.Default,
    ): Challenge? {
        val cooldownCutoff = today.toEpochDays() - COOLDOWN_DAYS
        val onCooldown = completions
            .filter { it.date.toEpochDays() >= cooldownCutoff }
            .map { it.challengeId }
            .toSet()

        val recentCutoff = today.toEpochDays() - RECENT_WINDOW_DAYS
        val recentCategoryCounts = completions
            .filter { it.date.toEpochDays() >= recentCutoff }
            .mapNotNull { completion -> library.firstOrNull { it.id == completion.challengeId }?.category }
            .groupingBy { it }
            .eachCount()

        val preferredDifficulty = difficultyForLevel(level)

        var candidates = library.filter { it.id != excludeChallengeId && it.id !in onCooldown }
        if (restrictTo != null) {
            candidates = candidates.filter { it.category == restrictTo }
        } else if (interests.isNotEmpty()) {
            val preferred = candidates.filter { it.category in interests }
            // Fall back to the full library rather than returning nothing when
            // the user's chosen interests are exhausted.
            if (preferred.isNotEmpty()) candidates = preferred
        }

        // Everything is on cooldown — relax that constraint before giving up.
        if (candidates.isEmpty()) {
            candidates = library.filter { it.id != excludeChallengeId }
            if (restrictTo != null) candidates = candidates.filter { it.category == restrictTo }
        }
        if (candidates.isEmpty()) return null

        val maxCategoryCount = recentCategoryCounts.values.maxOrNull() ?: 0
        val weights = candidates.map { challenge ->
            var weight = 1.0
            val seen = recentCategoryCounts[challenge.category] ?: 0
            // Least-used category gets roughly double the pull of the most-used.
            weight *= 1.0 + (maxCategoryCount - seen).coerceAtLeast(0) * 0.25
            if (challenge.difficulty == preferredDifficulty) weight *= 1.6
            weight
        }

        return weightedPick(candidates, weights, random)
    }

    private fun difficultyForLevel(level: Int): Difficulty = when {
        level <= 3 -> Difficulty.EASY
        level <= 8 -> Difficulty.MEDIUM
        else -> Difficulty.HARD
    }

    private fun <T> weightedPick(items: List<T>, weights: List<Double>, random: Random): T {
        val total = weights.sum()
        if (total <= 0.0) return items[random.nextInt(items.size)]
        var roll = random.nextDouble() * total
        for (index in items.indices) {
            roll -= weights[index]
            if (roll <= 0.0) return items[index]
        }
        return items.last()
    }
}
