import Observation
import SharedLogic

/// Application state, loaded from and written back to `OddlyRepository`.
///
/// Every mutation persists immediately rather than on a timer or at shutdown:
/// the writes are a few kilobytes of JSON, and a habit app that loses the
/// completion the user was just congratulated for is worse than a slow one.
///
/// Derived values — streak, statistics, category mix — are still computed from
/// the completion list on demand rather than stored, so there is only ever one
/// source of truth to keep consistent.
///
/// The Android app has the same class backed by Compose snapshot state; the two
/// stay deliberately parallel so the shared layer remains the only place the
/// rules live.
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
    private(set) var interests: Set<ChallengeCategory>

    var settings: AppSettings {
        didSet { persist() }
    }

    /// The challenge shown on Home; stable for the whole day (spec §6.1).
    private(set) var todayChallenge: Challenge

    /// Started but set aside — drives the completion-rate statistic.
    private(set) var skippedCount: Int

    private(set) var hasCompletedOnboarding: Bool

    /// XP and Humanity gained by the most recent completion, for the reward screen.
    private(set) var lastReward: Reward?

    private let repository: OddlyRepository
    private var dailyChallengeDate: LocalDate?

    init() {
        let repository = OddlyRepository(store: UserDefaultsStore())
        let today = Today.shared.local()

        let loaded = repository.load()
            ?? OddlyRepository.companion.freshState(today: today, interests: [])

        // Picking today's challenge is part of loading: it has to be settled and
        // written back before the first frame, or reopening the app mid-morning
        // would show a different challenge than it did at breakfast.
        let resolution = OddlyRepository.companion.resolveDailyChallenge(
            state: loaded,
            today: today,
            random: KotlinRandom.Default.shared
        )
        repository.save(state: resolution.state)

        self.repository = repository
        self.today = today
        self.completions = resolution.state.completions
        self.profile = resolution.state.profile
        self.settings = resolution.state.settings
        self.interests = resolution.state.profile.interests
        self.todayChallenge = resolution.challenge
        self.skippedCount = Int(resolution.state.skippedCount)
        self.hasCompletedOnboarding = resolution.state.hasCompletedOnboarding
        self.dailyChallengeDate = resolution.state.dailyChallenge?.date
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
        persist()
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
        dailyChallengeDate = today
        persist()
        return next
    }

    func toggleInterest(_ category: ChallengeCategory) {
        if interests.contains(category) {
            interests.remove(category)
        } else {
            interests.insert(category)
        }
        profile = profile.with(interests: interests)
        persist()
    }

    func completeOnboarding() {
        hasCompletedOnboarding = true
        // Today's challenge was picked while the app loaded, before onboarding
        // had asked what the user cares about. Give them one that matches.
        if let realigned = OddlyRepository.companion.realignToInterests(
            state: currentState(),
            today: today,
            random: KotlinRandom.Default.shared
        ) {
            todayChallenge = realigned.challenge
            dailyChallengeDate = today
        }
        persist()
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
        persist()
    }

    /// The saved document, for the export row in Settings (spec §S22).
    func exportJson() -> String { repository.exportJson() }

    private func currentState() -> PersistedState {
        let daily = dailyChallengeDate.map {
            DailyChallenge(date: $0, challengeId: todayChallenge.id, status: .pending)
        }
        return PersistedState(
            version: PersistedState.companion.CURRENT_VERSION,
            profile: profile.with(interests: interests),
            settings: settings,
            completions: completions,
            dailyChallenge: daily,
            skippedCount: Int32(skippedCount),
            hasCompletedOnboarding: hasCompletedOnboarding
        )
    }

    private func persist() {
        repository.save(state: currentState())
    }
}
