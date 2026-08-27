import SwiftUI
import SharedLogic

/// The visual hero of Home (spec §8.1). A gradient-edged card carrying today's
/// challenge and the single dominant CTA.
struct TodayChallengeCard: View {
    @Environment(\.palette) private var palette

    let challenge: Challenge
    let completed: Bool
    let onOpen: () -> Void
    let onStart: () -> Void

    var body: some View {
        let accent = challenge.category.color
        let shape = RoundedRectangle(cornerRadius: OddlyRadius.extraLarge, style: .continuous)

        VStack(alignment: .leading, spacing: 0) {
            HStack {
                SectionLabel("Thử thách hôm nay", color: palette.textSecondary)
                Spacer()
                Button(action: onOpen) {
                    OddlyIconView(.chevronRight, size: 18, tint: palette.textTertiary)
                }
                .buttonStyle(PressableStyle())
            }

            HStack(alignment: .top, spacing: 12) {
                Text(challenge.title)
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity, alignment: .leading)
                CompletionBadge(completed: completed, accent: accent)
            }
            .padding(.top, 16)

            Text(challenge.shortDescription)
                .font(OddlyFont.bodyMedium)
                .foregroundStyle(palette.textSecondary)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.top, 10)

            HStack(spacing: 8) {
                OddlyChip(text: challenge.category.title, accent: accent, leadingEmoji: challenge.category.emoji)
                OddlyChip(text: "\(challenge.estimatedMinutes) phút")
            }
            .padding(.top, 16)

            Group {
                if completed {
                    SecondaryButton("Đã hoàn thành hôm nay", leadingIcon: .check, action: onOpen)
                } else {
                    GradientButton("Tôi sẽ làm!", action: onStart)
                }
            }
            .padding(.top, 20)
        }
        .padding(20)
        .background(alignment: .top) {
            LinearGradient(
                colors: [accent.opacity(0.16), palette.surfaceElevated, palette.surfaceElevated],
                startPoint: .top,
                endPoint: .bottom
            )
            .overlay(alignment: .top) {
                StarField(starCount: 22, seed: 3).frame(height: 200)
            }
        }
        .clipShape(shape)
        .overlay(shape.stroke(accent.opacity(0.35), lineWidth: 1))
    }
}

private struct CompletionBadge: View {
    @Environment(\.palette) private var palette

    let completed: Bool
    let accent: Color

    var body: some View {
        OddlyIconView(
            .check,
            size: 16,
            tint: completed ? Color(rgb: 0x08240F) : palette.textTertiary,
            lineWidth: 2
        )
        .frame(width: 28, height: 28)
        .background(completed ? OddlyColors.success : .clear, in: Circle())
        .overlay(
            Circle().stroke(completed ? OddlyColors.success : accent.opacity(0.5), lineWidth: 1.5)
        )
    }
}

/// Compact challenge row used by the challenge library, calendar day lists and
/// history. `trailingText` carries the reward badge or a timestamp.
struct ChallengeRow: View {
    @Environment(\.palette) private var palette

    let challenge: Challenge
    var trailingText: String?
    var action: (() -> Void)?

