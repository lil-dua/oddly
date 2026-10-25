import Foundation
import UserNotifications
import SharedLogic

/// Schedules the daily reminder (spec §S16).
///
/// A single repeating `UNCalendarNotificationTrigger` would be simpler, but its
/// content is fixed forever — and the spec asks for copy that varies so the
/// reminder doesn't feel spammy. So a rolling window of one-shot requests is
/// scheduled instead, each with its own day's copy, and topped back up every
/// time the app launches or the settings change.
///
/// Each request is decided by `ReminderPlanner` (shared with Android): a day
/// that is already done gets none, a streak of 3+ gets streak copy, and the last
/// request of the window says "it's been a while" since reminders then pause.
/// Day indexes and identifiers use the user's *local* day, not UTC.
///
/// iOS keeps pending requests across launches and reboots, so unlike Android
/// there is nothing to re-arm on boot.
enum ReminderScheduler {

    /// Comfortably under the 64-request limit, and far longer than the gap
    /// between two launches of an app the user opens daily.
    private static let horizonInDays = 14
    private static let identifierPrefix = "oddly.reminder."

    /// Asks for permission the first time, then reports whether it was given.
    @discardableResult
    static func requestAuthorization() async -> Bool {
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()

        switch settings.authorizationStatus {
        case .notDetermined:
            return (try? await center.requestAuthorization(options: [.alert, .sound, .badge])) ?? false
        case .denied:
            return false
        default:
            return true
        }
    }

    static func isAuthorized() async -> Bool {
        let status = await UNUserNotificationCenter.current().notificationSettings().authorizationStatus
        return status == .authorized || status == .provisional || status == .ephemeral
    }

    /// Rewrites the whole schedule rather than patching it: the settings and
    /// completions are the source of truth, and a stale request is worse than a
    /// rebuilt one.
    static func reschedule(settings: AppSettings, completions: [ChallengeCompletion]) async {
        cancelAll()

        let center = UNUserNotificationCenter.current()
        let calendar = Calendar.current
        let now = Date()

        // A reminder already shown today is stale once today is done. Cleared
        // before the enabled/permission guard, like Android, so a notification
        // delivered earlier does not linger after the user turned reminders off.
        let todayLocal = LocalDate.from(now, calendar: calendar)
        if DailyChallengeRules.shared.isDayDone(completions: completions, today: todayLocal) {
            center.removeDeliveredNotifications(
                withIdentifiers: [identifierPrefix + String(todayLocal.toEpochDays())]
            )
        }

        guard settings.reminderEnabled, await isAuthorized() else { return }

        for offset in 0..<horizonInDays {
            // A newer run owns the schedule now; do not re-add what it cleared.
            if Task.isCancelled { return }

            guard
                let day = calendar.date(byAdding: .day, value: offset, to: now),
                let fireDate = calendar.date(
                    bySettingHour: Int(settings.reminderTime.hour),
                    minute: Int(settings.reminderTime.minute),
                    second: 0,
                    of: day
                ),
                fireDate > now
            else { continue }

            let fireDay = LocalDate.from(fireDate, calendar: calendar)
            guard let copy = ReminderPlanner.shared.content(
                completions: completions,
                fireDate: fireDay,
                language: settings.language,
                isLastScheduled: offset == horizonInDays - 1
            ) else { continue }

            let content = UNMutableNotificationContent()
            content.title = copy.title
            content.body = copy.body
            content.sound = settings.soundEnabled ? .default : nil

            let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: fireDate)
            let request = UNNotificationRequest(
                identifier: identifierPrefix + String(fireDay.toEpochDays()),
                content: content,
                trigger: UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
            )
            if Task.isCancelled { return }
            try? await center.add(request)
        }
    }

    static func cancelAll() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
}
