package dev.lildua.oddly.data.local

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.completionsOn
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.ThemeMode
import kotlinx.datetime.LocalTime
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OddlyRepositoryTest {

    private fun repository() = OddlyRepository(InMemoryKeyValueStore())

    // MARK: - Round trip

    @Test
    fun `a first launch has nothing saved`() {
        assertNull(repository().load())
    }

    @Test
    fun `saving then loading returns the same state`() {
        val repository = repository()
        val saved = OddlyRepository.freshState(TODAY, setOf(Category.HEALTH, Category.FINANCE)).copy(
            completions = completionsOn(0, 1, 2),
            skippedCount = 4,
            hasCompletedOnboarding = true,
        )

        repository.save(saved)

        assertEquals(saved, repository.load())
    }

    @Test
    fun `settings survive the round trip`() {
        val repository = repository()
        val fresh = OddlyRepository.freshState(TODAY)
        val saved = fresh.copy(
            settings = fresh.settings.copy(
                themeMode = ThemeMode.LIGHT,
                language = AppLanguage.ENGLISH,
                reminderTime = LocalTime(21, 15),
                reminderEnabled = false,
            ),
        )

        repository.save(saved)
        val loaded = repository.load()

        assertNotNull(loaded)
        assertEquals(ThemeMode.LIGHT, loaded.settings.themeMode)
        assertEquals(AppLanguage.ENGLISH, loaded.settings.language)
        assertEquals(LocalTime(21, 15), loaded.settings.reminderTime)
        assertEquals(false, loaded.settings.reminderEnabled)
    }

    @Test
    fun `clearing removes the saved state`() {
        val repository = repository()
        repository.save(OddlyRepository.freshState(TODAY))

        repository.clear()

        assertNull(repository.load())
    }

    @Test
    fun `a fresh state starts at level one with nothing done`() {
        val fresh = OddlyRepository.freshState(TODAY)

        assertEquals(1, fresh.profile.level)
        assertEquals(0, fresh.profile.xpInLevel)
        assertEquals(TODAY, fresh.profile.createdAt)
        assertTrue(fresh.completions.isEmpty())
        assertEquals(false, fresh.hasCompletedOnboarding)
        assertEquals(PersistedState.CURRENT_VERSION, fresh.version)
    }

    // MARK: - Export and import

    @Test
    fun `exporting with nothing saved still produces a valid document`() {
        val repository = repository()

        val restored = repository.importJson(repository.exportJson())

        assertNotNull(restored)
        assertEquals(1, restored.profile.level)
    }

    @Test
    fun `an exported document imports back into the same state`() {
        val source = repository()
        val saved = OddlyRepository.freshState(TODAY, setOf(Category.CREATIVITY)).copy(
            completions = completionsOn(0, 2, 5),
            skippedCount = 2,
            hasCompletedOnboarding = true,
        )
        source.save(saved)

        val destination = repository()
        val imported = destination.importJson(source.exportJson())

        assertEquals(saved, imported)
        assertEquals(saved, destination.load())
    }

    @Test
    fun `importing nonsense changes nothing`() {
        val repository = repository()
        val original = OddlyRepository.freshState(TODAY).copy(skippedCount = 9)
        repository.save(original)

        assertNull(repository.importJson("this is not a backup"))
        assertEquals(original, repository.load())
    }

    @Test
    fun `importing a truncated document changes nothing`() {
        val repository = repository()
        val original = OddlyRepository.freshState(TODAY)
        repository.save(original)
        val halfADocument = repository.exportJson().take(40)

        assertNull(repository.importJson(halfADocument))
        assertEquals(original, repository.load())
    }

    /**
     * A document written by a newer release may mean something different. It is
     * better to start clean than to load it as if the fields still matched.
     */
    @Test
    fun `a document from a future version is refused`() {
        val repository = repository()

        repository.save(OddlyRepository.freshState(TODAY).copy(version = PersistedState.CURRENT_VERSION + 1))

        assertNull(repository.load())
    }

    /** The other direction: a field this release has never heard of is ignored. */
    @Test
    fun `unknown fields do not break loading`() {
        val repository = repository()
        val withExtra = OddlyRepository
            .freshState(TODAY)
            .let { repository.save(it); repository.exportJson() }
            .replaceFirst("{", "{\"fieldFromALaterRelease\": 42,")

        assertNotNull(repository.importJson(withExtra))
    }

    // MARK: - Daily challenge

    @Test
    fun `today's pick is kept when the app is reopened`() {
        val picked = ChallengeSeed.all[5]
        val state = OddlyRepository.freshState(TODAY).copy(
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = picked.id),
        )

        val resolution = OddlyRepository.resolveDailyChallenge(state, TODAY, Random(1))

        assertEquals(picked, resolution.challenge)
        // Nothing was chosen, so nothing needs rewriting.
        assertSame(state, resolution.state)
    }

    @Test
    fun `yesterday's pick is replaced and the new one recorded`() {
        val yesterdays = ChallengeSeed.all[5]
        val state = OddlyRepository.freshState(daysAgo(1)).copy(
            dailyChallenge = DailyChallenge(date = daysAgo(1), challengeId = yesterdays.id),
        )

        val resolution = OddlyRepository.resolveDailyChallenge(state, TODAY, Random(1))

        assertNotEquals(yesterdays.id, resolution.challenge.id)
        assertEquals(TODAY, resolution.state.dailyChallenge?.date)
        assertEquals(resolution.challenge.id, resolution.state.dailyChallenge?.challengeId)
    }

    /** Content removed in a later release must not leave Home with no challenge. */
    @Test
    fun `a saved pick that no longer exists is replaced`() {
        val state = OddlyRepository.freshState(TODAY).copy(
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = "cut-from-the-library"),
        )

        val resolution = OddlyRepository.resolveDailyChallenge(state, TODAY, Random(1))

        assertNotNull(ChallengeSeed.byId(resolution.challenge.id))
        assertEquals(resolution.challenge.id, resolution.state.dailyChallenge?.challengeId)
    }

    @Test
    fun `a first launch gets a challenge from the chosen interests`() {
        val state = OddlyRepository.freshState(TODAY, setOf(Category.FINANCE))

        val resolution = OddlyRepository.resolveDailyChallenge(state, TODAY, Random(1))

        assertEquals(Category.FINANCE, resolution.challenge.category)
    }

    @Test
    fun `resolving twice on the same day is stable`() {
        val first = OddlyRepository.resolveDailyChallenge(
            OddlyRepository.freshState(TODAY),
            TODAY,
            Random(TODAY.toEpochDays()),
        )
        val second = OddlyRepository.resolveDailyChallenge(
            first.state,
            TODAY,
            Random(TODAY.toEpochDays()),
        )

        assertEquals(first.challenge, second.challenge)
    }

    // MARK: - Realigning after onboarding

    /**
     * The first-run order is load, then onboarding: today's pick is already made
     * by the time the user says what they care about.
     */
    @Test
    fun `the first challenge is realigned to the interests just chosen`() {
        val beforeOnboarding = OddlyRepository.resolveDailyChallenge(
            OddlyRepository.freshState(TODAY),
            TODAY,
            Random(1),
        ).state
        val afterChoosing = beforeOnboarding.copy(
            profile = beforeOnboarding.profile.copy(interests = setOf(Category.FINANCE)),
        )

        val realigned = OddlyRepository.realignToInterests(afterChoosing, TODAY, Random(1))

        assertNotNull(realigned)
        assertEquals(Category.FINANCE, realigned.challenge.category)
        assertEquals(realigned.challenge.id, realigned.state.dailyChallenge?.challengeId)
    }

    @Test
    fun `a pick that already fits the interests is left alone`() {
        val finance = ChallengeSeed.all.first { it.category == Category.FINANCE }
        val state = OddlyRepository.freshState(TODAY, setOf(Category.FINANCE)).copy(
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = finance.id),
        )

        assertNull(OddlyRepository.realignToInterests(state, TODAY, Random(1)))
    }

    /** Skipping the interest step is an answer, not an absence of one. */
    @Test
    fun `no interests means the challenge stays as it is`() {
        val state = OddlyRepository.resolveDailyChallenge(
            OddlyRepository.freshState(TODAY),
            TODAY,
            Random(1),
        ).state

        assertNull(OddlyRepository.realignToInterests(state, TODAY, Random(1)))
    }

    @Test
    fun `realigning does not count as a skip`() {
        val state = OddlyRepository
            .resolveDailyChallenge(OddlyRepository.freshState(TODAY), TODAY, Random(1))
            .state
            .let { it.copy(profile = it.profile.copy(interests = setOf(Category.CREATIVITY))) }

        val realigned = OddlyRepository.realignToInterests(state, TODAY, Random(1))

        assertNotNull(realigned)
        assertEquals(state.skippedCount, realigned.state.skippedCount)
    }
}
