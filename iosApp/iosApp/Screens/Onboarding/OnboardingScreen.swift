import SwiftUI

private struct Benefit: Identifiable {
    let id = UUID()
    let icon: OddlyIcon
    let label: String
    let tint: Color
}

/// S02 — explains the 1% concept in two sentences and three benefits. Kept to a
/// single page; the spec caps onboarding at 2–3 screens total.
struct OnboardingScreen: View {
    @Environment(\.palette) private var palette

    let onStart: () -> Void
    let onSkip: () -> Void

    private let benefits = [
        Benefit(icon: .sparkle, label: "Thử thách\nmỗi ngày", tint: OddlyColors.warning),
        Benefit(icon: .heart, label: "Dễ thực hiện\nnhưng ý nghĩa", tint: OddlyColors.pink),
        Benefit(icon: .refresh, label: "Thay đổi\ncuộc sống", tint: OddlyColors.blue),
    ]

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 70, seed: 9).ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Spacer()
                    TextAction("Bỏ qua", action: onSkip)
                }

                Text("Chào mừng bạn đến với")
                    .font(OddlyFont.headlineSmall)
                    .foregroundStyle(palette.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 24)

                GradientText("1% HUMAN", font: OddlyFont.displaySmall)

                Text("Mỗi ngày chúng tôi sẽ giao cho\nbạn một thử thách nhỏ.")
                    .font(OddlyFont.bodyMedium)
                    .foregroundStyle(palette.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 14)

                Spacer(minLength: 0)

                Astronaut(size: 210)

                Spacer(minLength: 0)

                HStack(alignment: .top, spacing: 0) {
                    ForEach(benefits) { benefit in
                        VStack(spacing: 10) {
                            OddlyIconView(benefit.icon, size: 24, tint: benefit.tint)
                            Text(benefit.label)
                                .font(OddlyFont.bodySmall)
                                .foregroundStyle(palette.textSecondary)
                                .multilineTextAlignment(.center)
                        }
                        .frame(maxWidth: .infinity)
                    }
                }

                GradientButton("Bắt đầu", action: onStart)
                    .padding(.top, 32)

                PageDots(count: 3, selected: 0)
                    .padding(.top, 20)
            }
            .padding(.horizontal, 28)
            .padding(.vertical, 24)
        }
    }
}

struct PageDots: View {
    @Environment(\.palette) private var palette

    let count: Int
    let selected: Int

    var body: some View {
        HStack(spacing: 7) {
            ForEach(0..<count, id: \.self) { index in
                Circle()
                    .fill(index == selected ? OddlyColors.purple : palette.textTertiary)
                    .frame(
                        width: index == selected ? 8 : 6,
                        height: index == selected ? 8 : 6
                    )
            }
        }
    }
}
