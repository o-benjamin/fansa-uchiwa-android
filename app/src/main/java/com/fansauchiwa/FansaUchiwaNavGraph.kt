package com.fansauchiwa

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fansauchiwa.edit.EditScreen
import com.fansauchiwa.edit.EditViewModel
import com.fansauchiwa.edit.imagepreview.ImagePreviewScreen
import com.fansauchiwa.edit.imagepreview.ImagePreviewViewModel
import com.fansauchiwa.home.HomeScreen
import com.fansauchiwa.preview.UchiwaPreviewScreen
import com.fansauchiwa.preview.UchiwaPreviewViewModel
import com.fansauchiwa.settings.SettingsScreen

@Composable
fun FansaUchiwaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = FansaUchiwaDestinations.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(FansaUchiwaDestinations.HOME) {
            HomeScreen(
                onImageClick = { id, templateId ->
                    val route = buildString {
                        append("${FansaUchiwaScreens.EDIT_SCREEN}?$UCHIWA_ID_ARG=$id")
                        if (templateId != null) {
                            append("&$TEMPLATE_ID_ARG=$templateId")
                        }
                    }
                    navController.navigate(route)
                },
                onAddClick = {
                    navController.navigate(FansaUchiwaScreens.EDIT_SCREEN)
                },
                onNavigateToSettings = {
                    navController.navigate(FansaUchiwaDestinations.SETTINGS)
                }
            )
        }
        composable(
            route = FansaUchiwaDestinations.EDIT,
            arguments = listOf(
                navArgument(UCHIWA_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(TEMPLATE_ID_ARG) {
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
                    navController.navigate("${FansaUchiwaScreens.PREVIEW_SCREEN}/$path")
                },
                onNavigateToImagePreview = { uri ->
                    navController.navigate("${FansaUchiwaScreens.IMAGE_PREVIEW_SCREEN}/$uri")
                },
                onNavigateToSettings = {
                    navController.navigate(FansaUchiwaDestinations.SETTINGS)
                }
            )
        }
        composable(
            route = FansaUchiwaDestinations.PREVIEW,
            arguments = listOf(
                navArgument(IMAGE_PATH_ARG) { type = NavType.StringType }
            )
        ) {
            val viewModel: UchiwaPreviewViewModel = hiltViewModel()
            UchiwaPreviewScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onBackToHome = {
                    navController.popBackStack(
                        route = FansaUchiwaDestinations.HOME,
                        inclusive = false
                    )
                }
            )
        }
        composable(
            route = FansaUchiwaDestinations.IMAGE_PREVIEW,
            arguments = listOf(
                navArgument(IMAGE_URI_ARG) { type = NavType.StringType }
            ),
            enterTransition = {
                slideInVertically(initialOffsetY = { it })
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { it })
            }
        ) { backStackEntry ->
            val viewModel: ImagePreviewViewModel = hiltViewModel()
            // EditScreenのバックスタックエントリからEditViewModelを取得
            // TODO: もっといいやり方があれば…
            val editBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(FansaUchiwaDestinations.EDIT)
            }
            val editViewModel: EditViewModel = hiltViewModel(editBackStackEntry)

            ImagePreviewScreen(
                onConfirm = { resultUri ->
                    // EditViewModelのメソッドを直接呼び出す
                    editViewModel.handleImageResult(resultUri)
                    navController.popBackStack()
                },
                onBack = { navController.navigateUp() },
                viewModel = viewModel
            )
        }
        composable(FansaUchiwaDestinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }
    }
}