package com.fansauchiwa

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(FansaUchiwaScreens.EDIT_SCREEN)
data class EditRoute(
    val inputArg: String? = null
)
