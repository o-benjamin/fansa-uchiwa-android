package com.example.fansauchiwa.ads

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.fansauchiwa.BuildConfig
import com.example.fansauchiwa.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * A composable function to display an Ad Manager banner advertisement.
 *
 * @param context The context to use for creating the AdView.
 * @param modifier The modifier to apply to the banner ad.
 */
@Composable
fun BannerAd(context: Context, modifier: Modifier = Modifier) {
    var adLoadState by remember { mutableStateOf(AdLoadState.LOADING) }

    val adView = remember {
        AdView(context).apply {
            adUnitId = BuildConfig.BANNER_AD_UNIT_ID
        }
    }

    val deviceWidth = LocalConfiguration.current.screenWidthDp
    val adSize = remember(deviceWidth) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, deviceWidth)
    }

    remember {
        adView.setAdSize(adSize)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adLoadState = AdLoadState.LOADED
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                adLoadState = AdLoadState.FAILED
            }
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    // Ad load does not work in preview mode because it requires a network connection.
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(adSize.height.dp)
        ) {
            Text(
                text = "Google Mobile Ads preview banner.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        contentAlignment = Alignment.Center
    ) {
        // Show loading or error state
        when (adLoadState) {
            AdLoadState.LOADING -> {
                AdLoadingView()
            }

            AdLoadState.FAILED -> {
                AdErrorView()
            }

            AdLoadState.LOADED -> {
                // Ad is loaded, AndroidView will display it
            }
        }

        // Always render AndroidView, but it will be covered by loading/error view when needed
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            factory = { adView }
        )
    }

    // Pause and resume the AdView when the lifecycle is paused and resumed.
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
}

@Composable
private fun AdLoadingView() {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun AdErrorView() {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.ad_failed_to_load),
            fontSize = 12.sp,
        )
    }
}

private enum class AdLoadState {
    LOADING,
    LOADED,
    FAILED
}

@Preview(showBackground = true)
@Composable
private fun AdLoadingViewPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AdLoadingView()
    }
}

@Preview(showBackground = true)
@Composable
private fun AdErrorViewPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AdErrorView()
    }
}
