package com.fansauchiwa.analytics

/**
 * プッシュ通知（#429）の計測のパラメータ
 *
 * - [AnalyticsActions.OPEN_FROM_PUSH]：Firebase コンソールから送ったお知らせの通知を押して、アプリを開いたとき。
 *   アプリの外にいるあいだに届いた通知だけが対象（アプリを開いているあいだに届いたお知らせは、FCM が通知を出さない）
 *
 * [PARAM_SOURCE] に、お知らせごとに決める名前（コンソールの「カスタムデータ」のキー `campaign` の値）を付ける。
 * 名前がないお知らせはパラメータなしで送る。パラメータ名は GA4 にカスタムディメンションとして登録済みの `source` を使い、
 * 登録を待たずに初日からお知らせごとに見られるようにした（[UpdatePromptAnalyticsParams] と同じ。イベント名で絞れば、ほかの `source` とは混ざらない）。
 * コンソールから送ったお知らせは、FCM も `notification_open` などを自動で送る（`.agents/push-notifications.md`）
 */
object PushAnalyticsParams {
    const val PARAM_SOURCE = "source"

    // GA4 のイベントのパラメータの値の上限
    private const val MAX_VALUE_LENGTH = 100

    /**
     * @param campaign 通知のカスタムデータの `campaign` の値。ないとき・空のときは null
     */
    fun params(campaign: String?): Map<String, Any> {
        val source = campaign?.trim()?.takeIf { it.isNotEmpty() } ?: return emptyMap()
        return mapOf(PARAM_SOURCE to source.take(MAX_VALUE_LENGTH))
    }
}
