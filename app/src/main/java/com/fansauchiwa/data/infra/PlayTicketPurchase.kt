package com.fansauchiwa.data.infra

/**
 * Play に残っている回数券の購入。[isPending] は支払いがまだ済んでいない（コンビニ払いなど）こと
 */
data class PlayTicketPurchase(val purchaseToken: String, val isPending: Boolean)
