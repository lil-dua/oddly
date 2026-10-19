package dev.lildua.oddly

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import dev.lildua.oddly.core.text.Strings
import dev.lildua.oddly.domain.usecase.FeedbackRules
import dev.lildua.oddly.notifications.ReminderScheduler
import dev.lildua.oddly.notifications.rememberNotificationPermission
import dev.lildua.oddly.data.seed.ChallengeSeed
import dev.lildua.oddly.data.seed.QuoteSeed
import dev.lildua.oddly.ui.navigation.BottomBarDivider
import dev.lildua.oddly.ui.navigation.Destination
import dev.lildua.oddly.ui.navigation.OddlyBottomBar
import dev.lildua.oddly.ui.navigation.TabDestination
import dev.lildua.oddly.ui.navigation.rememberNavigator
import dev.lildua.oddly.ui.screens.challenge.AllChallengesScreen
import dev.lildua.oddly.ui.screens.challenge.AnotherChallengeScreen
import dev.lildua.oddly.ui.screens.challenge.ChallengeCompleteScreen
import dev.lildua.oddly.ui.screens.challenge.ChallengeDetailScreen
import dev.lildua.oddly.ui.screens.challenge.ChooseCategoryScreen
import dev.lildua.oddly.ui.screens.home.HomeScreen
import dev.lildua.oddly.ui.screens.journey.CalendarScreen
import dev.lildua.oddly.ui.screens.journey.JourneyScreen
import dev.lildua.oddly.ui.screens.journey.StreakScreen
import dev.lildua.oddly.ui.screens.onboarding.ChooseInterestScreen
import dev.lildua.oddly.ui.screens.onboarding.NotificationPermissionScreen
import dev.lildua.oddly.ui.screens.onboarding.OnboardingScreen
import dev.lildua.oddly.ui.screens.quotes.QuotesScreen
import dev.lildua.oddly.ui.screens.settings.SettingsScreen
import dev.lildua.oddly.ui.screens.share.ShareCardScreen
import dev.lildua.oddly.ui.screens.statistics.StatisticsScreen
import dev.lildua.oddly.ui.feedback.rememberOddlyFeedback
import dev.lildua.oddly.ui.state.rememberOddlyAppState
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.localized
import dev.lildua.oddly.ui.theme.OddlyTheme
import dev.lildua.oddly.ui.theme.SystemBarsEffect

/**
 * Root composable for the Android app.
 *
 * Owns the theme, the navigator and the app state, then dispatches to screens.
 * Screens themselves take plain values and callbacks, so they stay independent
 * of both the navigation mechanism and the state container.
 */
