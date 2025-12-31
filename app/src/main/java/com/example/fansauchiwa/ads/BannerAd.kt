package com.example.fansauchiwa.ads

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.admanager.AdManagerAdView

/**
 * A composable function to display an Ad Manager banner advertisement.
 *
 * @param adView The banner [AdManagerAdView].
 * @param modifier The modifier to apply to the banner ad.
 */
@Composable
fun BannerAd(context: Context, modifier: Modifier = Modifier) {
    val adView = AdView(context)
    adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"

    val deviceWidth = LocalConfiguration.current.screenWidthDp
    val paddingHorizontal = 8
    val adWidth = deviceWidth - (paddingHorizontal * 2)
    val adSize =
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    adView.setAdSize(adSize)

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    // Ad load does not work in preview mode because it requires a network connection.
    if (LocalInspectionMode.current) {
        Box { Text(text = "Google Mobile Ads preview banner.", modifier.align(Alignment.Center)) }
        return
    }

    AndroidView(
        modifier = modifier
            .wrapContentSize()
            .padding(paddingHorizontal.dp), factory = { adView })

    // Pause and resume the AdView when the lifecycle is paused and resumed.
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
}
