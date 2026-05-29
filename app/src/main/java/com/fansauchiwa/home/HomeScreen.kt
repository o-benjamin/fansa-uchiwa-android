package com.fansauchiwa.home

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.fansauchiwa.R
import com.fansauchiwa.ads.BannerAd
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.decorationitem.StickerItemContent
import com.fansauchiwa.edit.decorationitem.TextItemContent
import com.fansauchiwa.edit.nonScaledSp
import com.fansauchiwa.ui.composable.FansaFloatingActionButton
import com.fansauchiwa.ui.composable.SelectionCircleIcon
import com.fansauchiwa.ui.modifier.fansaCombinedClickable
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.min

internal val TemplatePreviewSummaryKey = SemanticsPropertyKey<String>("TemplatePreviewSummary")
internal var SemanticsPropertyReceiver.templatePreviewSummary by TemplatePreviewSummaryKey

internal fun buildTemplatePreviewSummary(savedUchiwa: SavedUchiwa): String = buildString {
    append(
        savedUchiwa.decorations
            .filterIsInstance<Decoration.Text>()
            .joinToString(separator = "|") { it.text }
    )
    append(";background=")
    append(savedUchiwa.backgroundColor.value.toString())
    append(";uchiwa=")
    append(savedUchiwa.uchiwaColor.value.toString())
}

private val homeNavigationTabs = listOf(HomeTab.HOME, HomeTab.MY_DESIGN)

private fun HomeTab.icon(): ImageVector = when (this) {
    HomeTab.HOME -> Icons.Default.Home
    HomeTab.MY_DESIGN -> Icons.Default.PhotoLibrary
}

private fun HomeTab.labelResId(): Int = when (this) {
    HomeTab.HOME -> R.string.home
    HomeTab.MY_DESIGN -> R.string.my_design
}

@Composable
private fun HomeFab(
    isSelectionMode: Boolean,
    selectedCount: Int,
    isFabExpanded: Boolean,
    onExitSelectionMode: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelectionMode) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FansaFloatingActionButton(onClick = onExitSelectionMode) {
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            if (selectedCount > 0) {
                FansaFloatingActionButton(
                    onClick = onDuplicate,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.duplicate),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                FansaFloatingActionButton(
                    onClick = onDelete,
                    hapticFeedbackType = FansaHapticType.CONFIRM,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    } else {
        ExtendedFloatingActionButton(
            modifier = modifier,
            onClick = onAdd,
            expanded = isFabExpanded,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.add),
                    fontSize = 16.sp
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    onNavigateToSettings: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenOfficialSite: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {},
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            }
            IconButton(onClick = onNavigateToTimeline) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.event_timeline)
                )
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_options)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.official_site)) },
                    onClick = {
                        menuExpanded = false
                        onOpenOfficialSite()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.privacy_policy)) },
                    onClick = {
                        menuExpanded = false
                        onOpenPrivacyPolicy()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.feedback)) },
                    onClick = {
                        menuExpanded = false
                        onOpenFeedback()
                    }
                )
            }
        }
    )
}

