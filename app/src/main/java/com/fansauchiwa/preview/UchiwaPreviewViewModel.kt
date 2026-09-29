package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.analytics.FontSessionTracker
import com.fansauchiwa.analytics.PuffyStateAnalytics
import com.fansauchiwa.analytics.ShareAnalyticsParams
import com.fansauchiwa.data.extractUchiwaIdFromImagePath
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.InAppReviewRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
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
    private val inAppReviewRepository: InAppReviewRepository,
    private val fontSessionTracker: FontSessionTracker,
    private val puffyStateAnalytics: PuffyStateAnalytics,
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
        // Navigation引数からimagePathを取得してUI Stateに設定
        val encodedImagePath = savedStateHandle.get<String>(IMAGE_PATH_ARG)
        if (encodedImagePath != null) {
            val decodedImagePath = URLDecoder.decode(encodedImagePath, "UTF-8")
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(imagePath = decodedImagePath)
        }
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
        // 連打などで多重に実行しない。isSaveButtonPressedは一連の保存処理が終わるまでtrueのまま
        if (currentState.isSaveButtonPressed) return
        savedStateHandle[UI_STATE_KEY] = currentState.copy(isSaveButtonPressed = true)

        viewModelScope.launch {
            logExportEvent()

            if (hasEarnedRewardInSession) {
                saveToGallery()
                return@launch
            }

            // このタップで既に saveToGallery を呼んだかどうか。
            // onAdFailedOrSkipped と onAdDismissed は同時に呼ばれることがある（AdMobRepository参照）ため、
            // 「まだ呼んでいなければ」で判定しないと、保存処理の完了を待たずに連打防止フラグを戻してしまう
            var saveTriggered = false
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = {
                    hasEarnedRewardInSession = true
                    saveTriggered = true
                    saveToGallery()
                },
                onAdFailedOrSkipped = {
                    saveTriggered = true
                    saveToGallery()
                },
                onAdDismissed = {
                    // 報酬を獲得せずに広告を閉じた場合は、onUserEarnedReward/onAdFailedOrSkippedの
                    // どちらも呼ばれず saveToGallery が実行されない。連打防止用のフラグを戻さないと
                    // 再タップできなくなってしまうため、ここで戻す
                    // （このタップで既に保存処理を始めていれば、isSaveButtonPressed は
                    // saveToGallery 側で戻すのでここでは戻さない）
                    if (!saveTriggered) {
                        val state = uiState.value
                        savedStateHandle[UI_STATE_KEY] = state.copy(isSaveButtonPressed = false)
                    }
                }
            )
        }
    }

    /**
     * tap_preview_export を、フォントが「迷い」か「楽しみ」かを見分けるためのパラメータ（#242）と
     * ぷくぷくの状態（#268）付きで送る。
     * 呼び出し元（[showRewardedAdAndSave]）のコルーチンの中から直接呼ぶsuspend関数。
     */
    private suspend fun logExportEvent() {
        val uchiwaId = getCurrentUchiwaId()
        val params = fontSessionTracker.exportParams(currentUchiwaId = uchiwaId) +
            puffyStateAnalytics.exportParams(uchiwaId)
        analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.TAP_PREVIEW_EXPORT, params))
    }

    private fun saveToGallery() {
        viewModelScope.launch {
            val imagePath = uiState.value.imagePath
            if (imagePath != null) {
                val success = masterpieceRepository.saveMasterpieceToGallery(imagePath)
                if (success) {
                    // Screen が saveSuccess=true を受けてレビュー依頼の条件を判定するため、
                    // 今回の保存を回数に含めてから saveSuccess を流す（#243）
                    inAppReviewRepository.recordSaveSuccess()
                }
                val currentState = uiState.value
                savedStateHandle[UI_STATE_KEY] = currentState.copy(
                    saveSuccess = success,
                    isSaveButtonPressed = false
                )
            }
        }
    }

    /**
     * 保存成功の直後に、条件を満たしていればアプリ内レビュー依頼を出す（#243）
     * 広告の画面が閉じてこの画面が前面に戻ってから呼ぶこと
     */
    fun requestInAppReviewIfEligible(activity: Activity) {
        viewModelScope.launch {
            inAppReviewRepository.requestReviewIfEligible(activity)
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
     *
     * @param entryPoint どの導線から共有したか（[ShareAnalyticsParams] の ENTRY_POINT_*）
     */
    fun showRewardedAdAndShare(activity: Activity, entryPoint: String) {
        logEvent(
            AnalyticsActions.TAP_PREVIEW_SHARE,
            mapOf(ShareAnalyticsParams.PARAM_ENTRY_POINT to entryPoint)
        )

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
