import Foundation
import SharedLogic

// MARK: - Naming
//
// SwiftUI publishes a `Category` of its own, so the domain enum is referred to
// by an unambiguous alias everywhere in the iOS UI.

typealias ChallengeCategory = SharedLogic.Category

// MARK: - Shared domain conveniences
//
// The Kotlin framework exposes everything the UI needs, but through
// Objective-C conventions: `Int32` counters, `doCopy(…)` with every argument
// spelled out, and free-standing companion objects. These wrappers keep that
// noise out of the views.

extension LocalDate {
    var epochDays: Int { Int(toEpochDays()) }

    static func fromEpochDays(_ days: Int) -> LocalDate {
        LocalDate.companion.fromEpochDays(epochDays: Int32(days))
    }

    /// First day of the month containing this date.
    var startOfMonth: LocalDate {
        LocalDate(year: year, monthNumber: monthNumber, dayOfMonth: 1)
    }

    /// Move by [delta] months, keeping the day at the 1st.
    func shiftingMonth(by delta: Int) -> LocalDate {
        let zeroBased = Int(year) * 12 + Int(monthNumber) - 1 + delta
        return LocalDate(
            year: Int32(zeroBased / 12),
            monthNumber: Int32(zeroBased % 12 + 1),
            dayOfMonth: 1
        )
    }
}

extension AppSettings {
    /// `doCopy` with every argument defaulted to the current value.
    func with(
        themeMode: ThemeMode? = nil,
        reminderEnabled: Bool? = nil,
        reminderTime: LocalTime? = nil,
        language: AppLanguage? = nil,
        soundEnabled: Bool? = nil,
        hapticsEnabled: Bool? = nil
    ) -> AppSettings {
        doCopy(
            themeMode: themeMode ?? self.themeMode,
            reminderEnabled: reminderEnabled ?? self.reminderEnabled,
            reminderTime: reminderTime ?? self.reminderTime,
            language: language ?? self.language,
            soundEnabled: soundEnabled ?? self.soundEnabled,
            hapticsEnabled: hapticsEnabled ?? self.hapticsEnabled
        )
    }
}

extension UserProfile {
    var xpForNextLevelValue: Int { Int(xpForNextLevel) }

    func with(level: Int32? = nil, xpInLevel: Int32? = nil) -> UserProfile {
        doCopy(
            id: id,
            displayName: displayName,
            createdAt: createdAt,
            interests: interests,
            level: level ?? self.level,
            xpInLevel: xpInLevel ?? self.xpInLevel
        )
    }
}

extension StatsRange {
    /// `days` is nullable on the Kotlin side (the "all time" bucket has no span).
    var dayCount: Int? { days.map { Int(truncating: $0) } }
}

// MARK: - Identity for SwiftUI collections
//
// `Challenge`, `Quote` and `ChallengeCompletion` all carry a stable `id`, but
// they are Objective-C classes and cannot be retroactively conformed without
// tripping Swift's retroactive-conformance rules. A thin wrapper is cheaper
// than fighting that, and keeps `ForEach`/`sheet(item:)` call sites readable.

/// One challenge, addressable by SwiftUI.
struct ChallengeRef: Identifiable, Hashable {
    let id: String
    let challenge: Challenge

    init(_ challenge: Challenge) {
        self.id = challenge.id
        self.challenge = challenge
    }

    static func == (lhs: ChallengeRef, rhs: ChallengeRef) -> Bool { lhs.id == rhs.id }
    func hash(into hasher: inout Hasher) { hasher.combine(id) }
}

/// One completion plus the challenge it refers to, resolved once.
struct CompletionRef: Identifiable {
    let id: String
    let completion: ChallengeCompletion
    let challenge: Challenge
}

// MARK: - Deterministic randomness
//
// Star fields and confetti are generated from a fixed seed so the sky does not
// reshuffle on every redraw. Swift's system generator cannot be seeded, so this
// is a small SplitMix64.

struct SeededGenerator: RandomNumberGenerator {
    private var state: UInt64

    init(seed: UInt64) {
        self.state = seed &+ 0x9E37_79B9_7F4A_7C15
    }

    mutating func next() -> UInt64 {
        state = state &+ 0x9E37_79B9_7F4A_7C15
        var z = state
        z = (z ^ (z >> 30)) &* 0xBF58_476D_1CE4_E5B9
        z = (z ^ (z >> 27)) &* 0x94D0_49BB_1331_11EB
        return z ^ (z >> 31)
    }

    /// 0..<1, matching `kotlin.random.Random.nextFloat()`.
    mutating func nextUnit() -> CGFloat {
        CGFloat(Double(next() >> 11) * (1.0 / 9_007_199_254_740_992.0))
    }
}
