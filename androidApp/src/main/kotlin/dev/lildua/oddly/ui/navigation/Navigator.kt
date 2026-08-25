package dev.lildua.oddly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/**
 * A minimal back-stack navigator.
 *
 * Deliberately dependency-free: the app has a small, fixed route set and this
 * keeps navigation readable and testable. If deep links or state restoration
 * across process death become requirements, swap this for
 * `navigation-compose` — screens take plain callbacks, so only [OddlyApp] changes.
 */
@Stable
class Navigator(start: Destination) {

    private val backStack = mutableStateListOf(start)

    val current: Destination get() = backStack.last()

    val canGoBack: Boolean get() = backStack.size > 1

    fun push(destination: Destination) {
        backStack.add(destination)
    }

    fun pop(): Boolean {
        if (!canGoBack) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    /** Switch bottom-nav tab, discarding any child routes above it. */
    fun selectTab(tab: TabDestination) {
        if (current == tab) return
        resetTo(tab)
    }

    /** Replace the whole stack, e.g. when leaving onboarding for the main app. */
    fun resetTo(destination: Destination) {
        backStack.clear()
        backStack.add(destination)
    }
}

@Composable
fun rememberNavigator(start: Destination = Destination.Splash): Navigator =
    remember { Navigator(start) }
