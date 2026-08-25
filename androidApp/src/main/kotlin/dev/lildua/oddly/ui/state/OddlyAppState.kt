package dev.lildua.oddly.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.data.seed.SampleData
import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.StreakInfo
import dev.lildua.oddly.domain.model.UserProfile
import dev.lildua.oddly.domain.usecase.ChallengeSelector
import dev.lildua.oddly.domain.usecase.DayActivity
import dev.lildua.oddly.domain.usecase.StreakCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.random.Random

/**
 * In-memory application state, seeded from [SampleData].
 *
 * This stands in for the repository + ViewModel layer that arrives in Phase 2.
 * Everything is real, derived state — completing a challenge genuinely updates
 * the streak, calendar, statistics and XP — so the screens can be reviewed with
 * live behaviour rather than static mock values.
 */
@Stable
class OddlyAppState(
    val today: LocalDate,
) {
    private val _completions = mutableStateListOf<ChallengeCompletion>().apply {
        addAll(SampleData.completions)
    }

    val completions: List<ChallengeCompletion> get() = _completions

    var profile: UserProfile by mutableStateOf(SampleData.profile)
        private set

    var settings: AppSettings by mutableStateOf(SampleData.settings)

    var interests: Set<Category> by mutableStateOf(SampleData.profile.interests)

    /** The challenge shown on Home; stable for the whole day (spec §6.1). */
    var todayChallenge: Challenge by mutableStateOf(SampleData.todayChallenge)
        private set

    /** Set when the user opens a challenge from anywhere other than Home. */
    var skippedCount: Int by mutableStateOf(SampleData.SKIPPED_COUNT)
        private set

    /** XP and Humanity gained by the most recent completion, for the reward screen. */
    var lastReward: Reward? by mutableStateOf(null)
        private set

    private val random = Random(SampleData.today.toEpochDays())

    val streak: StreakInfo
        get() = StreakCalculator.calculate(_completions, today)

    val weekActivity: List<DayActivity>
        get() = StreakCalculator.recentActivity(_completions, today)

    val completedToday: Boolean
        get() = _completions.any { it.date == today }

    val totalCompleted: Int get() = _completions.size

    val completionRatePercent: Int
        get() {
            val total = _completions.size + skippedCount
            return if (total == 0) 0 else (_completions.size * 100) / total
        }

    val exploredCategoryCount: Int
        get() = _completions
            .mapNotNull { ChallengeSeed.byId(it.challengeId)?.category }
            .distinct()
            .size

    fun completionsOn(date: LocalDate): List<ChallengeCompletion> =
        _completions.filter { it.date == date }.sortedByDescending { it.completedAt }

    fun challengeOf(completion: ChallengeCompletion): Challenge? =
        ChallengeSeed.byId(completion.challengeId)

    fun isCompleted(challengeId: String): Boolean =
        _completions.any { it.challengeId == challengeId }

    fun isCompletedToday(challengeId: String): Boolean =
        _completions.any { it.date == today && it.challengeId == challengeId }

    /** Record a completion and award XP, levelling up if the bar fills. */
    fun complete(challenge: Challenge, at: LocalTime = LocalTime(9, 41)) {
        if (_completions.any { it.date == today && it.challengeId == challenge.id }) return

        _completions.add(
            ChallengeCompletion(
                id = "completion_${challenge.id}_${today.toEpochDays()}",
                challengeId = challenge.id,
                date = today,
                completedAt = at,
                xpEarned = challenge.rewardXp,
                humanityPercent = challenge.humanityPercent,
            ),
        )

        var level = profile.level
        var xp = profile.xpInLevel + challenge.rewardXp
        while (xp >= UserProfile.xpForLevel(level)) {
            xp -= UserProfile.xpForLevel(level)
            level += 1
        }
        val leveledUp = level > profile.level
        profile = profile.copy(level = level, xpInLevel = xp)

        lastReward = Reward(
            xp = challenge.rewardXp,
            humanityPercent = challenge.humanityPercent,
            leveledUp = leveledUp,
            newLevel = level,
        )
    }

    /** Swap today's challenge for another one (spec §2.3). */
    fun reroll(category: Category? = null) {
        val next = ChallengeSelector.select(
            library = ChallengeSeed.all,
            interests = interests,
            completions = _completions,
            level = profile.level,
            today = today,
            restrictTo = category,
            excludeChallengeId = todayChallenge.id,
            random = random,
        )
        if (next != null) {
            skippedCount += 1
            todayChallenge = next
        }
    }

    /** Make [challenge] the active one, e.g. after picking from the library. */
    fun chooseTodayChallenge(challenge: Challenge) {
        todayChallenge = challenge
    }

    fun toggleInterest(category: Category) {
        interests = if (category in interests) interests - category else interests + category
    }

    fun clearReward() {
        lastReward = null
    }

    /** Wipes local data (spec §S15). Used by the Reset action in Settings. */
    fun resetAllData() {
        _completions.clear()
        profile = profile.copy(level = 1, xpInLevel = 0)
        skippedCount = 0
        lastReward = null
    }

    data class Reward(
        val xp: Int,
        val humanityPercent: Int,
        val leveledUp: Boolean,
        val newLevel: Int,
    )
}

@Composable
fun rememberOddlyAppState(): OddlyAppState =
    remember { OddlyAppState(today = SampleData.today) }
