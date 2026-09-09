package dev.lildua.oddly.data.local

import platform.Foundation.NSUserDefaults

/**
 * [KeyValueStore] backed by `NSUserDefaults`, which iOS flushes for us and
 * carries through app updates and device restores.
 */
class UserDefaultsStore : KeyValueStore {

    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults


    override fun read(key: String): String? = defaults.stringForKey(key)

    override fun write(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
