package com.fansauchiwa

sealed interface FansaUchiwaNavigationDestination {
    val screen: String
    val route: String
}

object FansaUchiwaScreens {
    const val HOME_SCREEN = "home"
    const val EDIT_SCREEN = "edit"
    const val PREVIEW_SCREEN = "preview"
    const val IMAGE_PREVIEW_SCREEN = "image_preview"
    const val SETTINGS_SCREEN = "settings"
    const val TIMELINE_SCREEN = "timeline"
}

const val UCHIWA_ID_ARG = "uchiwaId"
const val TEMPLATE_ID_ARG = "templateId"
const val TEMPLATE_MAIN_COLOR_ARG = "templateMainColor"
const val IMAGE_PATH_ARG = "imagePath"
const val IMAGE_URI_ARG = "imageUri"

data object HomeDestination : FansaUchiwaNavigationDestination {
    override val screen: String = FansaUchiwaScreens.HOME_SCREEN
    override val route: String = screen
}

data object EditDestination : FansaUchiwaNavigationDestination {
    override val screen: String = FansaUchiwaScreens.EDIT_SCREEN
    override val route: String =
        "$screen?$UCHIWA_ID_ARG={$UCHIWA_ID_ARG}&$TEMPLATE_ID_ARG={$TEMPLATE_ID_ARG}&$TEMPLATE_MAIN_COLOR_ARG={$TEMPLATE_MAIN_COLOR_ARG}"

    fun createRoute(inputArg: EditScreenInputArg): String =
        buildQueryRoute(
            screen = screen,
            arguments = arrayOf(
                UCHIWA_ID_ARG to inputArg.uchiwaId,
                TEMPLATE_ID_ARG to inputArg.templateId,
                TEMPLATE_MAIN_COLOR_ARG to inputArg.templateMainColor
            )
        )
}

data object PreviewDestination : FansaUchiwaNavigationDestination {
    override val screen: String = FansaUchiwaScreens.PREVIEW_SCREEN
    override val route: String = "$screen/{$IMAGE_PATH_ARG}"

    fun createRoute(imagePath: String): String =
        buildPathRoute(screen = screen, argument = imagePath)
}

data object ImagePreviewDestination : FansaUchiwaNavigationDestination {
    override val screen: String = FansaUchiwaScreens.IMAGE_PREVIEW_SCREEN
    override val route: String = "$screen/{$IMAGE_URI_ARG}"

    fun createRoute(imageUri: String): String = buildPathRoute(screen = screen, argument = imageUri)
}

data object SettingsDestination : FansaUchiwaNavigationDestination {
    override val screen: String = FansaUchiwaScreens.SETTINGS_SCREEN
    override val route: String = screen
}

data object EventTimelineDestination : FansaUchiwaNavigationDestination {
    override val screen: String = FansaUchiwaScreens.TIMELINE_SCREEN
    override val route: String = "$screen?$UCHIWA_ID_ARG={$UCHIWA_ID_ARG}"

    fun createRoute(uchiwaId: String?): String =
        buildQueryRoute(
            screen = screen,
            arguments = arrayOf(UCHIWA_ID_ARG to uchiwaId)
        )
}

private fun buildPathRoute(screen: String, argument: String): String = "$screen/$argument"

private fun buildQueryRoute(screen: String, arguments: Array<Pair<String, Any?>>): String {
    val query = arguments.mapNotNull { (key, value) ->
        value?.let { "$key=${serializeRouteArgumentValue(it)}" }
    }
    return if (query.isEmpty()) {
        screen
    } else {
        "$screen?${query.joinToString("&")}"
    }
}

private fun serializeRouteArgumentValue(value: Any): String = when (value) {
    is Enum<*> -> value.name
    else -> value.toString()
}
