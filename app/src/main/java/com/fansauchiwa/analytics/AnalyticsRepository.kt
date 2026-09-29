package com.fansauchiwa.analytics

import javax.inject.Inject

interface AnalyticsRepository {
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

class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDataSource: AnalyticsDataSource
) : AnalyticsRepository {

    override suspend fun logEvent(event: AnalyticsEvent) {
        analyticsDataSource.logEvent(event)
    }

    override suspend fun logScreenView(screenName: String) {
        analyticsDataSource.logScreenView(screenName)
    }
}

