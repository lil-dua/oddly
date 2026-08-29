import SwiftUI
import SharedLogic

/// S09 — picking a category narrows the randomisation pool. Each tile shows how
/// far the user has got in that category so the grid doubles as progress.
struct ChooseCategoryScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onBack: () -> Void
    let onPick: (ChallengeCategory) -> Void

    /// Counts distinct challenges cleared, not completions: the same challenge
    /// can be done on several days, and "12 / 10 done" would be nonsense.
    private var completedPerCategory: [ChallengeCategory: Int] {
        var counts: [ChallengeCategory: Int] = [:]
        for id in Set(state.completions.map(\.challengeId)) {
            guard let category = ChallengeSeed.shared.byId(id: id)?.category else { continue }
            counts[category, default: 0] += 1
        }
        return counts
    }

    private let columns = [
        GridItem(.flexible(), spacing: 14),
        GridItem(.flexible(), spacing: 14),
    ]

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 45, seed: 37).ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    // Outside the horizontal padding so the back chevron lines
                    // up with every other screen's.
                    OddlyTopBar(title: "", onBack: onBack)

                    VStack(spacing: 0) {
                        Text(strings.chooseCategoryTitle)
                            .font(OddlyFont.headlineMedium)
                            .foregroundStyle(palette.textPrimary)
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: .infinity)

                        let counts = completedPerCategory
                        LazyVGrid(columns: columns, spacing: 14) {
                            ForEach(ChallengeCategory.entries, id: \.self) { category in
                                CategoryTile(
                                    category: category,
                                    completedCount: counts[category] ?? 0,
                                    totalCount: ChallengeSeed.shared.byCategory(category: category).count,
                                    onTap: { onPick(category) }
                                )
                            }
                        }
                        .padding(.top, 28)

                        SecondaryButton(strings.back, action: onBack)
                            .padding(.top, 30)
                            .padding(.bottom, 32)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
        .statusBarScrim(palette.background)
        .toolbar(.hidden, for: .navigationBar)
    }
}

private struct CategoryTile: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let category: ChallengeCategory
    let completedCount: Int
    let totalCount: Int
    let onTap: () -> Void

    var body: some View {
        let accent = category.color
        let shape = RoundedRectangle(cornerRadius: OddlyRadius.large, style: .continuous)

        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 0) {
                Text(category.emoji).font(OddlyFont.displaySmall)
                Spacer(minLength: 8)
                VStack(alignment: .leading, spacing: 4) {
                    Text(category.title.of(strings))
                        .font(OddlyFont.titleSmall)
                        .foregroundStyle(palette.textPrimary)
                        .multilineTextAlignment(.leading)
                    Text(strings.doneOutOf(done: Int32(completedCount), total: Int32(totalCount)))
                        .font(OddlyFont.bodySmall)
                        .foregroundStyle(accent)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .aspectRatio(1, contentMode: .fill)
            .padding(16)
            .background(OddlyGradients.categoryWash(accent), in: shape)
            .overlay(shape.stroke(accent.opacity(0.25), lineWidth: 1))
            .contentShape(shape)
        }
        .buttonStyle(PressableStyle(pressedScale: 0.97))
    }
}
