package dev.lildua.oddly.data.local

import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.ChallengeCompletion
import dev.lildua.oddly.domain.model.DailyChallenge
import dev.lildua.oddly.domain.model.UserProfile
import kotlinx.serialization.Serializable

/**
 * Everything that has to survive a relaunch.
 *
 * Deliberately *not* the seed content: challenges and quotes ship inside the
 * app, so a completion stores only the id it refers to. That keeps the saved
 * document tiny and means new content in a later release is picked up without
 * a migration.
 */
@Serializable
data class PersistedState(
    /**
     * Bumped whenever the shape changes, so a migration can branch on it.
     *
     * Rules for changing the shape: only add fields, and give each a default;
     * never rename or remove one. Every bump gets a migrate step in
     * [OddlyRepository] and a frozen fixture of the previous version in tests.
     */
    val version: Int = CURRENT_VERSION,
    val profile: UserProfile,
    val settings: AppSettings,
    val completions: List<ChallengeCompletion> = emptyList(),
    /** Today's pick, so reopening the app shows the same one (spec §6.1). */
    val dailyChallenge: DailyChallenge? = null,
    /** Started but set aside — drives the completion-rate statistic. */
    val skippedCount: Int = 0,
    /** Onboarding runs once; after that the splash goes straight to Home. */
    val hasCompletedOnboarding: Boolean = false,
) {
    companion object {
        const val CURRENT_VERSION: Int = 2
    }
}
