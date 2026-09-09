package dev.lildua.oddly.data.local

/**
 * The one thing persistence needs from a platform: somewhere to put a string.
 *
 * An interface rather than `expect`/`actual` so each platform can construct its
 * implementation with whatever it needs — Android's wants a `Context`, iOS's
 * wants nothing — without contorting a common constructor signature.
 */
interface KeyValueStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}

/** In-memory store, for tests and previews. */
class InMemoryKeyValueStore(
    private val values: MutableMap<String, String> = mutableMapOf(),
) : KeyValueStore {
    override fun read(key: String): String? = values[key]
    override fun write(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
}
