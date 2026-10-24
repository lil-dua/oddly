import SwiftUI
import SharedLogic

/// S10 — completion history by day. An empty day is neutral, never framed as a
/// miss (spec §S10).
struct CalendarScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onBack: () -> Void

    @State private var visibleMonth: LocalDate
    @State private var selectedDate: LocalDate?

    init(state: OddlyAppState, onBack: @escaping () -> Void) {
        self.state = state
        self.onBack = onBack
        _visibleMonth = State(initialValue: state.today.startOfMonth)
        _selectedDate = State(initialValue: state.today)
    }

    private var completedDays: Set<Int> {
        Set(state.completions.map(\.date.epochDays))
    }

    var body: some View {
        VStack(spacing: 0) {
            OddlyTopBar(title: strings.calendar, onBack: onBack)

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    monthSwitcher

                    HStack(spacing: 0) {
                        ForEach(DateFormat.shared.weekdayHeaders(language: strings.language), id: \.self) { label in
                            Text(label)
                                .oddlyFont(OddlyFont.labelSmall)
                                .tracking(0.5)
                                .foregroundStyle(palette.textTertiary)
                                .frame(maxWidth: .infinity)
                        }
                    }
                    // Cells are fixed-size; cap so two-digit days and weekday labels stay inside them.
                    .dynamicTypeSize(...DynamicTypeSize.accessibility1)
                    .padding(.top, 20)
                    // Each day cell already speaks its full weekday, so the header row is redundant.
                    .accessibilityHidden(true)

                    MonthGrid(
                        month: visibleMonth,
                        today: state.today,
                        selected: selectedDate,
                        completedDays: completedDays,
                        onSelect: { selectedDate = $0 }
                    )
                    .dynamicTypeSize(...DynamicTypeSize.accessibility1)
                    .padding(.top, 10)

                    if let date = selectedDate {
                        selectedDaySection(date)
                            .padding(.top, 24)
                    }

                    recentSection
                        .padding(.top, 24)
                        .padding(.bottom, 28)
                }
                .padding(.horizontal, 20)
            }
        }
        .background(palette.background)
        .toolbar(.hidden, for: .navigationBar)
    }

    private var monthSwitcher: some View {
        HStack(spacing: 8) {
            Text(DateFormat.shared.monthAndYear(date: visibleMonth, language: strings.language))
                .oddlyFont(OddlyFont.titleLarge)
                .foregroundStyle(palette.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityHeading()
            CircleIconButton(
                .chevronLeft,
                label: strings.a11yPreviousMonth,
                diameter: 34,
                iconSize: 16,
                tint: palette.textSecondary
            ) {
                visibleMonth = visibleMonth.shiftingMonth(by: -1)
            }
            CircleIconButton(
                .chevronRight,
                label: strings.a11yNextMonth,
                diameter: 34,
                iconSize: 16,
                tint: palette.textSecondary
            ) {
                visibleMonth = visibleMonth.shiftingMonth(by: 1)
            }
        }
        // Android uses a live region; here the new month is announced explicitly.
        .onChange(of: visibleMonth) { _, month in
            announce(DateFormat.shared.monthAndYear(date: month, language: strings.language))
        }
    }

    @ViewBuilder
    private func selectedDaySection(_ date: LocalDate) -> some View {
        let dayCompletions = state.resolved(state.completionsOn(date))

        VStack(alignment: .leading, spacing: 12) {
            SectionLabel(DateFormat.shared.numeric(date: date))

            if dayCompletions.isEmpty {
                VStack(spacing: 10) {
                    Text("🌙").oddlyFont(OddlyFont.headlineMedium).accessibilityHidden(true)
                    Text(strings.noChallengeThisDay)
                        .oddlyFont(OddlyFont.bodyMedium)
                        .foregroundStyle(palette.textSecondary)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(20)
                .background(
                    palette.surfaceElevated,
                    in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
                )
            } else {
                VStack(spacing: 10) {
                    ForEach(dayCompletions) { entry in
                        ChallengeRow(
                            challenge: entry.challenge.localized(strings),
                            trailingText: "+\(entry.completion.humanityPercent)%"
                        )
                    }
                }
            }
        }
    }

    private var recentSection: some View {
        let recent = state.resolved(
            state.completions
                .sorted { $0.date.epochDays > $1.date.epochDays }
                .prefix(6)
                .map { $0 }
        )

        return VStack(alignment: .leading, spacing: 12) {
            SectionLabel(strings.recent)
            ForEach(recent) { entry in
                VStack(alignment: .leading, spacing: 6) {
                    Text(DateFormat.shared.numeric(date: entry.completion.date))
                        .oddlyFont(OddlyFont.labelSmall)
                        .tracking(0.5)
                        .foregroundStyle(palette.textTertiary)
                    ChallengeRow(
                        challenge: entry.challenge.localized(strings),
                        trailingText: "+\(entry.completion.humanityPercent)%"
                    )
                }
            }
        }
    }
}

private struct MonthGrid: View {
    let month: LocalDate
    let today: LocalDate
    let selected: LocalDate?
    let completedDays: Set<Int>
    let onSelect: (LocalDate) -> Void

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)

    /// Pads the head so the 1st lands under the right weekday, and the tail so
    /// the final row is a complete week.
    private var cells: [LocalDate?] {
        let daysInMonth = Int(DateFormat.shared.daysInMonth(date: month))
        let leadingBlanks = Int(DateFormat.shared.weekdayIndex(date: month))

        var result: [LocalDate?] = Array(repeating: nil, count: leadingBlanks)
        for day in 1...daysInMonth {
            result.append(LocalDate(year: month.year, monthNumber: month.monthNumber, dayOfMonth: Int32(day)))
        }
        while result.count % 7 != 0 { result.append(nil) }
        return result
    }

    var body: some View {
        LazyVGrid(columns: columns, spacing: 6) {
            ForEach(Array(cells.enumerated()), id: \.offset) { _, date in
                if let date {
                    DayCell(
                        date: date,
                        isToday: date == today,
                        isSelected: date == selected,
                        isCompleted: completedDays.contains(date.epochDays),
                        onTap: { onSelect(date) }
                    )
                } else {
                    Color.clear.frame(height: DayCell.hitHeight)
                }
            }
        }
    }
}

