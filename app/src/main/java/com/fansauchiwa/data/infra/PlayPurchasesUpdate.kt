package com.fansauchiwa.data.infra

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

/** Play の購入の画面を閉じたときに `PurchasesUpdatedListener` に届く結果 */
internal data class PlayPurchasesUpdate(val result: BillingResult, val purchases: List<Purchase>)
