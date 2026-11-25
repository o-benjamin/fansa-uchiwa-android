package com.fansauchiwa.data.repository

import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.data.infra.AuthDataSource
import com.fansauchiwa.data.infra.InstallationIdDataSource
import com.fansauchiwa.data.infra.PushMessagingDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface AppInfoRepository {
    /**
     * 設定画面に出すアプリのバージョンと ID の Flow を返す
     */
    fun getAppInfoStream(): Flow<AppInfo>

    /**
     * アプリのバージョンと ID を取得して流す。
     * インストール ID・通知の登録トークンが取れないときは例外を投げず、それぞれ null にして流す。
     * 利用者の ID はログイン済みのときだけ入れる。設定画面を開いただけで匿名の利用者を作らないよう、ここではログインしない
     */
    suspend fun fetchAppInfo()
}

class AppInfoRepositoryImpl @Inject constructor(
    private val installationIdDataSource: InstallationIdDataSource,
    private val authDataSource: AuthDataSource,
    private val pushMessagingDataSource: PushMessagingDataSource
) : AppInfoRepository {

    private val _appInfoStream = MutableSharedFlow<AppInfo>(replay = 1)

    override fun getAppInfoStream(): Flow<AppInfo> = _appInfoStream.asSharedFlow()

    override suspend fun fetchAppInfo() {
        val installationId = installationIdDataSource.getInstallationIdStream().first()
        _appInfoStream.emit(
            AppInfo(
                versionName = BuildConfig.VERSION_NAME,
                installationId = installationId,
                userId = authDataSource.getCurrentUserId(),
                pushToken = pushMessagingDataSource.getTokenStream().first()
            )
        )
    }
}
