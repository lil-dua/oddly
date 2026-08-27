import SwiftUI
import SharedLogic

/// Colours the app needs that have no slot in a fixed palette constant — the
/// ones that flip between the light and dark schemes. Exposed through the
/// environment so screens read them the way they read any other style value.
struct OddlyPalette {
    let isDark: Bool
    let background: Color
    let surface: Color
    let surfaceElevated: Color
    let surfaceHighest: Color
    let outline: Color
    let textPrimary: Color
    let textSecondary: Color
    let textTertiary: Color
    let flame: Color
    let success: Color
    let warning: Color
    let danger: Color

    static let dark = OddlyPalette(
        isDark: true,
        background: OddlyColors.background,
        surface: OddlyColors.surface,
        surfaceElevated: OddlyColors.surfaceElevated,
        surfaceHighest: OddlyColors.surfaceHighest,
        outline: OddlyColors.outline,
        textPrimary: OddlyColors.textPrimary,
        textSecondary: OddlyColors.textSecondary,
        textTertiary: OddlyColors.textTertiary,
        flame: OddlyColors.flame,
        success: OddlyColors.success,
        warning: OddlyColors.warning,
        danger: OddlyColors.danger
    )

    static let light = OddlyPalette(
        isDark: false,
        background: OddlyColors.lightBackground,
        surface: OddlyColors.lightSurface,
        surfaceElevated: OddlyColors.lightSurfaceElevated,
        surfaceHighest: OddlyColors.lightSurfaceHighest,
        outline: OddlyColors.lightOutline,
        textPrimary: OddlyColors.lightTextPrimary,
        textSecondary: OddlyColors.lightTextSecondary,
        textTertiary: OddlyColors.lightTextSecondary.opacity(0.7),
        flame: OddlyColors.flame,
        success: Color(rgb: 0x16A34A),
        warning: Color(rgb: 0xD97706),
        danger: Color(rgb: 0xDC2626)
    )
}

private struct OddlyPaletteKey: EnvironmentKey {
    static let defaultValue = OddlyPalette.dark
}

extension EnvironmentValues {
    var palette: OddlyPalette {
        get { self[OddlyPaletteKey.self] }
        set { self[OddlyPaletteKey.self] = newValue }
    }
}

extension View {
    /// Installs the palette matching [mode] and pins the system chrome to it.
    func oddlyTheme(_ mode: ThemeMode, systemIsDark: Bool) -> some View {
        let dark: Bool
        switch mode {
        case ThemeMode.light: dark = false
        case ThemeMode.system: dark = systemIsDark
        default: dark = true
        }
        return self.oddlyPalette(dark ? .dark : .light)
    }

    /// Installs an already-resolved palette. Sheets and covers get their own
    /// `preferredColorScheme` resolution, so presented content re-applies the
    /// palette the presenter had rather than re-deriving it.
    func oddlyPalette(_ palette: OddlyPalette) -> some View {
        self
            .environment(\.palette, palette)
            .preferredColorScheme(palette.isDark ? .dark : .light)
    }
}

/// Bridges the platform-agnostic colour carried by the shared enums into SwiftUI.
extension ChallengeCategory {
    var color: Color { Color(argb: UInt64(bitPattern: Int64(colorHex))) }
}

/// Rounded cards, 16–24pt radius (spec §8).
enum OddlyRadius {
    static let small: CGFloat = 12
    static let medium: CGFloat = 16
    static let large: CGFloat = 20
    static let extraLarge: CGFloat = 24
}
