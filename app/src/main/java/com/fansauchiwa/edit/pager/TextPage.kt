package com.fansauchiwa.edit.pager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.ColorAndWeightControl
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.ItemBadge
import com.fansauchiwa.edit.TestTags
import com.fansauchiwa.edit.buildRankIndexMap
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Composable
fun TextPage(
    onAddText: (FontFamilies) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    onPuffyEnabledChanged: (Boolean) -> Unit,
    onPuffyUnsupportedClick: () -> Unit,
    isPukuPukuSupported: Boolean,
    selectedTextDecoration: Decoration.Text? = null
) {
    FontFamilySelectionGrid(
        onAddText = onAddText,
        onFontChanged = onFontChanged,
        onColorSelected = onColorSelected,
        onTextWeightChanged = onTextWeightChanged,
        onStrokeColorSelected = onStrokeColorSelected,
        onStrokeWeightChanged = onStrokeWeightChanged,
        onSecondBorderColorSelected = onSecondBorderColorSelected,
        onSecondBorderWeightChanged = onSecondBorderWeightChanged,
        onPuffyEnabledChanged = onPuffyEnabledChanged,
        onPuffyUnsupportedClick = onPuffyUnsupportedClick,
        isPukuPukuSupported = isPukuPukuSupported,
        selectedTextDecoration = selectedTextDecoration,
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
    onPuffyEnabledChanged: (Boolean) -> Unit,
    onPuffyUnsupportedClick: () -> Unit,
    textColor: Color,
    textWidth: Int,
    strokeColor: Color,
    strokeWidth: Float,
    secondBorderColor: Color,
    secondBorderWidth: Float,
    isPuffyEnabled: Boolean,
    isPukuPukuSupported: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ColorAndWeightControl(
            title = stringResource(R.string.text_color_and_weight),
            color = textColor,
            width = textWidth.toFloat(),
            valueRange = 100f..900f,
            steps = 7,
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
            steps = 8,
            onColorSelected = onStrokeColorSelected,
            onWeightChanged = onStrokeWeightChanged
        )

        ColorAndWeightControl(
            title = stringResource(R.string.second_stroke_color_and_weight),
            color = secondBorderColor,
            width = secondBorderWidth,
            valueRange = 0f..90f,
            steps = 8,
            onColorSelected = onSecondBorderColorSelected,
            onWeightChanged = onSecondBorderWeightChanged
        )

        val puffyModifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .alpha(if (isPukuPukuSupported) 1f else 0.38f)
            .testTag(TestTags.PUFFY_TEXT_ROW)
        Row(
            modifier = if (isPukuPukuSupported) {
                puffyModifier
            } else {
                puffyModifier.clickable(onClick = onPuffyUnsupportedClick)
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.text_puffy_enabled),
                fontWeight = FontWeight.Bold,
            )
            Switch(
                checked = isPuffyEnabled,
                onCheckedChange = onPuffyEnabledChanged,
                enabled = isPukuPukuSupported,
                modifier = Modifier.testTag(TestTags.PUFFY_TEXT_SWITCH)
            )
        }
    }
}

@Composable
fun FontFamilySelectionGrid(
    onAddText: (FontFamilies) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    onPuffyEnabledChanged: (Boolean) -> Unit,
    onPuffyUnsupportedClick: () -> Unit,
    isPukuPukuSupported: Boolean,
    selectedTextDecoration: Decoration.Text?,
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
        modifier = modifier.testTag(TestTags.FONT_FAMILY_GRID)
    ) {
        if (selectedTextDecoration != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextDecorationControls(
                    onColorSelected = onColorSelected,
                    onTextWeightChanged = onTextWeightChanged,
                    onStrokeColorSelected = onStrokeColorSelected,
                    onStrokeWeightChanged = onStrokeWeightChanged,
                    onSecondBorderColorSelected = onSecondBorderColorSelected,
                    onSecondBorderWeightChanged = onSecondBorderWeightChanged,
                    onPuffyEnabledChanged = onPuffyEnabledChanged,
                    onPuffyUnsupportedClick = onPuffyUnsupportedClick,
                    textColor = selectedTextDecoration.color,
                    strokeColor = selectedTextDecoration.strokeColor,
                    textWidth = selectedTextDecoration.width,
                    strokeWidth = selectedTextDecoration.strokeWidth,
                    secondBorderColor = selectedTextDecoration.secondBorderColor,
                    secondBorderWidth = selectedTextDecoration.secondBorderWidth,
                    isPuffyEnabled = selectedTextDecoration.isPuffyEnabled,
                    isPukuPukuSupported = isPukuPukuSupported
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(FontFamilies.entries.toList()) { fontFamily ->
            val isSelected = selectedTextDecoration?.font == fontFamily
            Box(contentAlignment = Alignment.Center) {
                FilledTonalButton(
                    onClick = {
                        if (selectedTextDecoration != null) {
                            onFontChanged(fontFamily)
                        } else {
                            onAddText(fontFamily)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = if (isSelected) BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    ) else null,
                    modifier = Modifier
                        .height(buttonHeight)
                        .testTag(TestTags.FONT_BUTTON_PREFIX + fontFamily.name)
                        .semantics { selected = isSelected }
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
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onPuffyEnabledChanged = {},
            onPuffyUnsupportedClick = {},
            isPukuPukuSupported = true,
            selectedTextDecoration = Decoration.Text(
                id = "preview-id",
                font = FontFamilies.HACHI_MARU_POP,
                text = "プレビュー",
                color = Color(0xFF000000),
                strokeColor = Color(0xFFFFFFFF),
                width = 700,
                strokeWidth = 2.5f,
                isPuffyEnabled = true
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 240)
@Composable
fun FontFamilySelectionGridNarrowPreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onPuffyEnabledChanged = {},
            onPuffyUnsupportedClick = {},
            isPukuPukuSupported = true,
            selectedTextDecoration = null
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun FontFamilySelectionGridMediumPreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onPuffyEnabledChanged = {},
            onPuffyUnsupportedClick = {},
            isPukuPukuSupported = true,
            selectedTextDecoration = null
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
fun FontFamilySelectionGridWidePreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onPuffyEnabledChanged = {},
            onPuffyUnsupportedClick = {},
            isPukuPukuSupported = true,
            selectedTextDecoration = null
        )
    }
}
