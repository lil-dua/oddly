package dev.lildua.oddly.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.lildua.oddly.core.text.Brand
import dev.lildua.oddly.core.text.ExternalLinks

/**
 * Hands links and email to whichever app the system picks.
 *
 * Each call just tries `startActivity` and catches the failure instead of
 * asking `resolveActivity` first: querying other apps needs a `<queries>`
 * manifest entry on Android 11+, whereas launching an implicit intent does not.
 * [SecurityException] is caught too because some OEMs throw it for restricted
 * target activities. Every launcher returns false so the caller can show the
 * address as text.
 */
internal object LinkLauncher {

    fun openUrl(context: Context, url: String): Boolean =
        launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun openEmail(context: Context, address: String, subject: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(ExternalLinks.mailto(address, subject)))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
        return launch(context, intent)
    }

    /** Play app first, then the web page for devices without Google Play. */
    fun openPlayStore(context: Context): Boolean {
        val pkg = context.packageName
        return openUrl(context, ExternalLinks.playStoreApp(pkg)) ||
            openUrl(context, ExternalLinks.playStoreWeb(pkg))
    }

    fun copy(context: Context, text: String) {
        context.getSystemService(ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText(Brand.NAME, text))
    }

    private fun launch(context: Context, intent: Intent): Boolean = try {
        // The context may be a plain application context; NEW_TASK keeps that working.
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
