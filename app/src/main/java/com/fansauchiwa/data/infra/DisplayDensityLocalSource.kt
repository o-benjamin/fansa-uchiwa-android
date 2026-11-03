package com.fansauchiwa.data.infra

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DisplayDensityLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : DisplayDensityDataSource {

    // 集めるたびに読み直す。「表示サイズ」を変えたあとも、画面（LocalDensity）と同じ値になる
    override fun getDensityStream(): Flow<Float> = flow {
        emit(context.resources.displayMetrics.density)
    }
}
