package dev.lildua.oddly.core.text

import dev.lildua.oddly.domain.model.AppLanguage

/** One day's reminder copy. */
data class ReminderContent(
    val title: String,
    val body: String,
)

/**
 * Rotating copy for the daily reminder (spec §S16).
 *
 * The spec asks for content that varies so the reminder does not feel spammy,
 * and the wording deliberately never names a challenge: reminders are scheduled
 * days ahead, and the pick can be swapped after that, so a notification
 * that named one would often be wrong. It also never mentions the streak —
 * ReminderPlanner has its own streak copy for when there is one worth protecting.
 *
 * Selection is by day index rather than random, so the same device shows the
 * same message on a given day however many times it is rescheduled.
 */
object ReminderSeed {

    private val vietnamese = listOf(
        ReminderContent(
            title = "Thử thách hôm nay đang đợi bạn",
            body = "Vài phút thôi, và hôm nay sẽ khác một chút.",
        ),
        ReminderContent(
            title = "1% của hôm nay?",
            body = "Mở app và xem thử thách nhỏ dành cho bạn.",
        ),
        ReminderContent(
            title = "Hôm nay vẫn còn chỗ cho 1%",
            body = "Một hành động nhỏ là đủ để thêm một ngày vào hành trình.",
        ),
        ReminderContent(
            title = "Một điều nhỏ, ngay bây giờ",
            body = "Thử thách hôm nay chỉ tốn của bạn vài phút.",
        ),
        ReminderContent(
            title = "Hôm nay bạn muốn khác đi chứ?",
            body = "Có một thử thách đang chờ trong app.",
        ),
        ReminderContent(
            title = "Đến giờ cho 1% của bạn",
            body = "Hoàn thành thử thách hôm nay để cộng thêm vào hành trình.",
        ),
    )

    private val english = listOf(
        ReminderContent(
            title = "Today's challenge is waiting",
            body = "A few minutes, and today looks a little different.",
        ),
        ReminderContent(
            title = "Ready for today's 1%?",
            body = "Open the app and see the small thing waiting for you.",
        ),
        ReminderContent(
            title = "There's still room for today's 1%",
            body = "One small action is enough to add a day to your journey.",
        ),
        ReminderContent(
            title = "One small thing, right now",
            body = "Today's challenge only costs you a few minutes.",
        ),
        ReminderContent(
            title = "Want today to be different?",
            body = "There's a challenge waiting in the app.",
        ),
        ReminderContent(
            title = "Time for your 1%",
            body = "Finish today's challenge and add it to your journey.",
        ),
    )

    /** Deterministic pick so a day's message is stable across reschedules. */
    fun content(dayIndex: Int, language: AppLanguage): ReminderContent {
        val pool = when (language) {
            AppLanguage.VIETNAMESE -> vietnamese
            AppLanguage.ENGLISH -> english
        }
        return pool[((dayIndex % pool.size) + pool.size) % pool.size]
    }
}
