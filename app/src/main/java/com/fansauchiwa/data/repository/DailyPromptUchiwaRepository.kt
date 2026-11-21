package com.fansauchiwa.data.repository

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * 今日のお題から作り始めたうちわ（#360）
 *
 * お題から作ったうちわの共有文にだけ `#今日のファンサお題` を足すため、ホームで「このお題で作る」を押したときのうちわの ID を覚えておく。
 * 計測の `source=daily_prompt`（tap_preview_export・tap_preview_share）も同じ値で決める。
 *
 * メモリにだけ持ち、DB・設定には入れない。そのため、アプリのプロセスが作り直されると忘れる（そのあとは共有文にタグを足さず、source も付けない）。
 * 同じプロセスの間は、「マイデザイン」から開き直したうちわもお題から作ったものとして扱う。
 */
interface DailyPromptUchiwaRepository {
    fun markStartedFromPrompt(uchiwaId: String)

    fun isStartedFromPrompt(uchiwaId: String): Boolean
}

class DailyPromptUchiwaRepositoryImpl @Inject constructor() : DailyPromptUchiwaRepository {

    // ホームとプレビューの ViewModel から呼ばれる。呼ぶスレッドを決めていないので、並行に触っても壊れない集合にする
    private val uchiwaIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override fun markStartedFromPrompt(uchiwaId: String) {
        uchiwaIds.add(uchiwaId)
    }

    override fun isStartedFromPrompt(uchiwaId: String): Boolean = uchiwaId in uchiwaIds
}
