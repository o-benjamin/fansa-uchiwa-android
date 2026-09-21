package com.fansauchiwa.ui.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.fansauchiwa.MainActivity
import com.fansauchiwa.R
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.EventAnalyticsParams
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.EventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val EVENT_REMINDER_WORK_NAME = "event-reminder-work"
private const val EVENT_REMINDER_CHANNEL_ID = "event-reminder-channel"
private const val EVENT_REMINDER_HOUR = 20
private const val EVENT_REMINDER_MINUTE = 0

// 通知のタップで起動したことを MainActivity が計測するための Intent の extra（#249）
const val EXTRA_REMINDER_DAYS_UNTIL = "reminder_days_until"

// WorkManager がクラス名を DB に永続化し、HiltWorkerFactory もそのクラス名で生成方法を引くため R8 から保護する。
// 端末に登録済みの定期ジョブが生成できなくなるので、クラス名・パッケージも変更しないこと
@Keep
@HiltWorker
class UchiwaReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val eventRepository: EventRepository,
    private val analyticsRepository: AnalyticsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // getEventsStream() は fetchEvents() するまで値を流さないため、先に取得する
        eventRepository.fetchEvents()
        val events = eventRepository.getEventsStream().first()

        val today = LocalDate.now()
        val reminderTargets = selectReminderTargets(events, today)

        if (reminderTargets.isEmpty()) return Result.success()

        createNotificationChannel(applicationContext)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        reminderTargets.forEach { eventWithUchiwas ->
            val daysUntil = calculateDaysUntil(today, eventWithUchiwas.event.eventDateEpochDay)
            UchiwaReminderNotifier.showReminder(
                context = applicationContext,
                eventId = eventWithUchiwas.event.id,
                eventName = eventWithUchiwas.event.name,
                daysUntil = daysUntil,
                notificationManager = notificationManager
            )
            // 計測の失敗で通知のジョブ自体を失敗させない
            runCatching {
                analyticsRepository.logEvent(
                    AnalyticsEvent(
                        name = AnalyticsActions.REMINDER_SHOW,
                        params = mapOf(EventAnalyticsParams.DAYS_UNTIL to daysUntil)
                    )
                )
            }
        }

        return Result.success()
    }
}

object UchiwaReminderScheduler {
    fun schedule(context: Context) {
        val initialDelay = calculateInitialDelay()
        val request = PeriodicWorkRequestBuilder<UchiwaReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENT_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun calculateInitialDelay(now: LocalDateTime = LocalDateTime.now()): Duration {
        val reminderTime = LocalTime.of(EVENT_REMINDER_HOUR, EVENT_REMINDER_MINUTE)
        val nextRunTime = if (now.toLocalTime().isBefore(reminderTime)) {
            now.toLocalDate().atTime(reminderTime)
        } else {
            now.toLocalDate().plusDays(1).atTime(reminderTime)
        }
        return Duration.between(now, nextRunTime)
    }
}

object UchiwaReminderNotifier {
    fun showReminder(
        context: Context,
        eventId: String,
        eventName: String,
        daysUntil: Int,
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    ) {
        createNotificationChannel(context)
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val openAppIntent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_REMINDER_DAYS_UNTIL, daysUntil)
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentText = context.getString(
            R.string.event_reminder_message,
            eventName,
            daysUntil
        )
        val notification = NotificationCompat.Builder(
            context,
            EVENT_REMINDER_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(
                context.getString(
                    R.string.event_reminder_title,
                    daysUntil
                )
            )
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(eventId.hashCode(), notification)
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        EVENT_REMINDER_CHANNEL_ID,
        context.getString(R.string.event_reminder_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = context.getString(R.string.event_reminder_channel_description)
    }
    notificationManager.createNotificationChannel(channel)
}
