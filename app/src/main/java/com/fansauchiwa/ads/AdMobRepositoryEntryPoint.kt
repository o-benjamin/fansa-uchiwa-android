package com.fansauchiwa.ads

import com.fansauchiwa.data.repository.AdMobRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * ViewModelを持たないコンポーザブル（[BannerAd]）から [AdMobRepository] を取得するためのEntryPoint
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdMobRepositoryEntryPoint {
    fun adMobRepository(): AdMobRepository
}
