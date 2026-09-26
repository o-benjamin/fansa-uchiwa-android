package com.fansauchiwa.analytics

import com.google.android.gms.ads.AdValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AdPaidEventFactoryTest {

    private fun create(
        valueMicros: Long = 1_500L,
        currencyCode: String = "JPY",
        precisionType: Int = AdValue.PrecisionType.PRECISE,
        adSource: String? = "AdMob Network"
    ) = AdPaidEventFactory.create(
        valueMicros = valueMicros,
        currencyCode = currencyCode,
        precisionType = precisionType,
        adFormat = AdFormat.REWARDED,
        placement = "preview_screen",
        adSource = adSource
    )

    @Test
    fun create_NormalValue_UsesAdPaidEventName() {
        assertEquals(AnalyticsActions.AD_PAID_EVENT, create().name)
    }

    @Test
    fun create_NormalValue_ConvertsMicrosToCurrencyUnit() {
        assertEquals(0.0015, create(valueMicros = 1_500L).params[AdPaidEventFactory.PARAM_VALUE])
    }

    @Test
    fun create_ZeroValue_ReturnsZero() {
        assertEquals(0.0, create(valueMicros = 0L).params[AdPaidEventFactory.PARAM_VALUE])
    }

    @Test
    fun create_NormalValue_IncludesCurrencyFormatAndPlacement() {
        val params = create().params
        assertEquals("JPY", params[AdPaidEventFactory.PARAM_CURRENCY])
        assertEquals(AdFormat.REWARDED, params[AdPaidEventFactory.PARAM_AD_FORMAT])
        assertEquals("preview_screen", params[AdPaidEventFactory.PARAM_PLACEMENT])
        assertEquals("AdMob Network", params[AdPaidEventFactory.PARAM_AD_SOURCE])
    }

    @Test
    fun create_EachPrecisionType_MapsToName() {
        mapOf(
            AdValue.PrecisionType.UNKNOWN to AdPaidEventFactory.PRECISION_UNKNOWN,
            AdValue.PrecisionType.ESTIMATED to AdPaidEventFactory.PRECISION_ESTIMATED,
            AdValue.PrecisionType.PUBLISHER_PROVIDED to AdPaidEventFactory.PRECISION_PUBLISHER_PROVIDED,
            AdValue.PrecisionType.PRECISE to AdPaidEventFactory.PRECISION_PRECISE
        ).forEach { (type, name) ->
            assertEquals(name, create(precisionType = type).params[AdPaidEventFactory.PARAM_PRECISION])
        }
    }

    @Test
    fun create_UndefinedPrecisionType_ReturnsUnknown() {
        assertEquals(
            AdPaidEventFactory.PRECISION_UNKNOWN,
            create(precisionType = -1).params[AdPaidEventFactory.PARAM_PRECISION]
        )
    }

    @Test
    fun create_NullAdSource_OmitsAdSourceParam() {
        assertFalse(create(adSource = null).params.containsKey(AdPaidEventFactory.PARAM_AD_SOURCE))
    }

    @Test
    fun create_BlankAdSource_OmitsAdSourceParam() {
        assertFalse(create(adSource = "").params.containsKey(AdPaidEventFactory.PARAM_AD_SOURCE))
    }
}
