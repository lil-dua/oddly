import SwiftUI
import UIKit
import SharedLogic

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

    /// Colour from a shared `PaletteSpec` field (Kotlin `Long` arrives as `Int64`).
    init(spec: Int64) {
        self.init(argb: UInt64(bitPattern: spec))
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
    static let background = Color(spec: PaletteSpecs.shared.dark.background)
    static let surface = Color(spec: PaletteSpecs.shared.dark.surface)
    static let surfaceElevated = Color(spec: PaletteSpecs.shared.dark.surfaceElevated)
    static let surfaceHighest = Color(spec: PaletteSpecs.shared.dark.surfaceHighest)
    static let outline = Color(spec: PaletteSpecs.shared.dark.outline)

    // Text
    static let textPrimary = Color(spec: PaletteSpecs.shared.dark.textPrimary)
    static let textSecondary = Color(spec: PaletteSpecs.shared.dark.textSecondary)
    static let textTertiary = Color(spec: PaletteSpecs.shared.dark.textTertiary)

    // Brand gradient stops (the "1%" wordmark and primary CTAs)
    static let pink = Color(rgb: 0xFF7EB3)
    static let magenta = Color(rgb: 0xE879F9)
    static let purple = Color(rgb: 0xA78BFA)
    static let indigo = Color(rgb: 0x818CF8)
    static let blue = Color(rgb: 0x60A5FA)

    // Semantic accents
    static let flame = Color(spec: PaletteSpecs.shared.dark.flame)
    static let flameBright = Color(rgb: 0xFFB067)
    static let success = Color(spec: PaletteSpecs.shared.dark.success)
    static let warning = Color(spec: PaletteSpecs.shared.dark.warning)
    static let danger = Color(spec: PaletteSpecs.shared.dark.danger)

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
    static let lightBackground = Color(spec: PaletteSpecs.shared.light.background)
    static let lightSurface = Color(spec: PaletteSpecs.shared.light.surface)
    static let lightSurfaceElevated = Color(spec: PaletteSpecs.shared.light.surfaceElevated)
    static let lightSurfaceHighest = Color(spec: PaletteSpecs.shared.light.surfaceHighest)
    static let lightTextPrimary = Color(spec: PaletteSpecs.shared.light.textPrimary)
    static let lightTextSecondary = Color(spec: PaletteSpecs.shared.light.textSecondary)
    static let lightTextTertiary = Color(spec: PaletteSpecs.shared.light.textTertiary)
    static let lightFlame = Color(spec: PaletteSpecs.shared.light.flame)
    static let lightSuccess = Color(spec: PaletteSpecs.shared.light.success)
    static let lightWarning = Color(spec: PaletteSpecs.shared.light.warning)
    static let lightDanger = Color(spec: PaletteSpecs.shared.light.danger)
    static let lightOutline = Color(spec: PaletteSpecs.shared.light.outline)
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
