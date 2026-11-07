package com.fansauchiwa.analytics

import com.fansauchiwa.data.repository.CrashReportingRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * マニフェストから作られる [ShareTargetReceiver] から、送信と例外の記録に使う Repository を取り出すための EntryPoint
 * （レシーバーにはコンストラクタで渡せないため）
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ShareTargetReceiverEntryPoint {
    fun analyticsRepository(): AnalyticsRepository
    fun crashReportingRepository(): CrashReportingRepository
}
