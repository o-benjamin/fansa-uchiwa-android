package com.example.fansauchiwa.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.ui.StickerAsset
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditPager(
    modifier: Modifier = Modifier,
    onStickerClick: (Decoration.Sticker) -> Unit,
    onTextClick: (Decoration.Text) -> Unit,
    selectedDecoration: Decoration? = null,
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
    onTextClick: (Decoration.Text) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.text_color_and_weight),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Red, Color.Blue)
                        )
                    )
                    .clickable { }
            )
            val weight = remember { mutableFloatStateOf(1f) }
            Slider(
                value = weight.floatValue,
                onValueChange = { weight.floatValue = it },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = stringResource(R.string.stroke_color_and_weight),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Red, Color.Blue)
                        )
                    )
                    .clickable { }
            )
            val strokeWidth = remember { mutableFloatStateOf(1f) }
            Slider(
                value = strokeWidth.floatValue,
                onValueChange = { strokeWidth.floatValue = it },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.weight(1f)
            )
        }
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
            },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(text = "テキストを追加")
        }
    }

}

const val DEFAULT_TEXT = "テキストを入力"
