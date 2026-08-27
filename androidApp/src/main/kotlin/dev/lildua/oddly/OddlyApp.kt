package dev.lildua.oddly

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
import androidx.compose.ui.Modifier
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
import dev.lildua.oddly.ui.screens.onboarding.SplashScreen
import dev.lildua.oddly.ui.screens.quotes.QuotesScreen
import dev.lildua.oddly.ui.screens.settings.SettingsScreen
import dev.lildua.oddly.ui.screens.share.ShareCardScreen
import dev.lildua.oddly.ui.screens.statistics.StatisticsScreen
import dev.lildua.oddly.ui.state.rememberOddlyAppState
import dev.lildua.oddly.ui.theme.OddlyTheme

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
    OddlyTheme(themeMode = state.settings.themeMode) {
        val navigator = rememberNavigator(Destination.Splash)
        val current = navigator.current

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

                        Destination.Splash -> SplashScreen(
                            onFinished = { navigator.resetTo(Destination.Onboarding) },
                        )

                        Destination.Onboarding -> OnboardingScreen(
                            onStart = { navigator.push(Destination.ChooseInterest) },
                            onSkip = { navigator.resetTo(TabDestination.HOME) },
                        )

                        Destination.ChooseInterest -> ChooseInterestScreen(
                            selected = state.interests,
                            onToggle = state::toggleInterest,
                            onContinue = { navigator.push(Destination.NotificationPermission) },
                        )

                        Destination.NotificationPermission -> NotificationPermissionScreen(
                            onAllow = { time ->
                                val (hour, minute) = time.split(":").map(String::toInt)
                                state.settings = state.settings.copy(
                                    reminderEnabled = true,
                                    reminderTime = kotlinx.datetime.LocalTime(hour, minute),
                                )
                                navigator.resetTo(TabDestination.HOME)
                            },
                            onSkip = {
                                state.settings = state.settings.copy(reminderEnabled = false)
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
                                challenge = challenge,
                                // Completion is once *per day* (spec §6.2), so a
                                // challenge done last month is startable again.
                                alreadyCompleted = state.isCompletedToday(challenge.id),
                                onBack = { navigator.pop() },
                                onComplete = {
                                    state.complete(challenge)
                                    navigator.push(Destination.ChallengeComplete(challenge.id))
                                },
                                onAnother = { navigator.push(Destination.AnotherChallenge) },
                            )
                        }

                        is Destination.ChallengeComplete -> {
                            val challenge = ChallengeSeed.byId(destination.challengeId)
                                ?: state.todayChallenge
                            ChallengeCompleteScreen(
                                challenge = challenge,
                                reward = state.lastReward,
                                streakDays = state.streak.current,
                                quote = QuoteSeed
                                    .forDayIndex(state.today.toEpochDays())
                                    .text,
                                onShare = { navigator.push(Destination.ShareCard(challenge.id)) },
                                onAnother = {
                                    state.clearReward()
                                    navigator.selectTab(TabDestination.HOME)
                                    navigator.push(Destination.AnotherChallenge)
                                },
                                onDone = {
                                    state.clearReward()
                                    navigator.selectTab(TabDestination.HOME)
                                },
                            )
                        }

                        Destination.AnotherChallenge -> AnotherChallengeScreen(
                            onBack = { navigator.pop() },
                            onSurpriseMe = {
                                state.reroll()
                                navigator.pop()
                                navigator.push(Destination.ChallengeDetail(state.todayChallenge.id))
                            },
                            onChooseCategory = { navigator.push(Destination.ChooseCategory) },
                        )

                        Destination.ChooseCategory -> ChooseCategoryScreen(
                            state = state,
                            onBack = { navigator.pop() },
                            onPick = { category ->
                                state.reroll(category)
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
                            challenge = ChallengeSeed.byId(destination.challengeId),
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
