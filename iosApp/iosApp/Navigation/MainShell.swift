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
        // A new day sends everyone back to Home: the screens above it were
        // built for yesterday. This shell only exists in the main phase, so
        // Splash and onboarding are never reset.
        .onChange(of: state.today.epochDays) { _, _ in
            celebrating = nil
            sharingFromReward = nil
            sharing = nil
            goHome()
        }
        .fullScreenCover(item: $celebrating) { ref in
            ChallengeCompleteScreen(
                challenge: ref.challenge.localized(strings),
                reward: state.lastReward,
                streakDays: Int(state.streak.current),
                quote: state.quoteOfTheDay.localized(strings).text,
                onShare: { sharingFromReward = ref },
                onBrowseLibrary: {
                    state.clearReward()
                    celebrating = nil
                    goHome(then: .allChallenges)
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
                // One scoring completion a day, for today's pick only; library
                // entries must be adopted first.
                availability: state.availability(challenge.id),
                canSwap: state.canReroll,
                onBack: pop,
                onComplete: {
                    // Refused (e.g. midnight passed): no reward screen; the
                    // day-change reset below takes over.
                    if state.complete(challenge) {
                        celebrating = ChallengeRef(challenge)
                    }
                },
                // Stays on this screen: availability flips to canComplete once
                // todayChallenge changes. A day change is handled by the reset.
                onAdopt: { state.adopt(challenge) },
                onAnother: { push(.anotherChallenge) }
            )

        case .anotherChallenge:
            AnotherChallengeScreen(
                swapsLeft: state.swapsLeft,
                onBack: pop,
                onSurpriseMe: {
                    guard state.canReroll else { goHome(); return }
                    let day = state.today
                    let next = state.reroll() ?? state.todayChallenge
                    // Day changed: the reset to Home takes over.
                    if state.today != day { return }
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
                    guard state.canReroll else { goHome(); return }
                    let day = state.today
                    let next = state.reroll(category: category) ?? state.todayChallenge
                    if state.today != day { return }
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
