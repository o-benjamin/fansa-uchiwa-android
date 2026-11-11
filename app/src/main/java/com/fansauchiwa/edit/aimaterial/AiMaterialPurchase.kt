package com.fansauchiwa.edit.aimaterial

import com.fansauchiwa.data.TicketProduct

/**
 * AI 素材の画面の中に出す、回数券の購入の案内（#348）。
 * 回数がない・無料の非常ブレーキで断られたとき（[AiMaterialGeneration.Rejected.offersPurchase]）に開き、
 * 文章を直しても閉じない。買えたら閉じる
 */
sealed interface AiMaterialPurchase {
    data object Closed : AiMaterialPurchase

    /** 足せていない購入の送り直しと、値段の取得をしている */
    data object Preparing : AiMaterialPurchase

    /** 値段が分かり、買える。[problem] は前に買おうとしたときの結果（閉じただけのときは null） */
    data class Ready(val product: TicketProduct, val problem: AiMaterialPurchaseProblem?) : AiMaterialPurchase

    /** Play の購入の画面を出している、またはサーバーが購入を確かめている */
    data class Purchasing(val product: TicketProduct) : AiMaterialPurchase

    /** 値段が取れず、今は買えない */
    data object Unavailable : AiMaterialPurchase
}

enum class AiMaterialPurchaseProblem {
    /** 支払いがまだ済んでいない（コンビニ払いなど）。済んだあとに画面を開き直すと足す */
    PENDING,

    /** サーバーが購入を確かめられず、回数を足すのを断った */
    REJECTED,

    /** Play の課金がエラーを返した（支払いはしていない） */
    PLAY_ERROR,

    /** 通信できないなど。支払いが済んでいれば、画面を開き直すと足す */
    FAILED
}
