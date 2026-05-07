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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.fansauchiwa.R
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.ui.composable.SelectionCircleIcon
import com.fansauchiwa.ui.modifier.fansaCombinedClickable
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Immutable
private data class ImagePageUiState(
    val items: List<ImageGridItemUiState>,
    val isDeletingImage: Boolean,
    val isPreview: Boolean
)

@Immutable
private data class ImageGridItemUiState(
    val id: String,
    val path: String,
    val isSelected: Boolean,
    val tapMode: ImageTapMode
)

private enum class ImageTapMode {
    OPEN_IMAGE,
    TOGGLE_SELECTION
}

private sealed interface ImagePageEvent {
    data object AddImageClicked : ImagePageEvent
    data object ImageLongPressed : ImagePageEvent

    data class ImageClicked(
        val imageId: String,
        val tapMode: ImageTapMode
    ) : ImagePageEvent
}

private class ImagePageEventDispatcher(
    private val onAddImageClick: () -> Unit,
    private val onImageSelected: (String) -> Unit,
    private val onImageLongPress: () -> Unit,
    private val onImageToggleSelection: (String) -> Unit
) {
    fun dispatch(event: ImagePageEvent) {
        when (event) {
            ImagePageEvent.AddImageClicked -> onAddImageClick()
            ImagePageEvent.ImageLongPressed -> onImageLongPress()
            is ImagePageEvent.ImageClicked -> dispatchImageClick(event)
        }
    }

    private fun dispatchImageClick(event: ImagePageEvent.ImageClicked) {
        when (event.tapMode) {
            ImageTapMode.OPEN_IMAGE -> onImageSelected(event.imageId)
            ImageTapMode.TOGGLE_SELECTION -> onImageToggleSelection(event.imageId)
        }
    }
}

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
    val uiState = images.toImagePageUiState(
        selectedImages = selectedImages,
        isDeletingImage = isDeletingImage,
        isPreview = isPreview
    )
    val eventDispatcher = ImagePageEventDispatcher(
        onAddImageClick = onAddImageClick,
        onImageSelected = onImageSelected,
        onImageLongPress = onImageLongPress,
        onImageToggleSelection = onImageToggleSelection
    )

    ImagePageContent(
        state = uiState,
        onEvent = eventDispatcher::dispatch
    )
}

private fun List<ImageReference>.toImagePageUiState(
    selectedImages: List<String>,
    isDeletingImage: Boolean,
    isPreview: Boolean
): ImagePageUiState {
    val selectedImageIds = selectedImages.toSet()
    val tapMode = if (isDeletingImage) {
        ImageTapMode.TOGGLE_SELECTION
    } else {
        ImageTapMode.OPEN_IMAGE
    }

    return ImagePageUiState(
        items = map { image ->
            ImageGridItemUiState(
                id = image.id,
                path = image.path,
                isSelected = image.id in selectedImageIds,
                tapMode = tapMode
            )
        },
        isDeletingImage = isDeletingImage,
        isPreview = isPreview
    )
}

@Composable
private fun ImagePageContent(
    state: ImagePageUiState,
    onEvent: (ImagePageEvent) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            AddImageButton(
                onClick = { onEvent(ImagePageEvent.AddImageClicked) }
            )
        }
        items(state.items) { item ->
            ImageGridItem(
                item = item,
                isDeletingImage = state.isDeletingImage,
                isPreview = state.isPreview,
                onEvent = onEvent
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
            contentDescription = stringResource(R.string.add),
        )
    }
}

@Composable
private fun ImageGridItem(
    item: ImageGridItemUiState,
    isDeletingImage: Boolean,
    isPreview: Boolean,
    onEvent: (ImagePageEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f)
    ) {
        if (isPreview) {
            PreviewImageContent(
                item = item,
                onEvent = onEvent
            )
        } else {
            AsyncImage(
                model = item.path,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .imageItemClickable(
                        item = item,
                        onEvent = onEvent
                    )
            )
        }
        if (isDeletingImage) {
            SelectionCircleIcon(
                isSelected = item.isSelected,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

private fun Modifier.imageItemClickable(
    item: ImageGridItemUiState,
    onEvent: (ImagePageEvent) -> Unit
): Modifier {
    return fansaCombinedClickable(
        onClick = {
            onEvent(
                ImagePageEvent.ImageClicked(
                    imageId = item.id,
                    tapMode = item.tapMode
                )
            )
        },
        onLongClick = {
            onEvent(ImagePageEvent.ImageLongPressed)
        }
    )
}

@Composable
private fun PreviewImageContent(
    item: ImageGridItemUiState,
    onEvent: (ImagePageEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(R.color.gray))
            .imageItemClickable(
                item = item,
                onEvent = onEvent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.layer_image),
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
