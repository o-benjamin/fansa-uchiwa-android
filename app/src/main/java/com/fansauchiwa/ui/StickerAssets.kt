package com.fansauchiwa.ui

import androidx.annotation.DrawableRes
import com.fansauchiwa.R

/**
 * 貼り付けられるステッカー。宣言順がステッカー選択画面の表示順と「1位〜5位」のバッジになる。
 * 並びは GA4 の `select_edit_sticker` の選択回数の多い順（#300、2026-09-19〜10-16 の28日間）。
 * v2.9.0（10-05 公開）で足した18個（`licenses/materials.md` の「追加」が v2.9.0 のもの）は、28日のうち公開後の12日分の回数で並んでいる。
 * 12日分だけで並べても上位5個（バッジが付くもの）は変わらないので、そのままにした。次の並べ替えで28日分そろう。
 * `isNew = true` のステッカーは先頭に置き、順位の対象から外している。今の NEW はない（v2.9.0 の18個は #300 で外した）。
 * 並べ替えの手順と `isNew` の運用ルールは `.agents/sticker-font-order.md` を参照。
 * 出どころとライセンスは `licenses/materials.md` の「ステッカー」節。
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
    STAR("star", R.drawable.baseline_star_24),
    AUDIO_TRACK("audio_track", R.drawable.round_audiotrack_24),
    PETS("pets", R.drawable.round_pets_24),
    CHESS_QUEEN("chess_queen", R.drawable.chess_queen_24px),
    WAVING_HAND("waving_hand", R.drawable.baseline_waving_hand_24),
    EMPHASIS_MARKS("emphasis_marks", R.drawable.sticker_emphasis_marks),
    SHOOTING_STAR("shooting_star", R.drawable.sticker_shooting_star),
    THUMB_UP("thumb_up", R.drawable.round_thumb_up_24),
    WINGED_HEART("winged_heart", R.drawable.sticker_winged_heart),
    BRIGHTNESS_1("brightness_1", R.drawable.baseline_brightness_1_24),
    MUSIC_NOTES("music_notes", R.drawable.sticker_music_notes),
    HAND_PEACE("hand_peace", R.drawable.sticker_hand_peace),
    BOLT("bolt", R.drawable.round_bolt_24),
    EYEGLASSES("eyeglasses", R.drawable.rounded_eyeglasses_2_24),
    CAKE("cake", R.drawable.round_cake_24),
    CROWN_SIMPLE("crown_simple", R.drawable.sticker_crown_simple),
    LOCAL_FIRE_DEPARTMENT("local_fire_department", R.drawable.round_local_fire_department_24),
    FLOWER("flower", R.drawable.sticker_flower),
    HEART_ARROW("heart_arrow", R.drawable.sticker_heart_arrow),
    RIBBON("ribbon", R.drawable.sticker_ribbon),
    MICROPHONE_STAGE("microphone_stage", R.drawable.sticker_microphone_stage),
    DIAMOND("diamond", R.drawable.sticker_diamond),
    ROCKET_LAUNCH("rocket_launch", R.drawable.baseline_rocket_launch_24),
    CELEBRATION("celebration", R.drawable.sticker_celebration),
    KISS_LIPS("kiss_lips", R.drawable.sticker_kiss_lips),
    ROCKET("rocket", R.drawable.baseline_rocket_24),
    BRIGHTNESS_2("brightness_2", R.drawable.baseline_brightness_2_24),
    BRIGHTNESS_3("brightness_3", R.drawable.baseline_brightness_3_24),
    CHESS("chess", R.drawable.chess_24px),
    SPEECH_BUBBLE("speech_bubble", R.drawable.sticker_speech_bubble),
    GIFT("gift", R.drawable.sticker_gift),
    BURST_BUBBLE("burst_bubble", R.drawable.sticker_burst_bubble),
    BALLOON("balloon", R.drawable.sticker_balloon),
    CURVED_ARROW("curved_arrow", R.drawable.sticker_curved_arrow),
}