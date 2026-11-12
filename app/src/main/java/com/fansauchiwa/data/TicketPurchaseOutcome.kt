package com.fansauchiwa.data

/**
 * 回数券の Play の購入の画面を閉じたあとの結果。失敗（Play やサーバーのエラー）はこの型にせず、例外で返す
 */
sealed interface TicketPurchaseOutcome {
    /** 支払いが済み、サーバーが回数を足した */
    data class Granted(val tickets: GrantedTickets) : TicketPurchaseOutcome

    /**
     * 支払いがまだ済んでいない（コンビニ払いなど）。回数はまだ足さない。
     * 済んだあとは、次に AI 素材の画面に来た・戻ったとき、または購入の案内を開いたときに `TicketPurchaseRepository.grantUnconsumedPurchases` が足す
     */
    data object Pending : TicketPurchaseOutcome

    /** 利用者が Play の購入の画面を閉じた */
    data object Cancelled : TicketPurchaseOutcome

    /**
     * 前の購入が Play に残っていて新しい購入を受け付けなかったが、その購入はもう足し済みだった
     * （サーバーが消費に失敗していた、Play の手元の情報が古いなど）。今回はお金も回数も動いていない。
     * サーバーが消費をやり直すので、時間をおくとまた買える
     */
    data class AlreadyGranted(val ticketsRemaining: Int) : TicketPurchaseOutcome
}
