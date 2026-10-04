package com.fansauchiwa.edit.decorationitem

import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ダウンロード式フォントの取得が測ったあとに終わっても、文字の装飾がそのフォントで測り直されることを確かめる（#305）。
 * 取得の終わりを手で決められる非同期フォント（[DeferredFont]）で、`GoogleFont` の取得を再現する。
 */
@RunWith(AndroidJUnit4::class)
class MeasureDecorationTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // 標準の書体（sans-serif）と等幅の書体で幅がはっきり変わる文字
    private val text = "iiiii"

    private var asyncFontWidth = 0
    private var defaultFontWidth = 0
    private var monospaceFontWidth = 0

    private fun setContentMeasuring(asyncFontFamily: FontFamily) {
        composeTestRule.setContent {
            asyncFontWidth = measureWidth(asyncFontFamily)
            defaultFontWidth = measureWidth(FontFamily.Default)
            monospaceFontWidth = measureWidth(FontFamily(Typeface.MONOSPACE))
        }
        composeTestRule.waitForIdle()
        // 前提：標準の書体と等幅の書体で幅が変わらないと、どちらで測ったか見分けられない
        assertNotEquals(defaultFontWidth, monospaceFontWidth)
    }

    @Composable
    private fun measureWidth(fontFamily: FontFamily): Int = measureDecorationText(
        text = text,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ).size.width

    @Test
    fun measureDecorationText_AsyncFontLoading_MeasuresWithDefaultFont() {
        val loadedTypeface = CompletableDeferred<Typeface?>()

        setContentMeasuring(FontFamily(DeferredFont(loadedTypeface)))

        assertEquals(defaultFontWidth, asyncFontWidth)
    }

    @Test
    fun measureDecorationText_AsyncFontLoadedAfterMeasure_RemeasuresWithLoadedFont() {
        val loadedTypeface = CompletableDeferred<Typeface?>()
        setContentMeasuring(FontFamily(DeferredFont(loadedTypeface)))

        loadedTypeface.complete(Typeface.MONOSPACE)

        composeTestRule.waitUntil(timeoutMillis = 5_000) { asyncFontWidth == monospaceFontWidth }
        assertEquals(monospaceFontWidth, asyncFontWidth)
    }

    @Test
    fun measureDecorationText_AsyncFontFailedToLoad_KeepsDefaultFont() {
        val loadedTypeface = CompletableDeferred<Typeface?>()
        setContentMeasuring(FontFamily(DeferredFont(loadedTypeface)))

        // 取得に失敗すると null が返る（端末で取得できないフォント）
        loadedTypeface.complete(null)
        composeTestRule.waitForIdle()

        assertEquals(defaultFontWidth, asyncFontWidth)
    }
}

/** [typeface] が完了するまで取得が終わらない非同期フォント。 */
private class DeferredFont(typeface: CompletableDeferred<Typeface?>) : AndroidFont(
    loadingStrategy = FontLoadingStrategy.Async,
    typefaceLoader = DeferredTypefaceLoader(typeface),
    variationSettings = FontVariation.Settings()
) {
    override val weight: FontWeight = FontWeight.Normal
    override val style: FontStyle = FontStyle.Normal
}

private class DeferredTypefaceLoader(
    private val typeface: CompletableDeferred<Typeface?>
) : AndroidFont.TypefaceLoader {
    override fun loadBlocking(context: Context, font: AndroidFont): Typeface? =
        error("非同期フォントは loadBlocking で読み込まれない")

    override suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface? = typeface.await()
}