@Composable
fun OddlyApp() {
    val state = rememberOddlyAppState()
    val feedback = rememberOddlyFeedback()
    val context = LocalContext.current
    val notifications = rememberNotificationPermission()

    // The alarm is derived state: whenever the reminder settings change, the
    // schedule is rewritten rather than patched.
    LaunchedEffect(
        state.settings.reminderEnabled,
        state.settings.reminderTime,
        state.settings.language,
    ) {
        if (state.settings.reminderEnabled) {
            ReminderScheduler.schedule(context, state.settings.reminderTime, state.settings.language)
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    // A reminder already in the tray is stale once today's challenge is done.
    LaunchedEffect(state.completedToday) {
        if (state.completedToday) ReminderScheduler.dismissDelivered(context)
    }

    // Coming back from the background is the common way to meet a new day.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { state.refreshDay() }

    // While visible, also follow the clock live. DATE_CHANGED alone is not
    // enough: it does not fire when the time is set by hand or moved backwards,
    // which TIME_CHANGED and TIMEZONE_CHANGED cover. Registered only between
    // start and stop; ON_RESUME above catches anything missed in the background.
    LifecycleStartEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                state.refreshDay()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onStopOrDispose { context.unregisterReceiver(receiver) }
    }

    CompositionLocalProvider(LocalStrings provides Strings.of(state.settings.language)) {
        OddlyTheme(themeMode = state.settings.themeMode) {
            SystemBarsEffect(isDark = OddlyTheme.palette.isDark)

            // Spec §S01: onboarding is a first-launch event, not something a
            // returning user walks through every morning. The system splash
            // covers the load, so the app opens straight onto its first screen.
            val navigator = rememberNavigator(
                if (state.hasCompletedOnboarding) TabDestination.HOME else Destination.Onboarding,
            )
            val current = navigator.current

            // A new day sends everyone back to Home: the screens above it were
            // built for yesterday. Onboarding is left alone; the data still updates.
            var shownDay by remember { mutableStateOf(state.today) }
            LaunchedEffect(state.today) {
                if (state.today == shownDay) return@LaunchedEffect
                shownDay = state.today
                val destination = navigator.current
                val inOnboarding = destination == Destination.Onboarding ||
                    destination == Destination.ChooseInterest ||
                    destination == Destination.NotificationPermission
                if (!inOnboarding) navigator.resetTo(TabDestination.HOME)
            }

            // Root tabs have nothing above them, so back falls through to the
            // system and leaves the app — which is the expected Android behaviour.
            BackHandler(enabled = navigator.canGoBack) { navigator.pop() }

            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Box(Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = current,
                        transitionSpec = {
                            (fadeIn(tween(240)) + slideInHorizontally { it / 14 })
                                .togetherWith(fadeOut(tween(160)))
                        },
                        label = "route",
                    ) { destination ->
                        when (destination) {

                            // --- Onboarding ---

                            Destination.Onboarding -> OnboardingScreen(
                                onStart = { navigator.push(Destination.ChooseInterest) },
                                onSkip = {
                                    state.completeOnboarding()
                                    navigator.resetTo(TabDestination.HOME)
                                },
                            )

                            Destination.ChooseInterest -> ChooseInterestScreen(
                                selected = state.interests,
                                onToggle = {
                                    state.toggleInterest(it)
                                    feedback.play(FeedbackRules.selection(state.settings))
                                },
                                onContinue = { navigator.push(Destination.NotificationPermission) },
                            )

                            Destination.NotificationPermission -> NotificationPermissionScreen(
                                onAllow = { time ->
                                    val (hour, minute) = time.split(":").map(String::toInt)
                                    state.settings = state.settings.copy(
                                        reminderEnabled = true,
                                        reminderTime = kotlinx.datetime.LocalTime(hour, minute),
                                    )
                                    notifications.request()
                                    state.completeOnboarding()
                                    navigator.resetTo(TabDestination.HOME)
                                },
                                onSkip = {
                                    state.settings = state.settings.copy(reminderEnabled = false)
                                    state.completeOnboarding()
                                    navigator.resetTo(TabDestination.HOME)
                                },
                            )

                            // --- Tabs ---

                            TabDestination.HOME -> HomeScreen(
                                state = state,
                                onOpenChallenge = {
                                    navigator.push(Destination.ChallengeDetail(state.todayChallenge.id))
                                },
                                onStartChallenge = {
                                    navigator.push(Destination.ChallengeDetail(state.todayChallenge.id))
                                },
                                onAnotherChallenge = { navigator.push(Destination.AnotherChallenge) },
                                onOpenStreak = { navigator.push(Destination.Streak) },
                                onOpenQuotes = { navigator.push(Destination.Quotes) },
                            )

                            TabDestination.JOURNEY -> JourneyScreen(
                                state = state,
                                onOpenCalendar = { navigator.push(Destination.Calendar) },
                                onOpenStreak = { navigator.push(Destination.Streak) },
                                onOpenAllChallenges = { navigator.push(Destination.AllChallenges) },
                                onStartFirstChallenge = { navigator.selectTab(TabDestination.HOME) },
                            )

                            TabDestination.STATISTICS -> StatisticsScreen(
                                state = state,
                                onStartFirstChallenge = { navigator.selectTab(TabDestination.HOME) },
                            )

                            TabDestination.SETTINGS -> SettingsScreen(state = state)

                            // --- Challenge flow ---

                            is Destination.ChallengeDetail -> {
                                val challenge = ChallengeSeed.byId(destination.challengeId)
                                    ?: state.todayChallenge
                                ChallengeDetailScreen(
                                    challenge = challenge.localized(),
                                    // One scoring completion a day, for today's pick only;
                                    // library entries must be adopted first.
                                    availability = state.availability(challenge.id),
                                    canSwap = state.canReroll,
                                    onBack = { navigator.pop() },
                                    onComplete = {
                                        // Refused (e.g. midnight passed): no reward screen;
                                        // the day-change reset above takes over.
                                        if (state.complete(challenge)) {
                                            // Here, not in the reward screen's effect: that screen is
                                            // rebuilt on Back from ShareCard and would replay it.
                                            feedback.play(FeedbackRules.reward(state.lastReward, state.settings))
                                            navigator.push(Destination.ChallengeComplete(challenge.id))
                                        }
                                    },
                                    // Stays on this screen: availability flips to
                                    // CAN_COMPLETE once todayChallenge changes. A day
                                    // change is handled by the reset to Home above.
                                    onAdopt = { state.adopt(challenge) },
                                    onAnother = { navigator.push(Destination.AnotherChallenge) },
                                )
                            }

                            is Destination.ChallengeComplete -> {
                                val challenge = ChallengeSeed.byId(destination.challengeId)
                                    ?: state.todayChallenge
                                ChallengeCompleteScreen(
                                    challenge = challenge.localized(),
                                    reward = state.lastReward,
                                    streakDays = state.streak.current,
                                    quote = QuoteSeed
                                        .forDayIndex(state.today.toEpochDays())
                                        .localized()
                                        .text,
                                    onShare = { navigator.push(Destination.ShareCard(challenge.id)) },
                                    onBrowseLibrary = {
                                        state.clearReward()
                                        navigator.selectTab(TabDestination.HOME)
                                        navigator.push(Destination.AllChallenges)
                                    },
                                    onDone = {
                                        state.clearReward()
                                        navigator.selectTab(TabDestination.HOME)
                                    },
                                )
                            }

                            Destination.AnotherChallenge -> AnotherChallengeScreen(
                                swapsLeft = state.swapsLeft,
                                onBack = { navigator.pop() },
                                onSurpriseMe = {
                                    // Reachable via the back stack after completing.
                                    if (!state.canReroll) {
                                        navigator.selectTab(TabDestination.HOME)
                                        return@AnotherChallengeScreen
                                    }
                                    val day = state.today
                                    state.reroll()
                                    // Day changed: the reset to Home takes over.
                                    if (state.today != day) return@AnotherChallengeScreen
                                    navigator.pop()
                                    navigator.push(Destination.ChallengeDetail(state.todayChallenge.id))
                                },
                                onChooseCategory = { navigator.push(Destination.ChooseCategory) },
                            )

                            Destination.ChooseCategory -> ChooseCategoryScreen(
                                state = state,
                                onBack = { navigator.pop() },
                                onPick = { category ->
                                    if (!state.canReroll) {
                                        navigator.selectTab(TabDestination.HOME)
                                        return@ChooseCategoryScreen
                                    }
                                    val day = state.today
                                    state.reroll(category)
                                    if (state.today != day) return@ChooseCategoryScreen
                                    feedback.play(FeedbackRules.selection(state.settings))
                                    navigator.push(Destination.ChallengeDetail(state.todayChallenge.id))
                                },
                            )

                            Destination.AllChallenges -> AllChallengesScreen(
                                state = state,
                                onBack = { navigator.pop() },
                                onSelect = { challenge ->
                                    navigator.push(Destination.ChallengeDetail(challenge.id))
                                },
                            )

                            // --- Progress & content ---

                            Destination.Calendar -> CalendarScreen(
                                state = state,
                                onBack = { navigator.pop() },
                            )

                            Destination.Streak -> StreakScreen(
                                state = state,
                                onBack = { navigator.pop() },
                            )

                            Destination.Quotes -> QuotesScreen(
                                state = state,
                                onBack = { navigator.pop() },
                                onShare = {
                                    navigator.push(Destination.ShareCard(state.todayChallenge.id))
                                },
                            )

                            is Destination.ShareCard -> ShareCardScreen(
                                state = state,
                                challenge = ChallengeSeed.byId(destination.challengeId)?.localized(),
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                }

                if (current is TabDestination) {
                    BottomBarDivider()
                    OddlyBottomBar(
                        selected = current,
                        onSelect = navigator::selectTab,
                    )
                }
            }
        }
    }
}