@Composable
internal fun HomeNavigationBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets()
    ) {
        homeNavigationTabs.forEach { tab ->
            val label = stringResource(tab.labelResId())
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon(),
                        contentDescription = label
                    )
                },
                label = {
                    Text(text = label)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: (String, String?) -> Unit = { _, _ -> },
    onAddClick: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()
    val isFabExpanded by remember {
        derivedStateOf { lazyGridState.firstVisibleItemIndex == 0 }
    }
    val context = LocalContext.current
    val duplicateMasterpieceSnackbar = stringResource(R.string.duplicate_masterpiece_snackbar)
    val deleteMasterpieceSnackbar = stringResource(R.string.delete_masterpiece_snackbar)

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
        // ViewModelのinitでloadすると、画面に戻ってきたに情報が更新されないため、描画時に毎回更新するようにする
        viewModel.loadAllMasterpieces()
        viewModel.loadTemplates()
        viewModel.observeApologyDialogState()
        viewModel.fetchApologyDialogState()
    }

    if (uiState.showApologyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissApologyDialog() },
            title = {
                Text(text = stringResource(R.string.apology_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.apology_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissApologyDialog() }) {
                    Text(text = "閉じる")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            val uriHandler = LocalUriHandler.current
            HomeTopAppBar(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToTimeline = onNavigateToTimeline,
                onOpenPrivacyPolicy = {
                    val url =
                        "https://o-benjamin.github.io/fansa-uchiwa-android/privacy-policy.html"
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, url.toUri())
                },
                onOpenFeedback = {
                    val url = "https://forms.gle/UyTgAZ2ewDHzwTwN6"
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, url.toUri())
                },
                onOpenOfficialSite = {
                    uriHandler.openUri("https://fansauchiwa-578d22ff.web.app")
                }
            )
        },
        bottomBar = {
            Column {
                HomeNavigationBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected,
                    modifier = Modifier.fillMaxWidth()
                )
                BannerAd(
                    LocalContext.current,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        },
        floatingActionButton = {
            HomeFab(
                isSelectionMode = uiState.isSelectionMode,
                selectedCount = uiState.selectedPaths.size,
                isFabExpanded = isFabExpanded,
                onExitSelectionMode = { viewModel.exitSelectionMode() },
                onDuplicate = {
                    viewModel.duplicateSelectedMasterpieces()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "${uiState.selectedPaths.size}${duplicateMasterpieceSnackbar}"
                        )
                    }
                },
                onDelete = {
                    viewModel.deleteSelectedMasterpieces()
                    scope.launch {
                        snackbarHostState.showSnackbar("${uiState.selectedPaths.size}${deleteMasterpieceSnackbar}")
                    }
                },
                onAdd = {
                    viewModel.logNewCreateTap()
                    onAddClick()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { innerPadding ->
        HomeTabContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            selectedTab = uiState.selectedTab,
            masterpiecePathList = uiState.masterpiecePathList,
            templates = uiState.templates,
            isSelectionMode = uiState.isSelectionMode,
            selectedPaths = uiState.selectedPaths,
            lazyGridState = lazyGridState,
            onImageClick = { path ->
                if (uiState.isSelectionMode) {
                    viewModel.togglePathSelection(path)
                } else {
                    val uchiwaId = viewModel.extractUchiwaId(path)
                    viewModel.logItemEditTap()
                    onImageClick(uchiwaId, null)
                }
            },
            onTemplateClick = { templateId ->
                viewModel.logTemplateTap(templateId)
                val newUchiwaId = UUID.randomUUID().toString()
                onImageClick(newUchiwaId, templateId)
            },
            onImageLongPress = {
                viewModel.enterSelectionMode()
            },
            statusBarPadding = innerPadding.calculateTopPadding()
        )

    }
}

@Composable
internal fun HomeTabContent(
    selectedTab: HomeTab,
    masterpiecePathList: List<String>,
    templates: List<Template>,
    isSelectionMode: Boolean,
    selectedPaths: List<String>,
    onImageClick: (String) -> Unit,
    onTemplateClick: (String) -> Unit,
    onImageLongPress: () -> Unit,
    statusBarPadding: Dp,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    isPreview: Boolean = false
) {
    when (selectedTab) {
        HomeTab.HOME -> HomeTabHomeContent(
            templates = templates,
            onTemplateClick = onTemplateClick,
            statusBarPadding = statusBarPadding,
            modifier = modifier,
            isPreview = isPreview
        )

        HomeTab.MY_DESIGN -> HomeTabMyDesignContent(
            masterpiecePathList = masterpiecePathList,
            isSelectionMode = isSelectionMode,
            selectedPaths = selectedPaths,
            lazyGridState = lazyGridState,
            onImageClick = onImageClick,
            onImageLongPress = onImageLongPress,
            statusBarPadding = statusBarPadding,
            modifier = modifier,
            isPreview = isPreview
        )
    }
}

