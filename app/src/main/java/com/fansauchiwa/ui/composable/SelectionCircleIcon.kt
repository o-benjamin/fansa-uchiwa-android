package com.fansauchiwa.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Composable
fun SelectionCircleIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = if (isSelected) {
            Icons.Filled.CheckCircle
        } else {
            Icons.Outlined.Circle
        },
        contentDescription = if (isSelected) "Selected" else "Not selected",
        tint = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            colorResource(R.color.white)
        },
        modifier = modifier
            .padding(4.dp)
            .then(
                if (isSelected) {
                    Modifier.background(colorResource(R.color.white), CircleShape)
                } else {
                    Modifier
                }
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectionCircleIconPreview_Selected() {
    FansaUchiwaTheme {
        SelectionCircleIcon(isSelected = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionCircleIconPreview_NotSelected() {
    FansaUchiwaTheme {
        SelectionCircleIcon(isSelected = false)
    }
}

