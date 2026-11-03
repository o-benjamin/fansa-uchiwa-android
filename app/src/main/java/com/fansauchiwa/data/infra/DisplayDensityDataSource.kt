package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

interface DisplayDensityDataSource {
    /**
     * 画面の密度（1dp が何 px か）の Flow を返す。端末の「表示サイズ」を変えると変わる
     */
    fun getDensityStream(): Flow<Float>
}
