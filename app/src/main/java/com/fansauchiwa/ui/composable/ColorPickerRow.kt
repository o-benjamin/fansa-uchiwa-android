package com.fansauchiwa.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.edit.ColorPickerDialog
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Composable
fun ColorPickerRow(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = DecorationColors.entries.map { it.value },
    includeCustomColorPicker: Boolean = true,
    chipSize: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    selectedScale: Float = 1f,
    showSelectionIndicator: Boolean = false,
    selectedBorderWidth: Dp = 1.dp,
    unselectedBorderWidth: Dp = 1.dp,
    applySelectedSemantics: Boolean = false,
    chipBorderColor: @Composable (Color, Boolean) -> Color = { _, _ -> colorResource(R.color.gray) },
    testTagProvider: ((Color) -> String)? = null
) {
    var showColorPickerDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (includeCustomColorPicker) {
            Box(
                modifier = Modifier
                    .size(chipSize)
                    .clip(CircleShape)
                    .border(1.dp, colorResource(R.color.gray), CircleShape)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            )
                        )
                    )
                    .clickable { showColorPickerDialog = true }
            )
        }

        colors.forEach { color ->
            val isSelected = color == currentColor
            val scale by animateFloatAsState(
                targetValue = if (isSelected) selectedScale else 1f,
                label = "colorPickerChipScale"
            )

            var chipModifier = Modifier
                .size(chipSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .border(
                    width = if (isSelected) selectedBorderWidth else unselectedBorderWidth,
                    color = chipBorderColor(color, isSelected),
                    shape = CircleShape
                )
                .background(color)
                .clickable { onColorSelected(color) }

            if (applySelectedSemantics) {
                chipModifier = chipModifier.semantics { selected = isSelected }
            }
            if (testTagProvider != null) {
                chipModifier = chipModifier.testTag(testTagProvider(color))
            }

            Box(
                modifier = chipModifier,
                contentAlignment = Alignment.Center
            ) {
                if (showSelectionIndicator && isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                    )
                }
            }
        }
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            initialColor = currentColor,
            onDismiss = { showColorPickerDialog = false },
            onColorSelected = onColorSelected
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerRowPreview() {
    FansaUchiwaTheme {
        ColorPickerRow(
            currentColor = Color(0xFFFF0000),
            onColorSelected = {}
        )
    }
}
