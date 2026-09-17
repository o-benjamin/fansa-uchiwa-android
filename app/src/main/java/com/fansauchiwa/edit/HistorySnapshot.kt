package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration

internal data class HistorySnapshot(
    val decorations: List<Decoration>,
    val uchiwaColor: Color,
    val backgroundColor: Color,
    val overallBorderColor: Color,
    val overallBorderWidth: Float,
    val isOverallBorderPuffyEnabled: Boolean
) {
    fun restore(currentState: EditUiState): EditUiState = currentState.copy(
        decorations = decorations,
        uchiwaColor = uchiwaColor,
        backgroundColor = backgroundColor,
        overallBorderColor = overallBorderColor,
        overallBorderWidth = overallBorderWidth,
        isOverallBorderPuffyEnabled = isOverallBorderPuffyEnabled,
        isDragging = false,
        selectedDecorationId = null,
        editingTextId = null
    )

    companion object {
        fun from(uiState: EditUiState): HistorySnapshot = HistorySnapshot(
            decorations = uiState.decorations,
            uchiwaColor = uiState.uchiwaColor,
            backgroundColor = uiState.backgroundColor,
            overallBorderColor = uiState.overallBorderColor,
            overallBorderWidth = uiState.overallBorderWidth,
            isOverallBorderPuffyEnabled = uiState.isOverallBorderPuffyEnabled
        )
    }
}
