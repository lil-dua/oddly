package dev.lildua.oddly.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import dev.lildua.oddly.MainActivity
import dev.lildua.oddly.R
import dev.lildua.oddly.core.text.ReminderSeed
import dev.lildua.oddly.domain.model.AppLanguage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Posts the daily reminder when the alarm fires.
 *
 * The copy is chosen here rather than baked into the alarm so it varies day to
 * day (spec §S16). The language rides along in the intent because the receiver
 * may run in a fresh process with no app state to read.
 *
 * The alarm is one-shot, so this also arms tomorrow's before returning.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Arm tomorrow first: a missing permission should stop the notification,
        // not the schedule, so that granting it later resumes the reminder.
        ReminderScheduler.rescheduleFromStore(context)

        if (!canPostNotifications(context)) return

        val language = AppLanguage.entries
            .firstOrNull { it.tag == intent.getStringExtra(ReminderScheduler.EXTRA_LANGUAGE) }
            ?: AppLanguage.VIETNAMESE

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val content = ReminderSeed.content(today.toEpochDays(), language)

        ReminderScheduler.ensureChannel(context)

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()

        context.getSystemService<NotificationManager>()?.notify(NOTIFICATION_ID, notification)
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        // Fixed, so a new reminder replaces yesterday's rather than stacking.
        const val NOTIFICATION_ID = 1
    }
}
