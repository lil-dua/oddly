import SwiftUI
import SharedLogic

/// S07 — the reward moment. Per spec §8.1 the celebration must not slow the user
/// down, so the animation is short and every action stays tappable throughout.
struct ChallengeCompleteScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let challenge: LocalizedChallenge
    let reward: OddlyAppState.Reward?
    let streakDays: Int
    let quote: String
    let onShare: () -> Void
    let onAnother: () -> Void
    let onDone: () -> Void

    @State private var burst: CGFloat = 0
    @State private var popped = false

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 60, seed: 29).ignoresSafeArea()
            Confetti(progress: burst).ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Spacer()
                    TextAction(strings.done, action: onDone)
                }

                Spacer(minLength: 0)

                Text("🥳")
                    .font(OddlyFont.displayLarge)
                    .scaleEffect(popped ? 1 : 0.4)
                    .animation(.spring(response: 0.5, dampingFraction: 0.6), value: popped)

                Text(strings.celebrationTitle)
                    .font(OddlyFont.headlineLarge)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 24)

                Text(strings.celebrationBody)
                    .font(OddlyFont.bodyLarge)
                    .foregroundStyle(palette.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 6)

                GradientText(
                    "+\(reward?.humanityPercent ?? Int(challenge.humanityPercent))% Humanity",
                    font: OddlyFont.headlineMedium
                )
                .padding(.top, 24)

                HStack(spacing: 12) {
                    RewardPill(text: "+\(reward?.xp ?? Int(challenge.rewardXp)) XP", color: OddlyColors.purple)
                    RewardPill(text: "🔥 \(strings.streakDays(count: Int32(streakDays)))", color: OddlyColors.flame)
                }
                .padding(.top, 20)

                if let reward, reward.leveledUp {
                    HStack(spacing: 8) {
                        OddlyIconView(.sparkle, size: 16, tint: OddlyColors.warning)
                        Text("\(strings.levelUp) \(reward.newLevel)!")
                            .font(OddlyFont.labelMedium)
                            .foregroundStyle(OddlyColors.warning)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(OddlyColors.warning.opacity(0.16), in: Capsule())
                    .padding(.top, 16)
                }

                Text("“\(quote)”")
                    .font(OddlyFont.bodyMedium)
                    .foregroundStyle(palette.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(
                        palette.surfaceElevated,
                        in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
                    )
                    .padding(.top, 28)

                Spacer(minLength: 0)

                GradientButton(strings.share, leadingIcon: .share, action: onShare)

                SecondaryButton(strings.seeAnotherChallenge, action: onAnother)
                    .padding(.top, 12)
            }
            .padding(.horizontal, 28)
            .padding(.vertical, 20)
        }
        .task {
            popped = true
            withAnimation(.easeOut(duration: 1.9)) { burst = 1 }
        }
    }
}

private struct RewardPill: View {
    let text: String
    let color: Color

    var body: some View {
        Text(text)
            .font(OddlyFont.labelMedium)
            .foregroundStyle(color)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(color.opacity(0.14), in: Capsule())
    }
}
