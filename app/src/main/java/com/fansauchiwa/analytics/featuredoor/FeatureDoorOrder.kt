package com.fansauchiwa.analytics.featuredoor

import kotlin.random.Random

/**
 * 入口の並び順（#270・一時的）
 *
 * 上に置いたものほど押されやすく比較がゆがむので、起動ごとに並びを入れ替える。
 * 並びはプロセスの間だけ保つ（ホームに戻るたびに変わらないようにする）。
 */
object FeatureDoorOrder {

    /** この起動での並び。プロセスが作り直されたら新しく決める */
    val forThisLaunch: List<FeatureDoor> by lazy { shuffled(Random.Default) }

    fun shuffled(random: Random): List<FeatureDoor> = FeatureDoor.entries.shuffled(random)
}
