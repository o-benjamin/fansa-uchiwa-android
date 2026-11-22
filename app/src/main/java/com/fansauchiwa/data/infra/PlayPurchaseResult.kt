package com.fansauchiwa.data.infra

/**
 * Play の購入の画面を閉じたあとの結果
 */
sealed interface PlayPurchaseResult {
    /** 支払いが済んだ。[purchaseToken] をサーバーに送って回数を足してもらう */
    data class Purchased(val purchaseToken: String) : PlayPurchaseResult

    /** 支払いがまだ済んでいない（コンビニ払いなど） */
    data object Pending : PlayPurchaseResult

    /** 利用者が閉じた */
    data object Cancelled : PlayPurchaseResult

    /** 前に買った回数券がまだ消費されていないので、Play が新しい購入を受け付けなかった（消費型のアイテムは1つずつしか持てない） */
    data object AlreadyOwned : PlayPurchaseResult
}
