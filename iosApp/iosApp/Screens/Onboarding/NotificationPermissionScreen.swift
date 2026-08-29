import SwiftUI

/// S04 — reminder opt-in. Declining never blocks onboarding, per the spec; the
/// screen explains the value rather than demanding the permission.
struct NotificationPermissionScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let onAllow: (String) -> Void
    let onSkip: () -> Void

    @State private var selectedTime = "09:00"

    private let times = ["08:00", "09:00", "12:00", "18:00"]

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 50, seed: 17).ignoresSafeArea()

            VStack(spacing: 0) {
                ZStack {
                    GlowOrb(color: OddlyColors.purple, alpha: 0.35)
                        .frame(width: 180, height: 180)
                    OddlyIconView(.bell, size: 42, tint: OddlyColors.purple, lineWidth: 2)
                        .frame(width: 96, height: 96)
                        .background(
                            palette.surfaceElevated,
                            in: RoundedRectangle(cornerRadius: 28, style: .continuous)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 28, style: .continuous)
                                .stroke(OddlyColors.purple.opacity(0.4), lineWidth: 1)
                        )
                }
                .padding(.top, 48)

                Text(strings.reminderPermissionTitle)
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 36)

                Text(strings.reminderPermissionBody)
                    .font(OddlyFont.bodyMedium)
                    .foregroundStyle(palette.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 12)

                Text(strings.reminderPermissionPickTime)
                    .font(OddlyFont.labelMedium)
                    .foregroundStyle(palette.textTertiary)
                    .padding(.top, 32)

                HStack(spacing: 10) {
                    ForEach(times, id: \.self) { time in
                        let active = time == selectedTime
                        Button {
                            selectedTime = time
                        } label: {
                            Text(time)
                                .font(OddlyFont.labelMedium)
                                .foregroundStyle(active ? OddlyColors.purple : palette.textSecondary)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(
                                    active ? OddlyColors.purple.opacity(0.18) : palette.surfaceElevated,
                                    in: RoundedRectangle(cornerRadius: 14, style: .continuous)
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                                        .stroke(active ? OddlyColors.purple : .clear, lineWidth: 1)
                                )
                        }
                        .buttonStyle(PressableStyle())
                    }
                }
                .padding(.top, 12)

                Spacer(minLength: 24)

                GradientButton(strings.reminderPermissionEnable) { onAllow(selectedTime) }

                TextAction(strings.later, action: onSkip)
                    .padding(.top, 8)
            }
            .padding(.horizontal, 28)
            .padding(.vertical, 24)
        }
    }
}