@Composable
private fun HomeTabHomeContent(
    templates: List<Template>,
    onTemplateClick: (String) -> Unit,
    statusBarPadding: Dp,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    if (templates.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(top = statusBarPadding),
            contentAlignment = Alignment.Center
        ) {
            EmptyTemplateMessage()
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(152.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(top = statusBarPadding),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TemplateSectionHeader()
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            TemplateRow(
                templates = templates,
                onTemplateClick = onTemplateClick,
                isPreview = isPreview
            )
        }
    }
}

@Composable
private fun HomeTabMyDesignContent(
    masterpiecePathList: List<String>,
    isSelectionMode: Boolean,
    selectedPaths: List<String>,
    lazyGridState: LazyGridState,
    onImageClick: (String) -> Unit,
    onImageLongPress: () -> Unit,
    statusBarPadding: Dp,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    if (masterpiecePathList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(top = statusBarPadding),
            contentAlignment = Alignment.Center
        ) {
            EmptyMasterpieceMessage()
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(152.dp),
        state = lazyGridState,
        modifier = modifier
            .fillMaxSize()
            .padding(top = statusBarPadding),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MyDesignSectionHeader()
        }
        items(masterpiecePathList) { path ->
            MasterpieceItem(
                imagePath = path,
                isSelected = selectedPaths.contains(path),
                isSelectionMode = isSelectionMode,
                onClick = { onImageClick(path) },
                onLongClick = onImageLongPress,
                isPreview = isPreview
            )
        }
    }
}

@Composable
private fun TemplateSectionHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.template_section_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun TemplateRow(
    templates: List<Template>,
    onTemplateClick: (String) -> Unit,
    isPreview: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(templates, key = { it.id }) { template ->
            TemplateItem(
                template = template,
                onClick = { onTemplateClick(template.id) },
                isPreview = isPreview
            )
        }
    }
}

@Composable
private fun TemplateItem(
    template: Template,
    onClick: () -> Unit,
    isPreview: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(152.dp)
            .aspectRatio(1.2f),
        onClick = onClick
    ) {
        ComponentTemplateItem(
            template = template,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ComponentTemplateItem(
    template: Template,
    modifier: Modifier = Modifier
) {
    val savedUchiwa = template.savedUchiwa

    BoxWithConstraints(
        modifier = modifier
            .padding(8.dp)
            .testTag("template-preview-${template.id}"),
        contentAlignment = Alignment.Center
    ) {
        val referenceWidth = 360.dp
        val referenceHeight = referenceWidth / 1.414f
        val scale = min(maxWidth / referenceWidth, maxHeight / referenceHeight)

        Box(
            modifier = Modifier
                .requiredSize(referenceWidth, referenceHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .semantics(mergeDescendants = true) {},
            contentAlignment = Alignment.Center
        ) {
            savedUchiwa.decorations.forEach { decoration ->
                when (decoration) {
                    is Decoration.Text -> TemplateTextItem(decoration)
                    is Decoration.Sticker -> TemplateStickerItem(decoration)
                    is Decoration.Image -> Unit
                }
            }
        }
    }
}

@Composable
private fun TemplateTextItem(
    decoration: Decoration.Text,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.graphicsLayer {
            translationX = decoration.offset.x
            translationY = decoration.offset.y
            scaleX = decoration.scale
            scaleY = decoration.scale
            rotationZ = decoration.rotation
        }
    ) {
        TextItemContent(
            decoration = decoration,
            textSize = 24.sp.nonScaledSp,
        )
    }
}

@Composable
private fun TemplateStickerItem(
    decoration: Decoration.Sticker,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.graphicsLayer {
            translationX = decoration.offset.x
            translationY = decoration.offset.y
            scaleX = decoration.scale
            scaleY = decoration.scale
            rotationZ = decoration.rotation
        }
    ) {
        StickerItemContent(
            decoration = decoration,
            modifier = Modifier
        )
    }
}

@Composable
private fun MyDesignSectionHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.my_design_section_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun EmptyTemplateMessage(modifier: Modifier = Modifier) {
    EmptyStateMessage(
        title = stringResource(R.string.empty_template_title),
        modifier = modifier
    )
}

@Composable
private fun EmptyMasterpieceMessage(modifier: Modifier = Modifier) {
    EmptyStateMessage(
        title = stringResource(R.string.empty_masterpiece_title),
        modifier = modifier
    )
}

@Composable
private fun EmptyStateMessage(
    title: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = {
                    uriHandler.openUri("https://fansauchiwa-578d22ff.web.app#how-to-use")
                }
            ) {
                Text(
                    text = stringResource(R.string.check_how_to_use),
                    textDecoration = TextDecoration.Underline,
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.external_link)
                )

            }
        }
    }
}