private struct DayCell: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    /// Fixed rather than derived from the column width: an `aspectRatio` here
    /// collapses to the label's intrinsic height inside a `LazyVGrid`, which
    /// clips two-digit dates.
    static let diameter: CGFloat = 40
    /// Row height: the drawn circle stays 40pt but the tap area is 44pt tall.
    /// Width is the grid column (about 45pt on a 393pt screen; narrower phones fall a few points short).
    static let hitHeight: CGFloat = 44

    let date: LocalDate
    let isToday: Bool
    let isSelected: Bool
    let isCompleted: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text("\(date.dayOfMonth)")
                .oddlyFont(OddlyFont.bodySmall)
                .foregroundStyle(textColor)
                .frame(width: Self.diameter, height: Self.diameter)
                .background {
                    if isCompleted {
                        Circle().fill(OddlyGradients.flame)
                    } else if isSelected {
                        Circle().fill(OddlyColors.purple.opacity(0.2))
                    }
                }
                .overlay {
                    if isSelected && !isCompleted {
                        Circle().stroke(OddlyColors.purple, lineWidth: 1.5)
                    } else if isToday && !isCompleted {
                        Circle().stroke(palette.textTertiary, lineWidth: 1)
                    }
                }
                .frame(maxWidth: .infinity, minHeight: Self.hitHeight)
                .contentShape(Rectangle())
        }
        .buttonStyle(PressableStyle(pressedScale: 0.9))
        .accessibilityLabel(AccessibilityText.shared.calendarDay(
            date: date,
            completed: isCompleted,
            isToday: isToday,
            strings: strings
        ))
        .accessibilitySelected(isSelected)
    }

    private var textColor: Color {
        if isCompleted { return Color(rgb: 0x2B1206) }
        if isToday || isSelected { return palette.textPrimary }
        return palette.textSecondary
    }
}
