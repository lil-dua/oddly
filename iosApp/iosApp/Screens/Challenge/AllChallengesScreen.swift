import SwiftUI
import SharedLogic

/// S20 — the full challenge library with a category filter. Search is P1; the
/// spec notes filters matter more than search at this catalogue size.
struct AllChallengesScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onBack: () -> Void
    let onSelect: (Challenge) -> Void

    @State private var filter: ChallengeCategory?

    private var challenges: [Challenge] {
        guard let filter else { return ChallengeSeed.shared.all }
        return ChallengeSeed.shared.byCategory(category: filter)
    }

    var body: some View {
        VStack(spacing: 0) {
            OddlyTopBar(title: strings.allChallengesTitle, onBack: onBack)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    OddlyChip(
                        text: strings.filterAll,
                        accent: OddlyColors.purple,
                        selected: filter == nil,
                        action: { filter = nil }
                    )
                    ForEach(ChallengeCategory.entries, id: \.self) { category in
                        OddlyChip(
                            text: category.title.of(strings),
                            accent: category.color,
                            leadingEmoji: category.emoji,
                            selected: filter == category,
                            action: { filter = category }
                        )
                    }
                }
                .padding(.horizontal, 20)
            }

            Text(strings.challengeCount(count: Int32(challenges.count)))
                .font(OddlyFont.labelSmall)
                .tracking(0.5)
                .foregroundStyle(palette.textTertiary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 8)

            if challenges.isEmpty {
                ScrollView {
                    EmptyState(
                        title: strings.noChallengesFound,
                        subtitle: strings.noChallengesFoundBody,
                        actionText: strings.seeAll,
                        action: { filter = nil }
                    )
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(challenges.map(ChallengeRef.init)) { ref in
                            ChallengeRow(
                                challenge: ref.challenge.localized(strings),
                                trailingText: state.isCompleted(ref.id)
                                    ? "✓"
                                    : "+\(ref.challenge.humanityPercent)%",
                                action: { onSelect(ref.challenge) }
                            )
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 12)
                    .padding(.bottom, 28)
                }
            }
        }
        .background(palette.background)
        .toolbar(.hidden, for: .navigationBar)
    }
}
