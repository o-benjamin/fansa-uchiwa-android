package com.fansauchiwa.data.infra

import com.fansauchiwa.data.analytics.AnalyticsEvent

interface AnalyticsDataSource {
    /**
     * イベントを送信する
     *
     * @param event 送信するイベント
     */
    suspend fun logEvent(event: AnalyticsEvent)

    /**
     * 画面表示イベントを送信する
     *
     * @param screenName 画面名
     */
    suspend fun logScreenView(screenName: String)
}

