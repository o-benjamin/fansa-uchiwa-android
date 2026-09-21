package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.EDIT_START_TIME_ARG
import com.fansauchiwa.FINAL_FONT_NAME_ARG
import com.fansauchiwa.FONT_SWITCH_COUNT_ARG
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.analytics.FontSessionAnalyticsParams
import com.fansauchiwa.data.analytics.editDurationBucket
import com.fansauchiwa.data.analytics.finalFontRankBucket
import com.fansauchiwa.data.analytics.fontSwitchBucket
import com.fansauchiwa.data.extractUchiwaIdFromImagePath
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.edit.FontFamilies
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val UI_STATE_KEY = "ui_state"

@HiltViewModel
class UchiwaPreviewViewModel @Inject constructor(
    private val masterpieceRepository: MasterpieceRepository,
    private val adMobRepository: AdMobRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: StateFlow<UchiwaPreviewUiState> =
        savedStateHandle.getStateFlow(UI_STATE_KEY, UchiwaPreviewUiState())

    /** この画面のセッション中にリワード広告を既に視聴済みかどうか */
    private var hasEarnedRewardInSession = false

    val hasEarnedReward: Boolean
        get() = hasEarnedRewardInSession

    init {
        adMobRepository.loadRewardedAd()
        // 広告のロード状態を監視
        viewModelScope.launch {
            adMobRepository.isLoadingRewardedAd.collect { isLoading ->
                val currentState = uiState.value
                savedStateHandle[UI_STATE_KEY] = currentState.copy(isLoadingAd = isLoading)
            }
        }
        // Navigation引数からimagePathとフォント計測データ（#242）を取得してUI Stateに設定
        val encodedImagePath = savedStateHandle.get<String>(IMAGE_PATH_ARG)
        val decodedImagePath = encodedImagePath?.let { URLDecoder.decode(it, "UTF-8") }
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            imagePath = decodedImagePath ?: currentState.imagePath,
            fontSwitchCount = savedStateHandle.get<Int>(FONT_SWITCH_COUNT_ARG) ?: 0,
            finalFontName = savedStateHandle.get<String>(FINAL_FONT_NAME_ARG),
            editStartTimeMillis = savedStateHandle.get<Long>(EDIT_START_TIME_ARG) ?: 0L
        )
    }

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.PREVIEW_SCREEN)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(eventName, params)
            )
        }
    }

    /**
     * リワード広告を表示し、報酬獲得後にギャラリーに保存する
     * 広告のロードに失敗している場合は即座に保存を実行（UX低下を防ぐ）
     * この画面で既に広告を視聴済みの場合は広告をスキップして保存を実行
     */
    fun showRewardedAdAndSave(activity: Activity) {
        val currentState = uiState.value
        // 連打などで多重に実行しない。isSaveButtonPressedは一連の保存処理が終わるまでtrueのままなので、
        // これによりfont_same_as_last用の読み取り/上書き（下記）が重ならないことも保証される
        if (currentState.isSaveButtonPressed) return
        savedStateHandle[UI_STATE_KEY] = currentState.copy(isSaveButtonPressed = true)

        viewModelScope.launch {
            // font_same_as_last の比較用に、保存処理（saveToGallery）で上書きされるより先に読んでおく
            val lastSavedFontName = settingsRepository.getLastSavedFontName()
            logExportEvent(lastSavedFontName)

            if (hasEarnedRewardInSession) {
                saveToGallery()
                return@launch
            }

            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = {
                    hasEarnedRewardInSession = true
                    saveToGallery()
                },
                onAdFailedOrSkipped = {
                    saveToGallery()
                }
            )
        }
    }

    /**
     * tap_preview_export を、フォントが「迷い」か「楽しみ」かを見分けるためのパラメータ（#242）付きで送る。
     * 呼び出し元（[showRewardedAdAndSave]）のコルーチンの中から直接呼ぶsuspend関数。
     * ここで別のコルーチンを起動しないのは、[showRewardedAdAndSave] が既にコルーチンの中で
     * このメソッドを呼んでおり、二重に起動する必要が無いため。
     *
     * 「前回保存したフォント」の上書きはここではしない（実際にギャラリーへの保存が成功した
     * ときだけ [saveToGallery] で上書きする。ここで上書きすると、保存に失敗したケースや
     * タップしただけで広告表示中に離脱したケースも「保存した」ことになってしまうため）。
     */
    private suspend fun logExportEvent(lastSavedFontName: String?) {
        val params = buildFontSessionAnalyticsParams(lastSavedFontName)
        analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.TAP_PREVIEW_EXPORT, params))
    }

    private fun buildFontSessionAnalyticsParams(lastSavedFontName: String?): Map<String, Any> {
        val state = uiState.value
        val elapsedMillis = System.currentTimeMillis() - state.editStartTimeMillis
        val baseParams = mapOf<String, Any>(
            FontSessionAnalyticsParams.FONT_SWITCH_BUCKET to fontSwitchBucket(state.fontSwitchCount),
            FontSessionAnalyticsParams.EDIT_DURATION_BUCKET to editDurationBucket(elapsedMillis)
        )
        val finalFont = resolveFinalFont(state.finalFontName) ?: return baseParams

        return baseParams + mapOf(
            FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET to finalFontRankBucket(finalFont),
            FontSessionAnalyticsParams.FONT_SAME_AS_LAST to
                (finalFont.name == lastSavedFontName).toString()
        )
    }

    private fun resolveFinalFont(finalFontName: String?): FontFamilies? =
        finalFontName?.let { name -> FontFamilies.entries.find { it.name == name } }

    private fun saveToGallery() {
        viewModelScope.launch {
            val state = uiState.value
            val imagePath = state.imagePath
            if (imagePath != null) {
                val success = masterpieceRepository.saveMasterpieceToGallery(imagePath)
                if (success) {
                    // 保存が成功したうちわの最終的なフォントを、次回のfont_same_as_last比較用に保存する
                    resolveFinalFont(state.finalFontName)?.let {
                        settingsRepository.setLastSavedFontName(it.name)
                    }
                }
                val currentState = uiState.value
                savedStateHandle[UI_STATE_KEY] = currentState.copy(
                    saveSuccess = success,
                    isSaveButtonPressed = false
                )
            }
        }
    }

    fun clearSaveStatus() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            saveSuccess = null
        )
    }

    /**
     * リワード広告を表示し、広告視聴後（または失敗時）に共有用パスをセットする
     * 広告のロードに失敗している場合は即座に共有を実行（UX低下を防ぐ）
     * この画面で既に広告を視聴済みの場合は広告をスキップして共有を実行
     */
    fun showRewardedAdAndShare(activity: Activity) {
        logEvent(AnalyticsActions.TAP_PREVIEW_SHARE)

        if (hasEarnedRewardInSession) {
            setShareImagePath()
            return
        }

        adMobRepository.showRewardedAd(
            activity = activity,
            placement = AnalyticsScreens.PREVIEW_SCREEN,
            waitForLoad = true,
            onUserEarnedReward = {
                hasEarnedRewardInSession = true
            },
            onAdFailedOrSkipped = {
                // 広告が表示されなかった場合は onAdDismissed が来ないためここで共有を実行
                setShareImagePath()
            },
            onAdDismissed = {
                // 広告が閉じられた後（画面が前面に戻ってから）共有シートを起動
                setShareImagePath()
            }
        )
    }

    private fun setShareImagePath() {
        val imagePath = uiState.value.imagePath ?: return
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(shareImagePath = imagePath)
    }

    fun clearShareImage() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(shareImagePath = null)
    }

    fun getCurrentUchiwaId(): String? {
        return uiState.value.imagePath
            ?.let(::extractUchiwaIdFromImagePath)
            ?.takeIf { it.isNotBlank() }
    }
}
