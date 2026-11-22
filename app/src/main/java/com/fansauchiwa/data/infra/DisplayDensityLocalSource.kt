package com.fansauchiwa.data.infra

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DisplayDensityLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : DisplayDensityDataSource {

    // collect（購読）のたびに読み直す。値を持ち回らないので、「表示サイズ」を変えたあとも新しい密度になる。
    // 読むのはアプリの設定の密度で、画面が1つの端末では画面（LocalDensity）と同じ。
    // 外部ディスプレイなど、Activity が別の密度の画面にあるときは違いうる
    override fun getDensityStream(): Flow<Float> = flow {
        emit(context.resources.displayMetrics.density)
    }
}
