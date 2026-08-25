package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalDate

enum class DailyChallengeStatus { PENDING, COMPLETED, SKIPPED }

/**
 * The challenge picked for a given [date]. Persisted as soon as it is chosen so
 * that reopening the app on the same day always shows the same challenge
 * (spec §6.1).
 */
data class DailyChallenge(
    val date: LocalDate,
    val challengeId: String,
    val status: DailyChallengeStatus = DailyChallengeStatus.PENDING,
)
