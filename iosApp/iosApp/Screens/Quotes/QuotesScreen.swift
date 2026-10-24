import SwiftUI
import SharedLogic

/// S14 — one quote a day, from the bundled offline database. Paging through the
/// archive is a P1 nicety. Hearts live in `OddlyAppState` so they survive leaving
/// the screen; the Favorites filter is a snapshot taken when the chip is tapped,
/// so un-hearting a card does not make it vanish under the reader.
struct QuotesScreen: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState
    let onBack: () -> Void
    let onShare: () -> Void

    @State private var index: Int
    @State private var showFavorites = false
    @State private var favoriteSnapshot: [Quote] = []

    private let allQuotes = QuoteSeed.shared.all
    private let todayIndex: Int

    private var quotes: [Quote] { showFavorites ? favoriteSnapshot : allQuotes }

    init(state: OddlyAppState, onBack: @escaping () -> Void, onShare: @escaping () -> Void) {
        self.state = state
        self.onBack = onBack
        self.onShare = onShare
        let all = QuoteSeed.shared.all
        let today = state.today.epochDays
        let start = ((today % all.count) + all.count) % all.count
        todayIndex = start
        _index = State(initialValue: start)
    }

    var body: some View {
        let quote = quotes.indices.contains(index) ? quotes[index].localized(strings) : nil

        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 55, seed: 47).ignoresSafeArea()

            VStack(spacing: 0) {
                OddlyTopBar(title: "", onBack: onBack)

                FillScrollView {
                    VStack(spacing: 0) {
                        Text(strings.quotesTitle)
                            .oddlyFont(OddlyFont.headlineSmall)
                            .foregroundStyle(palette.textPrimary)
                            .accessibilityHeading()

                        HStack(spacing: 8) {
                            OddlyChip(
                                text: strings.quotesFilterAll,
                                accent: OddlyColors.purple,
                                selected: !showFavorites
                            ) {
                                showFavorites = false
                                index = todayIndex
                            }
                            OddlyChip(
                                text: strings.quotesFilterFavorites,
                                accent: OddlyColors.pink,
                                selected: showFavorites
                            ) {
                                favoriteSnapshot = state.favoriteQuotes
                                showFavorites = true
                                index = 0
                            }
                        }
                        .padding(.top, 16)

                        if let quote {
                            let isFavorite = state.isFavorite(quote.id)

                            Button {
                                state.toggleFavorite(quote.id)
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
                            // Label names the action, so it flips after each tap ("Add" -> "Remove").
                            .accessibilityLabel(isFavorite ? strings.a11yRemoveFavorite : strings.a11yAddFavorite)
                            .padding(.top, 20)

                            Spacer(minLength: 0)

                            quoteCard(quote)

                            HStack(spacing: 20) {
                                CircleIconButton(
                                    .chevronLeft,
                                    label: strings.a11yPreviousQuote,
                                    diameter: 36,
                                    iconSize: 16,
                                    tint: palette.textSecondary
                                ) {
                                    index = (index - 1 + quotes.count) % quotes.count
                                }
                                Text("\(index + 1) / \(quotes.count)")
                                    .oddlyFont(OddlyFont.labelSmall)
                                    .tracking(0.5)
                                    .foregroundStyle(palette.textTertiary)
                                    .accessibilityLabel(strings.a11yQuotePosition(index: Int32(index + 1), total: Int32(quotes.count)))
                                CircleIconButton(
                                    .chevronRight,
                                    label: strings.a11yNextQuote,
                                    diameter: 36,
                                    iconSize: 16,
                                    tint: palette.textSecondary
                                ) {
                                    index = (index + 1) % quotes.count
                                }
                            }
                            .padding(.top, 20)

                            Spacer(minLength: 0)
                        } else {
                            Spacer(minLength: 0)
                            emptyCard
                            Spacer(minLength: 0)
                        }

                        GradientButton(strings.share, leadingIcon: .share, action: onShare)

                        reminderRow
                            .padding(.top, 20)
                            .padding(.bottom, 28)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        // Android uses a live region on the card; here the new quote is announced explicitly.
        .onChange(of: index) { _, newIndex in
            if quotes.indices.contains(newIndex) {
                let next = quotes[newIndex].localized(strings)
                announce("\(next.text) \(next.author)")
            }
        }
    }

    private var emptyCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(strings.quotesFavoritesEmptyTitle)
                .oddlyFont(OddlyFont.titleMedium)
                .foregroundStyle(palette.textPrimary)
            Text(strings.quotesFavoritesEmptyBody)
                .oddlyFont(OddlyFont.bodyMedium)
                .foregroundStyle(palette.textSecondary)
                .fixedSize(horizontal: false, vertical: true)
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

    private func quoteCard(_ quote: LocalizedQuote) -> some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("“\(quote.text)”")
                .oddlyFont(OddlyFont.headlineSmall)
                .foregroundStyle(palette.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
            Text("– \(quote.author)")
                .oddlyFont(OddlyFont.bodyMedium)
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
        .accessibilityElement(children: .combine)
    }

    private var reminderRow: some View {
        HStack {
            VStack(alignment: .leading, spacing: 0) {
                Text(strings.reminderLabel)
                    .oddlyFont(OddlyFont.titleSmall)
                    .foregroundStyle(palette.textPrimary)
                Text(strings.reminderToggleBody)
                    .oddlyFont(OddlyFont.bodySmall)
                    .foregroundStyle(palette.textTertiary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .accessibilityHidden(true)

            OddlyToggle(
                isOn: Binding(
                    get: { state.settings.reminderEnabled },
                    set: { state.settings = state.settings.with(reminderEnabled: $0) }
                ),
                label: "\(strings.reminderLabel), \(strings.reminderToggleBody)"
            )
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            palette.surfaceElevated,
            in: RoundedRectangle(cornerRadius: OddlyRadius.medium, style: .continuous)
        )
    }
}
