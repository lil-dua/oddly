package dev.lildua.oddly.domain.model

/**
 * A single actionable challenge from the content library (spec §6, §10).
 *
 * [whyItMatters] and [howToDoIt] back the "Why it matters" / "How to do it"
 * sections on the challenge detail screen.
 */
data class Challenge(
    val id: String,
    val title: LocalizedText,
    val shortDescription: LocalizedText,
    val category: Category,
    val difficulty: Difficulty,
    val estimatedMinutes: Int,
    val whyItMatters: LocalizedText,
    val howToDoIt: List<LocalizedText>,
) {
    val rewardXp: Int get() = difficulty.rewardXp
    val humanityPercent: Int get() = difficulty.humanityPercent

    /**
     * Resolves every string to [language] in one step.
     *
     * Screens take the resolved form rather than the raw challenge, so a view
     * never has to thread the current language through its own body — and it
     * cannot accidentally render one field in the wrong language.
     */
    fun localized(language: AppLanguage): LocalizedChallenge = LocalizedChallenge(
        id = id,
        title = title.of(language),
        shortDescription = shortDescription.of(language),
        category = category,
        categoryTitle = category.title.of(language),
        difficulty = difficulty,
        difficultyTitle = difficulty.title.of(language),
        estimatedMinutes = estimatedMinutes,
        whyItMatters = whyItMatters.of(language),
        howToDoIt = howToDoIt.map { it.of(language) },
        rewardXp = rewardXp,
        humanityPercent = humanityPercent,
    )
}

/** A [Challenge] with every string already resolved to one language. */
data class LocalizedChallenge(
    val id: String,
    val title: String,
    val shortDescription: String,
    val category: Category,
    val categoryTitle: String,
    val difficulty: Difficulty,
    val difficultyTitle: String,
    val estimatedMinutes: Int,
    val whyItMatters: String,
    val howToDoIt: List<String>,
    val rewardXp: Int,
    val humanityPercent: Int,
)
