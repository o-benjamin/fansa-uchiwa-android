package com.fansauchiwa.data

/**
 * 回数券の購入の画面を閉じたあとの結果。失敗（Play やサーバーのエラー）はこの型にせず、例外で返す
 */
sealed interface TicketPurchaseOutcome {
    /** 支払いが済み、サーバーが回数を足した */
    data class Granted(val tickets: GrantedTickets) : TicketPurchaseOutcome

    /**
     * 支払いがまだ済んでいない（コンビニ払いなど）。回数はまだ足さない。
     * 済んだあとは、次に購入の画面を開いたときに [com.fansauchiwa.data.repository.TicketPurchaseRepository.grantUnconsumedPurchases] が足す
     */
    data object Pending : TicketPurchaseOutcome

    /** 利用者が購入の画面を閉じた */
    data object Cancelled : TicketPurchaseOutcome
}
