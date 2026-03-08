package com.fansauchiwa.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R

@Composable
internal fun BoxScope.DecorationHandleIcons(
    currentScale: Float
) {
    TransformHandleIcon(
        modifier = Modifier
            .graphicsLayer {
                scaleX = 1 / currentScale
                scaleY = 1 / currentScale
            }
            .align(Alignment.BottomEnd)
            .offset(
                (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale
            )
    )
    DeleteIcon(
        modifier = Modifier
            .graphicsLayer {
                scaleX = 1 / currentScale
                scaleY = 1 / currentScale
            }
            .align(Alignment.TopEnd)
            .offset(
                (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                -((GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale)
            )
    )
    DuplicateIcon(
        modifier = Modifier
            .graphicsLayer {
                scaleX = 1 / currentScale
                scaleY = 1 / currentScale
            }
            .align(Alignment.TopStart)
            .offset(
                -((GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale),
                -((GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale)
            )
    )
}

@Composable
internal fun TransformHandleIcon(
    modifier: Modifier
) {
    Icon(
        painter = painterResource(R.drawable.outline_arrows_outward_24),
        contentDescription = "Zoom and Rotate",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .padding(4.dp)
    )
}

@Composable
internal fun DeleteIcon(
    modifier: Modifier
) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Delete",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .padding(4.dp)
    )
}

@Composable
internal fun DuplicateIcon(
    modifier: Modifier
) {
    Icon(
        imageVector = Icons.Default.ContentCopy,
        contentDescription = "Duplicate",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .padding(4.dp)
    )
}

