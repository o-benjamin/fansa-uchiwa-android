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

    fun getHasSeenEditCompletionTooltipStream(): Flow<Boolean>

    suspend fun setHasSeenEditCompletionTooltip(hasSeen: Boolean)

    /**
     * バージョンアップ後初回起動のお詫びダイアログを見たかどうかのFlowを返す
     */
    fun getHasSeenApologyDialogStream(): Flow<Boolean>

    /**
     * バージョンアップ後初回起動のお詫びダイアログを見たかどうかを設定する
     */
    suspend fun setHasSeenApologyDialog(hasSeen: Boolean)

    /**
     * 最後に保存したうちわの最終的なフォント名を取得する（#242: font_same_as_last の比較用）
     */
    suspend fun getLastSavedFontName(): String?

    /**
     * 最後に保存したうちわの最終的なフォント名を保存する
     */
    suspend fun setLastSavedFontName(fontName: String)
}
