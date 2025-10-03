package com.example.fansauchiwa

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fansauchiwa.Album.AlbumScreen
import com.example.fansauchiwa.edit.EditScreen
import com.example.fansauchiwa.home.HomeScreen

@Composable
fun FansaUchiwaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = FansaUchiwaDestinations.ALBUM
) {
    var selectedDestination by rememberSaveable { mutableIntStateOf(TabDestinations.HOME.ordinal) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != FansaUchiwaDestinations.EDIT) {
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
            if (currentRoute != FansaUchiwaDestinations.EDIT) {
                FloatingActionButton(onClick = { navController.navigate(FansaUchiwaDestinations.EDIT) }) {
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
                HomeScreen()
            }
            composable(FansaUchiwaDestinations.ALBUM) {
                AlbumScreen()
            }
            composable(FansaUchiwaDestinations.EDIT) {
                EditScreen()
            }
        }
    }
}