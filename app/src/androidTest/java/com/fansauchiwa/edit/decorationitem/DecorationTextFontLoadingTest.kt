package com.fansauchiwa.edit.decorationitem

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fansauchiwa.ui.theme.DeferredFont
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ダウンロード式フォントの取得が測ったあとに終わっても、文字の装飾がそのフォントで描き直されることを確かめる（#305）。
 * 取得の終わりを手で決められる非同期フォント（[DeferredFont]）で、`GoogleFont` の取得を再現する。
 */
@RunWith(AndroidJUnit4::class)
class DecorationTextFontLoadingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // 標準の書体（sans-serif）と等幅の書体で幅がはっきり変わる文字
    private val text = "iiiii"

    private val pendingTypeface = CompletableDeferred<Typeface?>()
    private val deferredFontFamily = FontFamily(DeferredFont(pendingTypeface))

    private var deferredFontWidth = 0
    private var defaultFontWidth = 0
    private var monospaceFontWidth = 0
    private var resolvedTypeface: Any? = null

    private fun setContentMeasuringWidths() {
        composeTestRule.setContent {
            deferredFontWidth = measureWidth(deferredFontFamily)
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
    fun measureDecorationText_FontLoading_MeasuresWithDefaultFont() {
        setContentMeasuringWidths()

        assertEquals(defaultFontWidth, deferredFontWidth)
    }

    @Test
    fun measureDecorationText_FontLoadedAfterMeasure_RemeasuresWithLoadedFont() {
        setContentMeasuringWidths()

        pendingTypeface.complete(Typeface.MONOSPACE)

        // 測り直されなければ waitUntil が失敗する
        composeTestRule.waitUntil(timeoutMillis = 5_000) { deferredFontWidth == monospaceFontWidth }
    }

    @Test
    fun measureDecorationText_FontFailedToLoad_KeepsDefaultFont() {
        setContentMeasuringWidths()

        // 取得の失敗。GoogleFont は例外で知らせるが、Compose は null が返ったときと同じ失敗として扱う
        pendingTypeface.complete(null)
        composeTestRule.waitForIdle()

        assertEquals(defaultFontWidth, deferredFontWidth)
    }

    private fun setContentResolvingTypeface(): Any? {
        composeTestRule.setContent {
            resolvedTypeface = resolveDecorationTypeface(deferredFontFamily, FontWeight.Normal)
        }
        composeTestRule.waitForIdle()
        return resolvedTypeface
    }

    @Test
    fun resolveDecorationTypeface_FontLoadedLater_ChangesToAnotherTypeface() {
        val typefaceWhileLoading = setContentResolvingTypeface()

        pendingTypeface.complete(Typeface.MONOSPACE)

        // 全体の縁取りを作り直すキーになるので、取得が終わったら別の値になること。変わらなければ waitUntil が失敗する
        composeTestRule.waitUntil(timeoutMillis = 5_000) { resolvedTypeface != typefaceWhileLoading }
    }

    @Test
    fun resolveDecorationTypeface_FontFailedToLoad_KeepsSameTypeface() {
        val typefaceWhileLoading = setContentResolvingTypeface()

        pendingTypeface.complete(null)
        composeTestRule.waitForIdle()

        // 失敗では縁取りを作り直す必要がないので、同じ値のままであること
        assertEquals(typefaceWhileLoading, resolvedTypeface)
    }
}
