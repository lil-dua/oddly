import SwiftUI
import SharedLogic

/// S05 — the default screen on every launch after the first. Today's challenge
/// is the visual hero; everything else supports it.
struct HomeScreen: View {
    @Environment(\.palette) private var palette

    let state: OddlyAppState
    let onOpenChallenge: () -> Void
    let onStartChallenge: () -> Void
    let onAnotherChallenge: () -> Void
    let onOpenStreak: () -> Void
    let onOpenQuotes: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(DateFormat.shared.dayAndMonth(date: state.today))
                        .font(OddlyFont.labelMedium)
                        .foregroundStyle(palette.textTertiary)
                    Text("Hôm nay của bạn\nsẽ khác biệt như thế nào?")
                        .font(OddlyFont.headlineSmall)
                        .foregroundStyle(palette.textPrimary)
                }
                .padding(.top, 20)
                .padding(.bottom, 4)

                TodayChallengeCard(
                    challenge: state.todayChallenge,
                    completed: state.isCompletedToday(state.todayChallenge.id),
                    onOpen: onOpenChallenge,
                    onStart: onStartChallenge
                )

                streakCard

                HStack(spacing: 12) {
                    StatTile(
                        value: "\(state.totalCompleted)",
                        label: "Thử thách\nđã hoàn thành",
                        accent: OddlyColors.purple
                    )
                    StatTile(
                        value: "Lv.\(state.profile.level)",
                        label: "\(state.profile.xpInLevel) / \(state.profile.xpForNextLevel) XP",
                        accent: OddlyColors.pink
                    )
                }

                quoteCard

                exploreCard
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 28)
        }
        .background(palette.background)
        .statusBarScrim(palette.background)
    }

    private var streakCard: some View {
        OddlyCard(action: onOpenStreak) {
            HStack(spacing: 6) {
                SectionLabel("Streak")
                Spacer()
                Text("\(state.streak.current) ngày liên tiếp 🔥")
                    .font(OddlyFont.labelMedium)
                    .foregroundStyle(palette.flame)
                OddlyIconView(.chevronRight, size: 14, tint: palette.textTertiary)
            }
            WeekStrip(days: state.weekActivity)
                .padding(.top, 16)
        }
    }

    private var quoteCard: some View {
        let quote = state.quoteOfTheDay
        return OddlyCard(action: onOpenQuotes) {
            HStack {
                SectionLabel("Cảm hứng mỗi ngày")
                Spacer()
                OddlyIconView(.chevronRight, size: 14, tint: palette.textTertiary)
            }
            Text("“\(quote.text)”")
                .font(OddlyFont.bodyMedium)
                .foregroundStyle(palette.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 12)
            Text("– \(quote.author)")
                .font(OddlyFont.bodySmall)
                .foregroundStyle(palette.textTertiary)
                .padding(.top, 8)
        }
    }

    private var exploreCard: some View {
        OddlyCard(action: onAnotherChallenge) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Muốn thử thách khác?")
                        .font(OddlyFont.titleSmall)
                        .foregroundStyle(palette.textPrimary)
                    Text("Để chúng tôi chọn ngẫu nhiên cho bạn.")
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textTertiary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                OddlyIconView(.dice, size: 28, tint: OddlyColors.purple)
            }
        }
    }
}
