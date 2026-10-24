import SwiftUI
import SharedLogic

/// S03 — interest selection. Choosing nothing is allowed and simply means "use
/// every category", so this screen never blocks onboarding.
struct ChooseInterestScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let selected: Set<ChallengeCategory>
    let onToggle: (ChallengeCategory) -> Void
    let onContinue: () -> Void

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 50, seed: 13).ignoresSafeArea()

            FillScrollView {
                VStack(spacing: 0) {
                    Text(strings.interestsTitle)
                        .oddlyFont(OddlyFont.headlineMedium)
                        .foregroundStyle(palette.textPrimary)
                        .multilineTextAlignment(.center)
                        .accessibilityHeading()
                        .padding(.top, 28)

                    Text(strings.interestsSubtitle)
                        .oddlyFont(OddlyFont.bodySmall)
                        .foregroundStyle(palette.textTertiary)
                        .padding(.top, 10)

                    VStack(spacing: 12) {
                        ForEach(ChallengeCategory.entries, id: \.self) { category in
                            InterestRow(
                                category: category,
                                selected: selected.contains(category),
                                onTap: { onToggle(category) }
                            )
                            .accessibilityIdentifier(UiTestTags.shared.interest(category: category))
                        }
                    }
                    .padding(.top, 28)

                    Spacer(minLength: 24)

                    GradientButton(strings.interestsContinue, action: onContinue)
                        .accessibilityIdentifier(UiTestTags.shared.INTERESTS_CONTINUE)

                    TextAction(
                        selected.isEmpty
                            ? strings.interestsUseAll
                            : strings.topicsSelected(count: Int32(selected.count)),
                        action: onContinue
                    )
                    .padding(.top, 8)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 24)
            }
        }
    }
}

private struct InterestRow: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let category: ChallengeCategory
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        let accent = category.color
        let shape = RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)

        Button(action: onTap) {
            HStack(spacing: 12) {
                Text(category.emoji).oddlyFont(OddlyFont.titleMedium).accessibilityHidden(true)
                Text(category.title.of(strings))
                    .oddlyFont(OddlyFont.bodyLarge)
                    .foregroundStyle(palette.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if selected {
                    OddlyIconView(.check, size: 14, tint: Color(rgb: 0x0B0B12), lineWidth: 2)
                        .frame(width: 22, height: 22)
                        .background(accent, in: Circle())
                        .accessibilityHidden(true)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 15)
            .background(selected ? accent.opacity(0.12) : palette.surfaceElevated, in: shape)
            .overlay(shape.stroke(selected ? accent.opacity(0.8) : .clear, lineWidth: 1.5))
            .contentShape(shape)
        }
        .buttonStyle(PressableStyle(pressedScale: 0.985))
        .accessibilitySelected(selected)
        .animation(.easeOut(duration: 0.18), value: selected)
    }
}
