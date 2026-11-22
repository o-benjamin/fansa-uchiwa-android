package com.fansauchiwa.data

import com.android.billingclient.api.BillingClient.BillingResponseCode

/**
 * Play の課金がエラーを返したときの例外。[responseCode] は `BillingClient.BillingResponseCode` の値
 */
class PlayBillingException(val responseCode: Int, debugMessage: String) :
    Exception("Play の課金がエラーを返した: $responseCode $debugMessage") {

    /**
     * 端末・アカウント・通信の都合で起きたエラーか（Play ストアが使えない・通信できないなど）。不具合ではないので Crashlytics に残さない。
     * それ以外（アイテムが見つからない・呼び方の誤り・原因の分からない応答）は設定や実装の誤りなので残す
     */
    val isCausedByDeviceOrNetwork: Boolean
        get() = responseCode in DEVICE_OR_NETWORK_CODES

    private companion object {
        val DEVICE_OR_NETWORK_CODES = setOf(
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.SERVICE_DISCONNECTED,
            BillingResponseCode.NETWORK_ERROR
        )
    }
}
