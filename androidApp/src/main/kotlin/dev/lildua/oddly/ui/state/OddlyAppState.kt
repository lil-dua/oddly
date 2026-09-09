package dev.lildua.oddly.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.lildua.oddly.core.time.Today
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.data.local.SharedPreferencesStore
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.StreakInfo
import dev.lildua.oddly.domain.model.UserProfile
import dev.lildua.oddly.domain.usecase.ChallengeSelector
import dev.lildua.oddly.domain.usecase.DayActivity
import dev.lildua.oddly.domain.usecase.StreakCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.random.Random

/**
 * Application state, loaded from and written back to [OddlyRepository].
 *
 * Every mutation persists immediately rather than on a timer or at shutdown:
 * the writes are a few kilobytes of JSON, and a habit app that loses the
 * completion the user was just congratulated for is worse than a slow one.
 *
 * Derived values — streak, statistics, category mix — are still computed from
 * the completion list on demand rather than stored, so there is only ever one
 * source of truth to keep consistent.
 */
@Stable
class OddlyAppState(
    val today: LocalDate,
    private val repository: OddlyRepository,
    initial: PersistedState,
    initialChallenge: Challenge,
) {
    private val _completions = mutableStateListOf<ChallengeCompletion>().apply {
        addAll(initial.completions)
    }

    val completions: List<ChallengeCompletion> get() = _completions

    var profile: UserProfile by mutableStateOf(initial.profile)
        private set

    private var _settings by mutableStateOf(initial.settings)

    var settings: AppSettings
        get() = _settings
        set(value) {
            _settings = value
            persist()
        }

    var interests: Set<Category> by mutableStateOf(initial.profile.interests)
        private set

    /** The challenge shown on Home; stable for the whole day (spec §6.1). */
    var todayChallenge: Challenge by mutableStateOf(initialChallenge)
        private set

    /** Started but set aside — drives the completion-rate statistic. */
    var skippedCount: Int by mutableStateOf(initial.skippedCount)
        private set

    var hasCompletedOnboarding: Boolean by mutableStateOf(initial.hasCompletedOnboarding)
        private set

    /** XP and Humanity gained by the most recent completion, for the reward screen. */
    var lastReward: Reward? by mutableStateOf(null)
        private set

    private var dailyChallengeDate: LocalDate? = initial.dailyChallenge?.date

    private val random = Random(today.toEpochDays())

    // MARK: - Derived state

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

    // MARK: - Mutations

    /** Record a completion and award XP, levelling up if the bar fills. */
    fun complete(challenge: Challenge, at: LocalTime = LocalTime(9, 41)) {
        if (isCompletedToday(challenge.id)) return

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
        persist()
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
        ) ?: return

        skippedCount += 1
        todayChallenge = next
        dailyChallengeDate = today
        persist()
    }

    fun toggleInterest(category: Category) {
        interests = if (category in interests) interests - category else interests + category
        profile = profile.copy(interests = interests)
        persist()
    }

    fun completeOnboarding() {
        hasCompletedOnboarding = true
        // Today's challenge was picked while the app loaded, before onboarding
        // had asked what the user cares about. Give them one that matches.
        OddlyRepository.realignToInterests(currentState(), today, random)?.let { realigned ->
            todayChallenge = realigned.challenge
            dailyChallengeDate = today
        }
        persist()
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
        persist()
    }

    /** The saved document, for the export row in Settings (spec §S22). */
    fun exportJson(): String = repository.exportJson()

    private fun currentState() = PersistedState(
        profile = profile.copy(interests = interests),
        settings = settings,
        completions = _completions.toList(),
        dailyChallenge = dailyChallengeDate?.let {
            DailyChallenge(date = it, challengeId = todayChallenge.id)
        },
        skippedCount = skippedCount,
        hasCompletedOnboarding = hasCompletedOnboarding,
    )

    private fun persist() {
        repository.save(currentState())
    }

    data class Reward(
        val xp: Int,
        val humanityPercent: Int,
        val leveledUp: Boolean,
        val newLevel: Int,
    )
}

@Composable
fun rememberOddlyAppState(): OddlyAppState {
    val context = LocalContext.current
    return remember {
        val repository = OddlyRepository(SharedPreferencesStore(context))
        val today = Today.local()

        val loaded = repository.load() ?: OddlyRepository.freshState(today)
        // Picking today's challenge is part of loading: it has to be settled and
        // written back before the first frame, or reopening the app mid-morning
        // would show a different challenge than it did at breakfast.
        val resolution = OddlyRepository.resolveDailyChallenge(
            state = loaded,
            today = today,
            random = Random(today.toEpochDays()),
        )
        repository.save(resolution.state)

        OddlyAppState(
            today = today,
            repository = repository,
            initial = resolution.state,
            initialChallenge = resolution.challenge,
        )
    }
}
