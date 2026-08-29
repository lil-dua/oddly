import SwiftUI
import SharedLogic

/// Root view for the iOS app.
///
/// Owns the theme and the app state, then hands off to either the onboarding
/// sequence or the main tab shell. Screens themselves take plain values and
/// callbacks, so they stay independent of both navigation and the state
/// container — the same contract the Android screens follow.
struct OddlyRootView: View {
    @Environment(\.colorScheme) private var systemColorScheme

    @State private var state = OddlyAppState()
    @State private var phase: Phase = .splash

    private enum Phase {
        case splash
        case onboarding
        case main
    }

    var body: some View {
        ZStack {
            switch phase {
            case .splash:
                SplashScreen {
                    withAnimation(.easeInOut(duration: 0.3)) { phase = .onboarding }
                }
                .transition(.opacity)

            case .onboarding:
                OnboardingFlow(state: state) {
                    withAnimation(.easeInOut(duration: 0.3)) { phase = .main }
                }
                .transition(.opacity)

            case .main:
                MainShell(state: state)
                    .transition(.opacity)
            }
        }
        .oddlyStrings(state.settings.language)
        // The schedule is derived state: whenever the reminder settings change,
        // the pending notifications are rebuilt rather than patched.
        .task(id: ReminderSettingsKey(state.settings)) {
            await ReminderScheduler.reschedule(settings: state.settings)
        }
        .measuringTopSafeArea()
        .oddlyTheme(state.settings.themeMode, systemIsDark: systemColorScheme == .dark)
    }
}

/// The parts of `AppSettings` the notification schedule depends on.
///
/// `AppSettings` is a Kotlin class without Swift `Hashable`, and `task(id:)`
/// needs a value it can compare — this narrows it to exactly what matters.
private struct ReminderSettingsKey: Equatable {
    let enabled: Bool
    let hour: Int32
    let minute: Int32
    let language: String
    let sound: Bool

    init(_ settings: AppSettings) {
        enabled = settings.reminderEnabled
        hour = settings.reminderTime.hour
        minute = settings.reminderTime.minute
        language = settings.language.tag
        sound = settings.soundEnabled
    }
}

/// The linear first-run sequence: onboarding → interests → reminder (spec §2.1).
private struct OnboardingFlow: View {
    let state: OddlyAppState
    let onFinish: () -> Void

    @State private var path: [OnboardingStep] = []

    var body: some View {
        NavigationStack(path: $path) {
            OnboardingScreen(
                onStart: { path.append(.chooseInterest) },
                onSkip: onFinish
            )
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: OnboardingStep.self) { step in
                switch step {
                case .chooseInterest:
                    ChooseInterestScreen(
                        selected: state.interests,
                        onToggle: { state.toggleInterest($0) },
                        onContinue: { path.append(.notificationPermission) }
                    )
                    .toolbar(.hidden, for: .navigationBar)

                case .notificationPermission:
                    NotificationPermissionScreen(
                        onAllow: { time in
                            let parts = time.split(separator: ":").compactMap { Int32($0) }
                            if parts.count == 2 {
                                state.settings = state.settings.with(
                                    reminderEnabled: true,
                                    reminderTime: LocalTime(hour: parts[0], minute: parts[1], second: 0, nanosecond: 0)
                                )
                            }
                            Task {
                                await ReminderScheduler.requestAuthorization()
                                await ReminderScheduler.reschedule(settings: state.settings)
                            }
                            onFinish()
                        },
                        onSkip: {
                            state.settings = state.settings.with(reminderEnabled: false)
                            onFinish()
                        }
                    )
                    .toolbar(.hidden, for: .navigationBar)
                }
            }
            .enableSwipeBack()
        }
    }
}
