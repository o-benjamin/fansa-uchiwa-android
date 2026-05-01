package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration

internal data class HistorySnapshot(
    val decorations: List<Decoration>,
    val uchiwaColor: Color,
    val backgroundColor: Color
) {
    fun restore(currentState: EditUiState): EditUiState = currentState.copy(
        decorations = decorations,
        uchiwaColor = uchiwaColor,
        backgroundColor = backgroundColor,
        selectedDecorationId = null,
        editingTextId = null
    )

    companion object {
        fun from(uiState: EditUiState): HistorySnapshot = HistorySnapshot(
            decorations = uiState.decorations,
            uchiwaColor = uiState.uchiwaColor,
            backgroundColor = uiState.backgroundColor
        )
    }
}
