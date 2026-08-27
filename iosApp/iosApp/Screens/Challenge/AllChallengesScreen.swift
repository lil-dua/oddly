import SwiftUI
import SharedLogic

/// S20 — the full challenge library with a category filter. Search is P1; the
/// spec notes filters matter more than search at this catalogue size.
struct AllChallengesScreen: View {
    @Environment(\.palette) private var palette

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
            OddlyTopBar(title: "Tất cả thử thách", onBack: onBack)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    OddlyChip(
                        text: "Tất cả",
                        accent: OddlyColors.purple,
                        selected: filter == nil,
                        action: { filter = nil }
                    )
                    ForEach(ChallengeCategory.entries, id: \.self) { category in
                        OddlyChip(
                            text: category.title,
                            accent: category.color,
                            leadingEmoji: category.emoji,
                            selected: filter == category,
                            action: { filter = category }
                        )
                    }
                }
                .padding(.horizontal, 20)
            }

            Text("\(challenges.count) thử thách")
                .font(OddlyFont.labelSmall)
                .tracking(0.5)
                .foregroundStyle(palette.textTertiary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 8)

            if challenges.isEmpty {
                ScrollView {
                    EmptyState(
                        title: "Không tìm thấy thử thách nào",
                        subtitle: "Thử chọn một chủ đề khác.",
                        actionText: "Xem tất cả",
                        action: { filter = nil }
                    )
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(challenges.map(ChallengeRef.init)) { ref in
                            ChallengeRow(
                                challenge: ref.challenge,
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
