package com.fansauchiwa.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.ui.composable.SelectionCircleIcon

@Composable
fun SelectUchiwasDialog(
    availableUchiwas: List<EventTimelineUchiwaUiModel>,
    selectedUchiwaIds: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    maxSelectionCount: Int? = null,
    titleResId: Int = R.string.event_add_uchiwas_dialog_title,
    confirmButtonResId: Int = R.string.event_add_uchiwas_confirm
) {
    var currentSelection by remember(selectedUchiwaIds) { mutableStateOf(selectedUchiwaIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(titleResId)) },
        text = {
            if (availableUchiwas.isEmpty()) {
                Text(text = stringResource(R.string.event_no_available_uchiwa))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(128.dp),
                    modifier = Modifier.height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableUchiwas, key = { it.id }) { uchiwa ->
                        Box(
                            modifier = Modifier.clickable {
                                currentSelection = if (uchiwa.id in currentSelection) {
                                    if (maxSelectionCount == 1) currentSelection else currentSelection - uchiwa.id
                                } else {
                                    if (maxSelectionCount != null && currentSelection.size >= maxSelectionCount) {
                                        if (maxSelectionCount == 1) setOf(uchiwa.id) else currentSelection
                                    } else {
                                        currentSelection + uchiwa.id
                                    }
                                }
                            }
                        ) {
                            UchiwaImage(
                                imagePath = uchiwa.imagePath,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp)
                            )
                            SelectionCircleIcon(
                                isSelected = uchiwa.id in currentSelection,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentSelection) },
                enabled = currentSelection.isNotEmpty() || maxSelectionCount != 1
            ) {
                Text(text = stringResource(confirmButtonResId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
