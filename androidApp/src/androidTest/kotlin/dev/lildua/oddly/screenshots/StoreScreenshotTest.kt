package dev.lildua.oddly.screenshots

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import dev.lildua.oddly.core.testing.StoreShotScenario
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.core.time.Today
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.local.SharedPreferencesStore
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.support.OddlyAppRule
import dev.lildua.oddly.support.clickTag
import dev.lildua.oddly.support.hasTag
import dev.lildua.oddly.support.waitForTag
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Produces the store screenshots (T-19). Skipped unless `tools/store-shots.sh`
 * passes `storeShots=true`, so the regular UI suite stays as fast as before.
 * Files are numbered in store order though they are taken in the order of the flow.
 */
class StoreScreenshotTest {
    @get:Rule
    val app = OddlyAppRule()

    @Before
    fun onlyWhenAsked() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("storeShots") == "true")
    }

    @Test
    fun captureVietnamese() = captureAll(AppLanguage.VIETNAMESE, "vi")

    @Test
    fun captureEnglish() = captureAll(AppLanguage.ENGLISH, "en")

    private fun captureAll(language: AppLanguage, prefix: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext

        Today.pinForTesting(StoreShotScenario.NOW)
        for (name in listOf("oddly", "reminder")) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
        OddlyRepository(SharedPreferencesStore(context)).save(StoreShotScenario.state(Today.local(), language))
        if (Build.VERSION.SDK_INT >= 33) {
            // So Settings shows the reminder on, without the red "notifications are off" notice.
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        app.start(StoreShotScenario.NOW, keepData = true)

        val c = app.compose
        fun shot(name: String) = c.capture("$prefix-$name")

        c.waitForTag(UiTestTags.HOME_TODAY_TITLE)
        shot("01-home")

        c.clickTag(UiTestTags.HOME_TODAY_START)
        c.clickTag(UiTestTags.DETAIL_BEGIN)
        c.waitForTag(UiTestTags.COMPLETE_DONE)
        shot("02-reward")

        c.clickTag(UiTestTags.COMPLETE_SHARE)
        c.waitForTag(UiTestTags.SHARE_CARD)
        shot("05-share")
        app.pressBack()

        c.clickTag(UiTestTags.COMPLETE_DONE)
        c.clickTag(UiTestTags.TAB_JOURNEY)
        // The row sits below the fold on this profile, so scroll it in before tapping.
        c.waitForTag(UiTestTags.JOURNEY_CALENDAR)
        c.onNodeWithTag(UiTestTags.JOURNEY_CALENDAR).performScrollTo().performClick()
        c.waitForTag(UiTestTags.CALENDAR_SCREEN)
        shot("03-calendar")
        app.pressBack()

        c.clickTag(UiTestTags.TAB_STATISTICS)
        c.waitForTag(UiTestTags.STATISTICS_SCREEN)
        shot("04-stats")

        c.clickTag(UiTestTags.TAB_SETTINGS)
        c.waitForTag(UiTestTags.SETTINGS_REMINDER)
        assertFalse("permission notice must not show", c.hasTag(UiTestTags.SETTINGS_NOTIFICATIONS_NOTICE))
        shot("06-settings")
    }
}
