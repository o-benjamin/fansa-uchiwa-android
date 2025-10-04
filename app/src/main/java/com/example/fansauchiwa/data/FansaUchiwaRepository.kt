package com.example.fansauchiwa.data

import android.util.Log
import javax.inject.Inject

interface FansaUchiwaRepository {
    suspend fun saveDecorations(decorations: List<Decoration>)
}

class FansaUchiwaRepositoryImpl @Inject constructor() : FansaUchiwaRepository {
    override suspend fun saveDecorations(decorations: List<Decoration>) {
        // 本来はここでRoomなどのDBに保存します
        Log.d("UchiwaRepository", "Saving decorations: $decorations")
    }
}