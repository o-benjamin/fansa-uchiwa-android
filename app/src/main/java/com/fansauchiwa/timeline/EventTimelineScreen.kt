package com.fansauchiwa.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.BuildConfig
import com.fansauchiwa.R
import com.fansauchiwa.ads.BannerAd
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

private val TimelineDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPAN)

private sealed interface TimelineSnackbarMessage {
    data object Saved : TimelineSnackbarMessage
    data object Deleted : TimelineSnackbarMessage
    data object DebugReminderSent : TimelineSnackbarMessage
}

@Composable
fun EventTimelineScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventTimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        EventTimelineUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventTimelineUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    textAlign = TextAlign.Center
                )
            }
        }

        is EventTimelineUiState.Success -> {
            EventTimelineContent(
                events = state.events,
                availableUchiwas = state.availableUchiwas,
                isSelectionMode = state.isSelectionMode,
                currentUchiwaId = state.currentUchiwaId,
                onBack = onBack,
                onLinkEvent = viewModel::linkUchiwaToEvent,
                onSaveEvent = viewModel::saveEvent,
                onDeleteEvent = viewModel::deleteEvent,
                onSendDebugReminder = viewModel::sendDebugReminder,
                onUpdateEventThumbnail = viewModel::updateEventThumbnail,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventTimelineContent(
    events: List<EventTimelineEventUiModel>,
    availableUchiwas: List<EventTimelineUchiwaUiModel>,
    isSelectionMode: Boolean,
    currentUchiwaId: String?,
    onBack: () -> Unit,
    onLinkEvent: (String) -> Unit,
    onSaveEvent: (String?, String, LocalDate, Boolean, Set<String>) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onSendDebugReminder: (EventTimelineEventUiModel) -> Unit,
    onUpdateEventThumbnail: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticManager = rememberFansaHapticManager()
    var previousSelectedIndex by remember { mutableIntStateOf(-1) }
    var deleteTarget by remember { mutableStateOf<EventTimelineEventUiModel?>(null) }
    var editingTarget by remember { mutableStateOf<EventTimelineEventUiModel?>(null) }
    var isCreateSheetVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<TimelineSnackbarMessage?>(null) }
    var hasScrolledToInitialItem by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val verticalPadding by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportHeight = layoutInfo.viewportSize.height
            val itemSize = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
            if (viewportHeight > 0 && itemSize > 0) {
                with(density) {
                    ((viewportHeight - itemSize) / 2).coerceAtLeast(0).toDp()
                }
            } else {
                0.dp
            }
        }
    }

    val selectedEventIndex by remember(listState, events) {
        derivedStateOf { calculateCenteredItemIndex(listState, events.size) }
    }
    val selectedEvent = selectedEventIndex?.let(events::getOrNull)
    val snackbarText = when (snackbarMessage) {
        TimelineSnackbarMessage.Saved -> stringResource(R.string.event_saved_snackbar)
        TimelineSnackbarMessage.Deleted -> stringResource(R.string.event_deleted_snackbar)
        TimelineSnackbarMessage.DebugReminderSent -> stringResource(R.string.event_debug_reminder_sent)
        null -> null
    }

    LaunchedEffect(events, listState.layoutInfo.viewportSize.height, verticalPadding) {
        if (!hasScrolledToInitialItem && events.isNotEmpty() && listState.layoutInfo.viewportSize.height > 0 && verticalPadding > 0.dp) {
            val today = LocalDate.now()
            val closestFutureIndex = events
                .mapIndexed { index, event -> index to event.eventDate }
                .filter { (_, date) -> !date.isBefore(today) }
                .minByOrNull { (_, date) -> ChronoUnit.DAYS.between(today, date) }
                ?.first
                ?: 0

            listState.animateScrollToItem(closestFutureIndex)
            hasScrolledToInitialItem = true
        }
    }

    LaunchedEffect(selectedEventIndex) {
        val currentIndex = selectedEventIndex ?: return@LaunchedEffect
        if (previousSelectedIndex != -1 && previousSelectedIndex != currentIndex) {
            hapticManager.perform(FansaHapticType.SEGMENT_TICK)
        }
        previousSelectedIndex = currentIndex
    }

    LaunchedEffect(snackbarText) {
        snackbarText ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(snackbarText)
        snackbarMessage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.event_timeline)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BannerAd(
                LocalContext.current,
                modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { isCreateSheetVisible = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add)
                    )
                }
                if (isSelectionMode && selectedEvent != null) {
                    TimelineActionFab(
                        text = stringResource(
                            R.string.event_link_fab,
                            selectedEvent.name
                        ),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = {
                            onLinkEvent(selectedEvent.id)
                            onBack()
                        }
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        if (events.isEmpty()) {
            EmptyEventTimeline(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = verticalPadding,
                    bottom = verticalPadding
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = events,
                    key = { _, item -> item.id }
                ) { index, item ->
                    EventTimelineCard(
                        event = item,
                        isSelected = index == selectedEventIndex,
                        onEdit = { editingTarget = item },
                        onDelete = { deleteTarget = item },
                        onSendDebugReminder = {
                            onSendDebugReminder(item)
                            snackbarMessage = TimelineSnackbarMessage.DebugReminderSent
                        },
                        onUpdateThumbnail = { imagePath ->
                            onUpdateEventThumbnail(item.id, imagePath)
                        }
                    )
                }
            }
        }
    }

    deleteTarget?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(text = stringResource(R.string.event_delete_title)) },
            text = {
                Text(text = stringResource(R.string.event_delete_message, event.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvent(event.id)
                        deleteTarget = null
                        snackbarMessage = TimelineSnackbarMessage.Deleted
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (isCreateSheetVisible || editingTarget != null) {
        EventEditBottomSheet(
            event = editingTarget,
            availableUchiwas = availableUchiwas,
            defaultSelectedUchiwaId = currentUchiwaId,
            onDismiss = {
                editingTarget = null
                isCreateSheetVisible = false
            },
            onSave = { eventId, name, eventDate, remindEnabled, selectedUchiwaIds ->
                onSaveEvent(
                    eventId,
                    name,
                    eventDate,
                    remindEnabled,
                    selectedUchiwaIds
                )
                editingTarget = null
                isCreateSheetVisible = false
                snackbarMessage = TimelineSnackbarMessage.Saved
            }
        )
    }
}

@Composable
private fun TimelineActionFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.widthIn(max = 240.dp),
        containerColor = containerColor ?: MaterialTheme.colorScheme.secondaryContainer,
        contentColor = contentColor ?: MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun EmptyEventTimeline(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.event_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.event_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun EventTimelineCard(
    event: EventTimelineEventUiModel,
    isSelected: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendDebugReminder: () -> Unit,
    onUpdateThumbnail: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isSelectDialogVisible by remember { mutableStateOf(false) }

    val selectedImagePath = event.thumbnailImagePath
        ?: event.linkedUchiwas.firstOrNull()?.imagePath

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable {
                        if (event.linkedUchiwas.isNotEmpty()) {
                            isSelectDialogVisible = true
                        }
                    }
            ) {
                TimelineUchiwaThumbnail(
                    imagePath = selectedImagePath,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatEventDate(event.eventDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = remainingDaysText(event.eventDate),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.event_linked_uchiwa_count,
                            event.linkedUchiwas.size
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(
                    onClick = { isMenuExpanded = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.edit)) },
                        onClick = {
                            isMenuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.delete)) },
                        onClick = {
                            isMenuExpanded = false
                            onDelete()
                        }
                    )
                    if (BuildConfig.DEBUG) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.event_debug_send_reminder)) },
                            onClick = {
                                isMenuExpanded = false
                                onSendDebugReminder()
                            }
                        )
                    }
                }
            }
        }
    }

    if (isSelectDialogVisible) {
        val selectedUchiwaId =
            event.linkedUchiwas.find { it.imagePath == event.thumbnailImagePath }?.id
                ?: event.linkedUchiwas.firstOrNull()?.id
        SelectUchiwasDialog(
            availableUchiwas = event.linkedUchiwas,
            selectedUchiwaIds = setOfNotNull(selectedUchiwaId),
            onDismiss = { isSelectDialogVisible = false },
            onConfirm = { ids ->
                val selectedId = ids.firstOrNull()
                val imagePath = event.linkedUchiwas.find { it.id == selectedId }?.imagePath
                onUpdateThumbnail(imagePath)
                isSelectDialogVisible = false
            },
            maxSelectionCount = 1,
            titleResId = R.string.event_select_thumbnail_title,
            confirmButtonResId = R.string.event_select_thumbnail_confirm
        )
    }
}

