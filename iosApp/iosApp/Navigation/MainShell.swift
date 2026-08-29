import SwiftUI
import SharedLogic

/// The main app shell: four tabs over one navigation stack, plus the two
/// screens that are modal by nature.
///
/// Switching tabs discards any child routes above the previous one — the same
/// rule the Android navigator follows — and the bottom bar hides while a child
/// screen is on top, which is both the Android behaviour and the iOS norm.
struct MainShell: View {
    @Environment(\.palette) private var palette
    @Environment(\.strings) private var strings

    let state: OddlyAppState

    @State private var tab: TabDestination = .home
    @State private var path: [Destination] = []

    /// The reward celebration: a modal moment, dismissed rather than popped.
    @State private var celebrating: ChallengeRef?
    /// The share card opened from the reward screen. It has to be presented by
    /// the celebration cover, not by the shell — a view can only have one
    /// presentation on screen at a time.
    @State private var sharingFromReward: ChallengeRef?
    /// The share card opened from anywhere in the navigation stack.
    @State private var sharing: ChallengeRef?

    var body: some View {
        NavigationStack(path: $path) {
            tabRoot
                .toolbar(.hidden, for: .navigationBar)
                // Placing the bar on the tab root alone means it slides away
                // with the push, and children get the full screen — the iOS
                // norm, and the same rule the Android shell follows.
                .safeAreaInset(edge: .bottom, spacing: 0) {
                    OddlyBottomBar(selected: tab) { selected in
                        guard selected != tab else { return }
                        path.removeAll()
                        tab = selected
                    }
                }
                .navigationDestination(for: Destination.self) { destination in
                    view(for: destination)
                }
                .enableSwipeBack()
        }
        .background(palette.background)
        .fullScreenCover(item: $celebrating) { ref in
            ChallengeCompleteScreen(
                challenge: ref.challenge.localized(strings),
                reward: state.lastReward,
                streakDays: Int(state.streak.current),
                quote: state.quoteOfTheDay.localized(strings).text,
                onShare: { sharingFromReward = ref },
                onAnother: {
                    state.clearReward()
                    celebrating = nil
                    goHome(then: .anotherChallenge)
                },
                onDone: {
                    state.clearReward()
                    celebrating = nil
                    goHome()
                }
            )
            .oddlyPalette(palette)
            .sheet(item: $sharingFromReward) { shared in
                ShareCardScreen(
                    state: state,
                    challenge: shared.challenge.localized(strings),
                    onClose: { sharingFromReward = nil }
                )
                .oddlyPalette(palette)
            }
        }
        .sheet(item: $sharing) { ref in
            ShareCardScreen(
                state: state,
                challenge: ref.challenge.localized(strings),
                onClose: { sharing = nil }
            )
            .oddlyPalette(palette)
        }
    }

    // MARK: - Tab roots

    @ViewBuilder
    private var tabRoot: some View {
        switch tab {
        case .home:
            HomeScreen(
                state: state,
                onOpenChallenge: { push(.challengeDetail(challengeId: state.todayChallenge.id)) },
                onStartChallenge: { push(.challengeDetail(challengeId: state.todayChallenge.id)) },
                onAnotherChallenge: { push(.anotherChallenge) },
                onOpenStreak: { push(.streak) },
                onOpenQuotes: { push(.quotes) }
            )

        case .journey:
            JourneyScreen(
                state: state,
                onOpenCalendar: { push(.calendar) },
                onOpenStreak: { push(.streak) },
                onOpenAllChallenges: { push(.allChallenges) },
                onStartFirstChallenge: { goHome() }
            )

        case .statistics:
            StatisticsScreen(state: state, onStartFirstChallenge: { goHome() })

        case .settings:
            SettingsScreen(state: state)
        }
    }

    // MARK: - Child routes

    @ViewBuilder
    private func view(for destination: Destination) -> some View {
        switch destination {
        case let .challengeDetail(challengeId):
            let challenge = ChallengeSeed.shared.byId(id: challengeId) ?? state.todayChallenge
            ChallengeDetailScreen(
                challenge: challenge.localized(strings),
                // Completion is once *per day* (spec §6.2), so a challenge done
                // last month is startable again.
                alreadyCompleted: state.isCompletedToday(challenge.id),
                onBack: pop,
                onComplete: {
                    state.complete(challenge)
                    celebrating = ChallengeRef(challenge)
                },
                onAnother: { push(.anotherChallenge) }
            )

        case .anotherChallenge:
            AnotherChallengeScreen(
                onBack: pop,
                onSurpriseMe: {
                    let next = state.reroll() ?? state.todayChallenge
                    pop()
                    push(.challengeDetail(challengeId: next.id))
                },
                onChooseCategory: { push(.chooseCategory) }
            )

        case .chooseCategory:
            ChooseCategoryScreen(
                state: state,
                onBack: pop,
                onPick: { category in
                    let next = state.reroll(category: category) ?? state.todayChallenge
                    push(.challengeDetail(challengeId: next.id))
                }
            )

        case .allChallenges:
            AllChallengesScreen(
                state: state,
                onBack: pop,
                onSelect: { push(.challengeDetail(challengeId: $0.id)) }
            )

        case .calendar:
            CalendarScreen(state: state, onBack: pop)

        case .streak:
            StreakScreen(state: state, onBack: pop)

        case .quotes:
            QuotesScreen(
                state: state,
                onBack: pop,
                onShare: { sharing = ChallengeRef(state.todayChallenge) }
            )
        }
    }

    // MARK: - Navigation

    private func push(_ destination: Destination) {
        path.append(destination)
    }

    private func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    /// Return to the Home tab, optionally opening one child route on arrival.
    private func goHome(then destination: Destination? = nil) {
        path.removeAll()
        tab = .home
        if let destination {
            path.append(destination)
        }
    }
}
