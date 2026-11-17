package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * 端末の ID（Firebase Installation ID）。
 * アンインストール・アプリのデータ消去・270日使わないと変わる
 */
interface InstallationIdDataSource {
    /**
     * 端末の ID の Flow を返す。取れなかったときは null
     */
    fun getInstallationIdStream(): Flow<String?>
}
