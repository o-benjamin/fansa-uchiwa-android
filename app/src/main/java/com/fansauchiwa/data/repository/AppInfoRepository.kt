package com.fansauchiwa.data.repository

import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.infra.InstallationIdDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface AppInfoRepository {
    /**
     * 設定画面に出すアプリの版と端末の ID の Flow を返す
     */
    fun getAppInfoStream(): Flow<AppInfo>

    /**
     * アプリの版と端末の ID を取得して流す。
     * 端末の ID が取れないときは例外を投げず、`installationId` を null にして流す
     */
    suspend fun fetchAppInfo()
}

class AppInfoRepositoryImpl @Inject constructor(
    private val installationIdDataSource: InstallationIdDataSource
) : AppInfoRepository {

    private val _appInfoStream = MutableSharedFlow<AppInfo>(replay = 1)

    override fun getAppInfoStream(): Flow<AppInfo> = _appInfoStream.asSharedFlow()

    override suspend fun fetchAppInfo() {
        val installationId = installationIdDataSource.getInstallationIdStream().first()
        _appInfoStream.emit(
            AppInfo(
                versionName = BuildConfig.VERSION_NAME,
                installationId = installationId
            )
        )
    }
}
