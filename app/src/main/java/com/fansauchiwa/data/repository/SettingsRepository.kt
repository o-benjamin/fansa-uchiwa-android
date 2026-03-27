package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.SettingsDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SettingsRepository {
    /**
     * 触覚フィードバックが有効かどうかのFlowを返す
     * デフォルト値は true
     */
    val isHapticFeedbackEnabled: Flow<Boolean>

    /**
     * 触覚フィードバックの有効/無効を設定する
     *
     * @param enabled 有効にする場合は true
     */
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
}

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : SettingsRepository {

    override val isHapticFeedbackEnabled: Flow<Boolean> =
        settingsDataSource.isHapticFeedbackEnabled

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        settingsDataSource.setHapticFeedbackEnabled(enabled)
    }
}

