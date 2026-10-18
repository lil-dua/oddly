package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.challenge
import dev.lildua.oddly.completion
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.data.local.InMemoryKeyValueStore
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.Difficulty
import dev.lildua.oddly.domain.model.Reward
import dev.lildua.oddly.domain.model.UserProfile
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DailyChallengeRulesTest {

    private val todays = challenge("today", difficulty = Difficulty.HARD)

    private fun state(
        level: Int = 1,
        xpInLevel: Int = 0,
    ): PersistedState {
        val fresh = OddlyRepository.freshState(TODAY)
        return fresh.copy(
            profile = fresh.profile.copy(level = level, xpInLevel = xpInLevel),
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = todays.id),
        )
    }

    private fun at(hour: Int, minute: Int) = LocalDateTime(TODAY, LocalTime(hour, minute))

    // MARK: - Completing

    @Test
    fun `complete records the wall-clock time of now`() {
        val outcome = DailyChallengeRules.complete(state(), todays, at(21, 7))

        val completed = assertIs<CompletionOutcome.Completed>(outcome)
        assertEquals(TODAY, completed.completion.date)
        assertEquals(LocalTime(21, 7), completed.completion.completedAt)
        assertEquals(listOf(completed.completion), completed.state.completions)
    }

    @Test
    fun `the completion id is the challenge id and the epoch day`() {
        val outcome = DailyChallengeRules.complete(state(), todays, at(8, 0))

        val completed = assertIs<CompletionOutcome.Completed>(outcome)
        assertEquals("completion_today_${TODAY.toEpochDays()}", completed.completion.id)
    }

    @Test
    fun `completing the same challenge twice on one day is rejected`() {
        val first = assertIs<CompletionOutcome.Completed>(
            DailyChallengeRules.complete(state(), todays, at(8, 0)),
        )

        val second = DailyChallengeRules.complete(first.state, todays, at(20, 0))

        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.ALREADY_COMPLETED_TODAY),
            second,
        )
    }

    @Test
    fun `the same challenge completed on an earlier day can be completed again`() {
        val earlier = state().copy(
            completions = listOf(completion(daysAgo(1), challengeId = todays.id)),
        )

        val outcome = DailyChallengeRules.complete(earlier, todays, at(8, 0))

        assertIs<CompletionOutcome.Completed>(outcome)
    }

    @Test
    fun `completing records xp and humanity without levelling when the bar is not full`() {
        val outcome = DailyChallengeRules.complete(state(level = 1, xpInLevel = 0), todays, at(8, 0))

        val completed = assertIs<CompletionOutcome.Completed>(outcome)
        assertEquals(
            Reward(xp = 35, humanityPercent = 10, leveledUp = false, newLevel = 1),
            completed.reward,
        )
        assertEquals(35, completed.completion.xpEarned)
        assertEquals(10, completed.completion.humanityPercent)
        assertEquals(1, completed.state.profile.level)
        assertEquals(35, completed.state.profile.xpInLevel)
    }

    @Test
    fun `completing awards xp and levels up across several levels`() {
        // Level 1 needs 200 and level 2 needs 250, so starting 34 short of the
        // two together, a 35-XP challenge lands one point into level 3.
        val start = state(level = 1, xpInLevel = 200 + 250 - 35 + 1)

        val outcome = DailyChallengeRules.complete(start, todays, at(8, 0))

        val completed = assertIs<CompletionOutcome.Completed>(outcome)
        assertEquals(
            Reward(xp = 35, humanityPercent = 10, leveledUp = true, newLevel = 3),
            completed.reward,
        )
        assertEquals(3, completed.state.profile.level)
        assertEquals(1, completed.state.profile.xpInLevel)
    }

    // MARK: - Awarding XP

    @Test
    fun `award xp keeps the level below the bar`() {
        val result = state(level = 2, xpInLevel = 10).profile.awardXp(20)

        assertFalse(result.leveledUp)
        assertEquals(2, result.profile.level)
        assertEquals(30, result.profile.xpInLevel)
    }

    @Test
    fun `award xp exactly on the bar levels up with nothing left over`() {
        val result = state(level = 1, xpInLevel = 190).profile.awardXp(10)

        assertTrue(result.leveledUp)
        assertEquals(2, result.profile.level)
        assertEquals(0, result.profile.xpInLevel)
    }

    @Test
    fun `award xp carries the remainder over several levels`() {
        val needed = UserProfile.xpForLevel(1) + UserProfile.xpForLevel(2) + UserProfile.xpForLevel(3)

        val result = state(level = 1, xpInLevel = 0).profile.awardXp(needed + 7)

        assertTrue(result.leveledUp)
        assertEquals(4, result.profile.level)
        assertEquals(7, result.profile.xpInLevel)
    }

    // MARK: - Rerolling

    @Test
    fun `reroll counts a skip and records the new daily challenge`() {
        val library = listOf(todays, challenge("other", category = Category.FINANCE))
        val start = state().copy(skippedCount = 2)

        val resolution = DailyChallengeRules.reroll(
            state = start,
            today = TODAY,
            currentChallengeId = todays.id,
            random = Random(1),
            library = library,
        )

        assertNotNull(resolution)
        assertEquals("other", resolution.challenge.id)
        assertEquals(3, resolution.state.skippedCount)
        assertEquals(DailyChallenge(date = TODAY, challengeId = "other"), resolution.state.dailyChallenge)
        // Swapping is not completing: nothing else about the state moves.
        assertEquals(start.completions, resolution.state.completions)
        assertEquals(start.profile, resolution.state.profile)
    }

    @Test
    fun `reroll never hands back the challenge being replaced`() {
        repeat(20) { seed ->
            val resolution = DailyChallengeRules.reroll(
                state = state(),
                today = TODAY,
                currentChallengeId = todays.id,
                random = Random(seed),
            )

            assertNotNull(resolution)
            assertNotEquals(todays.id, resolution.challenge.id)
        }
    }

    @Test
    fun `reroll by category stays inside that category`() {
        val resolution = DailyChallengeRules.reroll(
            state = state(),
            today = TODAY,
            currentChallengeId = todays.id,
            category = Category.FINANCE,
            random = Random(3),
        )

        assertNotNull(resolution)
        assertEquals(Category.FINANCE, resolution.challenge.category)
    }

    @Test
    fun `reroll returns null when nothing else is available`() {
        val resolution = DailyChallengeRules.reroll(
            state = state(),
            today = TODAY,
            currentChallengeId = todays.id,
            random = Random(1),
            library = listOf(todays),
        )

        assertNull(resolution)
    }

    // MARK: - Day changed

    @Test
    fun `completing when the daily challenge is from yesterday is rejected as day changed`() {
        val stale = state().copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = todays.id),
        )

        val outcome = DailyChallengeRules.complete(stale, todays, at(0, 10))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.DAY_CHANGED), outcome)
    }

    @Test
    fun `completing when the daily challenge is from tomorrow is rejected as day changed`() {
        val ahead = state().copy(
            dailyChallenge = DailyChallenge(date = daysAgo(-1), challengeId = todays.id),
        )

        val outcome = DailyChallengeRules.complete(ahead, todays, at(12, 0))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.DAY_CHANGED), outcome)
    }

    @Test
    fun `completing with no daily challenge is rejected as day changed`() {
        val none = state().copy(dailyChallenge = null)

        val outcome = DailyChallengeRules.complete(none, todays, at(12, 0))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.DAY_CHANGED), outcome)
    }

    @Test
    fun `day changed is reported before already completed`() {
        val stale = state().copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = todays.id),
            completions = listOf(completion(TODAY, challengeId = todays.id)),
        )

        val outcome = DailyChallengeRules.complete(stale, todays, at(0, 10))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.DAY_CHANGED), outcome)
    }

    @Test
    fun `a rejected completion leaves no trace to persist`() {
        val stale = state(level = 2, xpInLevel = 30).copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = todays.id),
        )

        val outcome = DailyChallengeRules.complete(stale, todays, at(0, 10))

        // Rejected carries no state, so the caller cannot accidentally save XP or a completion.
        assertIs<CompletionOutcome.Rejected>(outcome)
        assertTrue(stale.completions.isEmpty())
        assertEquals(30, stale.profile.xpInLevel)
    }

    @Test
    fun `after rollover the completion is dated the new day`() {
        val yesterday = OddlyRepository.freshState(daysAgo(1)).copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = todays.id),
        )
        val resolution = assertNotNull(OddlyRepository.rollover(yesterday, TODAY, Random(1)))

        val outcome = DailyChallengeRules.complete(resolution.state, resolution.challenge, at(0, 10))

        val completed = assertIs<CompletionOutcome.Completed>(outcome)
        assertEquals(TODAY, completed.completion.date)
        assertEquals(LocalTime(0, 10), completed.completion.completedAt)
        assertTrue(completed.state.completions.none { it.date != TODAY })
    }

    @Test
    fun `streak and week activity follow the new today`() {
        val done = listOf(completion(daysAgo(1)))

        val onTheDay = StreakCalculator.recentActivity(done, daysAgo(1))
        assertEquals(daysAgo(1), onTheDay.last().date)
        assertTrue(onTheDay.last().completed)
        assertEquals(1, StreakCalculator.calculate(done, daysAgo(1)).current)

        val nextDay = StreakCalculator.recentActivity(done, TODAY)
        assertEquals(TODAY, nextDay.last().date)
        assertTrue(nextDay.last().isToday)
        assertFalse(nextDay.last().completed)
        assertTrue(nextDay[nextDay.size - 2].completed)
        // Today not done yet does not break a run that ended yesterday.
        assertEquals(1, StreakCalculator.calculate(done, TODAY).current)
        // Two days on with nothing in between, it does.
        assertEquals(0, StreakCalculator.calculate(done, daysAgo(-1)).current)
    }

    // MARK: - One completion a day

    private val other = challenge("other", category = Category.FINANCE)
    private val library = listOf(todays, other, challenge("third", category = Category.HEALTH))

    private fun done(): PersistedState {
        val first = assertIs<CompletionOutcome.Completed>(
            DailyChallengeRules.complete(state(), todays, at(8, 0)),
        )
        return first.state
    }

    @Test
    fun `availability of todays challenge before completing is can complete`() {
        assertEquals(
            ChallengeAvailability.CAN_COMPLETE,
            DailyChallengeRules.availability(state(), todays.id, TODAY),
        )
    }

    @Test
    fun `availability of another challenge is not todays`() {
        assertEquals(
            ChallengeAvailability.NOT_TODAYS,
            DailyChallengeRules.availability(state(), other.id, TODAY),
        )
    }

    @Test
    fun `availability after completing todays challenge is done this today for it and day already done for others`() {
        val finished = done()

        assertEquals(
            ChallengeAvailability.DONE_THIS_TODAY,
            DailyChallengeRules.availability(finished, todays.id, TODAY),
        )
        assertEquals(
            ChallengeAvailability.DAY_ALREADY_DONE,
            DailyChallengeRules.availability(finished, other.id, TODAY),
        )
    }

    @Test
    fun `availability with a daily pick from another day treats every challenge as not todays`() {
        val stale = state().copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = todays.id),
        )

        assertEquals(ChallengeAvailability.NOT_TODAYS, DailyChallengeRules.availability(stale, todays.id, TODAY))
        assertEquals(ChallengeAvailability.NOT_TODAYS, DailyChallengeRules.availability(stale, other.id, TODAY))
    }

    @Test
    fun `availability ignores completions from earlier days`() {
        val earlier = state().copy(completions = listOf(completion(daysAgo(1), challengeId = todays.id)))

        assertEquals(
            ChallengeAvailability.CAN_COMPLETE,
            DailyChallengeRules.availability(earlier, todays.id, TODAY),
        )
    }

    @Test
    fun `completing a challenge that is not todays is rejected as not todays`() {
        val start = state()

        val outcome = DailyChallengeRules.complete(start, other, at(8, 0))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.NOT_TODAYS), outcome)
        assertTrue(start.completions.isEmpty())
        assertEquals(0, start.profile.xpInLevel)
    }

    @Test
    fun `completing another challenge after todays is done is rejected as day already done`() {
        val outcome = DailyChallengeRules.complete(done(), other, at(20, 0))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.DAY_ALREADY_DONE), outcome)
    }

    @Test
    fun `a second completion on the same day is rejected even after reroll`() {
        val finished = done()

        assertNull(DailyChallengeRules.reroll(finished, TODAY, todays.id, random = Random(1), library = library))
        // Even if a stale screen forced the pick onto another challenge.
        val forced = finished.copy(dailyChallenge = DailyChallenge(TODAY, other.id))
        val outcome = DailyChallengeRules.complete(forced, other, at(21, 0))

        assertEquals(CompletionOutcome.Rejected(CompletionRejection.DAY_ALREADY_DONE), outcome)
        assertEquals(1, finished.completions.size)
    }

    @Test
    fun `a second completion on the same day is rejected even after adopt`() {
        val finished = done()

        assertNull(DailyChallengeRules.adopt(finished, other, TODAY))
        val outcome = DailyChallengeRules.complete(finished, other, at(21, 0))

        assertIs<CompletionOutcome.Rejected>(outcome)
    }

    @Test
    fun `reroll after completing today returns null`() {
        assertNull(DailyChallengeRules.reroll(done(), TODAY, todays.id, random = Random(1), library = library))
        assertNull(DailyChallengeRules.reroll(done(), TODAY, todays.id, Category.FINANCE, Random(1), library))
    }

    @Test
    fun `reroll before completing still counts a skip`() {
        val resolution = assertNotNull(
            DailyChallengeRules.reroll(state(), TODAY, todays.id, random = Random(1), library = library),
        )

        assertEquals(1, resolution.state.skippedCount)
    }

    @Test
    fun `adopt before completing counts a skip and makes the challenge completable`() {
        val start = state().copy(skippedCount = 2, completions = listOf(completion(daysAgo(3), challengeId = "old")))

        val resolution = assertNotNull(DailyChallengeRules.adopt(start, other, TODAY))

        assertEquals(other, resolution.challenge)
        assertEquals(3, resolution.state.skippedCount)
        assertEquals(DailyChallenge(TODAY, other.id), resolution.state.dailyChallenge)
        // Adopting is not completing: completions and profile stay put.
        assertEquals(start.completions, resolution.state.completions)
        assertEquals(start.profile, resolution.state.profile)
        assertEquals(
            ChallengeAvailability.CAN_COMPLETE,
            DailyChallengeRules.availability(resolution.state, other.id, TODAY),
        )
        assertEquals(
            ChallengeAvailability.NOT_TODAYS,
            DailyChallengeRules.availability(resolution.state, todays.id, TODAY),
        )
        assertIs<CompletionOutcome.Completed>(DailyChallengeRules.complete(resolution.state, other, at(9, 0)))
    }

    @Test
    fun `adopt of todays own pick changes nothing`() {
        assertNull(DailyChallengeRules.adopt(state(), todays, TODAY))
    }

    @Test
    fun `adopt after a reroll to the adopted challenge is not counted twice`() {
        val rerolled = assertNotNull(
            DailyChallengeRules.reroll(state(), TODAY, todays.id, random = Random(1), library = listOf(todays, other)),
        )

        assertNull(DailyChallengeRules.adopt(rerolled.state, rerolled.challenge, TODAY))
    }

    @Test
    fun `adopt then complete earns xp once and closes the day`() {
        val adopted = assertNotNull(DailyChallengeRules.adopt(state(), other, TODAY))
        val completed = assertIs<CompletionOutcome.Completed>(
            DailyChallengeRules.complete(adopted.state, other, at(9, 0)),
        )

        assertEquals(other.rewardXp, completed.state.profile.xpInLevel)
        assertNull(DailyChallengeRules.adopt(completed.state, todays, TODAY))
        assertNull(DailyChallengeRules.reroll(completed.state, TODAY, other.id, library = library))
        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.ALREADY_COMPLETED_TODAY),
            DailyChallengeRules.complete(completed.state, other, at(10, 0)),
        )
    }

    @Test
    fun `legacy days with several completions count as done`() {
        val legacy = state().copy(
            completions = listOf(
                completion(TODAY, challengeId = "a"),
                completion(TODAY, challengeId = "b"),
            ),
        )

        assertTrue(DailyChallengeRules.isDayDone(legacy.completions, TODAY))
        assertFalse(DailyChallengeRules.isDayDone(legacy.completions, daysAgo(1)))
        assertNull(DailyChallengeRules.reroll(legacy, TODAY, todays.id, library = library))
        assertNull(DailyChallengeRules.adopt(legacy, other, TODAY))
        assertEquals(
            ChallengeAvailability.DONE_THIS_TODAY,
            DailyChallengeRules.availability(legacy, "a", TODAY),
        )
        assertEquals(
            ChallengeAvailability.DAY_ALREADY_DONE,
            DailyChallengeRules.availability(legacy, "zzz", TODAY),
        )
        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.DAY_ALREADY_DONE),
            DailyChallengeRules.complete(legacy, todays, at(12, 0)),
        )
    }

    @Test
    fun `legacy days with several completions survive a save and load`() {
        val repository = OddlyRepository(InMemoryKeyValueStore())
        val legacy = state().copy(
            completions = listOf(
                completion(TODAY, challengeId = "a"),
                completion(TODAY, challengeId = "b"),
            ),
        )

        repository.save(legacy)
        val loaded = assertNotNull(repository.load())

        assertEquals(legacy, loaded)
        assertTrue(DailyChallengeRules.isDayDone(loaded.completions, TODAY))
    }

    @Test
    fun `day changed is still reported before not todays and day already done`() {
        val stale = state().copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = todays.id),
            completions = listOf(completion(TODAY, challengeId = "a")),
        )

        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.DAY_CHANGED),
            DailyChallengeRules.complete(stale, other, at(0, 10)),
        )
    }

    @Test
    fun `already completed is reported before day already done and not todays`() {
        // The pick moved to "other" after todays was completed.
        val forced = done().copy(dailyChallenge = DailyChallenge(TODAY, other.id))

        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.ALREADY_COMPLETED_TODAY),
            DailyChallengeRules.complete(forced, todays, at(12, 0)),
        )
        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.DAY_ALREADY_DONE),
            DailyChallengeRules.complete(forced, other, at(12, 0)),
        )
    }

    @Test
    fun `day already done is reported before not todays`() {
        // Done with "a" (not the pick); "other" is neither the pick nor done.
        val legacy = state().copy(completions = listOf(completion(TODAY, challengeId = "a")))

        assertEquals(
            CompletionOutcome.Rejected(CompletionRejection.DAY_ALREADY_DONE),
            DailyChallengeRules.complete(legacy, other, at(12, 0)),
        )
    }
}
