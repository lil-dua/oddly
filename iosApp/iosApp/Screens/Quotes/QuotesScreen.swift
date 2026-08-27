import SwiftUI
import SharedLogic

/// S14 — one quote a day, from the bundled offline database. Paging through the
/// archive and favouriting are P1 niceties already wired up here.
struct QuotesScreen: View {
    @Environment(\.palette) private var palette

    let state: OddlyAppState
    let onBack: () -> Void
    let onShare: () -> Void

    @State private var index: Int
    @State private var favorites: Set<String> = []

    private let quotes = QuoteSeed.shared.all

    init(state: OddlyAppState, onBack: @escaping () -> Void, onShare: @escaping () -> Void) {
        self.state = state
        self.onBack = onBack
        self.onShare = onShare
        let all = QuoteSeed.shared.all
        let today = state.today.epochDays
        _index = State(initialValue: ((today % all.count) + all.count) % all.count)
    }

    var body: some View {
        let quote = quotes[index]
        let isFavorite = favorites.contains(quote.id)

        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 55, seed: 47).ignoresSafeArea()

            VStack(spacing: 0) {
                OddlyTopBar(title: "", onBack: onBack)

                VStack(spacing: 0) {
                    Text("Cảm hứng mỗi ngày")
                        .font(OddlyFont.headlineSmall)
                        .foregroundStyle(palette.textPrimary)

                    Button {
                        if isFavorite { favorites.remove(quote.id) } else { favorites.insert(quote.id) }
                    } label: {
                        OddlyIconView(
                            .heart,
                            size: 20,
                            tint: isFavorite ? OddlyColors.pink : palette.textTertiary
                        )
                        .frame(width: 44, height: 44)
                        .background(
                            isFavorite ? OddlyColors.pink.opacity(0.18) : palette.surfaceElevated,
                            in: Circle()
                        )
                    }
                    .buttonStyle(PressableStyle())
                    .padding(.top, 20)

                    Spacer(minLength: 0)

                    quoteCard(quote)

                    HStack(spacing: 20) {
                        CircleIconButton(.chevronLeft, diameter: 36, iconSize: 16, tint: palette.textSecondary) {
                            index = (index - 1 + quotes.count) % quotes.count
                        }
                        Text("\(index + 1) / \(quotes.count)")
                            .font(OddlyFont.labelSmall)
                            .tracking(0.5)
                            .foregroundStyle(palette.textTertiary)
                        CircleIconButton(.chevronRight, diameter: 36, iconSize: 16, tint: palette.textSecondary) {
                            index = (index + 1) % quotes.count
                        }
                    }
                    .padding(.top, 20)

                    Spacer(minLength: 0)

                    GradientButton("Chia sẻ", leadingIcon: .share, action: onShare)

                    reminderRow
                        .padding(.top, 20)
                        .padding(.bottom, 28)
                }
                .padding(.horizontal, 24)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    private func quoteCard(_ quote: Quote) -> some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("“\(quote.text)”")
                .font(OddlyFont.headlineSmall)
                .foregroundStyle(palette.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
            Text("– \(quote.author)")
                .font(OddlyFont.bodyMedium)
                .foregroundStyle(palette.textSecondary)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(28)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            LinearGradient(
                colors: [OddlyColors.purple.opacity(0.16), palette.surfaceElevated],
                startPoint: .top,
                endPoint: .bottom
            ),
            in: RoundedRectangle(cornerRadius: OddlyRadius.extraLarge, style: .continuous)
        )
    }

    private var reminderRow: some View {
        HStack {
            VStack(alignment: .leading, spacing: 0) {
                Text("Lời nhắc")
                    .font(OddlyFont.titleSmall)
                    .foregroundStyle(palette.textPrimary)
                Text("Bật thông báo hằng ngày")
                    .font(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textTertiary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            OddlyToggle(isOn: Binding(
                get: { state.settings.reminderEnabled },
                set: { state.settings = state.settings.with(reminderEnabled: $0) }
            ))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            palette.surfaceElevated,
            in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
        )
    }
}
