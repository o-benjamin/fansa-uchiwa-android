package com.fansauchiwa.ui.theme

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.fansauchiwa.data.repository.CrashReportingRepository
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * ダウンロード式フォント（`GoogleFont`）の取得の失敗を、非致命の例外として記録するフォントの解決。
 * `LocalFontFamilyResolver` に渡して使う（`MainActivity`）。
 *
 * Compose の既定の解決は、取得に失敗すると例外を捨てて標準の書体で描くだけで、
 * 取得できないフォントや機種があっても気づけない（#299）。描き方は既定と同じで、失敗したときも標準の書体で描き続ける。
 *
 * - 記録される例外は `Unable to load font Font(GoogleFont("フォント名", ...), weight=...)` で、取得に失敗した理由が cause に入る
 * - 失敗は Compose がプロセスの中で覚えて取得し直さないので、同じフォント・太さの記録はアプリを起動し直すまで1回だけ
 * - 取得が時間切れ（15秒）になったときは、Compose が例外を出さないので記録されない
 */
fun createFontFamilyResolverRecordingFailures(
    context: Context,
    crashReportingRepository: CrashReportingRepository
): FontFamily.Resolver = createFontFamilyResolver(
    context,
    CoroutineExceptionHandler { _, error -> crashReportingRepository.recordException(error) }
)
