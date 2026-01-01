package com.example.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.example.fansauchiwa.R

enum class StickerAsset(
    val type: String,
    @DrawableRes val resId: Int
) {
    AUDIO_TRACK("audio_track", R.drawable.round_audiotrack_24),
    AUTO_AWESOME("auto_awesome", R.drawable.round_auto_awesome_24),
    BOLT("bolt", R.drawable.round_bolt_24),
    CAKE("cake", R.drawable.round_cake_24),
    LOCAL_FIRE_DEPARTMENT("local_fire_department", R.drawable.round_local_fire_department_24),
    PETS("pets", R.drawable.round_pets_24),
    THUMB_UP("thumb_up", R.drawable.round_thumb_up_24),

}