package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

interface AppInstallDataSource {
    /**
     * アプリを最初にインストールした日時（ミリ秒）のFlowを返す
     * 判定できない場合は null
     */
    fun getFirstInstallTimeMillisStream(): Flow<Long?>
}
