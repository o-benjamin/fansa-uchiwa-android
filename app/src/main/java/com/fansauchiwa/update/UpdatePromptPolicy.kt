package com.fansauchiwa.update

import com.fansauchiwa.data.PlayAppUpdateStatus
import com.fansauchiwa.data.UpdateVersionCodes

/**
 * 起動したときに出す更新の案内を決める（#428）
 *
 * - 閉じられないダイアログ（[UpdatePromptType.FORCE]）は、Play が新しい版を出せると答えたときだけ出す。
 *   Play に聞けない端末や、まだ新しい版が届いていない端末を、更新できないまま使えなくしないため。
 *   そのときは閉じられるダイアログ（[UpdatePromptType.MIN]）に下げる
 * - Play の柔軟な更新は、新しい版が出てから [FLEXIBLE_MIN_STALENESS_DAYS] 日たっても入っていない端末だけに出す。
 *   Play の自動更新がすぐ入れる端末にまで、毎回の起動で案内を出さないため
 */
object UpdatePromptPolicy {

    const val FLEXIBLE_MIN_STALENESS_DAYS = 3

    /**
     * @param currentVersionCode 今動いている版の versionCode
     * @param versionCodes Remote Config の更新を促す版の境目。取得できなかったときは初期値（どちらも 0）
     * @param playUpdate Play から聞いた新しい版の更新の出し方。新しい版がない・Play に聞けなかったときは null
     */
    fun decide(
        currentVersionCode: Long,
        versionCodes: UpdateVersionCodes,
        playUpdate: PlayAppUpdateStatus?
    ): UpdatePromptType {
        val isBelowForce = currentVersionCode < versionCodes.forceVersionCode
        val isBelowMin = currentVersionCode < versionCodes.minVersionCode
        return when {
            isBelowForce && playUpdate != null -> UpdatePromptType.FORCE
            isBelowForce || isBelowMin -> UpdatePromptType.MIN
            playUpdate != null && playUpdate.isFlexibleAllowed &&
                (playUpdate.stalenessDays ?: 0) >= FLEXIBLE_MIN_STALENESS_DAYS -> UpdatePromptType.FLEXIBLE

            else -> UpdatePromptType.NONE
        }
    }
}
