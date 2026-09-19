package com.fansauchiwa.ads

import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.google.android.gms.ads.AdValue

/**
 * 広告の収益（OnPaidEventListener で受け取る AdValue）を Analytics イベントに変換する
 *
 * GA4 のレポートで ad_format / placement / ad_source / precision を軸に使うには、
 * GA4 の管理画面でイベントスコープのカスタムディメンションとして登録する必要がある。
 */
object AdPaidEventFactory {
    private const val MICROS_PER_UNIT = 1_000_000.0

    const val PARAM_VALUE = "value"
    const val PARAM_CURRENCY = "currency"
    const val PARAM_PRECISION = "precision"
    const val PARAM_AD_FORMAT = "ad_format"
    const val PARAM_PLACEMENT = "placement"
    const val PARAM_AD_SOURCE = "ad_source"

    const val PRECISION_UNKNOWN = "unknown"
    const val PRECISION_ESTIMATED = "estimated"
    const val PRECISION_PUBLISHER_PROVIDED = "publisher_provided"
    const val PRECISION_PRECISE = "precise"

    /**
     * @param valueMicros 収益額（通貨単位の100万分の1）
     * @param currencyCode ISO 4217 の通貨コード
     * @param precisionType [AdValue.PrecisionType] の値
     * @param adFormat 広告フォーマット（[AdFormat]）
     * @param placement 広告の表示場所
     * @param adSource 広告を配信したネットワーク名（不明なら null）
     */
    fun create(
        valueMicros: Long,
        currencyCode: String,
        precisionType: Int,
        adFormat: String,
        placement: String,
        adSource: String?
    ): AnalyticsEvent = AnalyticsEvent(
        name = AnalyticsActions.AD_PAID_EVENT,
        params = buildMap {
            // GA4 のイベントの値（event value）として通貨付きで集計できるよう、value は通貨単位の Double、
            // currency を併せて送る。GA4 の広告収益の指標（ad_impression 由来）には含まれない
            put(PARAM_VALUE, valueMicros / MICROS_PER_UNIT)
            put(PARAM_CURRENCY, currencyCode)
            put(PARAM_PRECISION, precisionName(precisionType))
            put(PARAM_AD_FORMAT, adFormat)
            put(PARAM_PLACEMENT, placement)
            if (!adSource.isNullOrBlank()) put(PARAM_AD_SOURCE, adSource)
        }
    )

    private fun precisionName(precisionType: Int): String = when (precisionType) {
        AdValue.PrecisionType.ESTIMATED -> PRECISION_ESTIMATED
        AdValue.PrecisionType.PUBLISHER_PROVIDED -> PRECISION_PUBLISHER_PROVIDED
        AdValue.PrecisionType.PRECISE -> PRECISION_PRECISE
        else -> PRECISION_UNKNOWN
    }
}
