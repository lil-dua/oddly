package dev.lildua.oddly.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import dev.lildua.oddly.domain.model.DayChangeNotice
import dev.lildua.oddly.domain.model.Quote
import dev.lildua.oddly.domain.model.Reward
import dev.lildua.oddly.domain.model.StreakInfo
import dev.lildua.oddly.domain.model.UserProfile
import dev.lildua.oddly.domain.usecase.ChallengeAvailability
import dev.lildua.oddly.domain.usecase.CompletionOutcome
import dev.lildua.oddly.domain.usecase.CompletionRejection
import dev.lildua.oddly.domain.usecase.DailyChallengeRules
import dev.lildua.oddly.domain.usecase.DayActivity
import dev.lildua.oddly.domain.usecase.QuoteFavorites
import dev.lildua.oddly.domain.usecase.StreakCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random

/**
 * Application state, loaded from and written back to [OddlyRepository].
 *
 * Every mutation persists immediately rather than on a timer or at shutdown:
 * the writes are a few kilobytes of JSON, and a habit app that loses the
 * completion the user was just congratulated for is worse than a slow one.
 *
 * Derived values — streak, week strip, explored categories — are never stored
 * or persisted, but they are memoised with derivedStateOf over [completions] and
 * [today], so they recompute only when one of those changes rather than on every
 * read in every recomposition. [completions] is an immutable list replaced as a
 * whole, so that is the single write that invalidates them.
 */
