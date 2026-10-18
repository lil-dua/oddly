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
import dev.lildua.oddly.core.time.Today
import dev.lildua.oddly.data.local.OddlyRepository
import dev.lildua.oddly.data.local.SharedPreferencesStore
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.usecase.ReminderPlanner

/**
 * Posts the daily reminder when the alarm fires.
 *
 * The copy is chosen here rather than baked into the alarm so it varies day to
 * day (spec §S16). The language rides along in the intent because the receiver
 * may run in a fresh process with no app state to read.
 *
 * The alarm is one-shot, so this also arms tomorrow's before returning. That
 * happens before the "already done today" check, which only skips the post.
 * Completions are read straight from the repository (the app may not be
 * running); [ReminderPlanner] decides whether to stay silent and what to say.
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

        val completions = OddlyRepository(SharedPreferencesStore(context)).load()
            ?.completions.orEmpty()
        val content = ReminderPlanner.content(
            completions = completions,
            fireDate = Today.local(),
            language = language,
            isLastScheduled = false,
        ) ?: return

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

        context.getSystemService<NotificationManager>()?.notify(ReminderScheduler.NOTIFICATION_ID, notification)
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
}
