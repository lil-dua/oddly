import SwiftUI
import SharedLogic

/// S12 — completion counts, trend and category mix across four time ranges.
struct StatisticsScreen: View {
    @Environment(\.palette) private var palette

    let state: OddlyAppState
    let onStartFirstChallenge: () -> Void

    @State private var range: StatsRange = .week

    var body: some View {
        let summary = StatsCalculator.shared.summarize(
            completions: state.completions,
            today: state.today,
            range: range
        )

        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Thống kê")
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .padding(.top, 20)
                    .padding(.bottom, 4)

                if state.completions.isEmpty {
                    EmptyState(
                        title: "Chưa có dữ liệu để thống kê",
                        subtitle: "Hoàn thành thử thách đầu tiên\nđể bắt đầu theo dõi tiến trình.",
                        actionText: "Khám phá thử thách",
                        action: onStartFirstChallenge
                    )
                } else {
                    RangeTabs(selected: range, onSelect: { range = $0 })
                        .padding(.bottom, 4)

                    OddlyCard {
                        SectionLabel("Tổng thử thách đã hoàn thành")

                        HStack(alignment: .bottom, spacing: 12) {
                            Text("\(summary.totalCompleted)")
                                .font(OddlyFont.displaySmall)
                                .foregroundStyle(palette.textPrimary)
                            if range != StatsRange.all {
                                let delta = summary.deltaVsPreviousPeriod
                                Text(delta >= 0
                                     ? "+\(delta) so với kỳ trước"
                                     : "\(delta) so với kỳ trước")
                                    .font(OddlyFont.bodySmall)
                                    .foregroundStyle(delta >= 0 ? OddlyColors.success : palette.textTertiary)
                                    .padding(.bottom, 6)
                            }
                        }
                        .padding(.top, 10)

                        BarChart(bars: summary.bars)
                            .padding(.top, 20)
                    }

                    HStack(spacing: 12) {
                        StatTile(
                            value: summary.mostActiveCategory?.emoji ?? "–",
                            label: summary.mostActiveCategory?.title ?? "Chưa có dữ liệu",
                            accent: summary.mostActiveCategory?.color ?? palette.textTertiary
                        )
                        StatTile(
                            value: "\(state.completionRatePercent)%",
                            label: "Tỷ lệ hoàn thành",
                            accent: OddlyColors.success
                        )
                    }

                    OddlyCard {
                        SectionLabel("Phân bổ chủ đề")
                        if summary.distribution.isEmpty {
                            Text("Chưa có thử thách nào trong khoảng thời gian này.")
                                .font(OddlyFont.bodyMedium)
                                .foregroundStyle(palette.textTertiary)
                                .padding(.top, 14)
                        } else {
                            CategoryDistribution(slices: summary.distribution)
                                .padding(.top, 14)
                        }
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 28)
        }
        .background(palette.background)
        .statusBarScrim(palette.background)
    }
}

private struct RangeTabs: View {
    @Environment(\.palette) private var palette

    let selected: StatsRange
    let onSelect: (StatsRange) -> Void

    var body: some View {
        HStack(spacing: 4) {
            ForEach(Array(StatsRange.entries.enumerated()), id: \.offset) { _, entry in
                let active = entry == selected
                Button {
                    onSelect(entry)
                } label: {
                    Text(entry.title)
                        .font(OddlyFont.labelMedium)
                        .foregroundStyle(active ? OddlyColors.purple : palette.textTertiary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(
                            active ? OddlyColors.purple.opacity(0.22) : .clear,
                            in: RoundedRectangle(cornerRadius: 10, style: .continuous)
                        )
                }
                .buttonStyle(PressableStyle())
            }
        }
        .padding(4)
        .background(
            palette.surfaceElevated,
            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
        )
        .animation(.easeOut(duration: 0.2), value: selected)
    }
}

/// Simple vertical bar chart. Bars are scaled against the busiest bucket so the
/// shape stays readable whatever the absolute counts are.
private struct BarChart: View {
    @Environment(\.palette) private var palette

    let bars: [DayBar]

    var body: some View {
        let maxCount = max(bars.map { Int($0.count) }.max() ?? 0, 1)

        HStack(alignment: .bottom, spacing: 10) {
            ForEach(Array(bars.enumerated()), id: \.offset) { _, bar in
                let fraction = Double(bar.count) / Double(maxCount)

                VStack(spacing: 0) {
                    Text(bar.count > 0 ? "\(bar.count)" : " ")
                        .font(OddlyFont.labelSmall)
                        .tracking(0.5)
                        .foregroundStyle(palette.textTertiary)

                    GeometryReader { geometry in
                        VStack(spacing: 0) {
                            Spacer(minLength: 0)
                            UnevenRoundedRectangle(
                                topLeadingRadius: 6,
                                bottomLeadingRadius: 0,
                                bottomTrailingRadius: 0,
                                topTrailingRadius: 6
                            )
                            .fill(bar.count > 0
                                  ? AnyShapeStyle(OddlyGradients.progress)
                                  : AnyShapeStyle(palette.surfaceHighest))
                            // Keep a sliver visible for empty buckets so the
                            // axis still reads as a chart.
                            .frame(height: geometry.size.height * max(fraction, 0.02))
                        }
                    }
                    .padding(.top, 6)

                    Text(bar.label)
                        .font(OddlyFont.labelSmall)
                        .tracking(0.5)
                        .foregroundStyle(bar.isToday ? palette.textPrimary : palette.textTertiary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)
                        .padding(.top, 8)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .frame(height: 140)
        .animation(.easeOut(duration: 0.5), value: bars.map { Int($0.count) })
    }
}
