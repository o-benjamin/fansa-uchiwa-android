package com.example.fansauchiwa.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fansauchiwa.data.MasterpieceRepository
import com.morayl.footprint.footprint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val masterpieceRepository: MasterpieceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAllMasterpieces()
    }

    private fun loadAllMasterpieces() {
        viewModelScope.launch {
            val pathList = masterpieceRepository.loadAllMasterpieces()
            _uiState.update { it.copy(masterpiecePathList = pathList) }
        }
    }

    fun extractUchiwaId(path: String): String {
        // ファイルパスからuchiwaId（ファイル名から.pngを除いた部分）を抽出
        return path.substringAfterLast("/").substringBeforeLast(".png")
    }
}

