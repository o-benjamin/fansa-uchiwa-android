package com.fansauchiwa.data.infra

import com.fansauchiwa.data.GrantedTickets

/**
 * 回数券の購入を確かめるサーバー（Cloud Functions の `verifyPurchase`。#348）
 */
interface TicketPurchaseDataSource {
    /**
     * 購入をサーバーで確かめて、回数を足してもらう。同じ購入を何度送っても、足すのは1回だけ。
     * サーバーが理由つきで断ったときは `TicketPurchaseRejectedException`、通信できないときなどはそのほかの例外を投げる
     */
    suspend fun verifyPurchase(purchaseToken: String): GrantedTickets
}
