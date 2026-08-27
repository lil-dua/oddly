import SwiftUI
import SharedLogic

/// S07 — the reward moment. Per spec §8.1 the celebration must not slow the user
/// down, so the animation is short and every action stays tappable throughout.
struct ChallengeCompleteScreen: View {
    @Environment(\.palette) private var palette

    let challenge: Challenge
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
                    TextAction("Xong", action: onDone)
                }

                Spacer(minLength: 0)

                Text("🥳")
                    .font(OddlyFont.displayLarge)
                    .scaleEffect(popped ? 1 : 0.4)
                    .animation(.spring(response: 0.5, dampingFraction: 0.6), value: popped)

                Text("Tuyệt vời!")
                    .font(OddlyFont.headlineLarge)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 24)

                Text("Bạn đã hoàn thành\nthử thách hôm nay.")
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
                    RewardPill(text: "🔥 \(streakDays) ngày", color: OddlyColors.flame)
                }
                .padding(.top, 20)

                if let reward, reward.leveledUp {
                    HStack(spacing: 8) {
                        OddlyIconView(.sparkle, size: 16, tint: OddlyColors.warning)
                        Text("Lên cấp \(reward.newLevel)!")
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

                GradientButton("Chia sẻ", leadingIcon: .share, action: onShare)

                SecondaryButton("Xem thử thách khác", action: onAnother)
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
