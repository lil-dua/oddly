import Foundation

/// The four bottom-navigation destinations (spec §5: exactly four, everything
/// deeper is a child route).
enum TabDestination: String, CaseIterable, Hashable {
    case home
    case journey
    case statistics
    case settings

    var label: String {
        switch self {
        case .home: return "Hôm nay"
        case .journey: return "Hành trình"
        case .statistics: return "Thống kê"
        case .settings: return "Cài đặt"
        }
    }

    var icon: OddlyIcon {
        switch self {
        case .home: return .sparkle
        case .journey: return .journey
        case .statistics: return .stats
        case .settings: return .settings
        }
    }
}

/// Child routes pushed onto a tab's navigation stack. Screens that are modal by
/// nature — the reward celebration and the share card — are presented as covers
/// and sheets instead, so they are not listed here.
enum Destination: Hashable {
    case challengeDetail(challengeId: String)
    case anotherChallenge
    case chooseCategory
    case allChallenges
    case calendar
    case streak
    case quotes
}

/// The linear onboarding sequence (spec §2.1).
enum OnboardingStep: Hashable {
    case chooseInterest
    case notificationPermission
}
