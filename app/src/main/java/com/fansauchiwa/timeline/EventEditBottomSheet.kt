package com.fansauchiwa.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditBottomSheet(
    event: EventEntity?,
    onDismiss: () -> Unit,
    onSave: (String?, String, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable(event?.id) { mutableStateOf(event?.name.orEmpty()) }
    var dateText by rememberSaveable(event?.id) {
        mutableStateOf(event?.eventDateEpochDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty())
    }
    var nameError by remember { mutableStateOf<Int?>(null) }
    var dateError by remember { mutableStateOf<Int?>(null) }

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
            OutlinedTextField(
                value = dateText,
                onValueChange = {
                    dateText = it
                    dateError = null
                },
                label = { Text(stringResource(R.string.event_date)) },
                placeholder = { Text(stringResource(R.string.event_date_hint)) },
                isError = dateError != null,
                supportingText = {
                    dateError?.let { Text(text = stringResource(it)) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val parsedDate = runCatching { LocalDate.parse(dateText.trim()) }.getOrNull()
                    nameError = if (trimmedName.isBlank()) R.string.event_invalid_name else null
                    dateError = if (parsedDate == null) R.string.event_invalid_date else null
                    if (nameError == null && dateError == null && parsedDate != null) {
                        onSave(event?.id, trimmedName, parsedDate)
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
}

@Preview(showBackground = true)
@Composable
private fun EventEditBottomSheetPreview() {
    FansaUchiwaTheme {
        EventEditBottomSheet(
            event = EventEntity(
                id = "event-1",
                name = "ライブツアー",
                eventDateEpochDay = LocalDate.of(2026, 7, 1).toEpochDay()
            ),
            onDismiss = {},
            onSave = { _, _, _ -> }
        )
    }
}
