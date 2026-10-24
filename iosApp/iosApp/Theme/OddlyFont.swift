import SwiftUI

/// A type token that scales with Dynamic Type while keeping its base size at the default
/// (Large) setting, so a screen still matches the Android scale one-for-one.
///
/// `relativeTo` picks the system text style whose growth curve is closest: large titles grow
/// less than body text, like Android's non-linear font scale on API 34+.
struct OddlyTextStyle {
    let size: CGFloat
    let weight: Font.Weight
    let relativeTo: Font.TextStyle

    func weight(_ weight: Font.Weight) -> OddlyTextStyle {
        OddlyTextStyle(size: size, weight: weight, relativeTo: relativeTo)
    }
}

/// Large bold headlines over readable body text (spec §8).
///
/// Uses the system family so the app ships no font binaries and Vietnamese
/// diacritics render correctly on every device. Sizes mirror the Android
/// typography scale one-for-one, so a screen reads the same on both platforms.
enum OddlyFont {
    static let displayLarge = OddlyTextStyle(size: 56, weight: .black, relativeTo: .largeTitle)
    static let displayMedium = OddlyTextStyle(size: 44, weight: .black, relativeTo: .largeTitle)
    static let displaySmall = OddlyTextStyle(size: 34, weight: .bold, relativeTo: .largeTitle)

    static let headlineLarge = OddlyTextStyle(size: 28, weight: .bold, relativeTo: .title)
    static let headlineMedium = OddlyTextStyle(size: 24, weight: .bold, relativeTo: .title2)
    static let headlineSmall = OddlyTextStyle(size: 20, weight: .semibold, relativeTo: .title3)

    static let titleLarge = OddlyTextStyle(size: 18, weight: .semibold, relativeTo: .headline)
    static let titleMedium = OddlyTextStyle(size: 16, weight: .semibold, relativeTo: .callout)
    static let titleSmall = OddlyTextStyle(size: 14, weight: .medium, relativeTo: .subheadline)

    static let bodyLarge = OddlyTextStyle(size: 16, weight: .regular, relativeTo: .callout)
    static let bodyMedium = OddlyTextStyle(size: 14, weight: .regular, relativeTo: .subheadline)
    static let bodySmall = OddlyTextStyle(size: 12, weight: .regular, relativeTo: .caption)

    static let labelLarge = OddlyTextStyle(size: 15, weight: .semibold, relativeTo: .subheadline)
    static let labelMedium = OddlyTextStyle(size: 13, weight: .medium, relativeTo: .footnote)
    static let labelSmall = OddlyTextStyle(size: 11, weight: .medium, relativeTo: .caption2)
}

/// `@ScaledMetric` needs the relative text style at init time, hence a modifier that builds it per use.
private struct OddlyFontModifier: ViewModifier {
    let style: OddlyTextStyle
    @ScaledMetric private var size: CGFloat

    init(style: OddlyTextStyle) {
        self.style = style
        _size = ScaledMetric(wrappedValue: style.size, relativeTo: style.relativeTo)
    }

    func body(content: Content) -> some View {
        content.font(.system(size: size, weight: style.weight))
    }
}

extension View {
    func oddlyFont(_ style: OddlyTextStyle) -> some View {
        modifier(OddlyFontModifier(style: style))
    }
}
