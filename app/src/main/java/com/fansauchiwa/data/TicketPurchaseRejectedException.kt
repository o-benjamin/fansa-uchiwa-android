package com.fansauchiwa.data

/**
 * サーバーが理由つきで回数を足すのを断ったときの例外。理由のない失敗（通信できない・時間切れなど）はこの例外にしない
 */
class TicketPurchaseRejectedException(val reason: TicketPurchaseRejectReason) :
    Exception("回数券の購入の確認を断られた: ${reason.serverValue}")
