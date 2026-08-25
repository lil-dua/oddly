package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalDate

/**
 * Consecutive-day tracking (spec §6). Per the "fun over guilt" principle a
 * broken streak never costs XP or resets the level.
 */
data class StreakInfo(
    val current: Int,
    val best: Int,
    val lastCompletedDate: LocalDate?,
)
