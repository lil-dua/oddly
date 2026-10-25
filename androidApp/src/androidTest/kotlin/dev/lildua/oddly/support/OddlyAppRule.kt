package dev.lildua.oddly.support

import android.content.Context
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dev.lildua.oddly.MainActivity
import dev.lildua.oddly.core.time.Today
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.data.local.SharedPreferencesStore
import dev.lildua.oddly.notifications.ReminderScheduler
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Drives the real [MainActivity] with a pinned clock and clean storage.
 *
 * The test runs in the app's own process, so [Today.pinForTesting] is the same
 * object the app reads — calling it again "moves" the day without waiting.
 */
class OddlyAppRule : TestRule {
    val compose: ComposeTestRule = createEmptyComposeRule()

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private var scenario: ActivityScenario<MainActivity>? = null

    override fun apply(base: Statement, description: Description): Statement =
        RuleChain.outerRule(compose).around(Cleanup()).apply(base, description)

    /** Pins [now] (ISO local date-time), wipes storage unless [keepData], launches the app. */
    fun start(now: String, keepData: Boolean = false) {
        Today.pinForTesting(now)
        if (!keepData) wipeStorage()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /** Moves the clock without touching the lifecycle; the app notices on its next action. */
    fun jumpTo(now: String) = Today.pinForTesting(now)

    /** Moves the clock, then pauses and resumes so ON_RESUME runs `refreshDay`. */
    fun resumeAt(now: String) {
        Today.pinForTesting(now)
        scenario!!.moveToState(Lifecycle.State.STARTED)
        scenario!!.moveToState(Lifecycle.State.RESUMED)
    }

    /** Kills and reopens the activity over the same storage and clock. */
    fun relaunch() {
        scenario?.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /** Recreates the activity as a font-scale or locale change would, carrying the saved-state Bundle. */
    fun recreate() = scenario!!.recreate()

    fun pressBack() {
        scenario!!.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    }

    fun storedState(): PersistedState =
        checkNotNull(OddlyRepository(SharedPreferencesStore(context)).load()) { "nothing saved yet" }

    private fun wipeStorage() {
        // The cached SharedPreferences instances are shared with the app in this
        // process, so clearing through them keeps both views consistent.
        for (name in listOf("oddly", "reminder")) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    private inner class Cleanup : TestRule {
        override fun apply(base: Statement, description: Description) = object : Statement() {
            override fun evaluate() {
                try {
                    base.evaluate()
                } finally {
                    scenario?.close()
                    scenario = null
                    ReminderScheduler.cancel(context)
                    Today.pinForTesting(null)
                    wipeStorage()
                }
            }
        }
    }
}
