package dev.lildua.oddly.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import dev.lildua.oddly.domain.model.AppLanguage
import kotlinx.datetime.LocalTime
import java.util.Calendar

/**
 * Schedules the daily reminder (spec §S16).
 *
 * Uses a windowed one-shot alarm that re-arms itself, rather than an exact
 * alarm or a repeating one. Exact alarms would mean asking for
 * `SCHEDULE_EXACT_ALARM`, a heavyweight permission for a habit reminder;
 * `setInexactRepeating` goes the other way and lets the system slide a daily
 * alarm by most of a day, which is useless for "remind me at 09:00".
 * [WINDOW_MILLIS] keeps the slack to something a user would not notice.
 *
 * The alarm survives the app being killed but not a reboot, so the chosen time
 * is mirrored into [ReminderStore] and re-armed by [BootReceiver]. That store
 * is scheduler state only; app data is still in memory, per the roadmap.
 */
object ReminderScheduler {

    const val CHANNEL_ID = "daily_reminder"
    const val EXTRA_LANGUAGE = "language_tag"

    private const val REQUEST_CODE = 1001

    /** How late the reminder may land. Wide enough for the system to batch. */
    private const val WINDOW_MILLIS = 15 * 60 * 1000L

    /** Re-arms the reminder for [time], replacing any previously set alarm. */
    fun schedule(context: Context, time: LocalTime, language: AppLanguage) {
        val appContext = context.applicationContext
        ensureChannel(appContext)
        ReminderStore.save(appContext, enabled = true, time = time, language = language)

        val alarms = appContext.getSystemService<AlarmManager>() ?: return
        alarms.setWindow(
            AlarmManager.RTC_WAKEUP,
            nextOccurrence(time),
            WINDOW_MILLIS,
            pendingIntent(appContext, language, mutable = false),
        )
    }

    fun cancel(context: Context) {
        val appContext = context.applicationContext
        ReminderStore.clear(appContext)
        appContext.getSystemService<AlarmManager>()
            ?.cancel(pendingIntent(appContext, AppLanguage.VIETNAMESE, mutable = false))
    }

    /** Re-arms from the stored settings; used after a reboot or app update. */
    fun rescheduleFromStore(context: Context) {
        val saved = ReminderStore.load(context) ?: return
        schedule(context, saved.time, saved.language)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "1% HUMAN",
                // Default rather than high: this is a gentle nudge, so it should
                // not interrupt with a heads-up banner.
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    private fun pendingIntent(
        context: Context,
        language: AppLanguage,
        mutable: Boolean,
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_LANGUAGE, language.tag)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }

    /** [time] today if it is still ahead of us, otherwise [time] tomorrow. */
    private fun nextOccurrence(time: LocalTime): Long {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}
