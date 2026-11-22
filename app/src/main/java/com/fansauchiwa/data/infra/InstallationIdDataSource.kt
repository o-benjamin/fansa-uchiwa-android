package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * インストール ID（Firebase Installation ID）。端末ではなくアプリのインストールごとに振られ、
 * アンインストール・アプリのデータ消去で変わる
 */
interface InstallationIdDataSource {
    /**
     * インストール ID の Flow を返す。取れなかったときは null
     */
    fun getInstallationIdStream(): Flow<String?>
}
