package dev.lildua.oddly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Alarms do not survive a reboot or an app update, so the reminder is re-armed
 * from [ReminderStore] once the device comes back up. Without this the daily
 * nudge would silently stop after a restart.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> ReminderScheduler.rescheduleFromStore(context)
        }
    }
}
