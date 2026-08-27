import SwiftUI

/// The standard content container: rounded, slightly lifted, hairline border.
struct OddlyCard<Content: View>: View {
    @Environment(\.palette) private var palette

    var cornerRadius: CGFloat = OddlyRadius.large
    var background: Color?
    var borderColor: Color?
    var contentPadding: CGFloat = 18
    var action: (() -> Void)?
    @ViewBuilder var content: () -> Content

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
        let surface = VStack(alignment: .leading, spacing: 0) { content() }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(contentPadding)
            .background(background ?? palette.surface, in: shape)
            .overlay(shape.stroke(borderColor ?? palette.outline.opacity(0.6), lineWidth: 1))

        if let action {
            Button(action: action) { surface }
                .buttonStyle(PressableStyle(pressedScale: 0.985))
        } else {
            surface
        }
    }
}

/// Card variant filled with a gradient — used for hero and category surfaces.
struct GradientCard<Content: View>: View {
    var gradient: LinearGradient
    var cornerRadius: CGFloat = OddlyRadius.large
    var borderColor: Color = .clear
    var contentPadding: CGFloat = 18
    var action: (() -> Void)?
    @ViewBuilder var content: () -> Content

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
        let surface = VStack(alignment: .leading, spacing: 0) { content() }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(contentPadding)
            .background(gradient, in: shape)
            .overlay(shape.stroke(borderColor, lineWidth: 1))

        if let action {
            Button(action: action) { surface }
                .buttonStyle(PressableStyle(pressedScale: 0.985))
        } else {
            surface
        }
    }
}

/// Text painted with a gradient — the "1%" wordmark and hero numbers.
struct GradientText: View {
    let text: String
    var font: Font
    var gradient: LinearGradient = OddlyGradients.brandText
    var tracking: CGFloat = 0

    init(_ text: String, font: Font, gradient: LinearGradient = OddlyGradients.brandText, tracking: CGFloat = 0) {
        self.text = text
        self.font = font
        self.gradient = gradient
        self.tracking = tracking
    }

    var body: some View {
        Text(text)
            .font(font)
            .tracking(tracking)
            .multilineTextAlignment(.center)
            .foregroundStyle(gradient)
    }
}

/// Small uppercase label that heads a section, e.g. "GỢI Ý", "PHẦN THƯỞNG".
struct SectionLabel: View {
    @Environment(\.palette) private var palette

    let text: String
    var color: Color?

    init(_ text: String, color: Color? = nil) {
        self.text = text
        self.color = color
    }

    var body: some View {
        Text(text.uppercased())
            .font(OddlyFont.labelSmall)
            .tracking(0.5)
            .foregroundStyle(color ?? palette.textTertiary)
    }
}

/// A rounded progress track filled with a gradient.
struct GradientProgressBar: View {
    @Environment(\.palette) private var palette

    let progress: Double
    var height: CGFloat = 8
    var gradient: LinearGradient = OddlyGradients.progress
    var trackColor: Color?

    var body: some View {
        GeometryReader { geometry in
            let clamped = min(max(progress, 0), 1)
            ZStack(alignment: .leading) {
                Capsule().fill(trackColor ?? palette.surfaceHighest)
                Capsule()
                    .fill(gradient)
                    .frame(width: geometry.size.width * clamped)
            }
        }
        .frame(height: height)
        .animation(.easeOut(duration: 0.35), value: progress)
    }
}

/// Screen header with an optional back affordance and trailing slot.
struct OddlyTopBar<Trailing: View>: View {
    @Environment(\.palette) private var palette

    let title: String
    var onBack: (() -> Void)?
    @ViewBuilder var trailing: () -> Trailing

    var body: some View {
        HStack(spacing: 12) {
            if let onBack {
                CircleIconButton(.chevronLeft, diameter: 38, iconSize: 20, action: onBack)
            }
            Text(title)
                .font(OddlyFont.headlineSmall.weight(.bold))
                .foregroundStyle(palette.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
            trailing()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }
}

extension OddlyTopBar where Trailing == EmptyView {
    init(title: String, onBack: (() -> Void)? = nil) {
        self.init(title: title, onBack: onBack) { EmptyView() }
    }
}

/// A pill-shaped tag: difficulty, category, estimated time.
struct OddlyChip: View {
    @Environment(\.palette) private var palette

    let text: String
    var accent: Color?
    var leadingEmoji: String?
    var selected: Bool = false
    var action: (() -> Void)?

    var body: some View {
        let tint = accent ?? palette.textSecondary
        let pill = HStack(spacing: 6) {
            if let leadingEmoji {
                Text(leadingEmoji).font(OddlyFont.labelMedium)
            }
            Text(text)
                .font(OddlyFont.labelMedium)
                .foregroundStyle(selected ? tint : palette.textSecondary)
                .lineLimit(1)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 7)
        .background(selected ? tint.opacity(0.2) : palette.surfaceElevated, in: Capsule())
        .overlay(Capsule().stroke(selected ? tint.opacity(0.7) : .clear, lineWidth: 1))

        if let action {
            Button(action: action) { pill }
                .buttonStyle(PressableStyle())
        } else {
            pill
        }
    }
}

/// Section heading with an optional trailing action, e.g. "Xem tất cả".
struct SectionHeader: View {
    @Environment(\.palette) private var palette

    let title: String
    var actionText: String?
    var action: (() -> Void)?

    var body: some View {
        HStack {
            Text(title)
                .font(OddlyFont.titleMedium)
                .foregroundStyle(palette.textPrimary)
            Spacer()
            if let actionText, let action {
                TextAction(actionText, color: OddlyColors.purple, action: action)
            }
        }
    }
}

/// A switch tinted to the app's palette rather than the system accent.
struct OddlyToggle: View {
    @Binding var isOn: Bool

    var body: some View {
        Toggle("", isOn: $isOn)
            .labelsHidden()
            .tint(OddlyColors.success)
    }
}
