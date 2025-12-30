package com.example.fansauchiwa.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.example.fansauchiwa.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onImageClick: (String) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        HomeScreenContent(
            modifier = Modifier.padding(innerPadding),
            masterpiecePathList = uiState.masterpiecePathList,
            onImageClick = { path ->
                val uchiwaId = viewModel.extractUchiwaId(path)
                onImageClick(uchiwaId)
            }
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    masterpiecePathList: List<String>,
    onImageClick: (String) -> Unit
) {
    if (masterpiecePathList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "保存されたうちわがありません")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(masterpiecePathList) { path ->
                MasterpieceItem(
                    imagePath = path,
                    onClick = { onImageClick(path) }
                )
            }
        }
    }
}

@Composable
private fun MasterpieceItem(
    imagePath: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .addLastModifiedToFileCacheKey(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

