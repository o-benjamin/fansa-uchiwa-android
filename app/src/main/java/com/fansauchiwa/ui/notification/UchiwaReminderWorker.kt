package com.fansauchiwa.ui.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.fansauchiwa.MainActivity
import com.fansauchiwa.R
import com.fansauchiwa.data.source.FansaUchiwaDatabase
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val EVENT_REMINDER_WORK_NAME = "event-reminder-work"
private const val EVENT_REMINDER_CHANNEL_ID = "event-reminder-channel"
private const val EVENT_REMINDER_HOUR = 20
private const val EVENT_REMINDER_MINUTE = 0

class UchiwaReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val events = EventReminderDatabaseProvider
            .get(applicationContext)
            .uchiwaDao()
            .getAllEventsWithUchiwasStream()
            .first()

        val today = LocalDate.now()
        val reminderTargets = events.filter { eventWithUchiwas ->
            val daysUntil = calculateDaysUntil(today, eventWithUchiwas.event.eventDateEpochDay)
            eventWithUchiwas.uchiwas.isNotEmpty() && daysUntil in 0..10
        }

        if (reminderTargets.isEmpty()) return Result.success()

        createNotificationChannel()

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
            val openAppIntent = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                eventWithUchiwas.event.id.hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val contentText = if (daysUntil == 0) {
                applicationContext.getString(R.string.event_reminder_today_message)
            } else {
                applicationContext.getString(
                    R.string.event_reminder_message,
                    eventWithUchiwas.event.name,
                    daysUntil
                )
            }
            val notification = NotificationCompat.Builder(
                applicationContext,
                EVENT_REMINDER_CHANNEL_ID
            )
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(
                    applicationContext.getString(
                        R.string.event_reminder_title,
                        eventWithUchiwas.event.name
                    )
                )
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            notificationManager.notify(eventWithUchiwas.event.id.hashCode(), notification)
        }

        return Result.success()
    }

    private fun calculateDaysUntil(today: LocalDate, eventDateEpochDay: Long): Int {
        return ChronoUnit.DAYS.between(
            today,
            LocalDate.ofEpochDay(eventDateEpochDay)
        ).toInt()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            EVENT_REMINDER_CHANNEL_ID,
            applicationContext.getString(R.string.event_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description =
                applicationContext.getString(R.string.event_reminder_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
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

private object EventReminderDatabaseProvider {
    @Volatile
    private var database: FansaUchiwaDatabase? = null

    fun get(context: Context): FansaUchiwaDatabase {
        return database ?: synchronized(this) {
            database ?: FansaUchiwaDatabase.build(context).also { database = it }
        }
    }
}
