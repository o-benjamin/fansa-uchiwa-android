package com.fansauchiwa.edit.aimaterial

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TicketExpiryFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)

/**
 * 回数券の期限を「2027/05/12 15:00」の形にする（「…まで使えます」に入れる）
 *
 * サーバーの期限は日の終わりではなく、購入と同じ時刻で（`functions/src/jstDate.ts` の `addMonthsJst`）、
 * 期限の時刻より前だけ使える（`functions/src/quota.ts`）。日付だけだとその日の終わりまで使えると読まれるので、時刻まで出す。
 * 書式に秒を入れないので秒は切り捨てになり、出す時刻は実際の期限より遅くならない。
 *
 * @param zone 表示に使うタイムゾーン。呼ぶ側が画面を組み立てるたびに `ZoneId.systemDefault()` で取って渡す
 *   （取っておくと、旅行などで変えたあとに画面を出し直しても、前のタイムゾーンの時刻を出す）
 */
internal fun formatTicketExpiry(expiresAt: Instant, zone: ZoneId): String =
    TicketExpiryFormatter.format(expiresAt.atZone(zone))
