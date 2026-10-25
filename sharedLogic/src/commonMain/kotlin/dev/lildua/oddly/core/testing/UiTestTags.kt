package dev.lildua.oddly.core.testing

import dev.lildua.oddly.domain.model.Category

/**
 * Element ids shared by the Android `testTag` and iOS `accessibilityIdentifier`
 * UI tests. They are not accessibility labels — screen readers never speak them.
 * Changing a value means updating the Swift mirror in `iosAppUITests` too.
 */
object UiTestTags {
    const val ONBOARDING_START = "onboarding.start"
    const val ONBOARDING_SKIP = "onboarding.skip"
    const val INTERESTS_CONTINUE = "interests.continue"
    fun interest(category: Category) = "interests.${category.id}"
    const val PERMISSION_LATER = "permission.later"

    const val HOME_DAY_BANNER = "home.dayBanner"
    const val HOME_TODAY_TITLE = "home.todayTitle"
    const val HOME_TODAY_START = "home.todayStart"
    const val HOME_TODAY_DONE = "home.todayDone"

    const val DETAIL_BEGIN = "detail.begin"
    const val DETAIL_ALREADY_DONE = "detail.alreadyDone"
    const val DETAIL_DAY_DONE = "detail.dayDone"
    const val DETAIL_ADOPT = "detail.adopt"

    const val COMPLETE_DONE = "complete.done"
    const val COMPLETE_XP = "complete.xp"
    const val COMPLETE_STREAK = "complete.streak"

    const val NAV_BACK = "nav.back"

    const val TAB_HOME = "tab.home"
    const val TAB_JOURNEY = "tab.journey"
    const val TAB_SETTINGS = "tab.settings"

    const val SETTINGS_RATE = "settings.rate"
    const val SETTINGS_CONTACT = "settings.contact"
    const val SETTINGS_PRIVACY = "settings.privacy"
    const val SETTINGS_ABOUT = "settings.about"

    const val JOURNEY_EMPTY = "journey.empty"
    const val JOURNEY_LEVEL = "journey.level"
    const val JOURNEY_ALL_CHALLENGES = "journey.allChallenges"
    fun libraryRow(challengeId: String) = "library.$challengeId"
}
