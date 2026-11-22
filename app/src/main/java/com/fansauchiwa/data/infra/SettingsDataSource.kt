package com.fansauchiwa.data.infra

import java.time.LocalDate
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

    /**
     * バージョンアップ後初回起動のお詫びダイアログを見たかどうかのFlowを返す
     */
    fun getHasSeenApologyDialogStream(): Flow<Boolean>

    /**
     * バージョンアップ後初回起動のお詫びダイアログを見たかどうかを設定する
     */
    suspend fun setHasSeenApologyDialog(hasSeen: Boolean)

    /**
     * ホームの「今日のお題」のカード（#360）を閉じた日付のFlowを返す。閉じたことがなければ null
     */
    fun getDailyPromptDismissedDateStream(): Flow<LocalDate?>

    /**
     * ホームの「今日のお題」のカードを閉じた日付を設定する。その日はカードを出さない
     */
    suspend fun setDailyPromptDismissedDate(date: LocalDate)
}
