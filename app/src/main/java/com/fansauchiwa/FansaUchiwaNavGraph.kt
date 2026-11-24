package com.fansauchiwa

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fansauchiwa.edit.EditScreen
import com.fansauchiwa.edit.EditViewModel
import com.fansauchiwa.edit.aimaterial.AiMaterialScreen
import com.fansauchiwa.edit.aimaterial.AiMaterialViewModel
import com.fansauchiwa.edit.imagepreview.ImagePreviewScreen
import com.fansauchiwa.edit.imagepreview.ImagePreviewViewModel
import com.fansauchiwa.home.HomeScreen
import com.fansauchiwa.inappmessage.InAppMessageHost
import com.fansauchiwa.inappmessage.InAppMessageViewModel
import com.fansauchiwa.preview.UchiwaPreviewScreen
import com.fansauchiwa.preview.UchiwaPreviewViewModel
import com.fansauchiwa.settings.SettingsScreen
import com.fansauchiwa.timeline.EventTimelineScreen

@Composable
fun FansaUchiwaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = HomeDestination.route
) {
    // アプリ内メッセージ（#428）は Activity に1つ。ホームが今の画面のときだけ出す
    val inAppMessageViewModel: InAppMessageViewModel = hiltViewModel()
    val inAppMessageUiState by inAppMessageViewModel.uiState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    InAppMessageHost(
        isHomeShown = currentBackStackEntry?.let { it.destination.route == HomeDestination.route },
        viewModel = inAppMessageViewModel
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(HomeDestination.route) {
            HomeScreen(
                onImageClick = { inputArg ->
                    navController.navigate(
                        EditRoute(inputArg = inputArg.toRouteArgument())
                    )
                },
                onAddClick = {
                    navController.navigate(EditRoute())
                },
                onAiMaterialEntryClick = { inputArg ->
                    // AI素材の画面は作った素材を下の編集画面に置くので、先に編集画面を積んでからその上に開く。
                    // 素材を置かずに戻ると、その編集画面に戻る
                    navController.navigate(EditRoute(inputArg = inputArg.toRouteArgument()))
                    navController.navigate(AiMaterialDestination.route)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsDestination.route)
                },
                onNavigateToTimeline = {
                    navController.navigate(EventTimelineDestination.screen)
                },
                inAppMessageDestination = inAppMessageUiState.pendingDestination,
                onInAppMessageDestinationOpened = inAppMessageViewModel::onDestinationOpened
            )
        }
        composable<EditRoute> {
            val viewModel: EditViewModel = hiltViewModel()
            EditScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onPreview = { path ->
                    navController.navigate(PreviewDestination.createRoute(path))
                },
                onNavigateToImagePreview = { uri ->
                    navController.navigate(ImagePreviewDestination.createRoute(uri))
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsDestination.route)
                }
            )
        }
        composable(
            route = PreviewDestination.route,
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
                        route = HomeDestination.route,
                        inclusive = false
                    )
                },
                onNavigateToTimeline = { uchiwaId ->
                    navController.navigate(EventTimelineDestination.createRoute(uchiwaId))
                }
            )
        }
        composable(
            route = ImagePreviewDestination.route,
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
                navController.getBackStackEntry(EditDestination.route)
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
        composable(AiMaterialDestination.route) { backStackEntry ->
            val viewModel: AiMaterialViewModel = hiltViewModel()
            val editBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(EditDestination.route)
            }
            val editViewModel: EditViewModel = hiltViewModel(editBackStackEntry)

            AiMaterialScreen(
                onConfirm = { resultUri ->
                    editViewModel.handleImageResult(resultUri)
                    navController.popBackStack()
                },
                onBack = { navController.navigateUp() },
                viewModel = viewModel
            )
        }
        composable(SettingsDestination.route) {
            SettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }
        composable(
            route = EventTimelineDestination.route,
            arguments = listOf(
                navArgument(UCHIWA_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            EventTimelineScreen(
                onBack = { navController.navigateUp() }
            )
        }
    }
}
