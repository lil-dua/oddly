import SwiftUI

/// Large bold headlines over readable body text (spec §8).
///
/// Uses the system family so the app ships no font binaries and Vietnamese
/// diacritics render correctly on every device. Sizes mirror the Android
/// typography scale one-for-one, so a screen reads the same on both platforms.
enum OddlyFont {
    static let displayLarge = Font.system(size: 56, weight: .black)
    static let displayMedium = Font.system(size: 44, weight: .black)
    static let displaySmall = Font.system(size: 34, weight: .bold)

    static let headlineLarge = Font.system(size: 28, weight: .bold)
    static let headlineMedium = Font.system(size: 24, weight: .bold)
    static let headlineSmall = Font.system(size: 20, weight: .semibold)

    static let titleLarge = Font.system(size: 18, weight: .semibold)
    static let titleMedium = Font.system(size: 16, weight: .semibold)
    static let titleSmall = Font.system(size: 14, weight: .medium)

    static let bodyLarge = Font.system(size: 16, weight: .regular)
    static let bodyMedium = Font.system(size: 14, weight: .regular)
    static let bodySmall = Font.system(size: 12, weight: .regular)

    static let labelLarge = Font.system(size: 15, weight: .semibold)
    static let labelMedium = Font.system(size: 13, weight: .medium)
    static let labelSmall = Font.system(size: 11, weight: .medium)
}
