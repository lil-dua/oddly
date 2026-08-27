import Observation
import SharedLogic

/// In-memory application state, seeded from `SampleData`.
///
/// This stands in for the repository layer that arrives in Phase 2. Everything
/// is real, derived state — completing a challenge genuinely updates the streak,
/// calendar, statistics and XP — so the screens can be reviewed with live
/// behaviour rather than static mock values.
///
/// The Android app has the same class backed by Compose snapshot state; this is
/// the SwiftUI half, and the two stay deliberately parallel so the shared
/// domain layer remains the only place business rules live.
@Observable
final class OddlyAppState {

    struct Reward {
        let xp: Int
        let humanityPercent: Int
        let leveledUp: Bool
        let newLevel: Int
    }

    let today: LocalDate

    private(set) var completions: [ChallengeCompletion]
    private(set) var profile: UserProfile
    var settings: AppSettings
    var interests: Set<ChallengeCategory>

    /// The challenge shown on Home; stable for the whole day (spec §6.1).
    private(set) var todayChallenge: Challenge

    /// How many challenges were started and set aside — drives completion rate.
    private(set) var skippedCount: Int

    /// XP and Humanity gained by the most recent completion, for the reward screen.
    private(set) var lastReward: Reward?

    init() {
        let sample = SampleData.shared
        today = sample.today
        completions = sample.completions
        profile = sample.profile
        settings = sample.settings
        interests = sample.profile.interests
        todayChallenge = sample.todayChallenge
        skippedCount = Int(sample.SKIPPED_COUNT)
    }

    // MARK: - Derived state

    var streak: StreakInfo {
        StreakCalculator.shared.calculate(completions: completions, today: today)
    }

    var weekActivity: [DayActivity] {
        StreakCalculator.shared.recentActivity(completions: completions, today: today, days: 7)
    }

    var completedToday: Bool {
        completions.contains { $0.date == today }
    }

    var totalCompleted: Int { completions.count }

    var completionRatePercent: Int {
        let total = completions.count + skippedCount
        return total == 0 ? 0 : (completions.count * 100) / total
    }

    var exploredCategoryCount: Int {
        Set(completions.compactMap { ChallengeSeed.shared.byId(id: $0.challengeId)?.category }).count
    }

    var quoteOfTheDay: Quote {
        QuoteSeed.shared.forDayIndex(dayIndex: today.toEpochDays())
    }

    // MARK: - Lookups

    func completionsOn(_ date: LocalDate) -> [ChallengeCompletion] {
        completions
            .filter { $0.date == date }
            .sorted { $0.completedAt.toSecondOfDay() > $1.completedAt.toSecondOfDay() }
    }

    func challenge(of completion: ChallengeCompletion) -> Challenge? {
        ChallengeSeed.shared.byId(id: completion.challengeId)
    }

    /// Resolves completions to their challenges, dropping any whose challenge is
    /// no longer in the library.
    func resolved(_ source: [ChallengeCompletion]) -> [CompletionRef] {
        source.compactMap { completion in
            guard let challenge = challenge(of: completion) else { return nil }
            return CompletionRef(id: completion.id, completion: completion, challenge: challenge)
        }
    }

    func isCompleted(_ challengeId: String) -> Bool {
        completions.contains { $0.challengeId == challengeId }
    }

    func isCompletedToday(_ challengeId: String) -> Bool {
        completions.contains { $0.date == today && $0.challengeId == challengeId }
    }

    // MARK: - Mutations

    /// Record a completion and award XP, levelling up if the bar fills.
    func complete(_ challenge: Challenge, at time: LocalTime = LocalTime(hour: 9, minute: 41, second: 0, nanosecond: 0)) {
        guard !isCompletedToday(challenge.id) else { return }

        completions.append(
            ChallengeCompletion(
                id: "completion_\(challenge.id)_\(today.epochDays)",
                challengeId: challenge.id,
                date: today,
                completedAt: time,
                xpEarned: challenge.rewardXp,
                humanityPercent: challenge.humanityPercent,
                note: nil
            )
        )

        var level = profile.level
        var xp = profile.xpInLevel + challenge.rewardXp
        while xp >= UserProfile.companion.xpForLevel(level: level) {
            xp -= UserProfile.companion.xpForLevel(level: level)
            level += 1
        }
        let leveledUp = level > profile.level
        profile = profile.with(level: level, xpInLevel: xp)

        lastReward = Reward(
            xp: Int(challenge.rewardXp),
            humanityPercent: Int(challenge.humanityPercent),
            leveledUp: leveledUp,
            newLevel: Int(level)
        )
    }

    /// Swap today's challenge for another one (spec §2.3).
    @discardableResult
    func reroll(category: ChallengeCategory? = nil) -> Challenge? {
        let next = ChallengeSelector.shared.select(
            library: ChallengeSeed.shared.all,
            interests: interests,
            completions: completions,
            level: profile.level,
            today: today,
            restrictTo: category,
            excludeChallengeId: todayChallenge.id,
            random: KotlinRandom.Default.shared
        )
        guard let next else { return nil }
        skippedCount += 1
        todayChallenge = next
        return next
    }

    /// Make [challenge] the active one, e.g. after picking from the library.
    func chooseTodayChallenge(_ challenge: Challenge) {
        todayChallenge = challenge
    }

    func toggleInterest(_ category: ChallengeCategory) {
        if interests.contains(category) {
            interests.remove(category)
        } else {
            interests.insert(category)
        }
    }

    func clearReward() {
        lastReward = nil
    }

    /// Wipes local data (spec §S15). Used by the Reset action in Settings.
    func resetAllData() {
        completions.removeAll()
        profile = profile.with(level: 1, xpInLevel: 0)
        skippedCount = 0
        lastReward = nil
    }
}
