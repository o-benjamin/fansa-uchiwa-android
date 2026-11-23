package com.fansauchiwa.data

/**
 * Play が出せると答えた、今の版より新しい版（#428）。新しい版がない・Play に聞けないときは作らない
 *
 * @property availableVersionCode 新しい版の versionCode
 * @property isImmediateAllowed 即時更新（全画面で、その場でダウンロードして入れる）の画面を出せるか。途中で止まった即時更新の続きを出せるときも true
 * @property isFlexibleAllowed 柔軟な更新（裏でダウンロードして、あとで再起動して入れる）の画面を出せるか。もうダウンロード中・ダウンロード済みなら false
 * @property stalenessDays 新しい版を Play がこの端末に出せるようになってからの日数。Play が知らなければ null
 */
data class AvailableAppUpdate(
    val availableVersionCode: Int,
    val isImmediateAllowed: Boolean,
    val isFlexibleAllowed: Boolean,
    val stalenessDays: Int?
)
