package com.fansauchiwa.ui.theme

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CompletableDeferred

/**
 * [typeface] が完了するまで取得が終わらない非同期フォント。`GoogleFont` の取得を再現する。
 * 取得の失敗は、`GoogleFont` と同じく例外（`completeExceptionally`）か null（`complete(null)`）で再現する。
 */
internal class DeferredFont(typeface: CompletableDeferred<Typeface?>) : AndroidFont(
    loadingStrategy = FontLoadingStrategy.Async,
    typefaceLoader = Loader(typeface),
    variationSettings = FontVariation.Settings()
) {
    override val weight: FontWeight = FontWeight.Normal
    override val style: FontStyle = FontStyle.Normal

    private class Loader(private val typeface: CompletableDeferred<Typeface?>) : TypefaceLoader {
        override fun loadBlocking(context: Context, font: AndroidFont): Typeface? =
            error("非同期フォントは loadBlocking で読み込まれない")

        override suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface? = typeface.await()
    }
}