@Stable
class OddlyAppState(
    initialToday: LocalDate,
    private val repository: OddlyRepository,
    initial: PersistedState,
    initialChallenge: Challenge,
    /** Wall-clock source for completion times; a parameter so it can be pinned. */
    private val clock: () -> LocalDateTime = Today::now,
) {
    /**
     * The calendar day every screen shows. Moves only through [refreshDay], so
     * a day change is one state write that Home, Statistics and the rest
     * recompose from.
     */
    var today: LocalDate by mutableStateOf(initialToday)
        private set

    var completions: List<ChallengeCompletion> by mutableStateOf(initial.completions)
        private set

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

    /** Ids of hearted quotes; edited only through [toggleFavorite] and [resetAllData]. */
    var favoriteQuoteIds: Set<String> by mutableStateOf(initial.favoriteQuoteIds)
        private set

    /** XP and Humanity gained by the most recent completion, for the reward screen. */
    var lastReward: Reward? by mutableStateOf(null)
        private set

    private var dailyChallengeDate: LocalDate? = initial.dailyChallenge?.date

    /**
     * Swaps used on today's pick. Observable (unlike [dailyChallengeDate]) so
     * [swapsLeft] recomposes the screens that show it.
     */
    private var swapsUsed: Int by mutableStateOf(initial.dailyChallenge?.swapsUsed ?: 0)

    // Reseeded on a day change so rerolls differ from day to day.
    private var random = Random(initialToday.toEpochDays())

    /** One-off banner for Home after the day changed under a running app. */
    var dayChangeNotice: DayChangeNotice? by mutableStateOf(null)
        private set

    // MARK: - Derived state

    val streak: StreakInfo by derivedStateOf { StreakCalculator.calculate(completions, today) }

    val weekActivity: List<DayActivity> by derivedStateOf {
        StreakCalculator.recentActivity(completions, today)
    }

    val completedToday: Boolean by derivedStateOf { DailyChallengeRules.isDayDone(completions, today) }

    /** Swaps left today; 0 once today is done (see [DailyChallengeRules.swapsLeft]). */
    val swapsLeft: Int
        get() = DailyChallengeRules.swapsLeft(completedToday, dailyChallengeDate, swapsUsed, today)

    /**
     * False once today is done or the swaps ran out: no more swapping. Also the
     * condition for [adopt].
     */
    val canReroll: Boolean get() = swapsLeft > 0

    val totalCompleted: Int get() = completions.size

    val completionRatePercent: Int
        get() {
            val total = completions.size + skippedCount
            return if (total == 0) 0 else (completions.size * 100) / total
        }

    val exploredCategoryCount: Int by derivedStateOf {
        completions.mapNotNull { ChallengeSeed.byId(it.challengeId)?.category }.distinct().size
    }

    /** Hearted quotes in seed order (see [QuoteFavorites.favorites]). */
    val favoriteQuotes: List<Quote> get() = QuoteFavorites.favorites(favoriteQuoteIds)

    fun isFavorite(quoteId: String): Boolean = quoteId in favoriteQuoteIds

    fun completionsOn(date: LocalDate): List<ChallengeCompletion> =
        completions.filter { it.date == date }.sortedByDescending { it.completedAt }

    fun challengeOf(completion: ChallengeCompletion): Challenge? =
        ChallengeSeed.byId(completion.challengeId)

    fun isCompleted(challengeId: String): Boolean =
        completions.any { it.challengeId == challengeId }

    fun isCompletedToday(challengeId: String): Boolean =
        completions.any { it.date == today && it.challengeId == challengeId }

    fun availability(challengeId: String): ChallengeAvailability =
        DailyChallengeRules.availability(currentState(), challengeId, today)

    // MARK: - Mutations

    /**
     * Moves the app to [date] (default: the clock's today) if the calendar has
     * changed since the state was built. Returns true when the day changed, so
     * the caller can stop whatever it was doing for the old day.
     *
     * Same-day calls are no-ops and write nothing, since this runs on every
     * resume. Going back in time counts too; see [OddlyRepository.rollover].
     */
    fun refreshDay(date: LocalDate = clock().date): Boolean {
        val dayChanged = date != today
        val nextRandom = if (dayChanged) Random(date.toEpochDays()) else random
        val resolution = OddlyRepository.rollover(currentState(), date, nextRandom) ?: return false

        random = nextRandom
        today = date
        absorb(resolution.state)
        todayChallenge = resolution.challenge
        if (dayChanged) {
            lastReward = null
            dayChangeNotice = DayChangeNotice.NEW_DAY
        }
        persist()
        return dayChanged
    }

    /**
     * Record a completion and award XP, levelling up if the bar fills.
     *
     * Returns true when it was recorded. False means nothing was awarded: either
     * the day had changed (the state has already moved on to the new day and
     * Home explains why), today already has a completion, or the challenge is
     * not today's pick (see [availability]).
     *
     * The completion is dated by the clock, never by [today]: if this state
     * missed midnight, filing it under the old day would be wrong, so the call
     * is refused instead.
     */
    fun complete(challenge: Challenge): Boolean {
        val now = clock()
        if (refreshDay(now.date)) {
            dayChangeNotice = DayChangeNotice.COMPLETION_REJECTED
            return false
        }
        val outcome = DailyChallengeRules.complete(
            state = currentState(),
            challenge = challenge,
            now = now,
        )
        if (outcome is CompletionOutcome.Rejected) {
            // Safety net: refreshDay already caught a stale day, so this should
            // not be reached unless the shared rule changes.
            if (outcome.reason == CompletionRejection.DAY_CHANGED) {
                dayChangeNotice = DayChangeNotice.COMPLETION_REJECTED
            }
            return false
        }
        outcome as CompletionOutcome.Completed

        absorb(outcome.state)
        lastReward = outcome.reward
        persist()
        return true
    }

    /**
     * Swap today's challenge for another one (spec §2.3).
     *
     * Returns the new challenge, or null when there was nothing to swap to,
     * today is already done, or the day had changed (nothing is rerolled or
     * counted as skipped then).
     */
    fun reroll(category: Category? = null): Challenge? {
        if (refreshDay()) return null
        val resolution = DailyChallengeRules.reroll(
            state = currentState(),
            today = today,
            currentChallengeId = todayChallenge.id,
            category = category,
            random = random,
        ) ?: return null

        absorb(resolution.state)
        todayChallenge = resolution.challenge
        persist()
        return resolution.challenge
    }

    /**
     * Makes a library [challenge] today's challenge; counts as one swap. Returns
     * false when nothing changed: the day changed, today is done, or it already
     * is today's pick.
     */
    fun adopt(challenge: Challenge): Boolean {
        if (refreshDay()) return false
        val resolution = DailyChallengeRules.adopt(currentState(), challenge, today) ?: return false

        absorb(resolution.state)
        todayChallenge = resolution.challenge
        persist()
        return true
    }

    fun toggleInterest(category: Category) {
        interests = if (category in interests) interests - category else interests + category
        profile = profile.copy(interests = interests)
        persist()
    }

    fun toggleFavorite(quoteId: String) {
        favoriteQuoteIds = QuoteFavorites.toggle(currentState(), quoteId).favoriteQuoteIds
        persist()
    }

    fun completeOnboarding() {
        hasCompletedOnboarding = true
        // A day change during onboarding is not news: Home was never shown.
        dayChangeNotice = null
        // Today's challenge was picked while the app loaded, before onboarding
        // had asked what the user cares about. Give them one that matches.
        OddlyRepository.realignToInterests(currentState(), today, random)?.let { realigned ->
            todayChallenge = realigned.challenge
            dailyChallengeDate = today
            swapsUsed = realigned.state.dailyChallenge?.swapsUsed ?: 0
        }
        persist()
    }

    fun clearReward() {
        lastReward = null
    }

    fun clearDayChangeNotice() {
        dayChangeNotice = null
    }

    /** Wipes local data (spec §S15). Used by the Reset action in Settings. */
    fun resetAllData() {
        completions = emptyList()
        profile = profile.copy(level = 1, xpInLevel = 0)
        skippedCount = 0
        swapsUsed = 0
        favoriteQuoteIds = emptySet()
        lastReward = null
        persist()
    }

    /** The saved document, for the export row in Settings (spec §S22). */
    fun exportJson(): String = repository.exportJson()

    /** Decodes a picked file without touching anything, for the confirm dialog. */
    fun readBackup(raw: String): PersistedState? = repository.readBackup(raw)

    /**
     * Replaces everything on the device with the backup in [raw]; false and
     * nothing changed if it is not a readable backup.
     *
     * [absorb] is not enough here: it deliberately skips settings, interests,
     * favorites and onboarding (they are edited elsewhere), but a restore has to
     * replace those too. Every field of [PersistedState] is loaded below, and
     * [_settings] is assigned directly so the setter's persist() does not write
     * a half-restored state.
     */
    fun importBackup(raw: String): Boolean {
        val imported = repository.importJson(raw) ?: return false
        val date = clock().date
        val nextRandom = Random(date.toEpochDays())
        val resolution = OddlyRepository.restoreFromBackup(imported, date, nextRandom)
        val state = resolution.state

        random = nextRandom
        today = date
        absorb(state)
        _settings = state.settings
        interests = state.profile.interests
        hasCompletedOnboarding = state.hasCompletedOnboarding
        favoriteQuoteIds = state.favoriteQuoteIds
        todayChallenge = resolution.challenge
        lastReward = null
        dayChangeNotice = null
        persist()
        return true
    }

    private fun currentState() = PersistedState(
        profile = profile.copy(interests = interests),
        settings = settings,
        completions = completions,
        dailyChallenge = dailyChallengeDate?.let {
            DailyChallenge(date = it, challengeId = todayChallenge.id, swapsUsed = swapsUsed)
        },
        skippedCount = skippedCount,
        hasCompletedOnboarding = hasCompletedOnboarding,
        // Left out, every persist() would write an empty set and wipe the favorites.
        favoriteQuoteIds = favoriteQuoteIds,
    )

    /**
     * Takes back the parts of [state] a shared rule may have changed. Interests,
     * settings and favorites are only ever edited here, so they are left alone.
     */
    private fun absorb(state: PersistedState) {
        completions = state.completions
        profile = state.profile
        skippedCount = state.skippedCount
        dailyChallengeDate = state.dailyChallenge?.date
        swapsUsed = state.dailyChallenge?.swapsUsed ?: 0
    }

    private fun persist() {
        repository.save(currentState())
    }
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
            initialToday = today,
            repository = repository,
            initial = resolution.state,
            initialChallenge = resolution.challenge,
        )
    }
}
