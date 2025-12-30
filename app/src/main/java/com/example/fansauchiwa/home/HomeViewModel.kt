package com.example.fansauchiwa.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fansauchiwa.data.LocalDatabaseRepository
import com.example.fansauchiwa.data.MasterpieceRepository
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
    private val localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
            val selectedPaths = _uiState.value.selectedDeletingPaths
            selectedPaths.forEach { path ->
                val uchiwaId = extractUchiwaId(path)
                // ファイル削除
                masterpieceRepository.deleteMasterpiece(path)
                // データベースのカラム削除
                localDatabaseRepository.deleteDecorations(uchiwaId)
            }
            exitDeletingMode()
            loadAllMasterpieces()
        }
    }
}
