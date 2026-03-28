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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.theme.SelectionCircleIconPadding

@Composable
fun SelectionCircleIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
    val contentDescription = stringResource(
        if (isSelected) R.string.selection_circle_icon_selected
        else R.string.selection_circle_icon_not_selected
    )
    val tint = if (isSelected) MaterialTheme.colorScheme.primary else colorResource(R.color.white)
    val backgroundModifier = if (isSelected) {
        Modifier.background(colorResource(R.color.white), CircleShape)
    } else {
        Modifier
    }

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
            .padding(SelectionCircleIconPadding)
            .then(backgroundModifier),
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

