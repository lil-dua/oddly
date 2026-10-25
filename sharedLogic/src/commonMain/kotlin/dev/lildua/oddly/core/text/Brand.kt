package dev.lildua.oddly.core.text

/**
 * Single source of truth for every brand link, address and store id.
 *
 * The share card is the app's only growth surface, and an image with no
 * attribution leaves a recipient with no idea what made it. The card, the share
 * caption, Settings and the store listing all read from here so they cannot
 * disagree.
 *
 * **Landing page.** [WEBSITE_URL] is not published yet; its source lives in
 * `docs/landing/index.html` and must go up before release.
 *
 * **No social handle.** There is no account the developer actually holds, and
 * printing a handle that belongs to someone else is a brand risk. Add one back
 * when a real account exists.
 *
 * **App Store id.** Fill [APP_STORE_ID] in T-17; the iOS "Rate the app" row
 * appears on its own once it is non-null.
 */
object Brand {

    const val NAME: String = "1% HUMAN"

    const val HASHTAG: String = "#1PercentHuman"

    const val DEVELOPER: String = "Lil Dua"

    /** Scheme-less form for the share card image, where it cannot be tapped. */
    const val WEBSITE_DISPLAY: String = "lil-dua.github.io/projects/oddly"

    /** Full form for captions so messaging apps linkify it; trailing slash avoids a Pages redirect. */
    const val WEBSITE_URL: String = "https://lil-dua.github.io/projects/oddly/"

    const val PRIVACY_POLICY_URL: String =
        "https://lil-dua.github.io/projects/oddly/privacy/privacy-policy.html"

    const val SUPPORT_EMAIL: String = "huuthom.it@gmail.com"

    /** Numeric App Store id; null until the app exists on the store. Not `const` so it can be null. */
    val APP_STORE_ID: String? = null
}
