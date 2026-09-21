package com.fansauchiwa.data.infra

import android.content.Context
import android.content.pm.PackageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppInstallLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AppInstallDataSource {

    override fun getIsFreshInstallStream(): Flow<Boolean> = flow {
        // 判定できない場合は、ダイアログを出す側（false）に倒す
        val packageInfo = getPackageInfoOrNull()
        emit(packageInfo != null && packageInfo.firstInstallTime == packageInfo.lastUpdateTime)
    }

    override fun getFirstInstallTimeMillisStream(): Flow<Long?> = flow {
        emit(getPackageInfoOrNull()?.firstInstallTime)
    }

    private fun getPackageInfoOrNull(): PackageInfo? = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        // PackageManager は別プロセスのため、NameNotFoundException 以外の RuntimeException も起こりうる。
        // ここで落とさず、呼び出し側で判定できない場合の値に倒す
        null
    }
}
