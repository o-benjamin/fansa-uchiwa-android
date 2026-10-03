package com.fansauchiwa.analytics

import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.buildRankIndexMap

/**
 * フォント切り替え回数を GA4 のバケット文字列に変換する（#242）。
 * 境界は Issue #242 の判定基準（保存/破棄の完了率をバケット別に比べる）に合わせる。
 * 生の回数ではなくバケット文字列にするのは、GA4 のカスタムディメンションの基数を抑えるため。
 */
fun fontSwitchBucket(switchCount: Int): String = when {
    switchCount <= 0 -> "0"
    switchCount <= 2 -> "1-2"
    switchCount <= 5 -> "3-5"
    switchCount <= 10 -> "6-10"
    switchCount <= 20 -> "11-20"
    else -> "21+"
}

// フォント選択画面のバッジと同じ順位（NEW は null）
private val fontRankIndexMap = buildRankIndexMap(FontFamilies.entries) { it.isNew }

/**
 * 最終的に選ばれたフォントの順位（1始まり）を GA4 のバケット文字列に変換する（#242）。
 *
 * 順位はフォント選択画面の「1位〜5位」のバッジと同じく、NEW を除いた [FontFamilies] の宣言順を使う。
 * #241 で宣言順を実使用データ順に並べ替えたため、この順位と人気順はそろっている。
 * NEW のフォントは順位を持たないので "new" を返す。NEW は選択画面の先頭に並ぶため、
 * 表示位置で数えると既存のフォントの順位が NEW の数だけずれ、#242 の判定（上位5件への着地）が
 * NEW を足したリリースの前後で比べられなくなる（#286）。
 */
fun finalFontRankBucket(font: FontFamilies): String {
    val rankIndex = fontRankIndexMap[font] ?: return "new"
    val rank = rankIndex + 1
    return when {
        rank <= 5 -> "1-5"
        rank <= 10 -> "6-10"
        rank <= 20 -> "11-20"
        else -> "21+"
    }
}

/**
 * 編集にかけた時間（ミリ秒）を GA4 のバケット文字列に変換する（#242 追加仕様）。
 *
 * 編集画面を開いてから保存・離脱までの経過時間の実測データがまだ無いため、
 * fontSwitchBucket と同じ考え方（GA4 のカスタムディメンションの基数を抑える）で
 * 仮の境界を置いた。1分未満はほぼ迷わず保存/破棄した編集、10分以上は
 * 通話や離席を挟んだ可能性が高い外れ値として別枠にしている。
 * データが溜まったら実測分布を見て境界を見直す。
 */
fun editDurationBucket(elapsedMillis: Long): String {
    val elapsedMinutes = elapsedMillis / 60_000.0
    return when {
        elapsedMinutes < 1 -> "0-1m"
        elapsedMinutes < 3 -> "1-3m"
        elapsedMinutes < 5 -> "3-5m"
        elapsedMinutes < 10 -> "5-10m"
        else -> "10m+"
    }
}

/**
 * font_switch_bucket と edit_duration_bucket のペアを返す（#242）。
 * tap_preview_export と tap_edit_back_dialog（action=delete）の両方で共通して送るパラメータで、
 * EditViewModel と UchiwaPreviewViewModel の双方から呼ばれる（実装を2箇所に重複させないための共通化）。
 */
fun baseFontSessionParams(switchCount: Int, elapsedMillis: Long): Map<String, Any> = mapOf(
    FontSessionAnalyticsParams.FONT_SWITCH_BUCKET to fontSwitchBucket(switchCount),
    FontSessionAnalyticsParams.EDIT_DURATION_BUCKET to editDurationBucket(elapsedMillis)
)
