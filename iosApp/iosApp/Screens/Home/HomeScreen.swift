import SwiftUI
import SharedLogic

/// S05 — the default screen on every launch after the first. Today's challenge
/// is the visual hero; everything else supports it.
struct HomeScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onOpenChallenge: () -> Void
    let onStartChallenge: () -> Void
    let onAnotherChallenge: () -> Void
    let onOpenStreak: () -> Void
    let onOpenQuotes: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if let notice = state.dayChangeNotice {
                    OddlyCard(action: state.clearDayChangeNotice) {
                        Text(noticeText(notice))
                            .font(OddlyFont.bodyMedium)
                            .foregroundStyle(palette.textPrimary)
                    }
                    .padding(.top, 20)
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text(DateFormat.shared.dayAndMonth(date: state.today, language: strings.language))
                        .font(OddlyFont.labelMedium)
                        .foregroundStyle(palette.textTertiary)
                    Text(strings.homeHeadline)
                        .font(OddlyFont.headlineSmall)
                        .foregroundStyle(palette.textPrimary)
                }
                .padding(.top, state.dayChangeNotice == nil ? 20 : 4)
                .padding(.bottom, 4)

                TodayChallengeCard(
                    challenge: state.todayChallenge.localized(strings),
                    completed: state.completedToday,
                    onOpen: onOpenChallenge,
                    onStart: onStartChallenge
                )

                streakCard

                HStack(spacing: 12) {
                    StatTile(
                        value: "\(state.totalCompleted)",
                        label: strings.challengesCompletedLabel,
                        accent: OddlyColors.purple
                    )
                    StatTile(
                        value: "Lv.\(state.profile.level)",
                        label: strings.xpProgress(current: state.profile.xpInLevel, total: state.profile.xpForNextLevel),
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
        // Self-dismiss; a fresh notice restarts the timer because the id changes.
        .task(id: state.dayChangeNotice) {
            guard state.dayChangeNotice != nil else { return }
            try? await Task.sleep(for: .seconds(4))
            if !Task.isCancelled { state.clearDayChangeNotice() }
        }
    }

    private func noticeText(_ notice: DayChangeNotice) -> String {
        switch notice {
        case .theNewDay: return strings.newDayNotice
        case .completionRejected: return strings.dayChangedCompletionNotice
        default: return strings.newDayNotice
        }
    }

    private var streakCard: some View {
        OddlyCard(action: onOpenStreak) {
            HStack(spacing: 6) {
                SectionLabel(strings.streak)
                Spacer()
                Text("\(strings.streakDays(count: state.streak.current)) 🔥")
                    .font(OddlyFont.labelMedium)
                    .foregroundStyle(palette.flame)
                OddlyIconView(.chevronRight, size: 14, tint: palette.textTertiary)
            }
            WeekStrip(days: state.weekActivity)
                .padding(.top, 16)
        }
    }

    private var quoteCard: some View {
        let quote = state.quoteOfTheDay.localized(strings)
        return OddlyCard(action: onOpenQuotes) {
            HStack {
                SectionLabel(strings.dailyInspiration)
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

    private var exploreBody: String {
        if state.completedToday { return strings.wantAnotherChallengeDoneBody }
        if state.swapsLeft == 0 { return strings.noSwapsLeftBody }
        return strings.swapsLeftToday(count: Int32(state.swapsLeft))
    }

    private var exploreCard: some View {
        // Dimmed and inert once today is done or swaps ran out: closed until tomorrow.
        OddlyCard(action: state.canReroll ? onAnotherChallenge : nil) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(strings.wantAnotherChallenge)
                        .font(OddlyFont.titleSmall)
                        .foregroundStyle(palette.textPrimary)
                    Text(exploreBody)
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textTertiary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                OddlyIconView(.dice, size: 28, tint: OddlyColors.purple)
            }
        }
        .opacity(state.canReroll ? 1 : 0.45)
    }
}
