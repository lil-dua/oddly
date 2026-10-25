import Foundation

/// Mirror of `UiTestTags` in
/// `sharedLogic/src/commonMain/kotlin/dev/lildua/oddly/core/testing/UiTestTags.kt`.
/// The UI test bundle does not link SharedLogic, so the values are copied; change both together.
enum UITestIds {
    static let onboardingStart = "onboarding.start"
    static let onboardingSkip = "onboarding.skip"
    static let interestsContinue = "interests.continue"
    static func interest(_ categoryId: String) -> String { "interests.\(categoryId)" }
    static let permissionLater = "permission.later"

    static let homeDayBanner = "home.dayBanner"
    static let homeTodayTitle = "home.todayTitle"
    static let homeTodayStart = "home.todayStart"
    static let homeTodayDone = "home.todayDone"

    static let detailBegin = "detail.begin"
    static let detailAlreadyDone = "detail.alreadyDone"
    static let detailDayDone = "detail.dayDone"
    static let detailAdopt = "detail.adopt"

    static let completeDone = "complete.done"
    static let completeXp = "complete.xp"
    static let completeStreak = "complete.streak"

    static let navBack = "nav.back"

    static let tabHome = "tab.home"
    static let tabJourney = "tab.journey"
    static let tabSettings = "tab.settings"

    static let settingsRate = "settings.rate"
    static let settingsContact = "settings.contact"
    static let settingsPrivacy = "settings.privacy"
    static let settingsAbout = "settings.about"

    static let journeyEmpty = "journey.empty"
    static let journeyLevel = "journey.level"
    static let journeyAllChallenges = "journey.allChallenges"
    static func libraryRow(_ challengeId: String) -> String { "library.\(challengeId)" }
}
