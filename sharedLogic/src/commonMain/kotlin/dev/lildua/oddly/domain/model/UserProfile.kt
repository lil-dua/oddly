package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Local-only user state (spec §6). No account, no server — everything here
 * lives on the device.
 */
@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val createdAt: LocalDate,
    val interests: Set<Category>,
    val level: Int,
    val xpInLevel: Int,
) {
    val xpForNextLevel: Int get() = xpForLevel(level)

    val levelProgress: Float
        get() = (xpInLevel.toFloat() / xpForNextLevel).coerceIn(0f, 1f)

    /**
     * This profile with [xp] added, carrying the remainder over as many levels
     * as it fills, so XP earned past a level boundary is never thrown away.
     */
    fun awardXp(xp: Int): LevelResult {
        var newLevel = level
        var remaining = xpInLevel + xp
        while (remaining >= xpForLevel(newLevel)) {
            remaining -= xpForLevel(newLevel)
            newLevel += 1
        }
        return LevelResult(
            profile = copy(level = newLevel, xpInLevel = remaining),
            leveledUp = newLevel > level,
        )
    }

    companion object {
        /** XP needed to clear [level]. Grows gently so levelling never stalls. */
        fun xpForLevel(level: Int): Int = 150 + level * 50
    }
}

/** The profile after [UserProfile.awardXp], and whether any level was crossed. */
data class LevelResult(
    val profile: UserProfile,
    val leveledUp: Boolean,
)
