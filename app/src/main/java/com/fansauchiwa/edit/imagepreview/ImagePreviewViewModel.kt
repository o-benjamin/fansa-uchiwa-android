package com.fansauchiwa.edit.imagepreview

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Path
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.IMAGE_URI_ARG
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.data.EraserPath
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.ImageProcessingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ImagePreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageProcessingRepository: ImageProcessingRepository,
    private val adMobRepository: AdMobRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImagePreviewUiState>(ImagePreviewUiState.Loading)
    val uiState: StateFlow<ImagePreviewUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<Unit>()
    val errorEvent: SharedFlow<Unit> = _errorEvent.asSharedFlow()

    private val _confirmEvent = MutableSharedFlow<String>()
    val confirmEvent: SharedFlow<String> = _confirmEvent.asSharedFlow()

    // 背景透過処理の結果をキャッシュ
    private var transparentUri: Uri? = null

    // 手動修正用のパスリスト
    private val _paths = mutableStateListOf<EraserPath>()
    val paths: List<EraserPath> get() = _paths

    // Redo用の履歴スタック
    private val _redoStack = mutableStateListOf<EraserPath>()

    init {
        loadImageUri()
    }

    fun showOriginal() {
        val currentState = _uiState.value
        if (currentState !is ImagePreviewUiState.Ready) return

        _uiState.value = ImagePreviewUiState.Ready.ShowingOriginal(currentState.originalUri)
    }

    fun showTransparent() {
        val currentState = _uiState.value
        if (currentState !is ImagePreviewUiState.Ready) return

        // すでに処理済みの場合はキャッシュを使用
        transparentUri?.let {
            _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                originalUri = currentState.originalUri,
                transparentUri = it
            )
            return
        }

        // 背景透過処理を実行
        viewModelScope.launch {
            _uiState.value =
                ImagePreviewUiState.Ready.ShowingTransparent.Loading(currentState.originalUri)

            val result = imageProcessingRepository.removeBackground(currentState.originalUri)

            result.fold(
                onSuccess = { uri ->
                    adMobRepository.loadInterstitialAd()
                    transparentUri = uri
                    _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                        originalUri = currentState.originalUri,
                        transparentUri = uri
                    )
                },
                onFailure = {
                    _errorEvent.emit(Unit)
                    _uiState.value =
                        ImagePreviewUiState.Ready.ShowingOriginal(currentState.originalUri)
                }
            )
        }
    }

    fun loadImageUri() {
        val imageUriString: String? = savedStateHandle.get<String>(IMAGE_URI_ARG)
        if (imageUriString != null) {
            _uiState.value = ImagePreviewUiState.Ready.ShowingOriginal(imageUriString.toUri())
        } else {
            _uiState.value =
                ImagePreviewUiState.LoadError(IllegalArgumentException("imageUri is null"))
        }
    }

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.IMAGE_PREVIEW_SCREEN)
        }
    }

    /**
     * 確認ボタンがタップされた時の処理
     * isOriginalSelected = false（透明画像選択時）の場合はインタースティシャル広告を表示してから遷移
     */
    fun onConfirmTapped(activity: Activity, imageUri: String, isOriginalSelected: Boolean) {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(AnalyticsActions.TAP_IMAGE_PREVIEW_CONFIRM)
            )
        }

        if (isOriginalSelected) {
            // オリジナル画像の場合は広告なしで即座に遷移
            viewModelScope.launch {
                _confirmEvent.emit(imageUri)
            }
        } else {
            // 透明画像の場合はインタースティシャル広告を表示してから遷移
            showInterstitialAdAndConfirm(activity, imageUri)
        }
    }

    /**
     * インタースティシャル広告を表示し、広告が閉じられた後に画面遷移する
     * 広告のロードに失敗している場合は即座に遷移（UX低下を防ぐ）
     */
    private fun showInterstitialAdAndConfirm(activity: Activity, imageUri: String) {
        adMobRepository.showInterstitialAd(
            activity = activity,
            onAdClosed = {
                // 広告が閉じられたら遷移
                viewModelScope.launch {
                    _confirmEvent.emit(imageUri)
                }
            }
        )
    }

    /**
     * 手動修正モードを開始する
     */
    fun startManualCorrection() {
        val currentState = _uiState.value
        if (currentState is ImagePreviewUiState.Ready.ShowingTransparent.Success) {
            _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection(
                originalUri = currentState.originalUri,
                transparentUri = currentState.transparentUri
            )
        }
    }

    /**
     * 手動修正を完了し、パスを画像に適用する
     * @param containerWidth 画像表示コンテナの幅（ピクセル）
     * @param containerHeight 画像表示コンテナの高さ（ピクセル）
     */
    fun completeManualCorrection(containerWidth: Int, containerHeight: Int) {
        val currentState = _uiState.value
        if (currentState !is ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection) return

        // パスがない場合は直接 Success に戻る
        if (_paths.isEmpty()) {
            _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                originalUri = currentState.originalUri,
                transparentUri = currentState.transparentUri
            )
            return
        }

        viewModelScope.launch {
            // ローディング状態に遷移
            _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Loading(
                originalUri = currentState.originalUri
            )

            val result = imageProcessingRepository.applyManualCorrection(
                imageUri = currentState.transparentUri,
                paths = _paths.toList(),
                previewWidth = containerWidth,
                previewHeight = containerHeight
            )

            result.fold(
                onSuccess = { uri ->
                    // キャッシュを更新
                    transparentUri = uri
                    // パスをクリア
                    _paths.clear()
                    _redoStack.clear()
                    // Success 状態に遷移
                    _uiState.value = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                        originalUri = currentState.originalUri,
                        transparentUri = uri
                    )
                },
                onFailure = {
                    _errorEvent.emit(Unit)
                    // 元の ManualCorrection 状態に戻す
                    _uiState.value = currentState
                }
            )
        }
    }

    /**
     * 新しいパスを追加する
     * @param path ユーザーが描画したパス（画像ローカル座標系）
     * @param scale 描画時のズーム倍率
     */
    fun addPath(path: Path, scale: Float) {
        val strokeWidth = BASE_STROKE_WIDTH / scale
        _paths.add(EraserPath(path, strokeWidth))
        _redoStack.clear()
    }

    companion object {
        /** 消しゴムの基準線幅（ズーム倍率1.0時の太さ） */
        private const val BASE_STROKE_WIDTH = 40f
    }

    /**
     * 修正を元に戻す
     */
    fun undoCorrection() {
        if (_paths.isNotEmpty()) {
            val lastPath = _paths.removeAt(_paths.lastIndex)
            _redoStack.add(lastPath)
        }
    }

    /**
     * 修正をやり直す
     */
    fun redoCorrection() {
        if (_redoStack.isNotEmpty()) {
            val path = _redoStack.removeAt(_redoStack.lastIndex)
            _paths.add(path)
        }
    }
}
