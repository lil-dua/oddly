package dev.lildua.oddly.domain.model

/**
 * What a single completion earned, for the reward screen (spec S07).
 *
 * Kept separate from [ChallengeCompletion] because [leveledUp] and [newLevel]
 * describe the moment of completing, not the record: they are true only once,
 * and are never saved.
 */
data class Reward(
    val xp: Int,
    val humanityPercent: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
)
