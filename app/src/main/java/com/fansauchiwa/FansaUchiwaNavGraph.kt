package com.fansauchiwa

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
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
    val navigator = rememberFansaUchiwaNavigator(navController)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        homeDestination(navigator)
        editDestination(navigator)
        previewDestination(navigator)
        imagePreviewDestination(
            navController = navController,
            navigator = navigator
        )
        settingsDestination(navigator)
    }
}

@Composable
private fun rememberFansaUchiwaNavigator(
    navController: NavHostController
): FansaUchiwaNavigator {
    return remember(navController) {
        FansaUchiwaNavigator(navController)
    }
}

private fun NavGraphBuilder.homeDestination(
    navigator: FansaUchiwaNavigator
) {
    composable(FansaUchiwaDestinations.HOME) {
        HomeScreen(
            onImageClick = { id, templateId ->
                navigator.openEdit(
                    EditNavigationRequest(
                        uchiwaId = id,
                        templateId = templateId
                    )
                )
            },
            onAddClick = navigator::openNewEdit,
            onNavigateToSettings = navigator::openSettings
        )
    }
}

private fun NavGraphBuilder.editDestination(
    navigator: FansaUchiwaNavigator
) {
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
            onBack = navigator::navigateUp,
            onPreview = { path ->
                navigator.openPreview(
                    PreviewNavigationRequest(imagePath = path)
                )
            },
            onNavigateToImagePreview = { uri ->
                navigator.openImagePreview(
                    ImagePreviewNavigationRequest(imageUri = uri)
                )
            },
            onNavigateToSettings = navigator::openSettings
        )
    }
}

private fun NavGraphBuilder.previewDestination(
    navigator: FansaUchiwaNavigator
) {
    composable(
        route = FansaUchiwaDestinations.PREVIEW,
        arguments = listOf(
            navArgument(IMAGE_PATH_ARG) { type = NavType.StringType }
        )
    ) {
        val viewModel: UchiwaPreviewViewModel = hiltViewModel()
        UchiwaPreviewScreen(
            viewModel = viewModel,
            onBack = navigator::navigateUp,
            onBackToHome = navigator::returnToHome
        )
    }
}

private fun NavGraphBuilder.imagePreviewDestination(
    navController: NavHostController,
    navigator: FansaUchiwaNavigator
) {
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
        val onConfirm = rememberImagePreviewConfirmHandler(
            navController = navController,
            backStackEntry = backStackEntry,
            navigator = navigator
        )

        ImagePreviewScreen(
            onConfirm = onConfirm,
            onBack = navigator::navigateUp,
            viewModel = viewModel
        )
    }
}

private fun NavGraphBuilder.settingsDestination(
    navigator: FansaUchiwaNavigator
) {
    composable(FansaUchiwaDestinations.SETTINGS) {
        SettingsScreen(
            onBack = navigator::navigateUp
        )
    }
}

@Composable
private fun rememberImagePreviewConfirmHandler(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    navigator: FansaUchiwaNavigator
): (String) -> Unit {
    val editBackStackEntry = remember(backStackEntry) {
        navController.getBackStackEntry(FansaUchiwaDestinations.EDIT)
    }
    val editViewModel: EditViewModel = hiltViewModel(editBackStackEntry)

    return remember(editViewModel, navigator) {
        { resultUri ->
            editViewModel.handleImageResult(resultUri)
            navigator.popBackStack()
        }
    }
}

private class FansaUchiwaNavigator(
    private val navController: NavHostController
) {
    fun openEdit(request: EditNavigationRequest) {
        navController.navigate(request.route)
    }

    fun openNewEdit() {
        navController.navigate(FansaUchiwaScreens.EDIT_SCREEN)
    }

    fun openPreview(request: PreviewNavigationRequest) {
        navController.navigate(request.route)
    }

    fun openImagePreview(request: ImagePreviewNavigationRequest) {
        navController.navigate(request.route)
    }

    fun openSettings() {
        navController.navigate(FansaUchiwaDestinations.SETTINGS)
    }

    fun navigateUp() {
        navController.navigateUp()
    }

    fun popBackStack() {
        navController.popBackStack()
    }

    fun returnToHome() {
        navController.popBackStack(
            route = FansaUchiwaDestinations.HOME,
            inclusive = false
        )
    }
}

private data class EditNavigationRequest(
    val uchiwaId: String? = null,
    val templateId: String? = null
) {
    val route: String
        get() {
            val queryParameters = buildList {
                uchiwaId?.let { add("$UCHIWA_ID_ARG=$it") }
                templateId?.let { add("$TEMPLATE_ID_ARG=$it") }
            }

            return if (queryParameters.isEmpty()) {
                FansaUchiwaScreens.EDIT_SCREEN
            } else {
                "${FansaUchiwaScreens.EDIT_SCREEN}?${queryParameters.joinToString("&")}"
            }
        }
}

private data class PreviewNavigationRequest(
    val imagePath: String
) {
    val route: String = "${FansaUchiwaScreens.PREVIEW_SCREEN}/$imagePath"
}

private data class ImagePreviewNavigationRequest(
    val imageUri: String
) {
    val route: String = "${FansaUchiwaScreens.IMAGE_PREVIEW_SCREEN}/$imageUri"
}
