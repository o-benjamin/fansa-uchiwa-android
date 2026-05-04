package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistorySnapshotTest {

    @Test
    fun from_uiState_returnsSnapshotWithEditableStateOnly() {
        val decorations = listOf(
            Decoration.Image(
                id = "decoration-id",
                imageId = "image-id"
            )
        )
        val uiState = EditUiState(
            decorations = decorations,
            selectedDecorationId = "selected-id",
            editingTextId = "editing-id",
            uchiwaColor = Color(0xFF123456),
            backgroundColor = Color(0xFF654321)
        )

        val snapshot = HistorySnapshot.from(uiState)

        assertEquals(decorations, snapshot.decorations)
        assertEquals(Color(0xFF123456), snapshot.uchiwaColor)
        assertEquals(Color(0xFF654321), snapshot.backgroundColor)
    }

    @Test
    fun restore_selectedAndEditingStateExist_restoresSnapshotAndClearsTransientState() {
        val currentState = EditUiState(
            decorations = listOf(Decoration.Image(id = "current-id", imageId = "current-image-id")),
            selectedDecorationId = "selected-id",
            editingTextId = "editing-id",
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )
        val snapshot = HistorySnapshot(
            decorations = listOf(Decoration.Image(id = "history-id", imageId = "history-image-id")),
            uchiwaColor = Color(0xFF111111),
            backgroundColor = Color(0xFF222222)
        )

        val restoredState = snapshot.restore(currentState)

        assertEquals(snapshot.decorations, restoredState.decorations)
        assertEquals(snapshot.uchiwaColor, restoredState.uchiwaColor)
        assertEquals(snapshot.backgroundColor, restoredState.backgroundColor)
        assertNull(restoredState.selectedDecorationId)
        assertNull(restoredState.editingTextId)
    }
}
