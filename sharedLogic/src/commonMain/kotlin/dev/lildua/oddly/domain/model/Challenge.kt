package dev.lildua.oddly.domain.model

/**
 * A single actionable challenge from the content library (spec §6, §10).
 *
 * [whyItMatters] and [howToDoIt] back the "Why it matters" / "How to do it"
 * sections on the challenge detail screen.
 */
data class Challenge(
    val id: String,
    val title: String,
    val shortDescription: String,
    val category: Category,
    val difficulty: Difficulty,
    val estimatedMinutes: Int,
    val whyItMatters: String,
    val howToDoIt: List<String>,
) {
    val rewardXp: Int get() = difficulty.rewardXp
    val humanityPercent: Int get() = difficulty.humanityPercent
}
