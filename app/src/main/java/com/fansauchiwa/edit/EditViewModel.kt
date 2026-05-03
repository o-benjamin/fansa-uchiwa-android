package com.fansauchiwa.edit

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.R
import com.fansauchiwa.TEMPLATE_ID_ARG
import com.fansauchiwa.UCHIWA_ID_ARG
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.LocalImageRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.analytics.AnalyticsUndoRedoActions
import com.fansauchiwa.data.analytics.BackGroundColorParams
import com.fansauchiwa.data.analytics.EditStickerTargetParams
import com.fansauchiwa.data.analytics.EditTextTargetParams
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val UI_STATE_KEY = "ui_state"
private const val UCHIWA_ID_KEY = "uchiwa_id"
private const val MAX_HISTORY_SIZE = 50

@HiltViewModel
class EditViewModel @Inject constructor(
    private val localImageRepository: LocalImageRepository,
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val masterpieceRepository: MasterpieceRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository,
    private val templateRepository: TemplateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState: StateFlow<EditUiState> = savedStateHandle.getStateFlow(UI_STATE_KEY, EditUiState())

    private val undoStack: ArrayDeque<HistorySnapshot> = ArrayDeque()
    private val redoStack: ArrayDeque<HistorySnapshot> = ArrayDeque()
    private var hasEdited = false
    private var hasSeenCompletionTooltip = false
    private var hasLoadedCompletionTooltip = false

    init {
        observeCompletionTooltip()
        fetchCompletionTooltip()
        loadExistingDecorations()
        loadAllImages()
    }

    private fun observeCompletionTooltip() {
        settingsRepository.getHasSeenEditCompletionTooltipStream()
            .onEach { hasSeen ->
                hasSeenCompletionTooltip = hasSeen
                hasLoadedCompletionTooltip = true
                if (hasSeen && uiState.value.showCompletionTooltip) {
                    val currentState = uiState.value
                    savedStateHandle[UI_STATE_KEY] = currentState.copy(showCompletionTooltip = false)
                }
                showCompletionTooltipIfNeeded()
            }
            .launchIn(viewModelScope)
    }

    private fun fetchCompletionTooltip() {
        viewModelScope.launch {
            settingsRepository.fetchHasSeenEditCompletionTooltip()
        }
    }

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.EDIT_SCREEN)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            analyticsRepository.logEvent(AnalyticsEvent(name = eventName, params = params))
        }
    }

    private fun loadExistingDecorations() {
        viewModelScope.launch {
            val uchiwaId: String? = savedStateHandle[UCHIWA_ID_ARG]
            if (uchiwaId != null) {
                savedStateHandle[UCHIWA_ID_KEY] = uchiwaId
                val uchiwa = localDatabaseRepository.getUchiwa(uchiwaId)
                if (uchiwa != null) {
                    restoreExistingUchiwa(uchiwaId, uchiwa)
                } else {
                    applyNewUchiwaState(uchiwaId)
                }
            } else {
                val newUchiwaId = UUID.randomUUID().toString()
                savedStateHandle[UCHIWA_ID_KEY] = newUchiwaId
                applyNewUchiwaState(newUchiwaId)
            }
        }
    }

    private suspend fun restoreExistingUchiwa(uchiwaId: String, savedUchiwa: Uchiwa) {
        savedStateHandle[UI_STATE_KEY] = uiState.value.copy(
            uchiwaId = uchiwaId,
            decorations = savedUchiwa.decorations,
            uchiwaColor = savedUchiwa.uchiwaColor,
            backgroundColor = savedUchiwa.backgroundColor
        )

        val imageDecorations =
            savedUchiwa.decorations.filterIsInstance<Decoration.Image>()
        val validImages = mutableListOf<ImageReference>()
        val missingImageIds = mutableListOf<String>()

        for (decoration in imageDecorations) {
            val imageData = localImageRepository.loadImage(decoration.imageId)
            if (imageData != null) {
                validImages.add(imageData)
            } else {
                missingImageIds.add(decoration.imageId)
            }
        }

        val finalDecorations = if (missingImageIds.isNotEmpty()) {
            savedUchiwa.decorations.filterNot {
                it is Decoration.Image && missingImageIds.contains(it.imageId)
            }
        } else {
            savedUchiwa.decorations
        }

        if (missingImageIds.isNotEmpty()) {
            localDatabaseRepository.saveUchiwa(
                Uchiwa(
                    id = uchiwaId,
                    decorations = finalDecorations,
                    uchiwaColor = savedUchiwa.uchiwaColor,
                    backgroundColor = savedUchiwa.backgroundColor
                )
            )
        }

        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            uchiwaId = uchiwaId,
            decorations = finalDecorations,
            uchiwaColor = savedUchiwa.uchiwaColor,
            backgroundColor = savedUchiwa.backgroundColor,
            images = currentState.images.filterNot { existing ->
                validImages.any { it.id == existing.id }
            } + validImages
        )
        showCompletionTooltipIfNeeded()
    }

    private suspend fun applyNewUchiwaState(uchiwaId: String) {
        val templateId: String? = savedStateHandle[TEMPLATE_ID_ARG]
        val currentState = uiState.value
        if (templateId != null) {
            val template = templateRepository.getTemplateById(templateId)
            if (template != null) {
                val savedUchiwa = template.savedUchiwa
                savedStateHandle[UI_STATE_KEY] = currentState.copy(
                    uchiwaId = uchiwaId,
                    decorations = savedUchiwa.decorations,
                    uchiwaColor = savedUchiwa.uchiwaColor,
                    backgroundColor = savedUchiwa.backgroundColor
                )
                showCompletionTooltipIfNeeded()
                return
            }
        }
        savedStateHandle[UI_STATE_KEY] = currentState.copy(uchiwaId = uchiwaId)
        showCompletionTooltipIfNeeded()
    }

    fun updateDecoration(id: String, transform: (Decoration) -> Decoration) {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = currentState.decorations.map { decoration ->
                if (decoration.id == id) transform(decoration) else decoration
            }
        )
        markEdited()
    }

    fun addDecoration(decoration: Decoration) {
        saveSnapshot()
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = currentState.decorations + decoration
        )
        markEdited()

        when (decoration) {
            is Decoration.Text -> {
                logEvent(
                    AnalyticsActions.SELECT_EDIT_TEXT,
                    mapOf("font_family" to decoration.font.name)
                )
            }

            is Decoration.Sticker -> {
                logEvent(
                    AnalyticsActions.SELECT_EDIT_STICKER,
                    mapOf("label" to decoration.label)
                )
            }

            is Decoration.Image -> {
                logEvent(AnalyticsActions.SELECT_EDIT_IMAGE)
                loadImage(decoration.imageId)
            }
        }
    }

    fun duplicateDecoration(id: String) {
        val original = uiState.value.decorations.find { it.id == id } ?: return
        saveSnapshot()
        val updatedState = uiState.value
        val newId = UUID.randomUUID().toString()
        val duplicatedOffset = Offset(50f, 50f)
        val duplicate = when (original) {
            is Decoration.Text -> original.copy(
                id = newId,
                offset = original.offset + duplicatedOffset
            )

            is Decoration.Sticker -> original.copy(
                id = newId,
                offset = original.offset + duplicatedOffset
            )

            is Decoration.Image -> original.copy(
                id = newId,
                offset = original.offset + duplicatedOffset
            )
        }
        savedStateHandle[UI_STATE_KEY] = updatedState.copy(
            decorations = updatedState.decorations + duplicate,
            selectedDecorationId = newId
        )
        markEdited()
        val decorationType = when (original) {
            is Decoration.Text -> "text"
            is Decoration.Sticker -> "sticker"
            is Decoration.Image -> "image"
        }
        logEvent(
            AnalyticsActions.TAP_EDIT_DUPLICATE,
            mapOf("type" to decorationType)
        )
    }

    fun deleteDecoration(id: String) {
        saveSnapshot()
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = currentState.decorations.filter { it.id != id }
        )
        markEdited()
    }

    /**
     * UIリストのインデックスからデコレーションの順序を移動する。
     * UIでは decorations.reversed() を表示しているため、
     * fromIndex/toIndex を実際のリストインデックスに変換して更新する。
     *
     * @param fromIndex UI上の移動元インデックス（reversed後）
     * @param toIndex UI上の移動先インデックス（reversed後）
     */
    fun moveDecoration(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val currentState = uiState.value
        val decorations = currentState.decorations
        if (decorations.isEmpty()) return

        // UIでは reversed() で表示しているので、実際のインデックスに変換
        val actualFromIndex = decorations.lastIndex - fromIndex
        val actualToIndex = decorations.lastIndex - toIndex

        val mutableList = decorations.toMutableList()
        val item = mutableList.removeAt(actualFromIndex)
        mutableList.add(actualToIndex, item)

        savedStateHandle[UI_STATE_KEY] = currentState.copy(decorations = mutableList)
        markEdited()
    }

    fun selectDecoration(id: String) {
        if (!canFinishEditing()) return
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            selectedDecorationId = id
        )
    }

    fun unSelectDecoration() {
        if (!canFinishEditing()) return
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            selectedDecorationId = null
        )
    }

    fun snackbarMessageShown() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            userMessage = null
        )
    }

    fun updateDecorationGraphic(id: String, offset: Offset, scale: Float, rotation: Float) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Sticker -> decoration.copy(
                    offset = decoration.offset + offset,
                    scale = decoration.scale + scale,
                    rotation = decoration.rotation + rotation
                )

                is Decoration.Text -> decoration.copy(
                    offset = decoration.offset + offset,
                    scale = decoration.scale + scale,
                    rotation = decoration.rotation + rotation
                )

                is Decoration.Image -> decoration.copy(
                    offset = decoration.offset + offset,
                    scale = decoration.scale + scale,
                    rotation = decoration.rotation + rotation
                )
            }
        }
    }

    fun startEditingText(id: String) {
        if (!canFinishEditing()) return
        saveSnapshot()
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            editingTextId = id
        )
    }

    fun finishEditingText() {
        if (!canFinishEditing()) return
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            editingTextId = null
        )
    }

    /**
     * テキスト編集中にキーボードを閉じる操作が空文字によりブロックされたことをUIから通知する。
     * スナックバーを表示するためのuserMessageを設定する。
     */
    fun notifyDismissBlocked() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            userMessage = R.string.snackbar_input_too_short
        )
    }

    fun updateText(id: String, newText: String) {
        updateDecoration(id) { decoration ->
            (decoration as? Decoration.Text)?.copy(text = newText) ?: decoration
        }
    }

    fun updateColor(id: String, newColor: Color) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Sticker -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_STICKER_COLOR,
                        mapOf("target" to EditStickerTargetParams.STICKER)
                    )
                    decoration.copy(color = newColor)
                }

                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_COLOR,
                        mapOf("target" to EditTextTargetParams.TEXT)
                    )
                    decoration.copy(color = newColor)
                }

                is Decoration.Image -> decoration.copy(color = newColor)
            }
        }
    }

    fun updateStrokeColor(id: String, newColor: Color) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_COLOR,
                        mapOf("target" to EditTextTargetParams.PARAM_STROKE_1)
                    )
                    decoration.copy(strokeColor = newColor)
                }

                is Decoration.Sticker -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_STICKER_COLOR,
                        mapOf("target" to EditStickerTargetParams.PARAM_STROKE_1)
                    )
                    decoration.copy(strokeColor = newColor)
                }

                is Decoration.Image -> decoration.copy(strokeColor = newColor)
            }
        }
    }

    fun updateFont(id: String, newFont: FontFamilies) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_FONT,
                        mapOf("font_family" to newFont.name)
                    )
                    decoration.copy(font = newFont)
                }

                else -> decoration
            }
        }
    }

    fun updateWidth(id: String, newWidth: Int) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_WEIGHT,
                        mapOf("target" to EditTextTargetParams.TEXT)
                    )
                    decoration.copy(width = newWidth)
                }

                else -> decoration
            }
        }
    }

    fun updateStrokeWidth(id: String, newWidth: Float) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_WEIGHT,
                        mapOf("target" to EditTextTargetParams.PARAM_STROKE_1)
                    )
                    decoration.copy(strokeWidth = newWidth)
                }

                is Decoration.Sticker -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_STICKER_WEIGHT,
                        mapOf("target" to EditStickerTargetParams.PARAM_STROKE_1)
                    )
                    decoration.copy(strokeWidth = newWidth)
                }

                is Decoration.Image -> decoration.copy(strokeWidth = newWidth)
            }
        }
    }

    fun updateSecondBorderColor(id: String, newColor: Color) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_COLOR,
                        mapOf("target" to EditTextTargetParams.PARAM_STROKE_2)
                    )
                    decoration.copy(secondBorderColor = newColor)
                }

                is Decoration.Sticker -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_STICKER_COLOR,
                        mapOf("target" to EditStickerTargetParams.PARAM_STROKE_2)
                    )
                    decoration.copy(secondStrokeColor = newColor)
                }

                else -> decoration
            }
        }
    }

    fun updateSecondBorderWidth(id: String, newWidth: Float) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            when (decoration) {
                is Decoration.Text -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_TEXT_WEIGHT,
                        mapOf("target" to EditTextTargetParams.PARAM_STROKE_2)
                    )
                    decoration.copy(secondBorderWidth = newWidth)
                }

                is Decoration.Sticker -> {
                    logEvent(
                        AnalyticsActions.SELECT_EDIT_STICKER_WEIGHT,
                        mapOf("target" to EditStickerTargetParams.PARAM_STROKE_2)
                    )
                    decoration.copy(secondStrokeWidth = newWidth)
                }

                else -> decoration
            }
        }
    }

    fun updatePuffyEnabled(id: String, isPuffyEnabled: Boolean) {
        saveSnapshot()
        updateDecoration(id) { decoration ->
            if (decoration is Decoration.Text) {
                decoration.copy(isPuffyEnabled = isPuffyEnabled)
            } else decoration
        }
    }

    fun updateUchiwaColor(color: Color) {
        saveSnapshot()
        logEvent(
            AnalyticsActions.SELECT_EDIT_BACKGROUND_COLOR,
            mapOf("target" to BackGroundColorParams.PARAM_UCHIWA)
        )
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(uchiwaColor = color)
        markEdited()
    }

    fun updateBackgroundColor(color: Color) {
        saveSnapshot()
        logEvent(
            AnalyticsActions.SELECT_EDIT_BACKGROUND_COLOR,
            mapOf("target" to BackGroundColorParams.PARAM_BACKGROUND)
        )
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(backgroundColor = color)
        markEdited()
    }

    fun saveImage(uri: Uri, id: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            localImageRepository.saveImage(uri, id)
            loadAllImages()
            onSaved()
        }
    }

    private fun loadImage(imageId: String) {
        viewModelScope.launch {
            val imageData = localImageRepository.loadImage(imageId)
            if (imageData != null) {
                val currentState = uiState.value
                val updatedImages = currentState.images.filter { it.id != imageId } + imageData
                savedStateHandle[UI_STATE_KEY] = currentState.copy(images = updatedImages)
            }
        }
    }

    fun loadAllImages() {
        viewModelScope.launch {
            val images = localImageRepository.getAllImages()
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(allImages = images)
        }
    }

    fun handleImageResult(resultUri: String) {
        viewModelScope.launch {
            val imageId = UUID.randomUUID().toString()
            val image = Decoration.Image(
                id = UUID.randomUUID().toString(),
                imageId = imageId
            )
            saveImage(resultUri.toUri(), imageId) {
                addDecoration(image)
            }
        }
    }

    private fun saveSnapshot() {
        val currentState = uiState.value
        val snapshot = HistorySnapshot(
            decorations = currentState.decorations,
            uchiwaColor = currentState.uchiwaColor,
            backgroundColor = currentState.backgroundColor
        )
        undoStack.addLast(snapshot)
        if (undoStack.size > MAX_HISTORY_SIZE) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        updateHistoryAvailability()
    }

    private fun updateHistoryAvailability() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        logEvent(
            AnalyticsActions.TAP_EDIT_UNDO_REDO,
            mapOf("actions" to AnalyticsUndoRedoActions.ACTION_UNDO)
        )
        val currentState = uiState.value
        val currentSnapshot = HistorySnapshot(
            decorations = currentState.decorations,
            uchiwaColor = currentState.uchiwaColor,
            backgroundColor = currentState.backgroundColor
        )
        redoStack.addLast(currentSnapshot)

        val previousSnapshot = undoStack.removeLast()
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = previousSnapshot.decorations,
            uchiwaColor = previousSnapshot.uchiwaColor,
            backgroundColor = previousSnapshot.backgroundColor,
            selectedDecorationId = null,
            editingTextId = null
        )
        updateHistoryAvailability()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        logEvent(
            AnalyticsActions.TAP_EDIT_UNDO_REDO,
            mapOf("actions" to AnalyticsUndoRedoActions.ACTION_REDO)
        )
        val currentState = uiState.value
        val currentSnapshot = HistorySnapshot(
            decorations = currentState.decorations,
            uchiwaColor = currentState.uchiwaColor,
            backgroundColor = currentState.backgroundColor
        )
        undoStack.addLast(currentSnapshot)

        val nextSnapshot = redoStack.removeLast()
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            decorations = nextSnapshot.decorations,
            uchiwaColor = nextSnapshot.uchiwaColor,
            backgroundColor = nextSnapshot.backgroundColor,
            selectedDecorationId = null,
            editingTextId = null
        )
        updateHistoryAvailability()
    }

    fun startImageDeletionMode() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(isDeletingImage = true)
    }

    fun toggleImageSelection(imageId: String) {
        val currentState = uiState.value
        val currentSelected = currentState.selectedDeletingImages
        val newSelected = if (currentSelected.contains(imageId)) {
            currentSelected - imageId
        } else {
            currentSelected + imageId
        }
        savedStateHandle[UI_STATE_KEY] = currentState.copy(selectedDeletingImages = newSelected)
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedDeletingImages
            if (selectedIds.isNotEmpty()) {
                val isUsedInOther = selectedIds.any { imageId ->
                    localDatabaseRepository.isImageUsedInAnyUchiwa(imageId)
                }
                if (isUsedInOther) {
                    val currentState = uiState.value
                    savedStateHandle[UI_STATE_KEY] = currentState.copy(
                        showImageDeleteWarningDialog = true
                    )
                } else {
                    executeImageDeletion(selectedIds)
                }
            }
        }
    }

    fun proceedImageDeletion() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedDeletingImages
            executeImageDeletion(selectedIds)
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                showImageDeleteWarningDialog = false
            )
        }
    }

    fun dismissImageDeleteWarningDialog() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            showImageDeleteWarningDialog = false
        )
    }

    private fun executeImageDeletion(imageIds: List<String>) {
        localImageRepository.deleteImages(imageIds)
        loadAllImages()
        loadExistingDecorations()
        cancelImageDeletionMode()
    }

    fun cancelImageDeletionMode() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            isDeletingImage = false,
            selectedDeletingImages = emptyList()
        )
    }

    /**
     * テキスト編集中（editingTextId != null）のとき、テキストが空文字であれば
     * スナックバーで警告を表示し、編集完了やselect状態の変更を禁止する。
     * 編集中でなければ常に true を返す。
     */
    private fun canFinishEditing(): Boolean {
        val currentState = uiState.value
        val editingTextId = currentState.editingTextId ?: return true
        val editingText =
            (currentState.decorations.find { it.id == editingTextId } as? Decoration.Text)
        if (editingText != null && editingText.text.isEmpty()) {
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                userMessage = R.string.snackbar_input_too_short
            )
            return false
        }
        return true
    }

    fun exportTemplateCode(onDecorationSave: (String) -> Unit) {
        viewModelScope.launch {
            val state = uiState.value
            val savedUchiwa = SavedUchiwa(
                decorations = state.decorations,
                uchiwaColor = state.uchiwaColor,
                backgroundColor = state.backgroundColor
            )
            val code = TemplateExportUtil.exportToKotlinCode(savedUchiwa)
            Log.d("TemplateExport", code)

            localDatabaseRepository.saveUchiwa(
                Uchiwa(
                    id = state.uchiwaId,
                    decorations = state.decorations,
                    uchiwaColor = state.uchiwaColor,
                    backgroundColor = state.backgroundColor
                )
            )
            onDecorationSave(state.uchiwaId)
        }
    }

    fun saveUchiwa(onDecorationSave: (String) -> Unit) {
        viewModelScope.launch {
            val state = uiState.value
            localDatabaseRepository.saveUchiwa(
                Uchiwa(
                    id = state.uchiwaId,
                    decorations = state.decorations,
                    uchiwaColor = state.uchiwaColor,
                    backgroundColor = state.backgroundColor
                )
            )
            onDecorationSave(state.uchiwaId)
        }
    }

    fun saveUchiwaBitmap(bitmap: Bitmap, uchiwaId: String) {
        viewModelScope.launch {
            val savedPath = masterpieceRepository.saveMasterpieceBitmap(bitmap, uchiwaId)
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                savedPath = savedPath
            )
        }
    }

    fun resetEditUiState() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            selectedDecorationId = null,
            editingTextId = null,
            userMessage = null,
            isDeletingImage = false,
            selectedDeletingImages = emptyList(),
            savedPath = null
        )
    }

    fun resetIsUchiwaSaved() {
        val currentState = uiState.value
        savedStateHandle[UI_STATE_KEY] = currentState.copy(
            savedPath = null
        )
    }

    fun onTooltipDismissed() {
        viewModelScope.launch {
            hasSeenCompletionTooltip = true
            settingsRepository.setHasSeenEditCompletionTooltip(true)
            settingsRepository.fetchHasSeenEditCompletionTooltip()
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(showCompletionTooltip = false)
        }
    }

    fun resetDataFromTemplate(template: Template) {
        viewModelScope.launch {
            val currentState = uiState.value
            localDatabaseRepository.saveUchiwa(
                Uchiwa(
                    id = currentState.uchiwaId,
                    decorations = template.savedUchiwa.decorations,
                    uchiwaColor = template.savedUchiwa.uchiwaColor,
                    backgroundColor = template.savedUchiwa.backgroundColor
                )
            )
            savedStateHandle[UI_STATE_KEY] = currentState.copy(
                decorations = template.savedUchiwa.decorations,
                uchiwaColor = template.savedUchiwa.uchiwaColor,
                backgroundColor = template.savedUchiwa.backgroundColor
            )
            markEdited()
        }
    }

    private fun markEdited() {
        hasEdited = true
        showCompletionTooltipIfNeeded()
    }

    private fun showCompletionTooltipIfNeeded() {
        if (shouldSuppressCompletionTooltip()) {
            return
        }
        if (hasEdited) {
            val currentState = uiState.value
            savedStateHandle[UI_STATE_KEY] = currentState.copy(showCompletionTooltip = true)
        }
    }

    private fun shouldSuppressCompletionTooltip(): Boolean =
        !hasLoadedCompletionTooltip || hasSeenCompletionTooltip || uiState.value.showCompletionTooltip
}
