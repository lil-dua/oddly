import SwiftUI

/// Tap feedback without a highlight wash.
///
/// The design leans on gradients and glows, and a standard highlight washes out
/// over them — interactive surfaces animate their own press scale instead.
struct PressableStyle: ButtonStyle {
    var pressedScale: CGFloat = 0.97

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? pressedScale : 1)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

/// The dominant call-to-action. Per spec §8.1 there is at most one of these on
/// screen at a time — everything else is a `SecondaryButton` or a `TextAction`.
struct GradientButton: View {
    let title: String
    var enabled: Bool = true
    var gradient: LinearGradient = OddlyGradients.primaryButton
    var height: CGFloat = 54
    var leadingIcon: OddlyIcon?
    let action: () -> Void

    init(
        _ title: String,
        enabled: Bool = true,
        gradient: LinearGradient = OddlyGradients.primaryButton,
        height: CGFloat = 54,
        leadingIcon: OddlyIcon? = nil,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.enabled = enabled
        self.gradient = gradient
        self.height = height
        self.leadingIcon = leadingIcon
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let leadingIcon {
                    OddlyIconView(leadingIcon, size: 18, tint: OddlyColors.onNeon)
                }
                Text(title)
                    .font(OddlyFont.labelLarge)
                    .foregroundStyle(OddlyColors.onNeon)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .background(gradient, in: Capsule())
        }
        .buttonStyle(PressableStyle())
        .disabled(!enabled)
        .opacity(enabled ? 1 : 0.4)
    }
}

/// Outlined alternative for the non-dominant action on a screen.
struct SecondaryButton: View {
    @Environment(\.palette) private var palette

    let title: String
    var height: CGFloat = 52
    var leadingIcon: OddlyIcon?
    let action: () -> Void

    init(
        _ title: String,
        height: CGFloat = 52,
        leadingIcon: OddlyIcon? = nil,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.height = height
        self.leadingIcon = leadingIcon
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let leadingIcon {
                    OddlyIconView(leadingIcon, size: 18, tint: palette.textPrimary)
                }
                Text(title)
                    .font(OddlyFont.labelLarge)
                    .foregroundStyle(palette.textPrimary)
            }
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .background(palette.surfaceElevated.opacity(0.5), in: Capsule())
            .overlay(Capsule().stroke(palette.outline, lineWidth: 1))
        }
        .buttonStyle(PressableStyle())
    }
}

/// Low-emphasis inline action, e.g. "Maybe later" or "Skip".
struct TextAction: View {
    @Environment(\.palette) private var palette

    let title: String
    var color: Color?
    let action: () -> Void

    init(_ title: String, color: Color? = nil, action: @escaping () -> Void) {
        self.title = title
        self.color = color
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(OddlyFont.labelMedium)
                .foregroundStyle(color ?? palette.textSecondary)
                .padding(.vertical, 8)
                .padding(.horizontal, 4)
        }
        .buttonStyle(PressableStyle(pressedScale: 0.94))
    }
}

/// A circular icon button — back chevrons, month arrows, quote paging.
struct CircleIconButton: View {
    @Environment(\.palette) private var palette

    let icon: OddlyIcon
    var diameter: CGFloat = 38
    var iconSize: CGFloat = 20
    var tint: Color?
    let action: () -> Void

    init(
        _ icon: OddlyIcon,
        diameter: CGFloat = 38,
        iconSize: CGFloat = 20,
        tint: Color? = nil,
        action: @escaping () -> Void
    ) {
        self.icon = icon
        self.diameter = diameter
        self.iconSize = iconSize
        self.tint = tint
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            OddlyIconView(icon, size: iconSize, tint: tint ?? palette.textPrimary)
                .frame(width: diameter, height: diameter)
                .background(palette.surfaceElevated, in: Circle())
        }
        .buttonStyle(PressableStyle())
    }
}
