package dev.lildua.oddly.domain.model

/**
 * Difficulty drives both the reward and the weighted-random selection: the
 * engine prefers challenges whose difficulty matches the user's current level.
 */
enum class Difficulty(
    val id: String,
    val title: String,
    val rewardXp: Int,
    val humanityPercent: Int,
) {
    EASY(id = "easy", title = "Dễ", rewardXp = 10, humanityPercent = 5),
    MEDIUM(id = "medium", title = "Vừa", rewardXp = 20, humanityPercent = 5),
    HARD(id = "hard", title = "Khó", rewardXp = 35, humanityPercent = 10);

    companion object {
        fun fromId(id: String): Difficulty? = entries.firstOrNull { it.id == id }
    }
}
