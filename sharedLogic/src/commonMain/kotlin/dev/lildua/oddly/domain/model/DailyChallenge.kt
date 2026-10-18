package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class DailyChallengeStatus { PENDING, COMPLETED, SKIPPED }

/**
 * The challenge picked for a given [date]. Persisted as soon as it is chosen so
 * that reopening the app on the same day always shows the same challenge
 * (spec §6.1).
 */
@Serializable
data class DailyChallenge(
    val date: LocalDate,
    val challengeId: String,
    val status: DailyChallengeStatus = DailyChallengeStatus.PENDING,
    /**
     * Swaps made today (rerolls and adopts from the library). Lives on the
     * day's pick so a new day, which gets a new pick, starts again at 0.
     */
    val swapsUsed: Int = 0,
)
