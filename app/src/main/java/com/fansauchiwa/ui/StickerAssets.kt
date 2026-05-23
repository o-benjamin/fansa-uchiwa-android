package com.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.fansauchiwa.R

enum class StickerAsset(
    val type: String,
    @DrawableRes val resId: Int,
    val isNew: Boolean = false
) {
    CROWN("crown", R.drawable.crown_24px, isNew = true),
    CHESS("chess", R.drawable.chess_24px, isNew = true),
    CHESS_QUEEN("chess_queen", R.drawable.chess_queen_24px, isNew = true),
    WAVING_HAND("waving_hand", R.drawable.baseline_waving_hand_24, isNew = true),
    ROCKET("rocket", R.drawable.baseline_rocket_24, isNew = true),
    ROCKET_LAUNCH("rocket_launch", R.drawable.baseline_rocket_launch_24, isNew = true),
    EYEGLASSES("eyeglasses", R.drawable.rounded_eyeglasses_2_24, isNew = true),
    HEART_CUTE("heart_cute", R.drawable.sticker_heart_cute),
    AUTO_AWESOME("auto_awesome", R.drawable.round_auto_awesome_24),
    HEART("heart", R.drawable.sticker_heart),
    HEART_HORIZONTAL("heart_horizontal", R.drawable.sticker_heart_horizontal),
    HEART_VERTICAL("heart_vertical", R.drawable.sticker_heart_vertical),
    STAR_ROUNDED("star_rounded", R.drawable.round_star_24),
    STAR("star", R.drawable.baseline_star_24),
    AUDIO_TRACK("audio_track", R.drawable.round_audiotrack_24),
    THUMB_UP("thumb_up", R.drawable.round_thumb_up_24),
    PETS("pets", R.drawable.round_pets_24),
    BOLT("bolt", R.drawable.round_bolt_24),
    BRIGHTNESS_1("brightness_1", R.drawable.baseline_brightness_1_24),
    CAKE("cake", R.drawable.round_cake_24),
    LOCAL_FIRE_DEPARTMENT("local_fire_department", R.drawable.round_local_fire_department_24),
    BRIGHTNESS_3("brightness_3", R.drawable.baseline_brightness_3_24),
    BRIGHTNESS_2("brightness_2", R.drawable.baseline_brightness_2_24),
}