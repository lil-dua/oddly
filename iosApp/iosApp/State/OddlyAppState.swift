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

    /// The calendar day every screen shows. Moves only through `refreshDay`.
    private(set) var today: LocalDate

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

    /// One-off banner for Home after the day changed under a running app.
    private(set) var dayChangeNotice: DayChangeNotice?

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
        DailyChallengeRules.shared.isDayDone(completions: completions, today: today)
    }

    /// False once today is done: no more swapping. Also the condition for `adopt`.
    var canReroll: Bool { !completedToday }

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

    func availability(_ challengeId: String) -> ChallengeAvailability {
        DailyChallengeRules.shared.availability(state: currentState(), challengeId: challengeId, today: today)
    }

    // MARK: - Mutations

    /// Moves the app to `date` (default: the clock's today) if the calendar has
    /// changed since the state was built. Returns true when the day changed, so
    /// the caller can stop whatever it was doing for the old day.
    ///
    /// Same-day calls are no-ops and write nothing, since this runs on every
    /// foreground. Going back in time counts too; see `OddlyRepository.rollover`.
    @discardableResult
    func refreshDay(_ date: LocalDate = Today.shared.local()) -> Bool {
        let dayChanged = date != today
        guard let resolution = OddlyRepository.companion.rollover(
            state: currentState(),
            today: date,
            random: KotlinRandom.Default.shared
        ) else { return false }

        today = date
        absorb(resolution.state)
        todayChallenge = resolution.challenge
        if dayChanged {
            lastReward = nil
            dayChangeNotice = .theNewDay
        }
        persist()
        return dayChanged
    }

    /// Record a completion and award XP, levelling up if the bar fills.
    ///
    /// Returns true when it was recorded. False means nothing was awarded: either
    /// the day had changed (the state has already moved on to the new day and
    /// Home explains why), today already has a completion, or the challenge is
    /// not today's pick (see `availability`).
    ///
    /// The completion is dated by the clock, never by `today`: if this state
    /// missed midnight, filing it under the old day would be wrong, so the call
    /// is refused instead.
    @discardableResult
    func complete(_ challenge: Challenge) -> Bool {
        let now = Today.shared.now()
        if refreshDay(now.date) {
            dayChangeNotice = .completionRejected
            return false
        }
        let outcome = DailyChallengeRules.shared.complete(
            state: currentState(),
            challenge: challenge,
            now: now
        )
        guard let completed = outcome as? CompletionOutcome.Completed else {
            // Safety net: refreshDay already caught a stale day, so this should
            // not be reached unless the shared rule changes.
            if let rejected = outcome as? CompletionOutcome.Rejected,
               rejected.reason == .dayChanged {
                dayChangeNotice = .completionRejected
            }
            return false
        }

        absorb(completed.state)
        lastReward = completed.reward
        persist()
        return true
    }

    /// Swap today's challenge for another one (spec §2.3).
    ///
    /// Returns the new challenge, or nil when there was nothing to swap to,
    /// today is already done, or the day had changed (nothing is rerolled or
    /// counted as skipped then).
    @discardableResult
    func reroll(category: ChallengeCategory? = nil) -> Challenge? {
        if refreshDay() { return nil }
        guard let resolution = DailyChallengeRules.shared.reroll(
            state: currentState(),
            today: today,
            currentChallengeId: todayChallenge.id,
            category: category,
            random: KotlinRandom.Default.shared,
            library: ChallengeSeed.shared.all
        ) else { return nil }

        absorb(resolution.state)
        todayChallenge = resolution.challenge
        persist()
        return resolution.challenge
    }

    /// Makes a library `challenge` today's challenge; counts as one swap. Returns
    /// false when nothing changed: the day changed, today is done, or it already
    /// is today's pick.
    @discardableResult
    func adopt(_ challenge: Challenge) -> Bool {
        if refreshDay() { return false }
        guard let resolution = DailyChallengeRules.shared.adopt(
            state: currentState(),
            challenge: challenge,
            today: today
        ) else { return false }

        absorb(resolution.state)
        todayChallenge = resolution.challenge
        persist()
        return true
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
        // A day change during onboarding is not news: Home was never shown.
        dayChangeNotice = nil
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

    func clearDayChangeNotice() {
        dayChangeNotice = nil
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

    /// Takes back the parts of `state` a shared rule may have changed. Interests
    /// and settings are only ever edited here, so they are left alone.
    private func absorb(_ state: PersistedState) {
        completions = state.completions
        profile = state.profile
        skippedCount = Int(state.skippedCount)
        dailyChallengeDate = state.dailyChallenge?.date
    }

    private func persist() {
        repository.save(state: currentState())
    }
}
