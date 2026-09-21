package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.AppInstallDataSource
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

    /**
     * お詫びダイアログを見たかどうかを取得して流す
     * 新規インストールの場合は「見た」を保存してから true を流す
     */
    suspend fun fetchHasSeenApologyDialog()

    suspend fun setHasSeenApologyDialog(hasSeen: Boolean)

    /**
     * 最後に保存したうちわの最終的なフォント名を取得する（#242: font_same_as_last の比較用）
     * 一度も保存していない場合は null
     */
    suspend fun getLastSavedFontName(): String?

    /**
     * 最後に保存したうちわの最終的なフォント名を保存する
     */
    suspend fun setLastSavedFontName(fontName: String)
}

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
    private val appInstallDataSource: AppInstallDataSource
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
        val hasSeen = settingsDataSource.getHasSeenApologyDialogStream().first()
        // お詫びダイアログは v2.5.0 より前から使っていた人向けなので、新規インストールでは見たことにする。
        // 次に更新すると新規インストールと判定できなくなるため、ここで保存しておく。
        // インストール後に一度も開かずに更新した場合は判定できず、ダイアログが出る（許容している）。
        // ダイアログは #239 で削除する予定
        val isSkippedForFreshInstall =
            !hasSeen && appInstallDataSource.getIsFreshInstallStream().first()
        if (isSkippedForFreshInstall) {
            settingsDataSource.setHasSeenApologyDialog(true)
        }
        _hasSeenApologyDialogStream.emit(hasSeen || isSkippedForFreshInstall)
    }

    override suspend fun setHasSeenApologyDialog(hasSeen: Boolean) {
        settingsDataSource.setHasSeenApologyDialog(hasSeen)
    }

    override suspend fun getLastSavedFontName(): String? {
        return settingsDataSource.getLastSavedFontName()
    }

    override suspend fun setLastSavedFontName(fontName: String) {
        settingsDataSource.setLastSavedFontName(fontName)
    }
}
