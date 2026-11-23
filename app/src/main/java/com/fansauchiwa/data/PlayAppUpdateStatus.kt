package com.fansauchiwa.data

/**
 * Play から聞いた、今の版より新しい版の更新の出し方（#428）。新しい版がないときは作らない
 *
 * @property isFlexibleAllowed 柔軟な更新（裏でダウンロードして、あとで再起動して入れる）の画面を出せるか
 * @property stalenessDays 新しい版を Play がこの端末に出せるようになってからの日数。Play が知らなければ null
 */
data class PlayAppUpdateStatus(
    val isFlexibleAllowed: Boolean,
    val stalenessDays: Int?
)
