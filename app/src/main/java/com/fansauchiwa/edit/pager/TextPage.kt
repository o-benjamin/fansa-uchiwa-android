package com.fansauchiwa.edit.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.ColorAndWeightControl
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.ItemBadge
import com.fansauchiwa.edit.buildRankIndexMap
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import java.util.UUID

@Composable
fun TextPage(
    onTextClick: (Decoration.Text) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    selectedDecoration: Decoration? = null
) {
    FontFamilySelectionGrid(
        onTextClick = onTextClick,
        onFontChanged = onFontChanged,
        onColorSelected = onColorSelected,
        onTextWeightChanged = onTextWeightChanged,
        onStrokeColorSelected = onStrokeColorSelected,
        onStrokeWeightChanged = onStrokeWeightChanged,
        onSecondBorderColorSelected = onSecondBorderColorSelected,
        onSecondBorderWeightChanged = onSecondBorderWeightChanged,
        selectedDecoration = selectedDecoration,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun TextDecorationControls(
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    textColor: Color,
    textWidth: Int,
    strokeColor: Color,
    strokeWidth: Float,
    secondBorderColor: Color,
    secondBorderWidth: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ColorAndWeightControl(
            title = stringResource(R.string.text_color_and_weight),
            color = textColor,
            width = textWidth.toFloat(),
            valueRange = 100f..900f,
            steps = 9,
            onColorSelected = onColorSelected,
            onWeightChanged = { newValue ->
                onTextWeightChanged(newValue.toInt())
            }
        )

        ColorAndWeightControl(
            title = stringResource(R.string.stroke_color_and_weight),
            color = strokeColor,
            width = strokeWidth,
            valueRange = 0f..90f,
            steps = 10,
            onColorSelected = onStrokeColorSelected,
            onWeightChanged = onStrokeWeightChanged
        )

        ColorAndWeightControl(
            title = stringResource(R.string.second_stroke_color_and_weight),
            color = secondBorderColor,
            width = secondBorderWidth,
            valueRange = 0f..90f,
            steps = 10,
            onColorSelected = onSecondBorderColorSelected,
            onWeightChanged = onSecondBorderWeightChanged
        )
    }
}

@Composable
fun FontFamilySelectionGrid(
    onTextClick: (Decoration.Text) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    selectedDecoration: Decoration?,
    modifier: Modifier = Modifier
) {
    val minButtonWidth = 88.dp
    val buttonHeight = 54.dp
    val spacing = 8.dp

    // isNew = false のエントリだけで 0 始まりの通し番号を付与するマップ
    val rankIndexMap = remember {
        buildRankIndexMap(FontFamilies.entries) { it.isNew }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minButtonWidth),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, bottom = 32.dp),
        modifier = modifier
    ) {
        if (selectedDecoration is Decoration.Text) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextDecorationControls(
                    onColorSelected = onColorSelected,
                    onTextWeightChanged = onTextWeightChanged,
                    onStrokeColorSelected = onStrokeColorSelected,
                    onStrokeWeightChanged = onStrokeWeightChanged,
                    onSecondBorderColorSelected = onSecondBorderColorSelected,
                    onSecondBorderWeightChanged = onSecondBorderWeightChanged,
                    textColor = selectedDecoration.color,
                    strokeColor = selectedDecoration.strokeColor,
                    textWidth = selectedDecoration.width,
                    strokeWidth = selectedDecoration.strokeWidth,
                    secondBorderColor = selectedDecoration.secondBorderColor,
                    secondBorderWidth = selectedDecoration.secondBorderWidth
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(FontFamilies.entries.toList()) { fontFamily ->
            Box(contentAlignment = Alignment.Center) {
                FilledTonalButton(
                    onClick = {
                        if (selectedDecoration is Decoration.Text) {
                            onFontChanged(fontFamily)
                        } else {
                            onTextClick(
                                Decoration.Text(
                                    id = UUID.randomUUID().toString(),
                                    font = fontFamily
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(buttonHeight)
                ) {
                    val density = LocalDensity.current
                    Text(
                        text = "あA!",
                        fontSize = (20.dp.value / density.fontScale).sp,
                        fontFamily = fontFamily.value
                    )
                }
                val rankIndex = rankIndexMap[fontFamily]
                ItemBadge(
                    rankIndex = rankIndex,
                    isNew = fontFamily.isNew,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextPagePreview() {
    FansaUchiwaTheme {
        TextPage(
            onTextClick = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            selectedDecoration = Decoration.Text(
                id = "preview-id",
                font = FontFamilies.HACHI_MARU_POP,
                text = "プレビュー",
                color = Color(0xFF000000),
                strokeColor = Color(0xFFFFFFFF),
                width = 700,
                strokeWidth = 2.5f
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 240)
@Composable
fun FontFamilySelectionGridNarrowPreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onTextClick = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            selectedDecoration = null
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun FontFamilySelectionGridMediumPreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onTextClick = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            selectedDecoration = null
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
fun FontFamilySelectionGridWidePreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onTextClick = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            selectedDecoration = null
        )
    }
}
