import SwiftUI
import SharedLogic

/// S13 — the streak. MVP keeps this encouraging: there is no punishment
/// mechanic, only the current run, the record, and a nudge to keep going.
struct StreakScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onBack: () -> Void

    var body: some View {
        let streak = state.streak

        VStack(spacing: 0) {
            OddlyTopBar(title: strings.streakTitle, onBack: onBack)

            ScrollView {
                VStack(spacing: 16) {
                    ZStack {
                        StarField(starCount: 30, seed: 43)
                            .frame(width: 280, height: 280)
                        GlowOrb(color: OddlyColors.flame, alpha: 0.4)
                            .frame(width: 260, height: 260)
                        VStack(spacing: 0) {
                            Text("\(streak.current)")
                                .font(OddlyFont.displayLarge)
                                .foregroundStyle(palette.textPrimary)
                            Text(strings.days)
                                .font(OddlyFont.bodyLarge)
                                .foregroundStyle(palette.textSecondary)
                        }
                        .frame(width: 190, height: 190)
                        .background(OddlyColors.flame.opacity(0.1), in: Circle())
                    }
                    .padding(.top, 12)

                    HStack(spacing: 12) {
                        StatTile(
                            value: "\(streak.best)",
                            label: strings.personalBest,
                            accent: OddlyColors.warning
                        )
                        StatTile(
                            value: "\(state.totalCompleted)",
                            label: strings.totalDone,
                            accent: OddlyColors.purple
                        )
                    }

                    OddlyCard {
                        SectionLabel(strings.lastSevenDays)
                        WeekStrip(days: state.weekActivity)
                            .padding(.top, 16)
                    }

                    encouragement

                    reminderCard

                    Text(strings.streakNoPenalty)
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textTertiary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 4)
                        .padding(.bottom, 32)
                }
                .padding(.horizontal, 20)
            }
        }
        .background(palette.background)
        .toolbar(.hidden, for: .navigationBar)
    }

    /// Encouragement, not punishment.
    private var encouragement: some View {
        HStack(alignment: .top, spacing: 14) {
            Text("🔥").font(OddlyFont.headlineSmall)
            VStack(alignment: .leading, spacing: 4) {
                Text(strings.keepStreakTitle)
                    .font(OddlyFont.titleSmall)
                    .foregroundStyle(palette.textPrimary)
                Text(state.completedToday ? strings.keepStreakDoneToday : strings.keepStreakPending)
                    .font(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(16)
        .background(
            OddlyColors.flame.opacity(0.1),
            in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
        )
    }

    private var reminderCard: some View {
        OddlyCard {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(strings.reminderLabel)
                        .font(OddlyFont.titleSmall)
                        .foregroundStyle(palette.textPrimary)
                    Text(strings.reminderToggleBody)
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textTertiary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                OddlyToggle(isOn: Binding(
                    get: { state.settings.reminderEnabled },
                    set: { state.settings = state.settings.with(reminderEnabled: $0) }
                ))
            }
        }
    }
}
