package com.example.fansauchiwa

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fansauchiwa.edit.EditScreen
import com.example.fansauchiwa.edit.EditViewModel
import com.example.fansauchiwa.home.HomeScreen
import com.example.fansauchiwa.preview.IMAGE_PATH_ARG
import com.example.fansauchiwa.preview.UchiwaPreviewScreen
import com.example.fansauchiwa.preview.UchiwaPreviewViewModel

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
                onImageClick = { id ->
                    navController.navigate("${FansaUchiwaScreens.EDIT_SCREEN}?$UCHIWA_ID_ARG=$id")
                },
                onAddClick = {
                    navController.navigate(FansaUchiwaScreens.EDIT_SCREEN)
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
    }
}