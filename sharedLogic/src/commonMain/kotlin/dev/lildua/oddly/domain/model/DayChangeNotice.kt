package dev.lildua.oddly.domain.model

/**
 * Why Home is showing a one-off banner about the calendar moving on.
 *
 * Shared so both apps pick the same wording for the same situation; each
 * AppState only records which one applies and the screen renders it.
 */
enum class DayChangeNotice {
    /**
     * The app was running (or resumed) and the day rolled over by itself.
     * Swift sees this as `.theNewDay`: Obj-C reserves the `new` prefix.
     */
    NEW_DAY,

    /** Complete was tapped after midnight: nothing was recorded, a new day was loaded. */
    COMPLETION_REJECTED,
}
