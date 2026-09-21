package com.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.fansauchiwa.R

/**
 * 貼り付けられるステッカー。宣言順がステッカー選択画面の表示順と「1位〜5位」のバッジになる。
 * 並びは GA4 の `select_edit_sticker` の選択回数の多い順（#241、2026-08-23〜09-19 の28日間）。
 * 並べ替えの手順と `isNew` の運用ルールは `.agents/sticker-font-order.md` を参照。
 */
enum class StickerAsset(
    val type: String,
    @DrawableRes val resId: Int,
    val isNew: Boolean = false
) {
    HEART_CUTE("heart_cute", R.drawable.sticker_heart_cute),
    AUTO_AWESOME("auto_awesome", R.drawable.round_auto_awesome_24),
    STAR_ROUNDED("star_rounded", R.drawable.round_star_24),
    HEART("heart", R.drawable.sticker_heart),
    HEART_HORIZONTAL("heart_horizontal", R.drawable.sticker_heart_horizontal),
    CROWN("crown", R.drawable.crown_24px),
    PAN_TOOL_ALT("pan_tool_alt", R.drawable.baseline_pan_tool_alt_24),
    HEART_VERTICAL("heart_vertical", R.drawable.sticker_heart_vertical),
    AUDIO_TRACK("audio_track", R.drawable.round_audiotrack_24),
    CHESS_QUEEN("chess_queen", R.drawable.chess_queen_24px),
    WAVING_HAND("waving_hand", R.drawable.baseline_waving_hand_24),
    STAR("star", R.drawable.baseline_star_24),
    PETS("pets", R.drawable.round_pets_24),
    THUMB_UP("thumb_up", R.drawable.round_thumb_up_24),
    EYEGLASSES("eyeglasses", R.drawable.rounded_eyeglasses_2_24),
    BRIGHTNESS_1("brightness_1", R.drawable.baseline_brightness_1_24),
    BOLT("bolt", R.drawable.round_bolt_24),
    CAKE("cake", R.drawable.round_cake_24),
    ROCKET_LAUNCH("rocket_launch", R.drawable.baseline_rocket_launch_24),
    CHESS("chess", R.drawable.chess_24px),
    BRIGHTNESS_2("brightness_2", R.drawable.baseline_brightness_2_24),
    BRIGHTNESS_3("brightness_3", R.drawable.baseline_brightness_3_24),
    LOCAL_FIRE_DEPARTMENT("local_fire_department", R.drawable.round_local_fire_department_24),
    ROCKET("rocket", R.drawable.baseline_rocket_24),
}