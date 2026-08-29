import SwiftUI
import SharedLogic

/// S11 — the long-term view: level, totals, streak and where the user spends
/// their effort. Entry point to Calendar, Streak and the full library.
struct JourneyScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onOpenCalendar: () -> Void
    let onOpenStreak: () -> Void
    let onOpenAllChallenges: () -> Void
    let onStartFirstChallenge: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(strings.journeyTitle)
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 20)
                    .padding(.bottom, 4)

                if state.completions.isEmpty {
                    EmptyState(
                        title: strings.emptyJourneyTitle,
                        subtitle: strings.emptyJourneyBody,
                        actionText: strings.exploreChallenges,
                        action: onStartFirstChallenge
                    )
                } else {
                    levelCard

                    SectionLabel(strings.quickStats)
                        .padding(.top, 4)

                    HStack(spacing: 12) {
                        StatTile(
                            value: "\(state.totalCompleted)",
                            label: strings.challengesCompletedLabel,
                            accent: OddlyColors.purple
                        )
                        StatTile(
                            value: "\(state.streak.current)",
                            label: strings.consecutiveDays,
                            accent: OddlyColors.flame
                        )
                    }

                    HStack(spacing: 12) {
                        StatTile(
                            value: "\(state.exploredCategoryCount)",
                            label: strings.categoriesExplored,
                            accent: OddlyColors.blue
                        )
                        StatTile(
                            value: "\(state.completionRatePercent)%",
                            label: strings.completionRate,
                            accent: OddlyColors.success
                        )
                    }

                    OddlyCard {
                        SectionLabel(strings.categoryBreakdown)
                        CategoryDistribution(
                            slices: StatsCalculator.shared.distribution(completions: state.completions)
                        )
                        .padding(.top, 14)
                    }

                    VStack(spacing: 10) {
                        NavigationRow(title: strings.completionCalendar, icon: .calendar, action: onOpenCalendar)
                        NavigationRow(title: strings.streakRow, icon: .flame, action: onOpenStreak)
                        NavigationRow(title: strings.allChallenges, icon: .target, action: onOpenAllChallenges)
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
                SectionLabel(strings.currentLevel)
                Text("Level \(profile.level)")
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 10)
                Text(strings.xpProgress(current: profile.xpInLevel, total: profile.xpForNextLevel))
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
    @Environment(\.strings) private var strings

    let slices: [CategorySlice]

    var body: some View {
        VStack(spacing: 14) {
            ForEach(Array(slices.enumerated()), id: \.offset) { _, slice in
                VStack(spacing: 8) {
                    HStack(spacing: 10) {
                        Text(slice.category.emoji).font(OddlyFont.bodyMedium)
                        Text(slice.category.title.of(strings))
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
