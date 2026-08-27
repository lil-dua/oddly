import SwiftUI
import UIKit

extension Color {
    /// 0xAARRGGBB, matching the `colorHex` values carried by the shared domain
    /// models so Android and iOS tint from a single source.
    init(argb: UInt64) {
        self.init(
            .sRGB,
            red: Double((argb >> 16) & 0xFF) / 255,
            green: Double((argb >> 8) & 0xFF) / 255,
            blue: Double(argb & 0xFF) / 255,
            opacity: Double((argb >> 24) & 0xFF) / 255
        )
    }

    /// 0xRRGGBB, fully opaque.
    init(rgb: UInt64) {
        self.init(argb: 0xFF00_0000 | rgb)
    }
}

/// Dark-first palette (spec §8). The app is a deep near-black space backdrop
/// with a small number of saturated neon accents used sparingly for emphasis.
enum OddlyColors {
    // Surfaces — near-black with a cool blue cast so the neon reads warm against it.
    static let background = Color(rgb: 0x07070C)
    static let surface = Color(rgb: 0x12121B)
    static let surfaceElevated = Color(rgb: 0x1A1A26)
    static let surfaceHighest = Color(rgb: 0x23232F)
    static let outline = Color(rgb: 0x2C2C3A)

    // Text
    static let textPrimary = Color(rgb: 0xF4F4F7)
    static let textSecondary = Color(rgb: 0xA0A0B0)
    static let textTertiary = Color(rgb: 0x6C6C7E)

    // Brand gradient stops (the "1%" wordmark and primary CTAs)
    static let pink = Color(rgb: 0xFF7EB3)
    static let magenta = Color(rgb: 0xE879F9)
    static let purple = Color(rgb: 0xA78BFA)
    static let indigo = Color(rgb: 0x818CF8)
    static let blue = Color(rgb: 0x60A5FA)

    // Semantic accents
    static let flame = Color(rgb: 0xFF7A45)
    static let flameBright = Color(rgb: 0xFFB067)
    static let success = Color(rgb: 0x4ADE80)
    static let warning = Color(rgb: 0xFBBF24)
    static let danger = Color(rgb: 0xF87171)

    /// [top] at [alpha] composited over [bottom], resolved up front.
    ///
    /// The share card exports to a PNG with nothing behind it, so any stop left
    /// translucent would come out washed out wherever the image is shared.
    static func composite(_ top: Color, over bottom: Color, alpha: Double) -> Color {
        Color(
            .sRGB,
            red: mix(top.components.red, bottom.components.red, alpha),
            green: mix(top.components.green, bottom.components.green, alpha),
            blue: mix(top.components.blue, bottom.components.blue, alpha),
            opacity: 1
        )
    }

    private static func mix(_ top: Double, _ bottom: Double, _ alpha: Double) -> Double {
        top * alpha + bottom * (1 - alpha)
    }

    // Ink used on top of the neon fills — deep plum rather than pure black,
    // which reads softer against a saturated gradient.
    static let onNeon = Color(rgb: 0x1B0A25)

    // Light theme surfaces — the spec lists Light as optional from MVP, so the
    // scheme exists but the app defaults to dark.
    static let lightBackground = Color(rgb: 0xF7F7FB)
    static let lightSurface = Color(rgb: 0xFFFFFF)
    static let lightSurfaceElevated = Color(rgb: 0xF0F0F6)
    static let lightSurfaceHighest = Color(rgb: 0xE6E6F0)
    static let lightTextPrimary = Color(rgb: 0x14141C)
    static let lightTextSecondary = Color(rgb: 0x5A5A6E)
    static let lightOutline = Color(rgb: 0xE0E0EA)
}

private extension Color {
    /// sRGB components of a colour built from a literal hex value.
    var components: (red: Double, green: Double, blue: Double) {
        let ui = UIColor(self)
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        ui.getRed(&r, green: &g, blue: &b, alpha: &a)
        return (Double(r), Double(g), Double(b))
    }
}
