package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

interface AppInstallDataSource {
    /**
     * アプリを新規インストールしてから一度も更新していないかどうかのFlowを返す
     * 一度でも更新したことがある場合や、判定できない場合は false
     */
    fun getIsFreshInstallStream(): Flow<Boolean>
}
