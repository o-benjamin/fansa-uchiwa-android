package com.fansauchiwa.data

import androidx.annotation.DrawableRes

data class Template(
    val id: String,
    @DrawableRes val previewImageResId: Int,
    val savedUchiwa: SavedUchiwa
)

