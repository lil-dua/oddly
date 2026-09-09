package dev.lildua.oddly.data.local

import android.content.Context

/**
 * [KeyValueStore] backed by `SharedPreferences`.
 *
 * `commit()` rather than `apply()`: the saved document is the app's only copy of
 * the user's history, and a write that is still in flight when the process is
 * killed loses a completion the user has already been congratulated for.
 */
class SharedPreferencesStore(context: Context) : KeyValueStore {

    private val prefs = context.applicationContext
        .getSharedPreferences("oddly", Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    override fun write(key: String, value: String) {
        prefs.edit().putString(key, value).commit()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).commit()
    }
}
