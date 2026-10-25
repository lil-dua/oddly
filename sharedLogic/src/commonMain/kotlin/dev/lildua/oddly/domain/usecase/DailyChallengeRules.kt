package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.data.local.DailyChallengeResolution
import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.Reward
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random

/**
 * What completing or swapping the daily challenge does to the saved state.
 *
 * Both apps used to carry their own copy of these rules, and every change had
 * to be made twice and kept in step by hand. Here they are pure functions from
 * one [PersistedState] to the next, so the apps only build the state, call in,
 * and store what comes back — and the rules can be tested without a UI.
 */
object DailyChallengeRules {

    /**
     * Records [challenge] as completed at [now] and awards its XP.
     *
     * The completion is always dated [now]'s date. If the challenge on screen
     * belongs to another day the call is refused with
     * [CompletionRejection.DAY_CHANGED] instead of stamping it with a date it
     * was never shown on (e.g. "yesterday's challenge, 00:10").
     *
     * Only one completion per day counts, and only for the day's own pick: any
     * other challenge, or any second completion, is refused (see [availability]).
     */
    fun complete(
        state: PersistedState,
        challenge: Challenge,
        now: LocalDateTime,
    ): CompletionOutcome {
        val date = now.date
        // First, so rules added later never see a stale day.
        if (state.dailyChallenge?.date != date) {
            return CompletionOutcome.Rejected(CompletionRejection.DAY_CHANGED)
        }
        // One scoring completion per day, for today's pick only: otherwise the
        // library or repeated rerolls would let a user farm XP.
        when (availability(state, challenge.id, date)) {
            ChallengeAvailability.DONE_THIS_TODAY ->
                return CompletionOutcome.Rejected(CompletionRejection.ALREADY_COMPLETED_TODAY)
            ChallengeAvailability.DAY_ALREADY_DONE ->
                return CompletionOutcome.Rejected(CompletionRejection.DAY_ALREADY_DONE)
            ChallengeAvailability.NOT_TODAYS ->
                return CompletionOutcome.Rejected(CompletionRejection.NOT_TODAYS)
            ChallengeAvailability.CAN_COMPLETE -> Unit
        }

        val completion = ChallengeCompletion(
            id = completionId(challenge.id, date),
            challengeId = challenge.id,
            date = date,
            completedAt = now.time,
            xpEarned = challenge.rewardXp,
            humanityPercent = challenge.humanityPercent,
        )
        val levelled = state.profile.awardXp(challenge.rewardXp)

        return CompletionOutcome.Completed(
            state = state.copy(
                profile = levelled.profile,
                completions = state.completions + completion,
            ),
            completion = completion,
            reward = Reward(
                xp = challenge.rewardXp,
                humanityPercent = challenge.humanityPercent,
                leveledUp = levelled.leveledUp,
                newLevel = levelled.profile.level,
            ),
        )
    }

    /** Swaps allowed per day, shared by [reroll] and [adopt]. */
    const val MAX_SWAPS_PER_DAY: Int = 3

    /**
     * Swaps still available on [today]: 0 once the day is done, otherwise
     * [MAX_SWAPS_PER_DAY] minus those used on today's pick (a pick from another
     * day, or none, means a fresh set).
     */
    fun swapsLeft(state: PersistedState, today: LocalDate): Int = swapsLeft(
        dayDone = isDayDone(state.completions, today),
        pickDate = state.dailyChallenge?.date,
        swapsUsed = state.dailyChallenge?.swapsUsed ?: 0,
        today = today,
    )

    /**
     * Same rule from loose parts, so an AppState can ask without building a
     * [PersistedState] (copying every completion) on each read.
     */
    fun swapsLeft(dayDone: Boolean, pickDate: LocalDate?, swapsUsed: Int, today: LocalDate): Int {
        if (dayDone) return 0
        val used = if (pickDate == today) swapsUsed else 0
        return (MAX_SWAPS_PER_DAY - used).coerceAtLeast(0)
    }

    private fun swapsUsedOn(state: PersistedState, today: LocalDate): Int =
        state.dailyChallenge?.takeIf { it.date == today }?.swapsUsed ?: 0

