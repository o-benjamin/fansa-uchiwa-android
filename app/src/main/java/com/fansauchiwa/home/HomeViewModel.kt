package com.fansauchiwa.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.TemplateRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.extractUchiwaIdFromImagePath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val masterpieceRepository: MasterpieceRepository,
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val templateRepository: TemplateRepository,
    private val settingsRepository: SettingsRepository,
    private val uuidProvider: UuidProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.HOME_SCREEN)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            analyticsRepository.logEvent(AnalyticsEvent(name = eventName, params = params))
        }
    }

    fun logNewCreateTap() {
        logEvent(AnalyticsActions.TAP_HOME_NEW_CREATE)
    }

    fun logItemEditTap() {
        logEvent(AnalyticsActions.TAP_HOME_ITEM_EDIT)
    }

    fun logTemplateTap(templateId: String) {
        logEvent(AnalyticsActions.TAP_HOME_TEMPLATE, mapOf("template_id" to templateId))
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onColorSelected(color: DecorationColor?) {
        _uiState.update { it.copy(selectedDefaultColor = color) }
    }

    fun showNameDialog(targetTemplate: Template? = null) {
        _uiState.update {
            it.copy(
                isNameDialogShown = true,
                selectedTargetTemplate = targetTemplate
            )
        }
    }

    fun dismissNameDialog() {
        _uiState.update {
            it.copy(
                isNameDialogShown = false,
                selectedTargetTemplate = null
            )
        }
    }

    fun onNameConfirmed() {
        dismissNameDialog()
    }

    fun loadAllMasterpieces() {
        viewModelScope.launch {
            val pathList = masterpieceRepository.loadAllMasterpieces()
            _uiState.update { it.copy(masterpiecePathList = pathList) }
        }
    }

    fun loadTemplates() {
        viewModelScope.launch {
            val templates = templateRepository.getTemplates()
            _uiState.update { it.copy(templates = templates) }
        }
    }

    fun observeApologyDialogState() {
        viewModelScope.launch {
            settingsRepository.getHasSeenApologyDialogStream().collect { hasSeen ->
                _uiState.update { it.copy(showApologyDialog = !hasSeen) }
            }
        }
    }

    fun fetchApologyDialogState() {
        viewModelScope.launch {
            settingsRepository.fetchHasSeenApologyDialog()
        }
    }

    fun dismissApologyDialog() {
        viewModelScope.launch {
            settingsRepository.setHasSeenApologyDialog(true)
            _uiState.update { it.copy(showApologyDialog = false) }
        }
    }

    fun extractUchiwaId(path: String): String {
        return extractUchiwaIdFromImagePath(path)
    }

    fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true, selectedPaths = emptyList()) }
    }

    fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedPaths = emptyList()) }
    }

    fun togglePathSelection(path: String) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedPaths
            val newSelected = if (path in currentSelected) {
                currentSelected - path
            } else {
                currentSelected + path
            }
            currentState.copy(selectedPaths = newSelected)
        }
    }

    fun deleteSelectedMasterpieces() {
        viewModelScope.launch {
            logEvent(AnalyticsActions.TAP_HOME_ITEM_DELETE)
            val selectedPaths = _uiState.value.selectedPaths
            selectedPaths.forEach { path ->
                val uchiwaId = extractUchiwaId(path)
                // ファイル削除
                masterpieceRepository.deleteMasterpiece(path)
                // データベースのカラム削除
                localDatabaseRepository.deleteUchiwa(uchiwaId)
            }
            exitSelectionMode()
            loadAllMasterpieces()
        }
    }

    fun duplicateSelectedMasterpieces() {
        viewModelScope.launch {
            logEvent(AnalyticsActions.TAP_HOME_ITEM_DUPLICATE)
            val selectedPaths = _uiState.value.selectedPaths
            selectedPaths.forEach { path ->
                val oldId = extractUchiwaId(path)
                val newId = uuidProvider.generate()
                // ファイル複製
                masterpieceRepository.duplicateMasterpiece(path, newId)
                    ?: return@forEach
                // データベースの複製
                val savedUchiwa = localDatabaseRepository.getUchiwa(oldId)
                    ?: return@forEach
                localDatabaseRepository.saveUchiwa(
                    Uchiwa(
                        id = newId,
                        decorations = savedUchiwa.decorations,
                        uchiwaColor = savedUchiwa.uchiwaColor,
                        backgroundColor = savedUchiwa.backgroundColor
                    )
                )
            }
            exitSelectionMode()
            loadAllMasterpieces()
        }
    }
}
