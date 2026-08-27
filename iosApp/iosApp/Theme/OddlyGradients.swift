import SwiftUI

/// The gradient vocabulary of the app. Kept in one place so the pink→purple→blue
/// sweep stays identical across the logo, CTAs, progress bars and share card.
enum OddlyGradients {

    static let brandStops: [Color] = [
        OddlyColors.pink,
        OddlyColors.magenta,
        OddlyColors.purple,
        OddlyColors.indigo,
    ]

    /// Primary CTA fill — horizontal pink → purple.
    static let primaryButton = LinearGradient(
        colors: [OddlyColors.pink, OddlyColors.magenta, OddlyColors.purple],
        startPoint: .leading,
        endPoint: .trailing
    )

    /// Logo / headline text fill.
    static let brandText = LinearGradient(
        colors: [OddlyColors.pink, OddlyColors.purple, OddlyColors.blue],
        startPoint: .leading,
        endPoint: .trailing
    )

    /// Level and XP progress bars.
    static let progress = LinearGradient(
        colors: [OddlyColors.warning, OddlyColors.pink, OddlyColors.purple],
        startPoint: .leading,
        endPoint: .trailing
    )

    /// Streak / fire accents.
    static let flame = LinearGradient(
        colors: [OddlyColors.flameBright, OddlyColors.flame],
        startPoint: .leading,
        endPoint: .trailing
    )

    /// Full-bleed sweep used by the splash ring and the streak halo.
    static let brandSweep = AngularGradient(
        colors: brandStops + [OddlyColors.pink],
        center: .center
    )

    /// A soft category-tinted wash for category cards and chips.
    static func categoryWash(_ color: Color) -> LinearGradient {
        LinearGradient(
            colors: [color.opacity(0.22), color.opacity(0.06)],
            startPoint: .top,
            endPoint: .bottom
        )
    }

    /// Radial glow behind hero elements.
    static func glow(_ color: Color, alpha: Double = 0.35, radius: CGFloat) -> RadialGradient {
        RadialGradient(
            colors: [color.opacity(alpha), color.opacity(0)],
            center: .center,
            startRadius: 0,
            endRadius: radius
        )
    }
}
