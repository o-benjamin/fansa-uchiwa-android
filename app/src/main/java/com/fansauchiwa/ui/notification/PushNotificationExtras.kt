package com.fansauchiwa.ui.notification

/**
 * FCM の通知を押して開いたとき、MainActivity の Intent に入っている extra の名前（#429）。
 * FCM はアプリの外にいるときに届いた通知を押されると、ランチャーの Activity を開き、通知のデータを extra に入れる
 */
object PushNotificationExtras {
    /** FCM が入れるメッセージの ID。これがあれば FCM の通知から開いたとみなす */
    const val MESSAGE_ID = "google.message_id"

    /**
     * Firebase コンソールの「追加オプション > カスタムデータ」に入れるキー。値はお知らせごとに決める名前（例 `2026-12-new-templates`）。
     * 送り方は `.agents/push-notifications.md`
     */
    const val CAMPAIGN = "campaign"
}
