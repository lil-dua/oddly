package dev.lildua.oddly.data.local

import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.UserProfile
import dev.lildua.oddly.domain.usecase.ChallengeSelector
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Local-first storage for everything the app remembers (spec §6).
 *
 * The saved document is JSON in a platform key-value store rather than a
 * relational database. The whole data set is one profile, one settings record
 * and a list of completions the app already loads into memory to derive the
 * streak, calendar and statistics from — there is nothing to query, so a schema
 * and a query engine would buy nothing and cost a build-plugin dependency.
 * kotlinx.serialization is what the spec already nominates for persistence and
 * export (§7), and using it for both means [exportJson] is the same code path.
 *
 * If the data ever outgrows this — a completion log spanning years, or queries
 * that should not walk the whole list — only this class changes: everything
 * above it takes a [PersistedState].
 */
class OddlyRepository(private val store: KeyValueStore) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }


    /** Returns the saved state, or null on a first launch or unreadable data. */
    fun load(): PersistedState? {
        val raw = store.read(KEY_STATE) ?: return null
        return decode(raw)
    }

    fun save(state: PersistedState) {
        store.write(KEY_STATE, json.encodeToString(PersistedState.serializer(), state))
    }

    fun clear() {
        store.remove(KEY_STATE)
    }

    /** The saved document, for the share sheet (spec §S22). */
    fun exportJson(): String =
        json.encodeToString(PersistedState.serializer(), load() ?: freshState())

    /**
     * Replaces the saved state from a previously exported document.
     *
     * Returns null and changes nothing if the text is not a document this
     * version understands — an import must never leave the app half-restored.
     */
    fun importJson(raw: String): PersistedState? {
        val imported = decode(raw) ?: return null
        save(imported)
        return imported
    }

    private fun decode(raw: String): PersistedState? = runCatching {
        val state = json.decodeFromString(PersistedState.serializer(), raw)
        // Refuse rather than silently load a document from a newer release.
        if (state.version > PersistedState.CURRENT_VERSION) null else migrate(state)
    }.getOrNull()

    /**
     * Brings an older document up to [PersistedState.CURRENT_VERSION], one
     * version at a time. Fields added since get their defaults on decode, so
     * a step only has to fix up values that need more than a default.
     */
    private fun migrate(state: PersistedState): PersistedState {
        var s = state
        // v2 added DailyChallenge.swapsUsed; an old pick is treated as unused.
        if (s.version < 2) s = s.copy(version = 2)
        return s
    }

    companion object {
        private const val KEY_STATE = "oddly.state.v1"

        /** The starting point for a brand-new install: level 1, nothing done. */
        fun freshState(
            today: LocalDate = LocalDate.fromEpochDays(0),
            interests: Set<Category> = emptySet(),
        ) = PersistedState(
            profile = UserProfile(
                id = "local-user",
                displayName = "1% Human",
                createdAt = today,
                interests = interests,
                level = 1,
                xpInLevel = 0,
            ),
            settings = AppSettings(),
        )

        /**
         * The challenge for [today], picking and recording a new one only when
         * the saved pick is from another day (spec §6.1).
         */
        fun resolveDailyChallenge(
            state: PersistedState,
            today: LocalDate,
            random: Random = Random.Default,
        ): DailyChallengeResolution {
            val saved = state.dailyChallenge
            if (saved != null && saved.date == today) {
                val challenge = ChallengeSeed.byId(saved.challengeId)
                if (challenge != null) return DailyChallengeResolution(challenge, state)
            }

            val picked = ChallengeSelector.select(
                library = ChallengeSeed.all,
                interests = state.profile.interests,
                completions = state.completions,
                level = state.profile.level,
                today = today,
                random = random,
            ) ?: ChallengeSeed.all.first()

            return DailyChallengeResolution(
                challenge = picked,
                state = state.copy(
                    dailyChallenge = DailyChallenge(date = today, challengeId = picked.id),
                ),
            )
        }

        /**
         * Moves the saved pick to [today] when the calendar has moved, and
         * returns null when nothing needs to change.
         *
         * Kept apart from [resolveDailyChallenge], which always answers with a
         * challenge: a running app has to know whether the day actually changed
         * so it can send the user back to Home. Going back in time (the clock
         * set to yesterday) counts as a change too; completions dated in the
         * "future" are left alone rather than deleted.
         */
        fun rollover(
            state: PersistedState,
            today: LocalDate,
            random: Random = Random.Default,
        ): DailyChallengeResolution? {
            val saved = state.dailyChallenge
            if (saved != null && saved.date == today && ChallengeSeed.byId(saved.challengeId) != null) {
                return null
            }
            return resolveDailyChallenge(state, today, random)
        }

        /**
         * Today's pick again, this time honouring interests chosen after it was
         * made. Returns null when the pick already fits and nothing should change.
         *
         * [resolveDailyChallenge] runs while the app loads, which on a first
         * launch is before onboarding has asked what the user cares about. Left
         * alone, someone who picks Finance is handed a Relationships challenge on
         * the one day that decides whether they come back at all.
         *
         * Called once, when onboarding finishes — not whenever interests change.
         * The daily challenge is meant to be settled for the day, and re-rolling
         * it from the Settings screen would turn the interest list into a free
         * reroll button.
         */
        fun realignToInterests(
            state: PersistedState,
            today: LocalDate,
            random: Random = Random.Default,
        ): DailyChallengeResolution? {
            val interests = state.profile.interests
            // Skipping the interest step is a valid answer: keep what they have.
            if (interests.isEmpty()) return null

            val current = state.dailyChallenge
                ?.takeIf { it.date == today }
                ?.let { ChallengeSeed.byId(it.challengeId) }
            if (current != null && current.category in interests) return null

            val picked = ChallengeSelector.select(
                library = ChallengeSeed.all,
                interests = interests,
                completions = state.completions,
                level = state.profile.level,
                today = today,
                random = random,
            ) ?: return null
            if (picked.id == current?.id) return null

            return DailyChallengeResolution(
                challenge = picked,
                state = state.copy(
                    dailyChallenge = DailyChallenge(date = today, challengeId = picked.id),
                ),
            )
        }
    }
}

/** Today's challenge, plus the state to save if the pick was newly made. */
data class DailyChallengeResolution(
    val challenge: Challenge,
    val state: PersistedState,
)
