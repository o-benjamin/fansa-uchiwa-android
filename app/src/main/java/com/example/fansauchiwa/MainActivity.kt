package com.example.fansauchiwa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fansauchiwa.data.AdMobRepository
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adMobRepository: AdMobRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 確実に広告を表示するため、アプリ起動時に広告を事前ロード
        adMobRepository.loadRewardedAd()

        setContent {
            FansaUchiwaTheme {
                FansaUchiwaNavGraph()
            }
        }
    }
}