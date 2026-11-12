package com.fansauchiwa.edit.aimaterial

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TicketExpiryFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)

/**
 * 回数券の期限を「2027/05/12 15:00」の形にする（「…まで使えます」に入れる）
 *
 * サーバーの期限は購入した時刻の6か月後の同じ時刻で、その時刻より前だけ使える（`functions/src/quota.ts`）。
 * 日付だけだと、その日の終わりまで使えると読まれるので、時刻まで出す。
 * 秒は切り捨てるので、出す時刻は実際の期限より遅くならない。
 *
 * @param zone 端末のタイムゾーン。旅行などで変わっても前のタイムゾーンの時刻を出さないよう、呼ぶたびに取る
 */
internal fun formatTicketExpiry(expiresAt: Instant, zone: ZoneId): String =
    TicketExpiryFormatter.format(expiresAt.atZone(zone))
