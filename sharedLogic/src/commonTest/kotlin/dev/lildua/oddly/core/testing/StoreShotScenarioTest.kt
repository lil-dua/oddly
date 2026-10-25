package dev.lildua.oddly.core.testing

import dev.lildua.oddly.data.local.InMemoryKeyValueStore
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.data.seed.PUBLISHED_CHALLENGE_CATEGORIES
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.DailyChallengeStatus
import dev.lildua.oddly.domain.usecase.CompletionOutcome
import dev.lildua.oddly.domain.usecase.DailyChallengeRules
import dev.lildua.oddly.domain.usecase.StreakCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StoreShotScenarioTest {
    private val now = LocalDateTime.parse(StoreShotScenario.NOW)
    private val today: LocalDate = now.date
    private fun state(language: AppLanguage = AppLanguage.VIETNAMESE) =
        StoreShotScenario.state(today, language)

    @Test
    fun `streak is 14 before today and 15 after completing the pick`() {
        val state = state()
        assertEquals(14, StreakCalculator.calculate(state.completions, today).current)
        val pick = checkNotNull(ChallengeSeed.byId(StoreShotScenario.TODAY_CHALLENGE_ID))
        val outcome = DailyChallengeRules.complete(state, pick, now)
        assertIs<CompletionOutcome.Completed>(outcome)
        assertEquals(15, StreakCalculator.calculate(outcome.state.completions, today).current)
    }

    @Test
    fun `covers all six categories with seventeen days and a gap on the fourth`() {
        val state = state()
        val categories = state.completions.map { checkNotNull(ChallengeSeed.byId(it.challengeId)).category }.toSet()
        assertEquals(Category.entries.toSet(), categories)
        assertEquals(17, state.completions.size)
        assertTrue(state.completions.none { it.date == LocalDate(2026, 11, 4) })
    }

    @Test
    fun `every challenge is published and none repeats`() {
        val ids = state().completions.map { it.challengeId }
        assertEquals(ids.size, ids.toSet().size)
        for (id in ids) {
            assertNotNull(ChallengeSeed.byId(id), id)
            assertTrue(id in PUBLISHED_CHALLENGE_CATEGORIES, id)
        }
    }

    @Test
    fun `completions are in the past and in the same month as today`() {
        for (c in state().completions) {
            assertTrue(c.date < today, c.id)
            assertEquals(today.month, c.date.month, c.id)
            assertEquals(today.year, c.date.year, c.id)
        }
    }

    @Test
    fun `profile equals the xp of the completions folded from level one`() {
        val state = state()
        var profile = OddlyRepository.freshState().profile
        for (c in state.completions) profile = profile.awardXp(c.xpEarned).profile
        assertEquals(profile.level, state.profile.level)
        assertEquals(profile.xpInLevel, state.profile.xpInLevel)
        assertEquals(Category.entries.toSet(), state.profile.interests)
    }

    @Test
    fun `today's pick is pending and not yet completed`() {
        val state = state()
        assertEquals(
            DailyChallenge(today, StoreShotScenario.TODAY_CHALLENGE_ID, DailyChallengeStatus.PENDING, 0),
            state.dailyChallenge,
        )
        assertTrue(state.completions.none { it.challengeId == StoreShotScenario.TODAY_CHALLENGE_ID })
    }

    @Test
    fun `language follows the parameter and onboarding is done`() {
        assertEquals(AppLanguage.VIETNAMESE, state(AppLanguage.VIETNAMESE).settings.language)
        assertEquals(AppLanguage.ENGLISH, state(AppLanguage.ENGLISH).settings.language)
        assertTrue(state().hasCompletedOnboarding)
    }

    @Test
    fun `state survives a repository round trip and keeps today's pick`() {
        val state = state()
        val repository = OddlyRepository(InMemoryKeyValueStore())
        repository.save(state)
        assertEquals(state, repository.load())
        assertEquals(state, OddlyRepository.resolveDailyChallenge(state, today).state)
    }
}
