package com.fansauchiwa.analytics

import com.fansauchiwa.data.InAppMessageTarget

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）の計測のパラメータ
 *
 * - [AnalyticsActions.TAP_IN_APP_MESSAGE]：メッセージのボタンを押したとき。[PARAM_TARGET] で行き先（[InAppMessageTarget.analyticsValue]）を付ける
 *
 * 表示・ボタン・閉じたの数とキャンペーン名は、Firebase In-App Messaging が Analytics に送る標準のイベント
 * （`firebase_in_app_message_impression`・`firebase_in_app_message_action`・`firebase_in_app_message_dismiss`、`message_name` など）で見る。
 * 標準のイベントにはボタンの URL が載らず、カードのメッセージはボタンを2つ置けるので、どの行き先を押したかだけをこちらで送る。
 * パラメータ名は GA4 にカスタムディメンションとして登録済みの `target`（[ShareAnalyticsParams] と同じ名前）を使い、
 * 登録を待たずに初日から見られるようにした。イベント名で絞れば、共有先の `target` とは混ざらない
 */
object InAppMessageAnalyticsParams {
    const val PARAM_TARGET = "target"

    fun params(target: InAppMessageTarget): Map<String, Any> = mapOf(PARAM_TARGET to target.analyticsValue)
}
