package dev.lildua.oddly

import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.Difficulty
import dev.lildua.oddly.domain.model.LocalizedText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Test fixtures.
 *
 * Dates are expressed as offsets from a fixed [TODAY] rather than as literals,
 * so a test reads as "three days ago" instead of a calendar date the reader has
 * to subtract in their head — and so nothing depends on the real clock.
 */

/** An arbitrary Wednesday, far from month and year boundaries. */
val TODAY: LocalDate = LocalDate(2026, 9, 9)

/** [days] before [TODAY]; negative values move forward. */
fun daysAgo(days: Int): LocalDate = LocalDate.fromEpochDays(TODAY.toEpochDays() - days)

fun challenge(
    id: String,
    category: Category = Category.HEALTH,
    difficulty: Difficulty = Difficulty.EASY,
): Challenge = Challenge(
    id = id,
    title = LocalizedText(vi = id, en = id),
    shortDescription = LocalizedText(vi = "", en = ""),
    category = category,
    difficulty = difficulty,
    estimatedMinutes = 5,
    whyItMatters = LocalizedText(vi = "", en = ""),
    howToDoIt = emptyList(),
)

fun completion(
    date: LocalDate,
    challengeId: String = "c1",
    xp: Int = 10,
): ChallengeCompletion = ChallengeCompletion(
    id = "${challengeId}_${date.toEpochDays()}",
    challengeId = challengeId,
    date = date,
    completedAt = LocalTime(9, 41),
    xpEarned = xp,
    humanityPercent = 5,
)

/** Completions on each of [offsets] days before [TODAY], with distinct ids. */
fun completionsOn(vararg offsets: Int): List<ChallengeCompletion> =
    offsets.map { completion(daysAgo(it), challengeId = "c$it") }
