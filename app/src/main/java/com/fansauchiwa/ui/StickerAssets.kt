package com.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.fansauchiwa.R

/**
 * 貼り付けられるステッカー。宣言順がステッカー選択画面の表示順と「1位〜5位」のバッジになる。
 * 並びは GA4 の `select_edit_sticker` の選択回数の多い順（#241、2026-08-23〜09-19 の28日間）。
 * `isNew = true` のステッカーは先頭に置き、順位の対象から外している。NEW の中の並びは採用を決めたときの順で、データの順ではない。
 * 今の NEW は v2.9.0 で追加した18個（#287）。外して並べ替える作業は #300。
 * 並べ替えの手順と `isNew` の運用ルールは `.agents/sticker-font-order.md` を参照。
 * 出どころとライセンスは `licenses/materials.md` の「ステッカー」節。
 */
enum class StickerAsset(
    val type: String,
    @DrawableRes val resId: Int,
    val isNew: Boolean = false
) {
    HAND_PEACE("hand_peace", R.drawable.sticker_hand_peace, isNew = true),
    MICROPHONE_STAGE("microphone_stage", R.drawable.sticker_microphone_stage, isNew = true),
    MUSIC_NOTES("music_notes", R.drawable.sticker_music_notes, isNew = true),
    CROWN_SIMPLE("crown_simple", R.drawable.sticker_crown_simple, isNew = true),
    DIAMOND("diamond", R.drawable.sticker_diamond, isNew = true),
    SHOOTING_STAR("shooting_star", R.drawable.sticker_shooting_star, isNew = true),
    FLOWER("flower", R.drawable.sticker_flower, isNew = true),
    BALLOON("balloon", R.drawable.sticker_balloon, isNew = true),
    GIFT("gift", R.drawable.sticker_gift, isNew = true),
    CELEBRATION("celebration", R.drawable.sticker_celebration, isNew = true),
    RIBBON("ribbon", R.drawable.sticker_ribbon, isNew = true),
    WINGED_HEART("winged_heart", R.drawable.sticker_winged_heart, isNew = true),
    HEART_ARROW("heart_arrow", R.drawable.sticker_heart_arrow, isNew = true),
    KISS_LIPS("kiss_lips", R.drawable.sticker_kiss_lips, isNew = true),
    EMPHASIS_MARKS("emphasis_marks", R.drawable.sticker_emphasis_marks, isNew = true),
    SPEECH_BUBBLE("speech_bubble", R.drawable.sticker_speech_bubble, isNew = true),
    BURST_BUBBLE("burst_bubble", R.drawable.sticker_burst_bubble, isNew = true),
    CURVED_ARROW("curved_arrow", R.drawable.sticker_curved_arrow, isNew = true),
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