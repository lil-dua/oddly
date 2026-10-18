package dev.lildua.oddly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Alarms do not survive a reboot or an app update, so the reminder is re-armed
 * from [ReminderStore] once the device comes back up. Without this the daily
 * nudge would silently stop after a restart.
 *
 * Time and time-zone changes also land here: the alarm is stored as an epoch
 * instant, so it must be recomputed to keep firing at the chosen local time.
 * Re-arming is idempotent, so a spurious NTP-style TIME_SET is harmless.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> ReminderScheduler.rescheduleFromStore(context)
        }
    }
}
