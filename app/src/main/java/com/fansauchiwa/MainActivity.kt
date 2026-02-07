package com.fansauchiwa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adMobRepository: AdMobRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // 確実に広告を表示するため、アプリ起動時に広告を事前ロード
        adMobRepository.loadRewardedAd()

        setContent {
            FansaUchiwaTheme {
                FansaUchiwaNavGraph()
            }
        }
    }
}