package com.example.fansauchiwa

import com.example.fansauchiwa.FansaUchiwaScreens.ALBUM_SCREEN
import com.example.fansauchiwa.FansaUchiwaScreens.EDIT_SCREEN
import com.example.fansauchiwa.FansaUchiwaScreens.HOME_SCREEN
import com.example.fansauchiwa.FansaUchiwaScreens.PREVIEW_SCREEN

object FansaUchiwaScreens {
    const val HOME_SCREEN = "home"
    const val ALBUM_SCREEN = "album"
    const val EDIT_SCREEN = "edit"
    const val PREVIEW_SCREEN = "preview"
}

const val UCHIWA_ID_ARG = "uchiwaId"

object FansaUchiwaDestinations {
    const val HOME = HOME_SCREEN
    const val ALBUM = ALBUM_SCREEN
    const val EDIT = "$EDIT_SCREEN?$UCHIWA_ID_ARG={$UCHIWA_ID_ARG}"
    const val PREVIEW = PREVIEW_SCREEN
}
