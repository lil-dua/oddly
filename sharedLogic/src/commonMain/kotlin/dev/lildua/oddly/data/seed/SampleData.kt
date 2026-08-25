package dev.lildua.oddly.data.seed

import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.UserProfile
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.random.Random

/**
 * Pre-populated state so every screen can be built and reviewed before the
 * database layer exists. Replace with real repositories in Phase 2 — the shape
 * of [SampleData] matches what those repositories will expose.
 *
 * Everything is generated relative to the real "today" so the calendar, streak
 * and statistics screens look live rather than frozen at a hardcoded date.
 */
object SampleData {

    val today: LocalDate get() = Clock.System.todayIn(TimeZone.currentSystemDefault())

    /** Deterministic: same run of days on every launch. */
    private const val SEED = 42

    /**
     * Day offsets that carry a completion, newest first.
     *
     * Today (offset 0) is deliberately left open so the app opens on an
     * unfinished challenge — that is the state the daily loop is designed
     * around. Days 1–12 form the current 12-day streak (a run ending yesterday
     * is still current). Days 14–31 form an earlier 18-day run, which becomes
     * the personal best. Two stragglers bring the total to 32.
     */
    private val completionDayOffsets: List<Int> =
        (1..12).toList() + (14..31).toList() + listOf(35, 40)

    /**
     * Category mix for the generated history, chosen so the distribution chart
     * shows a realistic spread rather than an even split.
     */
    private val completionCategoryPlan: List<Category> = buildList {
        repeat(12) { add(Category.HEALTH) }
        repeat(8) { add(Category.SELF_GROWTH) }
        repeat(6) { add(Category.RELATIONSHIPS) }
        repeat(4) { add(Category.LIFE_EXPERIENCE) }
        add(Category.CREATIVITY)
        add(Category.FINANCE)
    }

    /** How many challenges the user started but skipped — drives completion rate. */
    const val SKIPPED_COUNT: Int = 1

    val completions: List<ChallengeCompletion> by lazy { generateCompletions(today) }

    val profile: UserProfile by lazy {
        UserProfile(
            id = "local-user",
            displayName = "1% Human",
            createdAt = LocalDate.fromEpochDays(today.toEpochDays() - 64),
            interests = setOf(
                Category.HEALTH,
                Category.RELATIONSHIPS,
                Category.SELF_GROWTH,
                Category.LIFE_EXPERIENCE,
            ),
            level = 7,
            xpInLevel = 260,
        )
    }

    val settings: AppSettings = AppSettings()

    /** The hero challenge on Home — matches the design mockup. */
    val todayChallenge: Challenge
        get() = ChallengeSeed.byId("rel_thank_stranger") ?: ChallengeSeed.all.first()

    val quoteOfTheDay get() = QuoteSeed.forDayIndex(today.toEpochDays())

    /** Completion rate as a percentage, derived rather than hardcoded. */
    val completionRatePercent: Int
        get() {
            val total = completions.size + SKIPPED_COUNT
            return if (total == 0) 0 else (completions.size * 100) / total
        }

    val exploredCategoryCount: Int
        get() = completions
            .mapNotNull { ChallengeSeed.byId(it.challengeId)?.category }
            .distinct()
            .size

    fun completionsOn(date: LocalDate): List<ChallengeCompletion> =
        completions.filter { it.date == date }

    fun challengeFor(completion: ChallengeCompletion): Challenge? =
        ChallengeSeed.byId(completion.challengeId)

    private fun generateCompletions(today: LocalDate): List<ChallengeCompletion> {
        val random = Random(SEED)
        // Shuffle the category plan so the calendar reads as a natural mix
        // instead of long blocks of one category.
        val plan = completionCategoryPlan.shuffled(random)

        // Track a rotating index per category so we don't repeat the same
        // challenge over and over.
        val cursors = mutableMapOf<Category, Int>()

        return completionDayOffsets.mapIndexed { index, dayOffset ->
            val category = plan[index % plan.size]
            val pool = ChallengeSeed.byCategory(category)
            val cursor = cursors.getOrElse(category) { 0 }
            cursors[category] = cursor + 1
            val challenge = pool[cursor % pool.size]

            ChallengeCompletion(
                id = "completion_$index",
                challengeId = challenge.id,
                date = LocalDate.fromEpochDays(today.toEpochDays() - dayOffset),
                completedAt = LocalTime(
                    hour = random.nextInt(7, 22),
                    minute = random.nextInt(0, 60),
                ),
                xpEarned = challenge.rewardXp,
                humanityPercent = challenge.humanityPercent,
            )
        }
    }
}
