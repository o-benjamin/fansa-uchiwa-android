package com.fansauchiwa.ui.theme

import android.graphics.Typeface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fansauchiwa.data.repository.CrashReportingRepository
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ダウンロード式フォントの取得に失敗したとき、非致命の例外として記録され、標準の書体で描き続けることを確かめる（#299）。
 * `MainActivity` と同じく、[createFontFamilyResolverRecordingFailures] を `LocalFontFamilyResolver` に渡して解決する。
 */
@RunWith(AndroidJUnit4::class)
class FontFamilyResolverTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val recordedErrors = CopyOnWriteArrayList<Throwable>()
    private val crashReportingRepository = object : CrashReportingRepository {
        override fun recordException(throwable: Throwable) {
            recordedErrors += throwable
        }
    }

    private val pendingTypeface = CompletableDeferred<Typeface?>()
    private val deferredFontFamily = FontFamily(DeferredFont(pendingTypeface))

    @Volatile
    private var resolvedTypeface: Any? = null

    private fun setContentResolvingTypeface(): Any? {
        composeTestRule.setContent {
            val context = LocalContext.current
            val fontFamilyResolver = remember {
                createFontFamilyResolverRecordingFailures(context, crashReportingRepository)
            }
            CompositionLocalProvider(LocalFontFamilyResolver provides fontFamilyResolver) {
                resolvedTypeface = LocalFontFamilyResolver.current.resolve(deferredFontFamily).value
            }
        }
        composeTestRule.waitForIdle()
        return resolvedTypeface
    }

    @Test
    fun createFontFamilyResolverRecordingFailures_FontLoadThrows_RecordsExceptionWithCause() {
        setContentResolvingTypeface()

        pendingTypeface.completeExceptionally(IOException("取得の失敗"))

        // 記録されなければ waitUntil が失敗する
        composeTestRule.waitUntil(timeoutMillis = 5_000) { recordedErrors.isNotEmpty() }
        assertEquals(1, recordedErrors.size)
        assertEquals("取得の失敗", recordedErrors.single().cause?.message)
    }

    @Test
    fun createFontFamilyResolverRecordingFailures_FontLoadThrows_KeepsDefaultTypeface() {
        val typefaceWhileLoading = setContentResolvingTypeface()

        pendingTypeface.completeExceptionally(IOException("取得の失敗"))
        composeTestRule.waitUntil(timeoutMillis = 5_000) { recordedErrors.isNotEmpty() }
        composeTestRule.waitForIdle()

        // 失敗しても落ちずに、取得中と同じ標準の書体で描き続けること
        assertEquals(typefaceWhileLoading, resolvedTypeface)
    }

    @Test
    fun createFontFamilyResolverRecordingFailures_FontLoaded_RecordsNothing() {
        val typefaceWhileLoading = setContentResolvingTypeface()

        pendingTypeface.complete(Typeface.MONOSPACE)

        composeTestRule.waitUntil(timeoutMillis = 5_000) { resolvedTypeface != typefaceWhileLoading }
        assertTrue(recordedErrors.isEmpty())
    }
}
