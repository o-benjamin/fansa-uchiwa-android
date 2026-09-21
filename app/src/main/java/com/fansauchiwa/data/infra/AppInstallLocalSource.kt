package com.fansauchiwa.data.infra

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppInstallLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AppInstallDataSource {

    override fun getIsFreshInstallStream(): Flow<Boolean> = flow {
        val isFreshInstall = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.firstInstallTime == packageInfo.lastUpdateTime
        } catch (e: Exception) {
            // PackageManager は別プロセスのため、NameNotFoundException 以外の RuntimeException も起こりうる。
            // ここで落とさず、ダイアログを出す側（false）に倒す
            false
        }
        emit(isFreshInstall)
    }

    override fun getFirstInstallTimeMillisStream(): Flow<Long?> = flow {
        val firstInstallTime = try {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            // getIsFreshInstallStream と同じく、PackageManager の例外では落とさない
            null
        }
        emit(firstInstallTime)
    }
}
