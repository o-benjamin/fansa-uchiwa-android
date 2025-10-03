package com.example.fansauchiwa

import com.example.fansauchiwa.FansaUchiwaScreens.HOME_SCREEN
import com.example.fansauchiwa.FansaUchiwaScreens.ALBUM_SCREEN
import com.example.fansauchiwa.FansaUchiwaScreens.EDIT_SCREEN

object FansaUchiwaScreens {
    const val HOME_SCREEN = "home"
    const val ALBUM_SCREEN = "album"
    const val EDIT_SCREEN = "edit"
}

object FansaUchiwaDestinations {
    const val HOME = HOME_SCREEN
    const val ALBUM = ALBUM_SCREEN
    const val EDIT = EDIT_SCREEN
}

enum class TabDestinations(val icon: Int, val route: String, val label: Int) {
    HOME(R.drawable.ic_home, FansaUchiwaDestinations.HOME, R.string.home),
    ALBUM(R.drawable.ic_album, FansaUchiwaDestinations.ALBUM, R.string.album)
}