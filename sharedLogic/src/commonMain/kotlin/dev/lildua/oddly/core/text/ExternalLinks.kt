package dev.lildua.oddly.core.text

/**
 * Pure builders for the links Settings opens, kept in shared so they can be
 * unit tested and so both platforms build them identically.
 */
object ExternalLinks {

    /** The address is left raw (it is already a valid mailbox); only the subject is encoded. */
    fun mailto(address: String, subject: String): String =
        "mailto:$address?subject=${percentEncode(subject)}"

    fun playStoreApp(packageName: String): String = "market://details?id=$packageName"

    fun playStoreWeb(packageName: String): String =
        "https://play.google.com/store/apps/details?id=$packageName"

    fun appStoreReview(appId: String): String =
        "https://apps.apple.com/app/id$appId?action=write-review"

    /** Null while [Brand.APP_STORE_ID] is unset, which hides the iOS rating row. */
    val appStoreReviewUrl: String?
        get() = Brand.APP_STORE_ID?.let { appStoreReview(it) }

    /**
     * RFC 3986 percent-encoding. Done by hand because common code has no
     * `URLEncoder`, and a `%` in "1% HUMAN" must become `%25` or mail apps
     * misread the subject.
     */
    internal fun percentEncode(value: String): String {
        val hex = "0123456789ABCDEF"
        val out = StringBuilder()
        for (b in value.encodeToByteArray()) {
            val v = b.toInt() and 0xFF
            val c = v.toChar()
            if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '-' || c == '.' || c == '_' || c == '~') {
                out.append(c)
            } else {
                out.append('%').append(hex[v shr 4]).append(hex[v and 0xF])
            }
        }
        return out.toString()
    }
}
