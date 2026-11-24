package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.SettingsDataSource
import java.time.LocalDate
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

    /**
     * ホームの「今日のお題」のカード（#360）を閉じた日付。閉じたことがなければ null
     */
    fun getDailyPromptDismissedDateStream(): Flow<LocalDate?>

    suspend fun fetchDailyPromptDismissedDate()

    /**
     * カードを閉じた日付を保存し、続けて流す（カードをすぐ消すため）
     */
    suspend fun setDailyPromptDismissedDate(date: LocalDate)
}

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : SettingsRepository {

    private val _hapticFeedbackEnabledStream = MutableSharedFlow<Boolean>(replay = 1)
    private val _dailyPromptDismissedDateStream = MutableSharedFlow<LocalDate?>(replay = 1)

    override fun getHapticFeedbackEnabledStream(): Flow<Boolean> =
        _hapticFeedbackEnabledStream.asSharedFlow()

    override suspend fun fetchHapticFeedbackEnabled() {
        val value = settingsDataSource.getHapticFeedbackEnabledStream().first()
        _hapticFeedbackEnabledStream.emit(value)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        settingsDataSource.setHapticFeedbackEnabled(enabled)
    }

    override fun getDailyPromptDismissedDateStream(): Flow<LocalDate?> =
        _dailyPromptDismissedDateStream.asSharedFlow()

    override suspend fun fetchDailyPromptDismissedDate() {
        _dailyPromptDismissedDateStream.emit(
            settingsDataSource.getDailyPromptDismissedDateStream().first()
        )
    }

    override suspend fun setDailyPromptDismissedDate(date: LocalDate) {
        settingsDataSource.setDailyPromptDismissedDate(date)
        _dailyPromptDismissedDateStream.emit(date)
    }
}
