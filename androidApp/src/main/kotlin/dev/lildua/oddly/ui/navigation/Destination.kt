package dev.lildua.oddly.ui.navigation

/**
 * Every screen the app can show.
 *
 * Only [TabDestination]s appear in the bottom bar (spec §5: exactly four
 * destinations); everything else is a full-screen child route.
 */
sealed interface Destination {

    // --- Onboarding ---
    data object Splash : Destination
    data object Onboarding : Destination
    data object ChooseInterest : Destination
    data object NotificationPermission : Destination

    // --- Child routes ---
    data class ChallengeDetail(val challengeId: String) : Destination
    data class ChallengeComplete(val challengeId: String) : Destination
    data class ShareCard(val challengeId: String) : Destination
    data object AnotherChallenge : Destination
    data object ChooseCategory : Destination
    data object AllChallenges : Destination
    data object Calendar : Destination
    data object Streak : Destination
    data object Quotes : Destination
}

/** The four bottom-navigation destinations. */
enum class TabDestination(val label: String) : Destination {
    HOME("Hôm nay"),
    JOURNEY("Hành trình"),
    STATISTICS("Thống kê"),
    SETTINGS("Cài đặt"),
}
