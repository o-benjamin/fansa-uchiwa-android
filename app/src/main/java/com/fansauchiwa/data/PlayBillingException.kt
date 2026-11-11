package com.fansauchiwa.data

/**
 * Play の課金がエラーを返したときの例外。[responseCode] は `BillingClient.BillingResponseCode` の値
 */
class PlayBillingException(val responseCode: Int, debugMessage: String) :
    Exception("Play の課金がエラーを返した: $responseCode $debugMessage")
