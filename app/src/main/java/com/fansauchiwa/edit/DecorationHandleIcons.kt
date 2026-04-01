package com.fansauchiwa.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.DecorationHandleIconPadding
import com.fansauchiwa.ui.theme.DecorationHandleSize
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

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
                (DecorationHandleSize / 2) * currentScale,
                (DecorationHandleSize / 2) * currentScale
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
                (DecorationHandleSize / 2) * currentScale,
                -((DecorationHandleSize / 2) * currentScale)
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
                -((DecorationHandleSize / 2) * currentScale),
                -((DecorationHandleSize / 2) * currentScale)
            )
    )
}

@Composable
internal fun TransformHandleIcon(
    modifier: Modifier
) {
    Icon(
        painter = painterResource(R.drawable.outline_arrows_outward_24),
        contentDescription = stringResource(R.string.decoration_handle_transform_content_description),
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(DecorationHandleSize)
            .padding(DecorationHandleIconPadding)
    )
}

@Composable
internal fun DeleteIcon(
    modifier: Modifier
) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = stringResource(R.string.decoration_handle_delete_content_description),
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(DecorationHandleSize)
            .padding(DecorationHandleIconPadding)
    )
}

@Composable
internal fun DuplicateIcon(
    modifier: Modifier
) {
    Icon(
        imageVector = Icons.Default.ContentCopy,
        contentDescription = stringResource(R.string.decoration_handle_duplicate_content_description),
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(DecorationHandleSize)
            .padding(DecorationHandleIconPadding)
    )
}

@Preview
@Composable
private fun PreviewTransformHandleIcon() {
    FansaUchiwaTheme {
        TransformHandleIcon(modifier = Modifier)
    }
}

@Preview
@Composable
private fun PreviewDeleteIcon() {
    FansaUchiwaTheme {
        DeleteIcon(modifier = Modifier)
    }
}

@Preview
@Composable
private fun PreviewDuplicateIcon() {
    FansaUchiwaTheme {
        DuplicateIcon(modifier = Modifier)
    }
}

@Preview
@Composable
private fun PreviewDecorationHandleIcons() {
    FansaUchiwaTheme {
        Box(modifier = Modifier.size(DecorationHandleSize * 3)) {
            DecorationHandleIcons(currentScale = 1f)
        }
    }
}

