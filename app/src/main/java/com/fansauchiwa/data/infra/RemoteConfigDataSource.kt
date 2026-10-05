package com.fansauchiwa.data.infra

import kotlinx.coroutines.flow.Flow

/**
 * Firebase Remote Config の値。アプリを出し直さずに変えたい値を置く
 */
interface RemoteConfigDataSource {
    /**
     * うちわの材料のアフィリエイトのリンク（`affiliate_materials`。#311）の JSON。
     * 取得を試みてから、有効になっている値を流す。取得に失敗したときは前回取得した値、未設定なら空文字
     */
    fun getAffiliateMaterialsJsonStream(): Flow<String>
}
