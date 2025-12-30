package com.example.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.example.fansauchiwa.R

enum class StickerAsset(
    val type: String,
    @DrawableRes val resId: Int
) {
    HEART("heart", R.drawable.sticker_heart),
    STAR("star", R.drawable.sticker_star),
    MUSIC("music", R.drawable.sticker_music),
    KING("king", R.drawable.sticker_king);
}