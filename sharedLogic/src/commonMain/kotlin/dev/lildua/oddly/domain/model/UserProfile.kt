package dev.lildua.oddly.domain.model

import kotlinx.datetime.LocalDate

/**
 * Local-only user state (spec §6). No account, no server — everything here
 * lives on the device.
 */
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

    companion object {
        /** XP needed to clear [level]. Grows gently so levelling never stalls. */
        fun xpForLevel(level: Int): Int = 150 + level * 50
    }
}
