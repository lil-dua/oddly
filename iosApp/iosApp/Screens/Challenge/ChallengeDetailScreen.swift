import SwiftUI
import SharedLogic

/// S06 — the full brief for a challenge: why it matters, how to do it, and what
/// it pays. One dominant CTA, with reroll as a secondary text action.
struct ChallengeDetailScreen: View {
    @Environment(\.palette) private var palette

    let challenge: Challenge
    let alreadyCompleted: Bool
    let onBack: () -> Void
    let onComplete: () -> Void
    let onAnother: () -> Void

    var body: some View {
        let accent = challenge.category.color

        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 45, seed: 23).ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    OddlyTopBar(title: "", onBack: onBack)

                    ZStack {
                        GlowOrb(color: accent, alpha: 0.35)
                            .frame(width: 150, height: 150)
                        Text(challenge.category.emoji)
                            .font(OddlyFont.displaySmall)
                            .frame(width: 76, height: 76)
                            .background(
                                accent.opacity(0.16),
                                in: RoundedRectangle(cornerRadius: OddlyRadius.extraLarge, style: .continuous)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: OddlyRadius.extraLarge, style: .continuous)
                                    .stroke(accent.opacity(0.45), lineWidth: 1)
                            )
                    }

                    Text(challenge.title)
                        .font(OddlyFont.headlineMedium)
                        .foregroundStyle(palette.textPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 20)
                        .padding(.horizontal, 24)

                    HStack(spacing: 8) {
                        OddlyChip(text: challenge.category.title, accent: accent, leadingEmoji: challenge.category.emoji)
                        OddlyChip(text: challenge.difficulty.title)
                        OddlyChip(text: "\(challenge.estimatedMinutes) phút")
                    }
                    .padding(.top, 14)

                    VStack(spacing: 16) {
                        InfoBlock(label: "Vì sao điều này quan trọng") {
                            Text(challenge.whyItMatters)
                                .font(OddlyFont.bodyMedium)
                                .foregroundStyle(palette.textSecondary)
                                .fixedSize(horizontal: false, vertical: true)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        InfoBlock(label: "Gợi ý") {
                            VStack(alignment: .leading, spacing: 10) {
                                ForEach(Array(challenge.howToDoIt.enumerated()), id: \.offset) { _, step in
                                    HStack(alignment: .top, spacing: 10) {
                                        Text("✦")
                                            .font(OddlyFont.bodyMedium)
                                            .foregroundStyle(accent)
                                        Text(step)
                                            .font(OddlyFont.bodyMedium)
                                            .foregroundStyle(palette.textSecondary)
                                            .fixedSize(horizontal: false, vertical: true)
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        InfoBlock(label: "Phần thưởng") {
                            HStack {
                                Text("+\(challenge.humanityPercent)% Humanity")
                                    .font(OddlyFont.titleMedium)
                                    .foregroundStyle(OddlyColors.success)
                                Spacer()
                                Text("+\(challenge.rewardXp) XP")
                                    .font(OddlyFont.titleMedium)
                                    .foregroundStyle(OddlyColors.purple)
                            }
                        }
                    }
                    .padding(.top, 28)
                    .padding(.horizontal, 24)

                    VStack(spacing: 12) {
                        if alreadyCompleted {
                            HStack(spacing: 8) {
                                OddlyIconView(.check, size: 18, tint: OddlyColors.success, lineWidth: 2)
                                Text("Bạn đã hoàn thành thử thách này")
                                    .font(OddlyFont.labelLarge)
                                    .foregroundStyle(OddlyColors.success)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(OddlyColors.success.opacity(0.14), in: Capsule())
                        } else {
                            GradientButton("Bắt đầu", action: onComplete)
                        }

                        TextAction("Xem thử thách khác", action: onAnother)
                    }
                    .padding(.top, 28)
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
                }
            }
        }
        .statusBarScrim(palette.background)
        .toolbar(.hidden, for: .navigationBar)
    }
}

private struct InfoBlock<Content: View>: View {
    @Environment(\.palette) private var palette

    let label: String
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionLabel(label)
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .background(palette.surfaceElevated, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}
