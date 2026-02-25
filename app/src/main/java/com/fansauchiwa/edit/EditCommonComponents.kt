package com.fansauchiwa.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.ui.DecorationColors
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Composable
fun HeaderTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun ColorPickerRow(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPickerDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
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
                .clickable {
                    showColorPickerDialog = true
                }
        )
        DecorationColors.entries.forEach { decorationColor ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, colorResource(R.color.gray), CircleShape)
                    .background(color = decorationColor.value)
                    .clickable {
                        onColorSelected(decorationColor.value)
                    }
            )
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

@Composable
fun ColorAndWeightControl(
    title: String,
    color: Color,
    width: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit = {},
    onWeightChanged: (Float) -> Unit = {},
) {
    val isColorPickerOpen = remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(top = 16.dp)) {
        HeaderTitle(title)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = {
                        isColorPickerOpen.value = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Color picker toggle"
                    )
                }
                this@Column.AnimatedVisibility(
                    visible = !isColorPickerOpen.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, colorResource(R.color.gray), CircleShape)
                            .background(color = color)
                            .clickable {
                                isColorPickerOpen.value = true
                            }
                    )
                }
            }
            Slider(
                value = width,
                onValueChange = { newValue ->
                    onWeightChanged(newValue)
                    isColorPickerOpen.value = false
                },
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(isColorPickerOpen.value) {
            ColorPickerRow(
                onColorSelected = onColorSelected,
                modifier = Modifier.padding(top = 8.dp),
                currentColor = color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderTitlePreview() {
    FansaUchiwaTheme {
        HeaderTitle(title = "サンプルタイトル")
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerRowPreview() {
    FansaUchiwaTheme {
        ColorPickerRow(
            onColorSelected = {},
            currentColor = Color(0xFFFF0000)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorAndWeightControlPreview() {
    FansaUchiwaTheme {
        ColorAndWeightControl(
            title = "枠線",
            color = Color(0xFFFF0000),
            width = 5f,
            valueRange = 1f..10f,
            steps = 8,
            onColorSelected = {},
            onWeightChanged = {}
        )
    }
}
