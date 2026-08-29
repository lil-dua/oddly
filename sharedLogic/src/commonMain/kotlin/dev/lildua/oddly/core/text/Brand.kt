package dev.lildua.oddly.core.text

/**
 * Where a share card sends the person who receives it.
 *
 * The share card is the app's only growth surface, and an image with no
 * attribution leaves a recipient with no idea what made it. These strings are
 * the call to action, kept in one place so the card, the share caption and any
 * future store listing cannot disagree.
 *
 * **Placeholders.** Replace [shortLink] with the real landing page or store
 * short link before release, and [handle] with the account you actually hold.
 */
object Brand {

    const val NAME: String = "1% HUMAN"

    /** Shown on the card and appended to the share caption. */
    const val SHORT_LINK: String = "1percenthuman.app"

    /** Social handle, for captions on platforms where it is tappable. */
    const val HANDLE: String = "@1percenthuman"

    const val HASHTAG: String = "#1PercentHuman"
}
