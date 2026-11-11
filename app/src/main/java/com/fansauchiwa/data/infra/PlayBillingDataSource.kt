package com.fansauchiwa.data.infra

import android.app.Activity
import com.fansauchiwa.data.TicketProduct

/**
 * Google Play の課金（Play Billing Library。#348）。扱うのは AI 素材の回数券のアイテムだけ。
 * Play がエラーを返したときは `PlayBillingException` を投げる
 */
interface PlayBillingDataSource {
    /** 回数券の値段を Play から受け取る。アイテムが見つからないとき（Play Console で作っていない・有効でないなど）は null */
    suspend fun queryTicketProduct(): TicketProduct?

    /** 回数券の購入の画面を出し、利用者が閉じたあとの結果を返す。アイテムが見つからないときは `PlayBillingException` */
    suspend fun purchaseTicket(activity: Activity): PlayPurchaseResult

    /**
     * 買ったのにまだ消費されていない回数券の購入。サーバーが回数を足して消費するので、ふつうは空。
     * 残っているのは、回数を足す前にアプリが落ちた・通信が切れた、支払いが済んでいない（コンビニ払いなど）、などのとき
     */
    suspend fun queryUnconsumedTicketPurchases(): List<PlayTicketPurchase>
}
