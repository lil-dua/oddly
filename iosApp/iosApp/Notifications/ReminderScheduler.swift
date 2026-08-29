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

    /// Rewrites the whole schedule rather than patching it: the settings are
    /// the source of truth, and a stale request is worse than a rebuilt one.
    static func reschedule(settings: AppSettings) async {
        cancelAll()

        guard settings.reminderEnabled, await isAuthorized() else { return }

        let center = UNUserNotificationCenter.current()
        let calendar = Calendar.current
        let now = Date()

        for offset in 0..<horizonInDays {
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

            let dayIndex = Int32(floor(fireDate.timeIntervalSince1970 / 86_400))
            let copy = ReminderSeed.shared.content(dayIndex: dayIndex, language: settings.language)

            let content = UNMutableNotificationContent()
            content.title = copy.title
            content.body = copy.body
            content.sound = settings.soundEnabled ? .default : nil

            let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: fireDate)
            let request = UNNotificationRequest(
                identifier: identifierPrefix + String(dayIndex),
                content: content,
                trigger: UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
            )
            try? await center.add(request)
        }
    }

    static func cancelAll() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
}
