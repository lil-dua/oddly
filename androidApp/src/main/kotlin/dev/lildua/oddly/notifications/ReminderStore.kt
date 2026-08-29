package dev.lildua.oddly.notifications

import android.content.Context
import dev.lildua.oddly.domain.model.AppLanguage
import kotlinx.datetime.LocalTime

/**
 * The reminder time, mirrored to disk so the alarm can be re-armed after a
 * reboot.
 *
 * Deliberately narrow: this is scheduler state, not the app's data layer. When
 * the Room repositories land (see the roadmap) the reminder settings move there
 * and this goes away.
 */
internal object ReminderStore {

    private const val FILE = "reminder"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val KEY_LANGUAGE = "language"

    data class Saved(val time: LocalTime, val language: AppLanguage)

    fun save(context: Context, enabled: Boolean, time: LocalTime, language: AppLanguage) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_HOUR, time.hour)
            .putInt(KEY_MINUTE, time.minute)
            .putString(KEY_LANGUAGE, language.tag)
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, false).apply()
    }

    fun load(context: Context): Saved? {
        val prefs = prefs(context)
        if (!prefs.getBoolean(KEY_ENABLED, false)) return null

        val tag = prefs.getString(KEY_LANGUAGE, null)
        return Saved(
            time = LocalTime(prefs.getInt(KEY_HOUR, 9), prefs.getInt(KEY_MINUTE, 0)),
            language = AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.VIETNAMESE,
        )
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
