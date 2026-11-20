package com.fansauchiwa.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * サーバーや Play を呼ぶ処理の結果を `Result` で返す（AI 素材 #338・回数券 #348。Play だけを呼ぶ値段の取得にも使う）。
 * Play 開発者サービス（ML Kit）や Firebase の Task が取り消されたときも await() は CancellationException を投げる。
 * 呼び出し元のコルーチンが取り消されたときだけそのまま投げ、それ以外は失敗として画面に出す
 * （投げたままにすると、画面が「作っています」のまま、または報告のボタンが押せないまま戻れなくなる）
 */
internal suspend fun <T> runServerCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    currentCoroutineContext().ensureActive()
    Result.failure(e)
} catch (e: Exception) {
    Result.failure(e)
}
