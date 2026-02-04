package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration

internal data class HistorySnapshot(
    val decorations: List<Decoration>,
    val uchiwaColor: Color,
    val backgroundColor: Color
)
