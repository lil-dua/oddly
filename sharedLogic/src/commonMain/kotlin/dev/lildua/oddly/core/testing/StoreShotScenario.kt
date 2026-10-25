package dev.lildua.oddly.core.testing

import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.DailyChallengeStatus
import dev.lildua.oddly.domain.model.ThemeMode
import dev.lildua.oddly.domain.usecase.DailyChallengeRules
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * The saved state behind the store screenshots: about two weeks of completions
 * ending yesterday, today's pick still pending, so the shots show a lived-in app.
 *
 * It lives in commonMain, next to [UiTestTags], so Android and iOS seed the exact
 * same data instead of keeping two copies that drift. Only the screenshot test and
 * the DEBUG-only iOS hook call it; nothing in a release path does, so it is inert
 * there (R8 drops it on Android).
 *
 * Completions are placed relative to `today`, so the scenario holds for any pinned
 * date; the store shots pin [NOW]. Do not change an id below unless it leaves the
 * seed: the unit test fails if a listed id is no longer published.
 */
object StoreShotScenario {
    /** The pinned clock for the shots; 9:41 matches the status bar override. */
    const val NOW = "2026-11-19T09:41:00"

    /** Matches the store description ("Message a friend you haven't seen in ages"). */
    const val TODAY_CHALLENGE_ID = "rel_old_friend"

    /** One finished day: how many days before today, which challenge, and when. */
    internal class Entry(val daysAgo: Int, val challengeId: String, val hour: Int, val minute: Int)

    /**
     * 17 days, all six categories, mixed difficulty. The gap at 15 days ago is
     * deliberate so the calendar is not a solid block; the run since is 14 days.
     */
    internal val entries: List<Entry> = listOf(
        Entry(18, "health_water_2l", 7, 30),
        Entry(17, "rel_compliment", 12, 10),
        Entry(16, "growth_read_10_pages", 21, 30),
        Entry(14, "create_doodle", 19, 15),
        Entry(13, "fin_check_subscriptions", 8, 20),
        Entry(12, "life_new_route", 18, 40),
        Entry(11, "health_walk_10min", 13, 5),
        Entry(10, "rel_call_family", 20, 15),
        Entry(9, "growth_journal_3_lines", 22, 0),
        Entry(8, "create_3_sentence_story", 16, 45),
        Entry(7, "fin_skip_purchase", 11, 30),
        Entry(6, "life_sunrise", 7, 5),
        Entry(5, "health_stretch_3min", 9, 10),
        Entry(4, "rel_thank_note", 14, 25),
        Entry(3, "growth_single_task", 10, 50),
        Entry(2, "life_visit_local", 17, 20),
        Entry(1, "create_finish_something_rough", 19, 55),
    )

    fun state(today: LocalDate, language: AppLanguage): PersistedState {
        val base = OddlyRepository.freshState(
            today = LocalDate.fromEpochDays(today.toEpochDays() - FIRST_DAY_AGO),
            interests = Category.entries.toSet(),
        )
        var profile = base.profile
        val completions = entries.map { entry ->
            val challenge = checkNotNull(ChallengeSeed.byId(entry.challengeId)) { entry.challengeId }
            val date = LocalDate.fromEpochDays(today.toEpochDays() - entry.daysAgo)
            profile = profile.awardXp(challenge.rewardXp).profile
            ChallengeCompletion(
                id = DailyChallengeRules.completionId(challenge.id, date),
                challengeId = challenge.id,
                date = date,
                completedAt = LocalTime(entry.hour, entry.minute),
                xpEarned = challenge.rewardXp,
                humanityPercent = challenge.humanityPercent,
            )
        }
        return base.copy(
            profile = profile,
            settings = AppSettings(
                themeMode = ThemeMode.DARK,
                reminderEnabled = true,
                reminderTime = LocalTime(20, 0),
                language = language,
            ),
            completions = completions,
            dailyChallenge = DailyChallenge(
                date = today,
                challengeId = TODAY_CHALLENGE_ID,
                status = DailyChallengeStatus.PENDING,
                swapsUsed = 0,
            ),
            hasCompletedOnboarding = true,
        )
    }

    private const val FIRST_DAY_AGO = 18
}