@Composable
private fun MasterpieceItem(
    imagePath: String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fansaCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        ) {
            if (isPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = imagePath,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagePath)
                        .addLastModifiedToFileCacheKey(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            if (isSelectionMode) {
                SelectionCircleIcon(
                    isSelected = isSelected,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun previewTemplates(): List<Template> {
    return (1..3).map { index ->
        Template(
            id = "template_$index",
            previewImageResId = R.drawable.uchiwa_shape,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        id = "preview_text_$index",
                        text = "サンプル$index",
                        color = Color.White,
                        strokeColor = Color(0xFF65E8FF),
                        strokeWidth = 24f,
                        width = FontWeight.W900.weight,
                        font = FontFamilies.M_PLUS_ROUNDED_1C
                    )
                ),
                uchiwaColor = Color(0xFFF6D6FF),
                backgroundColor = Color(0x11000000)
            )
        )
    }
}

@Preview(showBackground = true, name = "ホームタブ")
@Composable
private fun HomeTabHomeContentPreview() {
    HomeTabHomeContent(
        modifier = Modifier.fillMaxSize(),
        templates = previewTemplates(),
        onTemplateClick = {},
        statusBarPadding = 0.dp,
        isPreview = true
    )
}

@Preview(showBackground = true, name = "マイデザインタブ")
@Composable
private fun HomeTabMyDesignContentPreview() {
    HomeTabMyDesignContent(
        modifier = Modifier.fillMaxSize(),
        masterpiecePathList = (1..6).map { "masterpiece_$it" },
        isSelectionMode = false,
        selectedPaths = emptyList(),
        lazyGridState = rememberLazyGridState(),
        onImageClick = {},
        onImageLongPress = {},
        statusBarPadding = 0.dp,
        isPreview = true
    )
}

// region FAB Previews

@Preview(showBackground = true, name = "FAB - 通常モード（展開）")
@Composable
private fun HomeFabPreview_Normal_Expanded() {
    FansaUchiwaTheme {
        HomeFab(
            isSelectionMode = false,
            selectedCount = 0,
            isFabExpanded = true,
            onExitSelectionMode = {},
            onDuplicate = {},
            onDelete = {},
            onAdd = {}
        )
    }
}

@Preview(showBackground = true, name = "FAB - 通常モード（折りたたみ）")
@Composable
private fun HomeFabPreview_Normal_Collapsed() {
    FansaUchiwaTheme {
        HomeFab(
            isSelectionMode = false,
            selectedCount = 0,
            isFabExpanded = false,
            onExitSelectionMode = {},
            onDuplicate = {},
            onDelete = {},
            onAdd = {}
        )
    }
}

@Preview(showBackground = true, name = "FAB - 選択モード（選択数0）")
@Composable
private fun HomeFabPreview_SelectionMode_NoSelection() {
    FansaUchiwaTheme {
        HomeFab(
            isSelectionMode = true,
            selectedCount = 0,
            isFabExpanded = false,
            onExitSelectionMode = {},
            onDuplicate = {},
            onDelete = {},
            onAdd = {}
        )
    }
}

@Preview(showBackground = true, name = "FAB - 選択モード（選択あり）")
@Composable
private fun HomeFabPreview_SelectionMode_WithSelection() {
    FansaUchiwaTheme() {
        HomeFab(
            isSelectionMode = true,
            selectedCount = 3,
            isFabExpanded = false,
            onExitSelectionMode = {},
            onDuplicate = {},
            onDelete = {},
            onAdd = {}
        )
    }
}

// endregion
