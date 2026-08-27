import SwiftUI
import SharedLogic

/// S03 — interest selection. Choosing nothing is allowed and simply means "use
/// every category", so this screen never blocks onboarding.
struct ChooseInterestScreen: View {
    @Environment(\.palette) private var palette

    let selected: Set<ChallengeCategory>
    let onToggle: (ChallengeCategory) -> Void
    let onContinue: () -> Void

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 50, seed: 13).ignoresSafeArea()

            VStack(spacing: 0) {
                Text("Bạn muốn tập trung vào\nkhía cạnh nào?")
                    .font(OddlyFont.headlineMedium)
                    .foregroundStyle(palette.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 28)

                Text("(Bạn có thể thay đổi sau)")
                    .font(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textTertiary)
                    .padding(.top, 10)

                VStack(spacing: 12) {
                    ForEach(ChallengeCategory.entries, id: \.self) { category in
                        InterestRow(
                            category: category,
                            selected: selected.contains(category),
                            onTap: { onToggle(category) }
                        )
                    }
                }
                .padding(.top, 28)

                Spacer(minLength: 24)

                GradientButton("Tiếp tục", action: onContinue)

                TextAction(
                    selected.isEmpty ? "Dùng tất cả chủ đề" : "\(selected.count) chủ đề đã chọn",
                    action: onContinue
                )
                .padding(.top, 8)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 24)
        }
    }
}

private struct InterestRow: View {
    @Environment(\.palette) private var palette

    let category: ChallengeCategory
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        let accent = category.color
        let shape = RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)

        Button(action: onTap) {
            HStack(spacing: 12) {
                Text(category.emoji).font(OddlyFont.titleMedium)
                Text(category.title)
                    .font(OddlyFont.bodyLarge)
                    .foregroundStyle(palette.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if selected {
                    OddlyIconView(.check, size: 14, tint: Color(rgb: 0x0B0B12), lineWidth: 2)
                        .frame(width: 22, height: 22)
                        .background(accent, in: Circle())
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 15)
            .background(selected ? accent.opacity(0.12) : palette.surfaceElevated, in: shape)
            .overlay(shape.stroke(selected ? accent.opacity(0.8) : .clear, lineWidth: 1.5))
            .contentShape(shape)
        }
        .buttonStyle(PressableStyle(pressedScale: 0.985))
        .animation(.easeOut(duration: 0.18), value: selected)
    }
}
