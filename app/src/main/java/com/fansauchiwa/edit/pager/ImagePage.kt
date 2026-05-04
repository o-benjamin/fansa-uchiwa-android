package com.fansauchiwa.edit.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.fansauchiwa.R
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.ui.composable.SelectionCircleIcon
import com.fansauchiwa.ui.modifier.fansaCombinedClickable
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Composable
fun ImagePage(
    onAddImageClick: () -> Unit,
    images: List<ImageReference>,
    onImageSelected: (String) -> Unit,
    onImageLongPress: () -> Unit,
    isDeletingImage: Boolean,
    selectedImages: List<String>,
    onImageToggleSelection: (String) -> Unit,
    isPreview: Boolean = false
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            AddImageButton(onClick = onAddImageClick)
        }
        items(images) { image ->
            val isSelected = selectedImages.contains(image.id)
            ImageGridItem(
                image = image,
                isSelected = isSelected,
                isDeletingImage = isDeletingImage,
                onImageSelected = onImageSelected,
                onImageLongPress = onImageLongPress,
                onImageToggleSelection = onImageToggleSelection,
                isPreview = isPreview
            )
        }
    }
}

@Composable
private fun AddImageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = IconButtonDefaults.filledIconButtonColors(),
        modifier = modifier.aspectRatio(1f)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
        )
    }
}

@Composable
private fun ImageGridItem(
    image: ImageReference,
    isSelected: Boolean,
    isDeletingImage: Boolean,
    onImageSelected: (String) -> Unit,
    onImageLongPress: () -> Unit,
    onImageToggleSelection: (String) -> Unit,
    isPreview: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f)
    ) {
        if (isPreview) {
            PreviewImageContent(
                image = image,
                isDeletingImage = isDeletingImage,
                onImageSelected = onImageSelected,
                onImageLongPress = onImageLongPress,
                onImageToggleSelection = onImageToggleSelection
            )
        } else {
            AsyncImage(
                model = image.path,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .fansaCombinedClickable(
                        onClick = {
                            if (isDeletingImage) {
                                onImageToggleSelection(image.id)
                            } else {
                                onImageSelected(image.id)
                            }
                        },
                        onLongClick = { onImageLongPress() }
                    )
            )
        }
        if (isDeletingImage) {
            SelectionCircleIcon(
                isSelected = isSelected,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun PreviewImageContent(
    image: ImageReference,
    isDeletingImage: Boolean,
    onImageSelected: (String) -> Unit,
    onImageLongPress: () -> Unit,
    onImageToggleSelection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(R.color.gray))
            .fansaCombinedClickable(
                onClick = {
                    if (isDeletingImage) {
                        onImageToggleSelection(image.id)
                    } else {
                        onImageSelected(image.id)
                    }
                },
                onLongClick = { onImageLongPress() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Image",
            color = colorResource(R.color.black),
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePagePreview() {
    FansaUchiwaTheme {
        ImagePage(
            onAddImageClick = {},
            images = listOf(
                ImageReference(
                    id = "image-1",
                    path = "path/to/image1.jpg"
                ),
                ImageReference(
                    id = "image-2",
                    path = "path/to/image2.jpg"
                ),
                ImageReference(
                    id = "image-3",
                    path = "path/to/image3.jpg"
                ),
                ImageReference(
                    id = "image-4",
                    path = "path/to/image4.jpg"
                ),
            ),
            onImageSelected = {},
            onImageLongPress = {},
            isDeletingImage = false,
            selectedImages = emptyList(),
            onImageToggleSelection = {},
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePagePreview_DeletingMode() {
    FansaUchiwaTheme {
        ImagePage(
            onAddImageClick = {},
            images = listOf(
                ImageReference(
                    id = "image-1",
                    path = "path/to/image1.jpg"
                ),
                ImageReference(
                    id = "image-2",
                    path = "path/to/image2.jpg"
                ),
            ),
            onImageSelected = {},
            onImageLongPress = {},
            isDeletingImage = true,
            selectedImages = listOf("image-1"),
            onImageToggleSelection = {},
            isPreview = true
        )
    }
}
