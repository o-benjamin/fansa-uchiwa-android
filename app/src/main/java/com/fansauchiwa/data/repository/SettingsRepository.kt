package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.SettingsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface SettingsRepository {
    /**
     * 触覚フィードバックが有効かどうかのFlowを返す
     * デフォルト値は true
     */
    fun getHapticFeedbackEnabledStream(): Flow<Boolean>

    /**
     * 触覚フィードバックの現在の設定値を取得する
     */
    suspend fun fetchHapticFeedbackEnabled()

    /**
     * 触覚フィードバックの有効/無効を設定する
     *
     * @param enabled 有効にする場合は true
     */
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)

    fun getHasSeenEditCompletionTooltipStream(): Flow<Boolean>

    suspend fun fetchHasSeenEditCompletionTooltip()

    suspend fun setHasSeenEditCompletionTooltip(hasSeen: Boolean)

    fun getHasSeenApologyDialogStream(): Flow<Boolean>

    suspend fun fetchHasSeenApologyDialog()

    suspend fun setHasSeenApologyDialog(hasSeen: Boolean)
}

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : SettingsRepository {

    private val _hapticFeedbackEnabledStream = MutableSharedFlow<Boolean>(replay = 1)
    private val _hasSeenEditCompletionTooltipStream = MutableSharedFlow<Boolean>(replay = 1)
    private val _hasSeenApologyDialogStream = MutableSharedFlow<Boolean>(replay = 1)

    override fun getHapticFeedbackEnabledStream(): Flow<Boolean> =
        _hapticFeedbackEnabledStream.asSharedFlow()

    override fun getHasSeenEditCompletionTooltipStream(): Flow<Boolean> =
        _hasSeenEditCompletionTooltipStream.asSharedFlow()

    override fun getHasSeenApologyDialogStream(): Flow<Boolean> =
        _hasSeenApologyDialogStream.asSharedFlow()

    override suspend fun fetchHapticFeedbackEnabled() {
        val value = settingsDataSource.getHapticFeedbackEnabledStream().first()
        _hapticFeedbackEnabledStream.emit(value)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        settingsDataSource.setHapticFeedbackEnabled(enabled)
    }

    override suspend fun fetchHasSeenEditCompletionTooltip() {
        val value = settingsDataSource.getHasSeenEditCompletionTooltipStream().first()
        _hasSeenEditCompletionTooltipStream.emit(value)
    }

    override suspend fun setHasSeenEditCompletionTooltip(hasSeen: Boolean) {
        settingsDataSource.setHasSeenEditCompletionTooltip(hasSeen)
    }

    override suspend fun fetchHasSeenApologyDialog() {
        val value = settingsDataSource.getHasSeenApologyDialogStream().first()
        _hasSeenApologyDialogStream.emit(value)
    }

    override suspend fun setHasSeenApologyDialog(hasSeen: Boolean) {
        settingsDataSource.setHasSeenApologyDialog(hasSeen)
    }
}
