package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

interface SettingsDataSource {
    /**
     * 触覚フィードバックが有効かどうかのFlowを返す
     * デフォルト値は true
     */
    fun getHapticFeedbackEnabledStream(): Flow<Boolean>

    /**
     * 触覚フィードバックの有効/無効を設定する
     *
     * @param enabled 有効にする場合は true
     */
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
}
