import SwiftUI
import SharedLogic

/// S11 — the long-term view: level, totals, streak and where the user spends
/// their effort. Entry point to Calendar, Streak and the full library.
struct JourneyScreen: View {
    @Environment(\.palette) private var palette

    let state: OddlyAppState
    let onOpenCalendar: () -> Void
    let onOpenStreak: () -> Void
    let onOpenAllChallenges: () -> Void
    let onStartFirstChallenge: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Hành trình của bạn")
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 20)
                    .padding(.bottom, 4)

                if state.completions.isEmpty {
                    EmptyState(
                        title: "Bạn chưa có thử thách nào",
                        subtitle: "Hãy bắt đầu hành trình\n1% tốt hơn mỗi ngày.",
                        actionText: "Khám phá thử thách",
                        action: onStartFirstChallenge
                    )
                } else {
                    levelCard

                    SectionLabel("Thống kê nhanh")
                        .padding(.top, 4)

                    HStack(spacing: 12) {
                        StatTile(
                            value: "\(state.totalCompleted)",
                            label: "Thử thách\nđã hoàn thành",
                            accent: OddlyColors.purple
                        )
                        StatTile(
                            value: "\(state.streak.current)",
                            label: "Ngày liên tiếp",
                            accent: OddlyColors.flame
                        )
                    }

                    HStack(spacing: 12) {
                        StatTile(
                            value: "\(state.exploredCategoryCount)",
                            label: "Chủ đề\nđã khám phá",
                            accent: OddlyColors.blue
                        )
                        StatTile(
                            value: "\(state.completionRatePercent)%",
                            label: "Tỷ lệ hoàn thành",
                            accent: OddlyColors.success
                        )
                    }

                    OddlyCard {
                        SectionLabel("Phân bổ chủ đề")
                        CategoryDistribution(
                            slices: StatsCalculator.shared.distribution(completions: state.completions)
                        )
                        .padding(.top, 14)
                    }

                    VStack(spacing: 10) {
                        NavigationRow(title: "Lịch hoàn thành", icon: .calendar, action: onOpenCalendar)
                        NavigationRow(title: "Chuỗi ngày liên tiếp", icon: .flame, action: onOpenStreak)
                        NavigationRow(title: "Tất cả thử thách", icon: .target, action: onOpenAllChallenges)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 28)
        }
        .background(palette.background)
        .statusBarScrim(palette.background)
    }

    private var levelCard: some View {
        let profile = state.profile
        return HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 0) {
                SectionLabel("Cấp độ hiện tại")
                Text("Level \(profile.level)")
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 10)
                Text("\(profile.xpInLevel) / \(profile.xpForNextLevel) XP")
                    .font(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textTertiary)
                    .padding(.top, 12)
                GradientProgressBar(progress: Double(profile.levelProgress))
                    .padding(.top, 8)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Astronaut(size: 96)
        }
        .padding(18)
        .background(alignment: .top) {
            palette.surfaceElevated
                .overlay(alignment: .top) {
                    StarField(starCount: 20, seed: 41).frame(height: 150)
                }
        }
        .clipShape(RoundedRectangle(cornerRadius: OddlyRadius.large, style: .continuous))
    }
}

/// Shared by Journey and Statistics: one tinted bar per category.
struct CategoryDistribution: View {
    @Environment(\.palette) private var palette

    let slices: [CategorySlice]

    var body: some View {
        VStack(spacing: 14) {
            ForEach(Array(slices.enumerated()), id: \.offset) { _, slice in
                VStack(spacing: 8) {
                    HStack(spacing: 10) {
                        Text(slice.category.emoji).font(OddlyFont.bodyMedium)
                        Text(slice.category.title)
                            .font(OddlyFont.bodyMedium)
                            .foregroundStyle(palette.textSecondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Text("\(Int(slice.fraction * 100))%")
                            .font(OddlyFont.labelMedium)
                            .foregroundStyle(slice.category.color)
                    }
                    GradientProgressBar(
                        progress: Double(slice.fraction),
                        height: 6,
                        gradient: LinearGradient(
                            colors: [slice.category.color, slice.category.color.opacity(0.5)],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                }
            }
        }
    }
}

private struct NavigationRow: View {
    @Environment(\.palette) private var palette

    let title: String
    let icon: OddlyIcon
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                OddlyIconView(icon, size: 20, tint: OddlyColors.purple)
                Text(title)
                    .font(OddlyFont.bodyLarge)
                    .foregroundStyle(palette.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                OddlyIconView(.chevronRight, size: 16, tint: palette.textTertiary)
            }
            .padding(16)
            .background(
                palette.surfaceElevated,
                in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
            )
        }
        .buttonStyle(PressableStyle(pressedScale: 0.985))
    }
}
