package com.fansauchiwa

import com.fansauchiwa.FansaUchiwaScreens.EDIT_SCREEN
import com.fansauchiwa.FansaUchiwaScreens.HOME_SCREEN
import com.fansauchiwa.FansaUchiwaScreens.IMAGE_PREVIEW_SCREEN
import com.fansauchiwa.FansaUchiwaScreens.PREVIEW_SCREEN
import com.fansauchiwa.FansaUchiwaScreens.SETTINGS_SCREEN

object FansaUchiwaScreens {
    const val HOME_SCREEN = "home"
    const val EDIT_SCREEN = "edit"
    const val PREVIEW_SCREEN = "preview"
    const val IMAGE_PREVIEW_SCREEN = "image_preview"
    const val SETTINGS_SCREEN = "settings"
}

const val UCHIWA_ID_ARG = "uchiwaId"
const val TEMPLATE_ID_ARG = "templateId"
const val IMAGE_PATH_ARG = "imagePath"
const val IMAGE_URI_ARG = "imageUri"

object FansaUchiwaDestinations {
    const val HOME = HOME_SCREEN
    const val EDIT = "$EDIT_SCREEN?$UCHIWA_ID_ARG={$UCHIWA_ID_ARG}&$TEMPLATE_ID_ARG={$TEMPLATE_ID_ARG}"
    const val PREVIEW = "$PREVIEW_SCREEN/{$IMAGE_PATH_ARG}"
    const val IMAGE_PREVIEW = "$IMAGE_PREVIEW_SCREEN/{$IMAGE_URI_ARG}"
    const val SETTINGS = SETTINGS_SCREEN
}