    var body: some View {
        let accent = challenge.category.color
        let row = HStack(spacing: 12) {
            CategoryBadge(category: challenge.category)

            VStack(alignment: .leading, spacing: 3) {
                Text(challenge.title)
                    .font(OddlyFont.titleSmall)
                    .foregroundStyle(palette.textPrimary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                Text("\(challenge.category.title) · \(challenge.estimatedMinutes) phút")
                    .font(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textTertiary)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Text(trailingText ?? "+\(challenge.humanityPercent)%")
                .font(OddlyFont.labelMedium)
                .foregroundStyle(accent)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(palette.surfaceElevated, in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous))

        if let action {
            Button(action: action) { row }
                .buttonStyle(PressableStyle(pressedScale: 0.985))
        } else {
            row
        }
    }
}

/// Rounded square holding a category's emoji, tinted with its colour.
struct CategoryBadge: View {
    let category: ChallengeCategory
    var size: CGFloat = 40

    var body: some View {
        Text(category.emoji)
            .font(OddlyFont.titleMedium)
            .frame(width: size, height: size)
            .background(
                category.color.opacity(0.18),
                in: RoundedRectangle(cornerRadius: OddlyRadius.small, style: .continuous)
            )
    }
}

/// Rolling activity strip (Home and Streak screens).
///
/// The window ends today rather than on a fixed week boundary, so each cell is
/// labelled from its own date instead of a hardcoded Mon–Sun sequence.
struct WeekStrip: View {
    @Environment(\.palette) private var palette

    let days: [DayActivity]

    var body: some View {
        HStack {
            ForEach(Array(days.enumerated()), id: \.offset) { _, day in
                VStack(spacing: 6) {
                    Text(DateFormat.shared.shortWeekday(date: day.date))
                        .font(OddlyFont.labelSmall)
                        .foregroundStyle(day.isToday ? palette.textPrimary : palette.textTertiary)

                    ZStack {
                        Circle()
                            .fill(day.completed
                                  ? AnyShapeStyle(OddlyGradients.flame)
                                  : AnyShapeStyle(palette.surfaceHighest))
                        if day.completed {
                            Text("🔥").font(OddlyFont.labelMedium)
                        }
                    }
                    .frame(width: 30, height: 30)
                    .overlay(
                        Circle().stroke(day.isToday ? OddlyColors.purple : .clear, lineWidth: 1.5)
                    )
                }
                .frame(maxWidth: .infinity)
            }
        }
    }
}

/// Big-number tile used across Journey and Statistics.
struct StatTile: View {
    @Environment(\.palette) private var palette

    let value: String
    let label: String
    var accent: Color = OddlyColors.purple

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(value)
                .font(OddlyFont.headlineMedium)
                .foregroundStyle(accent)
                .lineLimit(1)
                .minimumScaleFactor(0.6)
            Text(label)
                .font(OddlyFont.bodySmall)
                .foregroundStyle(palette.textTertiary)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(palette.surfaceElevated, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}

/// Friendly zero-data state (spec §S19). Empty is framed as the start of a
/// journey, never as a failure.
struct EmptyState: View {
    @Environment(\.palette) private var palette

    let title: String
    var subtitle: String?
    var actionText: String?
    var action: (() -> Void)?

    var body: some View {
        VStack(spacing: 0) {
            StarryBox(starCount: 30, seed: 21) {
                Astronaut(size: 140)
            }
            .frame(height: 180)

            Text(title)
                .font(OddlyFont.titleLarge)
                .foregroundStyle(palette.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 12)

            if let subtitle {
                Text(subtitle)
                    .font(OddlyFont.bodyMedium)
                    .foregroundStyle(palette.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 8)
            }

            if let actionText, let action {
                GradientButton(actionText, action: action)
                    .padding(.top, 24)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(24)
    }
}

/// Settings-style row: leading icon, title, optional value, trailing slot.
struct SettingsRow<Trailing: View>: View {
    @Environment(\.palette) private var palette

    let icon: OddlyIcon
    let title: String
    var value: String?
    var tint: Color?
    var showChevron: Bool = true
    var action: (() -> Void)?
    @ViewBuilder var trailing: () -> Trailing

    var body: some View {
        let iconTint = tint ?? palette.textSecondary
        let hasTrailing = !(Trailing.self == EmptyView.self)

        let row = HStack(spacing: 0) {
            OddlyIconView(icon, size: 20, tint: iconTint)
            Text(title)
                .font(OddlyFont.bodyLarge)
                .foregroundStyle(tint ?? palette.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.leading, 14)
            if let value {
                Text(value)
                    .font(OddlyFont.bodyMedium)
                    .foregroundStyle(palette.textTertiary)
                    .padding(.trailing, 8)
            }
            trailing()
            if showChevron && !hasTrailing {
                OddlyIconView(.chevronRight, size: 16, tint: palette.textTertiary)
                    .padding(.leading, 4)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .contentShape(Rectangle())

        if let action {
            Button(action: action) { row }
                .buttonStyle(PressableStyle(pressedScale: 0.99))
        } else {
            row
        }
    }
}

extension SettingsRow where Trailing == EmptyView {
    init(
        icon: OddlyIcon,
        title: String,
        value: String? = nil,
        tint: Color? = nil,
        showChevron: Bool = true,
        action: (() -> Void)? = nil
    ) {
        self.init(
            icon: icon,
            title: title,
            value: value,
            tint: tint,
            showChevron: showChevron,
            action: action
        ) { EmptyView() }
    }
}

/// Groups settings rows into one rounded surface.
struct SettingsGroup<Content: View>: View {
    @Environment(\.palette) private var palette
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(spacing: 0) { content() }
            .frame(maxWidth: .infinity)
            .background(palette.surfaceElevated, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}