private fun calculateCenteredItemIndex(
    listState: LazyListState,
    itemCount: Int
): Int? {
    if (itemCount == 0) return null
    val layoutInfo = listState.layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty()) return 0
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo.minByOrNull { itemInfo ->
        abs(itemInfo.offset + itemInfo.size / 2 - viewportCenter)
    }?.index
}

private fun formatEventDate(eventDate: LocalDate): String {
    return eventDate.format(TimelineDateFormatter)
}

@Composable
private fun remainingDaysText(eventDate: LocalDate): String {
    val daysUntil = ChronoUnit.DAYS.between(
        LocalDate.now(),
        eventDate
    ).toInt()
    return when {
        daysUntil < 0 -> stringResource(R.string.event_finished)
        daysUntil == 0 -> stringResource(R.string.event_today)
        else -> stringResource(R.string.event_days_left, daysUntil)
    }
}

@Preview(showBackground = true)
@Composable
private fun EventTimelineContentPreview() {
    FansaUchiwaTheme {
        EventTimelineContent(
            events = previewEvents(),
            availableUchiwas = previewEvents().flatMap { it.linkedUchiwas },
            isSelectionMode = true,
            currentUchiwaId = "uchiwa-1",
            onBack = {},
            onLinkEvent = {},
            onSaveEvent = { _, _, _, _, _ -> },
            onDeleteEvent = {},
            onSendDebugReminder = {},
            onUpdateEventThumbnail = { _, _ -> }
        )
    }
}

private fun previewEvents(): List<EventTimelineEventUiModel> {
    val today = LocalDate.now()
    return listOf(
        EventTimelineEventUiModel(
            id = "event-1",
            name = "東京ドーム",
            eventDate = today.plusDays(7),
            remindEnabled = true,
            linkedUchiwas = listOf(previewUchiwa("uchiwa-1"))
        ),
        EventTimelineEventUiModel(
            id = "event-2",
            name = "大阪城ホール",
            eventDate = today.plusDays(14),
            remindEnabled = true,
            linkedUchiwas = listOf(
                previewUchiwa("uchiwa-2"),
                previewUchiwa("uchiwa-3")
            )
        )
    )
}

private fun previewUchiwa(id: String): EventTimelineUchiwaUiModel {
    return EventTimelineUchiwaUiModel(
        id = id,
        imagePath = null
    )
}
