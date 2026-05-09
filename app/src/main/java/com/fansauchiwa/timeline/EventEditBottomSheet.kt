package com.fansauchiwa.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.ui.composable.SelectionCircleIcon
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val EventDateTextFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditBottomSheet(
    event: EventTimelineEventUiModel?,
    availableUchiwas: List<EventTimelineUchiwaUiModel>,
    defaultSelectedUchiwaId: String?,
    onDismiss: () -> Unit,
    onSave: (String?, String, LocalDate, Boolean, Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable(event?.id) { mutableStateOf(event?.name.orEmpty()) }
    var selectedDateEpochDay by rememberSaveable(event?.id) {
        mutableStateOf((event?.eventDate ?: LocalDate.now()).toEpochDay())
    }
    var remindEnabled by rememberSaveable(event?.id) {
        mutableStateOf(event?.remindEnabled ?: true)
    }
    var selectedUchiwaIds by remember(event?.id) {
        mutableStateOf(
            event?.linkedUchiwas?.map { it.id }?.toSet()
                ?: defaultSelectedUchiwaId?.let(::setOf)
                ?: emptySet()
        )
    }
    val selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
    var nameError by remember { mutableStateOf<Int?>(null) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isUchiwaDialogVisible by remember { mutableStateOf(false) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(
                    if (event == null) {
                        R.string.event_create_title
                    } else {
                        R.string.event_edit_title
                    }
                ),
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text(stringResource(R.string.event_name)) },
                isError = nameError != null,
                supportingText = {
                    nameError?.let { Text(text = stringResource(it)) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDate.format(EventDateTextFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.event_date)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(role = Role.Button) { isDatePickerVisible = true }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.event_reminder_enabled),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = remindEnabled,
                    onCheckedChange = { remindEnabled = it }
                )
            }
            Button(
                onClick = { isUchiwaDialogVisible = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.event_add_uchiwas))
            }
            Text(
                text = stringResource(R.string.event_selected_uchiwa_count, selectedUchiwaIds.size),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    nameError = if (trimmedName.isBlank()) R.string.event_invalid_name else null
                    if (nameError == null) {
                        onSave(
                            event?.id,
                            trimmedName,
                            selectedDate,
                            remindEnabled,
                            selectedUchiwaIds
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.save))
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }

    if (isDatePickerVisible) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateEpochDay = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toEpochDay()
                        }
                        isDatePickerVisible = false
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (isUchiwaDialogVisible) {
        SelectUchiwasDialog(
            availableUchiwas = availableUchiwas,
            selectedUchiwaIds = selectedUchiwaIds,
            onDismiss = { isUchiwaDialogVisible = false },
            onConfirm = {
                selectedUchiwaIds = it
                isUchiwaDialogVisible = false
            }
        )
    }
}

@Composable
private fun SelectUchiwasDialog(
    availableUchiwas: List<EventTimelineUchiwaUiModel>,
    selectedUchiwaIds: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var currentSelection by remember(selectedUchiwaIds) { mutableStateOf(selectedUchiwaIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.event_add_uchiwas_dialog_title)) },
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
                                    currentSelection - uchiwa.id
                                } else {
                                    currentSelection + uchiwa.id
                                }
                            }
                        ) {
                            TimelineUchiwaThumbnail(
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
            TextButton(onClick = { onConfirm(currentSelection) }) {
                Text(text = stringResource(R.string.event_add_uchiwas_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun EventEditBottomSheetPreview() {
    FansaUchiwaTheme {
        EventEditBottomSheet(
            event = EventTimelineEventUiModel(
                id = "event-1",
                name = "ライブツアー",
                eventDate = LocalDate.of(2026, 7, 1),
                remindEnabled = true,
                linkedUchiwas = listOf(
                    EventTimelineUchiwaUiModel(
                        id = "uchiwa-1",
                        imagePath = null
                    )
                )
            ),
            availableUchiwas = listOf(
                EventTimelineUchiwaUiModel(
                    id = "uchiwa-1",
                    imagePath = null
                )
            ),
            defaultSelectedUchiwaId = "uchiwa-1",
            onDismiss = {},
            onSave = { _, _, _, _, _ -> }
        )
    }
}