    /**
     * Swaps today's challenge for another one (spec §2.3), optionally from
     * [category] only.
     *
     * Swaps are capped at [MAX_SWAPS_PER_DAY] so "one challenge a day" keeps
     * its meaning, and only the first swap of the day counts as skipped, so a
     * few tries do not drag the completion rate down.
     *
     * Returns null — and the caller changes nothing — when today is already
     * done, no swaps are left, or the library has no other challenge to offer.
     */
    fun reroll(
        state: PersistedState,
        today: LocalDate,
        currentChallengeId: String,
        category: Category? = null,
        random: Random = Random.Default,
        library: List<Challenge> = ChallengeSeed.all,
    ): DailyChallengeResolution? {
        if (swapsLeft(state, today) == 0) return null
        val next = ChallengeSelector.select(
            library = library,
            interests = state.profile.interests,
            completions = state.completions,
            level = state.profile.level,
            today = today,
            restrictTo = category,
            excludeChallengeId = currentChallengeId,
            random = random,
        ) ?: return null
        val used = swapsUsedOn(state, today)

        return DailyChallengeResolution(
            challenge = next,
            state = state.copy(
                dailyChallenge = DailyChallenge(date = today, challengeId = next.id, swapsUsed = used + 1),
                skippedCount = state.skippedCount + if (used == 0) 1 else 0,
            ),
        )
    }

    /**
     * Makes [challenge] (picked from the library) today's challenge. Uses one of
     * the same [MAX_SWAPS_PER_DAY] as [reroll], otherwise the library would be a
     * way around the cap, and is skipped-counted by the same first-swap rule.
     *
     * Returns null — and the caller changes nothing — when today is already done,
     * no swaps are left, or [challenge] already is today's pick (nothing was
     * set aside).
     */
    fun adopt(
        state: PersistedState,
        challenge: Challenge,
        today: LocalDate,
    ): DailyChallengeResolution? {
        if (swapsLeft(state, today) == 0) return null
        // Compared by id and day, not as a whole: swapsUsed differs on a re-adopt.
        val current = state.dailyChallenge
        if (current != null && current.date == today && current.challengeId == challenge.id) return null
        val used = swapsUsedOn(state, today)
        return DailyChallengeResolution(
            challenge = challenge,
            state = state.copy(
                dailyChallenge = DailyChallenge(date = today, challengeId = challenge.id, swapsUsed = used + 1),
                skippedCount = state.skippedCount + if (used == 0) 1 else 0,
            ),
        )
    }

    /**
     * True when any completion is dated [today]. The "once a day" rule: older
     * data with several completions on one day still just means "done".
     */
    fun isDayDone(completions: List<ChallengeCompletion>, today: LocalDate): Boolean =
        completions.any { it.date == today }

    /** What [challengeId] can do right now, from the saved [state] on [today]. */
    fun availability(
        state: PersistedState,
        challengeId: String,
        today: LocalDate,
    ): ChallengeAvailability = when {
        state.completions.any { it.date == today && it.challengeId == challengeId } ->
            ChallengeAvailability.DONE_THIS_TODAY
        isDayDone(state.completions, today) -> ChallengeAvailability.DAY_ALREADY_DONE
        state.dailyChallenge?.takeIf { it.date == today }?.challengeId == challengeId ->
            ChallengeAvailability.CAN_COMPLETE
        else -> ChallengeAvailability.NOT_TODAYS
    }

    /**
     * One completion per challenge per day, so the id only needs both. Kept in
     * this exact shape because saved and exported documents already contain it.
     */
    internal fun completionId(challengeId: String, date: LocalDate): String =
        "completion_${challengeId}_${date.toEpochDays()}"
}

/** The result of [DailyChallengeRules.complete]. */
sealed class CompletionOutcome {
    /** Recorded: save [state] and show [reward]. */
    data class Completed(
        val state: PersistedState,
        val completion: ChallengeCompletion,
        val reward: Reward,
    ) : CompletionOutcome()

    /** Nothing changed, and [reason] says why. */
    data class Rejected(val reason: CompletionRejection) : CompletionOutcome()
}

/**
 * Why a completion was refused. An enum rather than a boolean so the screen can
 * explain each case differently as more rules join this one.
 */
enum class CompletionRejection {
    /**
     * The saved daily challenge belongs to a day other than [now]'s. The caller
     * must roll over to the new day and show it again.
     */
    DAY_CHANGED,

    /** This exact challenge already has a completion dated today. */
    ALREADY_COMPLETED_TODAY,

    /** Today already has a completion for another challenge; one a day counts. */
    DAY_ALREADY_DONE,

    /** The challenge is not today's pick; adopt it first. */
    NOT_TODAYS,
}

/** Whether a challenge can be completed today, for screens to pick their call to action. */
enum class ChallengeAvailability {
    /** It is today's pick and today is not done yet. */
    CAN_COMPLETE,

    /** Today is not done, but this is not today's pick (a library entry). */
    NOT_TODAYS,

    /** This very challenge is already completed today. */
    DONE_THIS_TODAY,

    /** Today is done with a different challenge. */
    DAY_ALREADY_DONE,
}
