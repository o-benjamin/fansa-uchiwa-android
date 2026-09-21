package com.fansauchiwa.data.analytics

import com.fansauchiwa.edit.FontFamilies

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

/**
 * 最終的に選ばれたフォントの表示順位（1始まり）を GA4 のバケット文字列に変換する（#242）。
 *
 * 順位はフォント選択画面の表示順（[FontFamilies] の宣言順）を使う。
 * 実使用データ順への並べ替え（#241）は本Issueの時点では未実装で、
 * v2.7.0.md によれば手書きの表示順は実使用データ順とほぼ一致しているため、
 * 現状の表示順をそのまま「上位/下位」の基準として使う。
 */
fun finalFontRankBucket(font: FontFamilies): String {
    val rank = font.ordinal + 1
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
