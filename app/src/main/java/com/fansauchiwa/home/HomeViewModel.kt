package com.fansauchiwa.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.repository.AnalyticsRepository
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
    private val analyticsRepository: AnalyticsRepository
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

    fun loadAllMasterpieces() {
        viewModelScope.launch {
            val pathList = masterpieceRepository.loadAllMasterpieces()
            _uiState.update { it.copy(masterpiecePathList = pathList) }
        }
    }

    fun extractUchiwaId(path: String): String {
        // ファイルパスからuchiwaId（ファイル名から.pngを除いた部分）を抽出
        return path.substringAfterLast("/").substringBeforeLast(".png")
    }

    fun enterDeletingMode() {
        _uiState.update { it.copy(isDeletingMode = true, selectedDeletingPaths = emptyList()) }
    }

    fun exitDeletingMode() {
        _uiState.update { it.copy(isDeletingMode = false, selectedDeletingPaths = emptyList()) }
    }

    fun togglePathSelection(path: String) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedDeletingPaths
            val newSelected = if (path in currentSelected) {
                currentSelected - path
            } else {
                currentSelected + path
            }
            currentState.copy(selectedDeletingPaths = newSelected)
        }
    }

    fun deleteSelectedMasterpieces() {
        viewModelScope.launch {
            logEvent(AnalyticsActions.TAP_HOME_ITEM_DELETE)
            val selectedPaths = _uiState.value.selectedDeletingPaths
            selectedPaths.forEach { path ->
                val uchiwaId = extractUchiwaId(path)
                // ファイル削除
                masterpieceRepository.deleteMasterpiece(path)
                // データベースのカラム削除
                localDatabaseRepository.deleteUchiwa(uchiwaId)
            }
            exitDeletingMode()
            loadAllMasterpieces()
        }
    }
}
