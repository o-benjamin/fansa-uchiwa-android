package com.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.fansauchiwa.R

enum class StickerAsset(
    val type: String,
    @DrawableRes val resId: Int
) {
    HEART("heart", R.drawable.sticker_heart),
    AUTO_AWESOME("auto_awesome", R.drawable.round_auto_awesome_24),
    AUDIO_TRACK("audio_track", R.drawable.round_audiotrack_24),
    STAR("star", R.drawable.baseline_star_24),
    STAR_ROUNDED("star_rounded", R.drawable.round_star_24),
    BRIGHTNESS_1("brightness_1", R.drawable.baseline_brightness_1_24),
    BRIGHTNESS_2("brightness_2", R.drawable.baseline_brightness_2_24),
    BRIGHTNESS_3("brightness_3", R.drawable.baseline_brightness_3_24),
    BOLT("bolt", R.drawable.round_bolt_24),
    PETS("pets", R.drawable.round_pets_24),
    THUMB_UP("thumb_up", R.drawable.round_thumb_up_24),
    CAKE("cake", R.drawable.round_cake_24),
    LOCAL_FIRE_DEPARTMENT("local_fire_department", R.drawable.round_local_fire_department_24),
}