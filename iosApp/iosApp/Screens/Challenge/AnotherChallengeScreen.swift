import SwiftUI

/// S08 — random challenge. The dice roll is a short piece of anticipation before
/// the reroll actually happens.
struct AnotherChallengeScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let onBack: () -> Void
    let onSurpriseMe: () -> Void
    let onChooseCategory: () -> Void

    @State private var spin: Double = 0

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 50, seed: 31).ignoresSafeArea()

            VStack(spacing: 0) {
                OddlyTopBar(title: "", onBack: onBack)
                content
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    private var content: some View {
        VStack(spacing: 0) {
            Text(strings.anotherChallengeTitle)
                .font(OddlyFont.headlineMedium)
                .foregroundStyle(palette.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 16)

            Spacer(minLength: 0)

            ZStack {
                GlowOrb(color: OddlyColors.purple, alpha: 0.3)
                    .frame(width: 240, height: 240)
                OddlyIconView(.dice, size: 72, tint: palette.textPrimary, lineWidth: 2)
                    .rotationEffect(.degrees(spin))
                    .frame(width: 128, height: 128)
                    .background(
                        palette.surfaceElevated,
                        in: RoundedRectangle(cornerRadius: 32, style: .continuous)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 32, style: .continuous)
                            .stroke(OddlyColors.purple.opacity(0.35), lineWidth: 1)
                    )
            }

            Text(strings.anotherChallengeBody)
                .font(OddlyFont.bodyLarge)
                .foregroundStyle(palette.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 28)

            Spacer(minLength: 0)

            GradientButton(strings.surpriseMe) {
                withAnimation(.easeInOut(duration: 0.7)) { spin += 720 }
                Task {
                    try? await Task.sleep(for: .milliseconds(700))
                    onSurpriseMe()
                }
            }

            Text(strings.or_)
                .font(OddlyFont.labelSmall)
                .tracking(0.5)
                .foregroundStyle(palette.textTertiary)
                .padding(.vertical, 20)

            SecondaryButton(strings.chooseAnotherCategory, action: onChooseCategory)
                .padding(.bottom, 32)
        }
        .padding(.horizontal, 28)
    }
}
