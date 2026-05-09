package com.fansauchiwa.timeline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventWithUchiwas
import com.fansauchiwa.data.source.FansaUchiwaEntity
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

private val TimelineDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPAN)
private val TimelineContentPadding = PaddingValues(
    start = 24.dp,
    end = 24.dp,
    top = 160.dp,
    bottom = 160.dp
)
private const val TimelineMaxRotationDegrees = 10f
private const val TimelineAlphaAttenuation = 0.4f
private const val TimelineMinAlpha = 0.55f

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
                isSelectionMode = state.isSelectionMode,
                onBack = onBack,
                onLinkEvent = viewModel::linkUchiwaToEvent,
                onSaveEvent = viewModel::saveEvent,
                onDeleteEvent = viewModel::deleteEvent,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventTimelineContent(
    events: List<EventWithUchiwas>,
    isSelectionMode: Boolean,
    onBack: () -> Unit,
    onLinkEvent: (String) -> Unit,
    onSaveEvent: (String?, String, LocalDate, Boolean) -> Unit,
    onDeleteEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hapticManager = rememberFansaHapticManager()
    var previousSelectedIndex by remember { mutableIntStateOf(-1) }
    var deleteTarget by remember { mutableStateOf<EventEntity?>(null) }
    var editingTarget by remember { mutableStateOf<EventEntity?>(null) }
    var isCreateSheetVisible by remember { mutableStateOf(false) }
    val selectedEventIndex by remember(listState, events) {
        derivedStateOf { calculateCenteredItemIndex(listState, events.size) }
    }
    val selectedEvent = selectedEventIndex?.let(events::getOrNull)

    LaunchedEffect(selectedEventIndex) {
        val currentIndex = selectedEventIndex ?: return@LaunchedEffect
        if (previousSelectedIndex != -1 && previousSelectedIndex != currentIndex) {
            hapticManager.perform(FansaHapticType.SEGMENT_TICK)
        }
        previousSelectedIndex = currentIndex
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
                },
                actions = {
                    IconButton(onClick = { isCreateSheetVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.event_add)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isSelectionMode && selectedEvent != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onLinkEvent(selectedEvent.event.id)
                        isCreateSheetVisible = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(
                                    R.string.event_linked_snackbar,
                                    selectedEvent.event.name
                                )
                            )
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.event_link_fab,
                                selectedEvent.event.name
                            )
                        )
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        if (events.isEmpty()) {
            EmptyEventTimeline(
                onAddClick = { isCreateSheetVisible = true },
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
                contentPadding = TimelineContentPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = events,
                    key = { _, item -> item.event.id }
                ) { index, item ->
                    EventTimelineCard(
                        eventWithUchiwas = item,
                        listState = listState,
                        index = index,
                        isSelected = index == selectedEventIndex,
                        onEdit = { editingTarget = item.event },
                        onDelete = { deleteTarget = item.event }
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
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.event_deleted_snackbar)
                            )
                        }
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
            onDismiss = {
                editingTarget = null
                isCreateSheetVisible = false
            },
            onSave = { eventId, name, eventDate ->
                val shouldLinkCurrentUchiwa = isSelectionMode && eventId == null
                onSaveEvent(eventId, name, eventDate, shouldLinkCurrentUchiwa)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (shouldLinkCurrentUchiwa) {
                            context.getString(R.string.event_linked_snackbar, name)
                        } else {
                            context.getString(R.string.event_saved_snackbar)
                        }
                    )
                }
                editingTarget = null
                isCreateSheetVisible = false
            }
        )
    }
}

@Composable
private fun EmptyEventTimeline(
    onAddClick: () -> Unit,
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
        TextButton(
            onClick = onAddClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.event_add))
        }
    }
}

@Composable
private fun EventTimelineCard(
    eventWithUchiwas: EventWithUchiwas,
    listState: LazyListState,
    index: Int,
    isSelected: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    val density = LocalDensity.current
    val maxTranslationPx = with(density) { 64.dp.toPx() }
    val rotation = timelineCardRotation(itemInfo, listState)
    val translationX = timelineCardTranslationX(itemInfo, listState, maxTranslationPx)
    val targetAlpha = timelineCardAlpha(itemInfo, listState)
    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, label = "timelineCardAlpha")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 352.dp)
            .graphicsLayer {
                this.translationX = translationX
                rotationZ = rotation
                alpha = animatedAlpha
            },
        shape = RoundedCornerShape(24.dp),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = eventWithUchiwas.event.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatEventDate(eventWithUchiwas.event.eventDateEpochDay),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = remainingDaysText(eventWithUchiwas.event.eventDateEpochDay),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.event_linked_uchiwa_count,
                            eventWithUchiwas.uchiwas.size
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(
                    onClick = { isMenuExpanded = true },
                    modifier = Modifier.align(Alignment.TopEnd)
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
                }
            }
        }
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

private fun timelineCardTranslationX(
    itemInfo: LazyListItemInfo?,
    listState: LazyListState,
    maxTranslationPx: Float
): Float {
    return maxTranslationPx * abs(normalizedTimelineDistance(itemInfo, listState))
}

private fun timelineCardRotation(
    itemInfo: LazyListItemInfo?,
    listState: LazyListState
): Float {
    return normalizedTimelineDistance(itemInfo, listState) * TimelineMaxRotationDegrees
}

private fun timelineCardAlpha(
    itemInfo: LazyListItemInfo?,
    listState: LazyListState
): Float {
    val alpha = 1f - abs(normalizedTimelineDistance(itemInfo, listState)) * TimelineAlphaAttenuation
    return alpha.coerceIn(TimelineMinAlpha, 1f)
}

private fun normalizedTimelineDistance(
    itemInfo: LazyListItemInfo?,
    listState: LazyListState
): Float {
    val layoutInfo = listState.layoutInfo
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
    val itemCenter = itemInfo?.let { it.offset + it.size / 2f } ?: viewportCenter
    val viewportSize = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
        .coerceAtLeast(1)
    return ((itemCenter - viewportCenter) / viewportSize).coerceIn(-1f, 1f)
}

private fun formatEventDate(eventDateEpochDay: Long): String {
    return LocalDate.ofEpochDay(eventDateEpochDay).format(TimelineDateFormatter)
}

@Composable
private fun remainingDaysText(eventDateEpochDay: Long): String {
    val daysUntil = ChronoUnit.DAYS.between(
        LocalDate.now(),
        LocalDate.ofEpochDay(eventDateEpochDay)
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
            isSelectionMode = true,
            onBack = {},
            onLinkEvent = {},
            onSaveEvent = { _, _, _, _ -> },
            onDeleteEvent = {}
        )
    }
}

private fun previewEvents(): List<EventWithUchiwas> {
    val today = LocalDate.now()
    return listOf(
        EventWithUchiwas(
            event = EventEntity(
                id = "event-1",
                name = "東京ドーム",
                eventDateEpochDay = today.plusDays(7).toEpochDay()
            ),
            uchiwas = listOf(previewUchiwa("uchiwa-1"))
        ),
        EventWithUchiwas(
            event = EventEntity(
                id = "event-2",
                name = "大阪城ホール",
                eventDateEpochDay = today.plusDays(14).toEpochDay()
            ),
            uchiwas = listOf(previewUchiwa("uchiwa-2"), previewUchiwa("uchiwa-3"))
        )
    )
}

private fun previewUchiwa(id: String): FansaUchiwaEntity {
    return FansaUchiwaEntity(
        id = id,
        decorations = emptyList(),
        uchiwaColorValue = 0L,
        backgroundColorValue = 0L
    )
}
