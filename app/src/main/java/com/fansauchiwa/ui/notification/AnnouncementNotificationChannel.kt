package com.fansauchiwa.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.fansauchiwa.R

/**
 * Firebase コンソールから送るお知らせ（#429）の通知チャンネル。
 * FCM はアプリの外にいるときの通知を自分で出し、その先に AndroidManifest.xml の `default_notification_channel_id` を使うので、
 * 届く前にチャンネルを作っておく。同じ ID で作り直しても、利用者が端末の設定で変えた内容は消えない
 */
object AnnouncementNotificationChannel {
    fun create(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            context.getString(R.string.announcement_notification_channel_id),
            context.getString(R.string.announcement_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.announcement_notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
