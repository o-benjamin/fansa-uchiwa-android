package com.example.fansauchiwa.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.ui.StickerAsset
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditPager(
    onStickerClick: (Decoration.Sticker) -> Unit,
    onTextClick: (Decoration.Text) -> Unit,
    selectedDecoration: Decoration? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        val pagerState = rememberPagerState(pageCount = { DecorationTabType.entries.size })
        val tabIndex = pagerState.currentPage
        val scope = rememberCoroutineScope()

        // selectedDecorationの種類に応じてページを自動的に切り替える
        LaunchedEffect(selectedDecoration) {
            if (selectedDecoration != null) {
                val targetPage = when (selectedDecoration) {
                    is Decoration.Text -> 0
                    is Decoration.Sticker -> 1
                }
                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
            }
        }

        TabRow(
            selectedTabIndex = tabIndex
        ) {
            DecorationTabType.entries.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title.tabText, maxLines = 1) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> {
                    TextPage(onTextClick = onTextClick)
                }

                1 -> {
                    StickerPage(onStickerClick = onStickerClick)
                }

                else -> {
                    // Placeholder for other tabs
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${DecorationTabType.entries[page]} content")
                    }
                }
            }
        }
    }
}

@Composable
fun StickerPage(
    onStickerClick: (Decoration.Sticker) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(StickerAsset.entries) { sticker ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onStickerClick(
                            Decoration.Sticker(
                                label = sticker.type,
                                id = UUID.randomUUID().toString(),
                                offset = Offset.Zero,
                                rotation = 0f,
                                scale = 1f
                            )
                        )
                    }
            ) {
                Image(
                    painter = painterResource(id = sticker.resId),
                    contentDescription = sticker.type,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TextPage(
    modifier: Modifier = Modifier,
    onTextClick: (Decoration.Text) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                onTextClick(
                    Decoration.Text(
                        text = DEFAULT_TEXT,
                        id = UUID.randomUUID().toString(),
                        offset = Offset.Zero,
                        rotation = 0f,
                        scale = 1f,
                    )
                )
            }
        ) {
            Text(text = "テキストを追加")
        }
    }
}

const val DEFAULT_TEXT = "テキストを入力"
