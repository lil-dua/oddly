package dev.lildua.oddly.data.local

import dev.lildua.oddly.TODAY
import dev.lildua.oddly.completionsOn
import dev.lildua.oddly.daysAgo
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.ThemeMode
import dev.lildua.oddly.domain.usecase.DailyChallengeRules
import kotlinx.datetime.LocalDate
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

    /** Pins [V2_DOCUMENT] to what the code reads today; the fixture itself never changes. */
    @Test
    fun `a version 2 document loads with every field intact`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V2_DOCUMENT)

        val loaded = assertNotNull(OddlyRepository(store).load())

        assertEquals("local-user", loaded.profile.id)
        assertEquals(setOf(Category.SELF_GROWTH, Category.CREATIVITY), loaded.profile.interests)
        assertEquals(4, loaded.profile.level)
        assertEquals(15, loaded.profile.xpInLevel)
        assertEquals(ThemeMode.SYSTEM, loaded.settings.themeMode)
        assertEquals(AppLanguage.VIETNAMESE, loaded.settings.language)
        assertEquals(true, loaded.settings.reminderEnabled)
        assertEquals(LocalTime(7, 30), loaded.settings.reminderTime)
        assertEquals(3, loaded.completions.size)
        assertEquals("Drew a cat", loaded.completions[0].note)
        assertNull(loaded.completions[1].note)
        assertEquals(listOf(daysAgo(3), daysAgo(2), daysAgo(1)), loaded.completions.map { it.date })
        assertEquals("create_new_playlist", loaded.dailyChallenge?.challengeId)
        assertEquals(TODAY, loaded.dailyChallenge?.date)
        assertEquals(2, loaded.dailyChallenge?.swapsUsed)
        assertEquals(4, loaded.skippedCount)
        assertTrue(loaded.hasCompletedOnboarding)
    }

    /** Pins [V1_DOCUMENT] to what the code reads today; the fixture itself never changes. */
    @Test
    fun `a version 1 document loads with every field intact`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V1_DOCUMENT)

        val loaded = assertNotNull(OddlyRepository(store).load())

        assertEquals("local-user", loaded.profile.id)
        assertEquals(setOf(Category.HEALTH, Category.FINANCE), loaded.profile.interests)
        assertEquals(3, loaded.profile.level)
        assertEquals(40, loaded.profile.xpInLevel)
        assertEquals(ThemeMode.LIGHT, loaded.settings.themeMode)
        assertEquals(AppLanguage.ENGLISH, loaded.settings.language)
        assertEquals(false, loaded.settings.reminderEnabled)
        assertEquals(LocalTime(21, 15), loaded.settings.reminderTime)
        assertEquals(3, loaded.completions.size)
        assertEquals(LocalTime(8, 30), loaded.completions.first().completedAt)
        assertEquals("health_stairs", loaded.dailyChallenge?.challengeId)
        assertEquals(TODAY, loaded.dailyChallenge?.date)
        assertEquals(5, loaded.skippedCount)
        assertTrue(loaded.hasCompletedOnboarding)
    }

    @Test
    fun `a version 1 document loads as the current version`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V1_DOCUMENT)

        val loaded = assertNotNull(OddlyRepository(store).load())

        assertEquals(PersistedState.CURRENT_VERSION, loaded.version)
        assertEquals(0, loaded.dailyChallenge?.swapsUsed)
        assertEquals(5, loaded.skippedCount)
        assertEquals(3, loaded.completions.size)
        assertTrue(loaded.hasCompletedOnboarding)
    }

    @Test
    fun `a version 1 document can be imported`() {
        val repository = repository()

        val imported = assertNotNull(repository.importJson(V1_DOCUMENT))

        assertEquals(PersistedState.CURRENT_VERSION, imported.version)
        assertEquals(0, imported.dailyChallenge?.swapsUsed)
        assertEquals("health_stairs", imported.dailyChallenge?.challengeId)
        assertEquals(imported, repository.load())
    }

    @Test
    fun `a migrated document is saved as the current version`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V1_DOCUMENT)
        val repository = OddlyRepository(store)

        repository.save(assertNotNull(repository.load()))

        val raw = assertNotNull(store.read("oddly.state.v1"))
        assertTrue(raw.contains("\"version\": 3"), raw)
        assertTrue(raw.contains("\"swapsUsed\""), raw)
        assertEquals(3, assertNotNull(repository.load()).version)
    }

    @Test
    fun `swaps used survive the round trip`() {
        val repository = repository()
        val saved = OddlyRepository.freshState(TODAY).copy(
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = "health_stairs", swapsUsed = 2),
        )

        repository.save(saved)

        assertEquals(2, repository.load()?.dailyChallenge?.swapsUsed)
        assertEquals(saved, repository.importJson(repository.exportJson()))
    }

    @Test
    fun `a version 2 document loads as the current version`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V2_DOCUMENT)

        val loaded = assertNotNull(OddlyRepository(store).load())

        assertEquals(PersistedState.CURRENT_VERSION, loaded.version)
        assertEquals(3, loaded.version)
        assertTrue(loaded.favoriteQuoteIds.isEmpty())
        assertEquals(2, loaded.dailyChallenge?.swapsUsed)
        assertEquals(3, loaded.completions.size)
    }

    @Test
    fun `a version 2 document can be imported`() {
        val repository = repository()

        val imported = assertNotNull(repository.importJson(V2_DOCUMENT))

        assertEquals(PersistedState.CURRENT_VERSION, imported.version)
        assertTrue(imported.favoriteQuoteIds.isEmpty())
        assertEquals(2, imported.dailyChallenge?.swapsUsed)
        assertEquals(imported, repository.load())
    }

    @Test
    fun `a version 1 document migrates with no favorites`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V1_DOCUMENT)

        val loaded = assertNotNull(OddlyRepository(store).load())

        assertTrue(loaded.favoriteQuoteIds.isEmpty())
        assertEquals(0, loaded.dailyChallenge?.swapsUsed)
        assertEquals(PersistedState.CURRENT_VERSION, loaded.version)
    }

    @Test
    fun `a migrated version 2 document is saved as the current version`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V2_DOCUMENT)
        val repository = OddlyRepository(store)

        repository.save(assertNotNull(repository.load()))

        val raw = assertNotNull(store.read("oddly.state.v1"))
        assertTrue(raw.contains("\"version\": 3"), raw)
        assertTrue(raw.contains("\"favoriteQuoteIds\""), raw)
        assertEquals(2, assertNotNull(repository.load()).dailyChallenge?.swapsUsed)
    }

    @Test
    fun `favorite quotes survive the round trip`() {
        val repository = repository()
        val saved = OddlyRepository.freshState(TODAY).copy(
            favoriteQuoteIds = setOf("q_ziglar_start", "q_tree_time"),
        )

        repository.save(saved)

        assertEquals(saved, repository.load())
        assertEquals(saved, repository.importJson(repository.exportJson()))
        assertTrue(repository.exportJson().contains("q_tree_time"))
    }

    @Test
    fun `favorites with ids missing from the seed survive the round trip`() {
        val repository = repository()
        val saved = OddlyRepository.freshState(TODAY).copy(favoriteQuoteIds = setOf("q_gone_from_seed"))

        repository.save(saved)

        assertEquals(setOf("q_gone_from_seed"), repository.load()?.favoriteQuoteIds)
    }

    @Test
    fun `clearing the repository forgets favorites`() {
        val repository = repository()
        repository.save(OddlyRepository.freshState(TODAY).copy(favoriteQuoteIds = setOf("q_tree_time")))

        repository.clear()

        assertNull(repository.load())
    }

    @Test
    fun `a version 4 document is still refused`() {
        val store = InMemoryKeyValueStore()
        store.write("oddly.state.v1", V1_DOCUMENT.replace("\"version\": 1", "\"version\": 4"))

        assertNull(OddlyRepository(store).load())
        assertNull(repository().importJson(V1_DOCUMENT.replace("\"version\": 1", "\"version\": 4")))
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

    // MARK: - Rollover

    private fun stateWithPick(date: kotlinx.datetime.LocalDate, id: String) =
        OddlyRepository.freshState(date).copy(
            dailyChallenge = DailyChallenge(date = date, challengeId = id),
        )

    @Test
    fun `rollover on the same day returns null`() {
        val state = stateWithPick(TODAY, ChallengeSeed.all[5].id)

        assertNull(OddlyRepository.rollover(state, TODAY, Random(1)))
    }

    @Test
    fun `rollover to a new day picks a new challenge and keeps completions`() {
        val base = stateWithPick(daysAgo(1), ChallengeSeed.all[5].id)
        val state = base.copy(
            completions = completionsOn(1, 2, 3),
            skippedCount = 4,
            profile = base.profile.copy(level = 3, xpInLevel = 40, interests = setOf(Category.HEALTH)),
        )

        val resolution = OddlyRepository.rollover(state, TODAY, Random(1))

        assertNotNull(resolution)
        assertEquals(TODAY, resolution.state.dailyChallenge?.date)
        assertEquals(resolution.challenge.id, resolution.state.dailyChallenge?.challengeId)
        assertEquals(state.completions, resolution.state.completions)
        assertEquals(state.profile, resolution.state.profile)
        assertEquals(4, resolution.state.skippedCount)
    }

    @Test
    fun `rollover to a new day resets swaps used`() {
        val state = stateWithPick(daysAgo(1), ChallengeSeed.all[5].id).copy(
            dailyChallenge = DailyChallenge(daysAgo(1), ChallengeSeed.all[5].id, swapsUsed = 3),
            skippedCount = 4,
        )

        val resolution = assertNotNull(OddlyRepository.rollover(state, TODAY, Random(1)))

        assertEquals(0, resolution.state.dailyChallenge?.swapsUsed)
        assertEquals(3, DailyChallengeRules.swapsLeft(resolution.state, TODAY))
        assertEquals(4, resolution.state.skippedCount)
    }

    @Test
    fun `rollover does not hand back a challenge just completed`() {
        val doneYesterday = ChallengeSeed.all[5]
        val base = stateWithPick(daysAgo(1), doneYesterday.id)
        val state = base.copy(
            completions = listOf(
                dev.lildua.oddly.completion(daysAgo(1), challengeId = doneYesterday.id),
            ),
        )

        for (seed in 0 until 50) {
            val resolution = OddlyRepository.rollover(state, TODAY, Random(seed))
            assertNotNull(resolution)
            assertNotEquals(doneYesterday.id, resolution.challenge.id, "seed $seed")
        }
    }

    @Test
    fun `rollover to an earlier day re-resolves`() {
        val state = stateWithPick(daysAgo(-1), ChallengeSeed.all[5].id)

        val resolution = OddlyRepository.rollover(state, TODAY, Random(1))

        assertNotNull(resolution)
        assertEquals(TODAY, resolution.state.dailyChallenge?.date)
    }

    @Test
    fun `rollover replaces a pick that is no longer in the library`() {
        val state = stateWithPick(TODAY, "cut-from-the-library")

        val resolution = OddlyRepository.rollover(state, TODAY, Random(1))

        assertNotNull(resolution)
        assertNotNull(ChallengeSeed.byId(resolution.challenge.id))
    }

    @Test
    fun `rollover with no saved pick picks one`() {
        val state = OddlyRepository.freshState(TODAY).copy(dailyChallenge = null)

        val resolution = OddlyRepository.rollover(state, TODAY, Random(1))

        assertNotNull(resolution)
        assertEquals(TODAY, resolution.state.dailyChallenge?.date)
    }

    // MARK: - Reading and restoring a backup

    private val backupSource = OddlyRepository.freshState(daysAgo(30), setOf(Category.HEALTH, Category.CREATIVITY)).copy(
        profile = OddlyRepository.freshState(daysAgo(30), setOf(Category.HEALTH, Category.CREATIVITY)).profile.copy(
            level = 4,
            xpInLevel = 25,
        ),
        settings = AppSettings(
            themeMode = ThemeMode.LIGHT,
            reminderEnabled = false,
            reminderTime = LocalTime(7, 30),
            language = AppLanguage.ENGLISH,
            soundEnabled = false,
            hapticsEnabled = false,
        ),
        completions = completionsOn(1, 2, 3, 4, 5),
        skippedCount = 7,
        hasCompletedOnboarding = true,
        favoriteQuoteIds = setOf("q1", "q2"),
    )

    private fun exportOf(state: PersistedState): String {
        val source = repository()
        source.save(state)
        return source.exportJson()
    }

    @Test
    fun `reading a backup does not write to the store`() {
        val store = InMemoryKeyValueStore()
        val repository = OddlyRepository(store)
        val current = OddlyRepository.freshState(TODAY).copy(skippedCount = 1)
        repository.save(current)

        val read = repository.readBackup(exportOf(backupSource))

        assertEquals(backupSource, read)
        assertEquals(current, repository.load())
    }

    @Test
    fun `reading a backup into an empty store leaves it empty`() {
        val repository = repository()

        assertNotNull(repository.readBackup(exportOf(backupSource)))

        assertNull(repository.load())
    }

    @Test
    fun `reading nonsense returns null`() {
        assertNull(repository().readBackup("not json at all"))
        assertNull(repository().readBackup(""))
        assertNull(repository().readBackup("{}"))
    }

    @Test
    fun `reading a truncated backup returns null`() {
        val text = exportOf(backupSource)

        assertNull(repository().readBackup(text.take(text.length / 2)))
    }

    @Test
    fun `reading a backup from a newer version returns null`() {
        val text = V2_DOCUMENT.replace("\"version\": 2", "\"version\": ${PersistedState.CURRENT_VERSION + 1}")

        assertNull(repository().readBackup(text))
    }

    @Test
    fun `reading a version 1 backup migrates it`() {
        val read = assertNotNull(repository().readBackup(V1_DOCUMENT))

        assertEquals(PersistedState.CURRENT_VERSION, read.version)
        assertTrue(read.favoriteQuoteIds.isEmpty())
        assertEquals(3, read.completions.size)
    }

    @Test
    fun `reading a version 2 backup migrates it`() {
        val read = assertNotNull(repository().readBackup(V2_DOCUMENT))

        assertEquals(PersistedState.CURRENT_VERSION, read.version)
        assertTrue(read.favoriteQuoteIds.isEmpty())
        assertEquals(2, read.dailyChallenge?.swapsUsed)
    }

    @Test
    fun `reading a version 1 backup writes nothing`() {
        val store = InMemoryKeyValueStore()

        OddlyRepository(store).readBackup(V1_DOCUMENT)

        assertNull(store.read("oddly.state.v1"))
    }

    @Test
    fun `the backup size limit is five megabytes`() {
        assertEquals(5 * 1024 * 1024, OddlyRepository.MAX_BACKUP_BYTES)
    }

    @Test
    fun `restoring a backup from an earlier day picks today's challenge`() {
        val old = backupSource.copy(
            dailyChallenge = DailyChallenge(date = daysAgo(3), challengeId = "health_stairs", swapsUsed = 2),
        )

        val restored = OddlyRepository.restoreFromBackup(old, TODAY, Random(1)).state

        assertEquals(TODAY, restored.dailyChallenge?.date)
        assertEquals(0, restored.dailyChallenge?.swapsUsed)
        assertNotNull(ChallengeSeed.byId(assertNotNull(restored.dailyChallenge).challengeId))
        assertEquals(old.copy(dailyChallenge = restored.dailyChallenge), restored)
    }

    @Test
    fun `restoring a backup from a future day also picks today's challenge`() {
        val future = backupSource.copy(
            dailyChallenge = DailyChallenge(date = daysAgo(-5), challengeId = "health_stairs", swapsUsed = 1),
        )

        val restored = OddlyRepository.restoreFromBackup(future, TODAY, Random(1)).state

        assertEquals(TODAY, restored.dailyChallenge?.date)
        assertEquals(0, restored.dailyChallenge?.swapsUsed)
    }

    @Test
    fun `restoring a backup with no pick picks one for today`() {
        val restored = OddlyRepository.restoreFromBackup(backupSource.copy(dailyChallenge = null), TODAY, Random(1))

        assertEquals(TODAY, restored.state.dailyChallenge?.date)
        assertEquals(restored.challenge.id, restored.state.dailyChallenge?.challengeId)
    }

    @Test
    fun `restoring a backup from today keeps its pick and swaps`() {
        val pick = ChallengeSeed.all.first().id
        val sameDay = backupSource.copy(
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = pick, swapsUsed = 2),
        )

        val resolution = OddlyRepository.restoreFromBackup(sameDay, TODAY, Random(1))

        assertEquals(pick, resolution.challenge.id)
        assertEquals(sameDay, resolution.state)
        assertEquals(2, resolution.state.dailyChallenge?.swapsUsed)
    }

    @Test
    fun `restoring marks onboarding as done`() {
        val midOnboarding = backupSource.copy(hasCompletedOnboarding = false)

        val restored = OddlyRepository.restoreFromBackup(midOnboarding, TODAY, Random(1)).state

        assertTrue(restored.hasCompletedOnboarding)
    }

    @Test
    fun `restoring a backup whose pick left the library picks another`() {
        val stale = backupSource.copy(
            dailyChallenge = DailyChallenge(date = TODAY, challengeId = "no_such_challenge", swapsUsed = 2),
        )

        val restored = OddlyRepository.restoreFromBackup(stale, TODAY, Random(1)).state

        assertNotNull(ChallengeSeed.byId(assertNotNull(restored.dailyChallenge).challengeId))
        assertEquals(TODAY, restored.dailyChallenge.date)
    }

    @Test
    fun `restoring keeps favorites settings interests and history`() {
        val restored = OddlyRepository.restoreFromBackup(backupSource, TODAY, Random(1)).state

        assertEquals(backupSource.favoriteQuoteIds, restored.favoriteQuoteIds)
        assertEquals(backupSource.settings, restored.settings)
        assertEquals(backupSource.profile, restored.profile)
        assertEquals(backupSource.completions, restored.completions)
        assertEquals(backupSource.skippedCount, restored.skippedCount)
    }

    @Test
    fun `importing then restoring keeps favorites and settings`() {
        val destination = repository()
        destination.save(OddlyRepository.freshState(TODAY))

        val imported = assertNotNull(destination.importJson(exportOf(backupSource)))
        val restored = OddlyRepository.restoreFromBackup(imported, TODAY, Random(1)).state

        assertEquals(backupSource.favoriteQuoteIds, restored.favoriteQuoteIds)
        assertEquals(backupSource.settings, restored.settings)
        assertEquals(backupSource.profile.interests, restored.profile.interests)
        assertEquals(backupSource.completions, restored.completions)
    }

    @Test
    fun `a failed import leaves the saved state untouched`() {
        val repository = repository()
        val current = OddlyRepository.freshState(TODAY).copy(skippedCount = 3)
        repository.save(current)

        assertNull(repository.importJson("{}"))
        assertNull(repository.importJson(V2_DOCUMENT.replace("\"version\": 2", "\"version\": 99")))

        assertEquals(current, repository.load())
    }

    @Test
    fun `restoring a version 2 backup from an earlier day resets swaps`() {
        val v2 = assertNotNull(repository().readBackup(V2_DOCUMENT))
        val later = LocalDate.fromEpochDays(TODAY.toEpochDays() + 4)

        val restored = OddlyRepository.restoreFromBackup(v2, later, Random(1)).state

        assertEquals(later, restored.dailyChallenge?.date)
        assertEquals(0, restored.dailyChallenge?.swapsUsed)
        assertEquals(v2.completions, restored.completions)
    }
}
