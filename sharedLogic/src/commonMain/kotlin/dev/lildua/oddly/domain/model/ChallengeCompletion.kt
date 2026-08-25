package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * A record that the user finished [challengeId] on [date] (spec §6).
 *
 * Completions are the single source of truth for the calendar, statistics,
 * streak and journey screens — everything else is derived from this list.
 */
data class ChallengeCompletion(
    val id: String,
    val challengeId: String,
    val date: LocalDate,
    val completedAt: LocalTime,
    val xpEarned: Int,
    val humanityPercent: Int,
    val note: String? = null,
)
