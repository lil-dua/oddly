package dev.lildua.oddly.screenshots

import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

/**
 * Where AGP/UTP collects extra test artefacts. When the Gradle plugin passes
 * `additionalTestOutputDir`, files written there are pulled into
 * `androidApp/build/outputs/connected_android_test_additional_output/`.
 */
private fun outputDir(arguments: Bundle): File {
    val base = arguments.getString("additionalTestOutputDir") ?: error("additionalTestOutputDir not provided")
    return File(base, "store-shots").also { it.mkdirs() }
}

/**
 * Saves a full-screen PNG (status bar included, so the demo-mode clock shows)
 * named [name] under `store-shots/`.
 */
fun ComposeTestRule.capture(name: String) {
    waitForIdle()
    // Let the last frame settle: compose idle does not cover image decode or a finishing transition.
    Thread.sleep(500)
    waitForIdle()

    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val bitmap = instrumentation.uiAutomation.takeScreenshot()
    assertNotNull("no screenshot for $name", bitmap)
    val metrics = instrumentation.targetContext.resources.displayMetrics
    assertEquals("width of $name", metrics.widthPixels, bitmap.width)
    assertEquals("height of $name", metrics.heightPixels, bitmap.height)

    File(outputDir(InstrumentationRegistry.getArguments()), "$name.png").outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
}
