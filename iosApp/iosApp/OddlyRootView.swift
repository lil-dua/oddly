import Combine
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
    @Environment(\.scenePhase) private var scenePhase

    @State private var state = OddlyAppState()
    @State private var phase: Phase = .splash

    /// Spec §S01: onboarding is a first-launch event, not something a returning
    /// user walks through every morning.
    private var afterSplash: Phase {
        state.hasCompletedOnboarding ? .main : .onboarding
    }

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
                    // A day change during Splash is not news: Home was never shown.
                    state.clearDayChangeNotice()
                    withAnimation(.easeInOut(duration: 0.3)) { phase = afterSplash }
                }
                .transition(.opacity)

            case .onboarding:
                OnboardingFlow(state: state) {
                    state.completeOnboarding()
                    withAnimation(.easeInOut(duration: 0.3)) { phase = .main }
                }
                .transition(.opacity)

            case .main:
                MainShell(state: state)
                    .transition(.opacity)
            }
        }
        .oddlyStrings(state.settings.language)
        // The schedule is derived state of the settings, the local day and whether
        // today is done: any of them changing rebuilds the pending notifications
        // rather than patching them. A new day also refills the 14-day window.
        .task(id: ReminderScheduleKey(state)) {
            await ReminderScheduler.reschedule(settings: state.settings, completions: state.completions)
        }
        // Coming back to the foreground is the common way to meet a new day.
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { state.refreshDay() }
        }
        // While running, follow the clock too. NSCalendarDayChanged can be posted
        // from another thread, so hop to the main run loop before touching the
        // @Observable state. The significant-time-change one covers manual clock
        // and time-zone edits, which the day-changed one does not.
        .onReceive(
            NotificationCenter.default.publisher(for: .NSCalendarDayChanged)
                .receive(on: RunLoop.main)
        ) { _ in state.refreshDay() }
        .onReceive(
            NotificationCenter.default.publisher(for: UIApplication.significantTimeChangeNotification)
                .receive(on: RunLoop.main)
        ) { _ in state.refreshDay() }
        .measuringTopSafeArea()
        .oddlyTheme(state.settings.themeMode, systemIsDark: systemColorScheme == .dark)
    }
}

/// What the notification schedule depends on: the reminder settings, the local
/// day, whether today is done and how many completions there are.
///
/// `AppSettings` is a Kotlin class without Swift `Hashable`, and `task(id:)`
/// needs a value it can compare — this narrows it to exactly what matters.
private struct ReminderScheduleKey: Equatable {
    let enabled: Bool
    let hour: Int32
    let minute: Int32
    let language: String
    let sound: Bool
    let today: Int
    let completedToday: Bool
    /// An import on the same day can change the streak the reminder copy quotes.
    let completionCount: Int

    init(_ state: OddlyAppState) {
        let settings = state.settings
        enabled = settings.reminderEnabled
        hour = settings.reminderTime.hour
        minute = settings.reminderTime.minute
        language = settings.language.tag
        sound = settings.soundEnabled
        today = state.today.epochDays
        completedToday = state.completedToday
        completionCount = state.completions.count
    }
}

/// The linear first-run sequence: onboarding → interests → reminder (spec §2.1).
private struct OnboardingFlow: View {
    let state: OddlyAppState
    let onFinish: () -> Void

    @State private var path: [OnboardingStep] = []
    @State private var feedback = FeedbackTrigger()

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
                        onToggle: {
                            state.toggleInterest($0)
                            feedback.fire(FeedbackRules.shared.selection(settings: state.settings))
                        },
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
                                await ReminderScheduler.reschedule(settings: state.settings, completions: state.completions)
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
        .oddlyFeedback(feedback)
    }
}
