package com.example.fansauchiwa

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fansauchiwa.album.AlbumScreen
import com.example.fansauchiwa.edit.EditScreen
import com.example.fansauchiwa.home.HomeScreen

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
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle save action */ }) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                }
            )
        },
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