package com.example.fansauchiwa

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fansauchiwa.album.AlbumScreen
import com.example.fansauchiwa.edit.EditScreen
import com.example.fansauchiwa.edit.EditViewModel
import com.example.fansauchiwa.home.HomeScreen
import com.example.fansauchiwa.preview.IMAGE_PATH_ARG
import com.example.fansauchiwa.preview.UchiwaPreviewScreen
import com.example.fansauchiwa.preview.UchiwaPreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FansaUchiwaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = FansaUchiwaDestinations.ALBUM
) {
    var selectedDestination by rememberSaveable { mutableIntStateOf(TabDestinations.HOME.ordinal) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            // EditScreenは独自のTopAppBarを持つため、ここでは表示しない
            if (currentRoute != FansaUchiwaDestinations.EDIT) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        if (currentRoute == FansaUchiwaDestinations.PREVIEW) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute != FansaUchiwaDestinations.EDIT && currentRoute != FansaUchiwaDestinations.PREVIEW) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    TabDestinations.entries.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = selectedDestination == index,
                            onClick = {
                                navController.navigate(route = destination.route)
                                selectedDestination = index
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(destination.icon),
                                    contentDescription = stringResource(destination.label)
                                )
                            },
                            label = { Text(stringResource(destination.label)) },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute != FansaUchiwaDestinations.EDIT && currentRoute != FansaUchiwaDestinations.PREVIEW) {
                FloatingActionButton(onClick = { navController.navigate(FansaUchiwaScreens.EDIT_SCREEN) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add)
                    )
                }
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(contentPadding)
        ) {
            composable(FansaUchiwaDestinations.HOME) {
                HomeScreen(
                    onImageClick = { id ->
                        navController.navigate("${FansaUchiwaScreens.EDIT_SCREEN}?$UCHIWA_ID_ARG=$id")
                    }
                )
            }
            composable(FansaUchiwaDestinations.ALBUM) {
                AlbumScreen()
            }
            composable(
                route = FansaUchiwaDestinations.EDIT,
                arguments = listOf(
                    navArgument(UCHIWA_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                val viewModel: EditViewModel = hiltViewModel()
                EditScreen(
                    viewModel = viewModel,
                    onBack = { navController.navigateUp() },
                    onPreview = { path ->
                        navController.navigate("${FansaUchiwaDestinations.PREVIEW}/$path")
                    }
                )
            }
            composable(
                route = "${FansaUchiwaDestinations.PREVIEW}/{$IMAGE_PATH_ARG}",
                arguments = listOf(
                    navArgument(IMAGE_PATH_ARG) { type = NavType.StringType }
                )
            ) {
                val viewModel: UchiwaPreviewViewModel = hiltViewModel()
                UchiwaPreviewScreen(viewModel = viewModel)
            }
        }
    }
}