package com.fansauchiwa.update

import com.fansauchiwa.data.AvailableAppUpdate
import com.fansauchiwa.data.UpdateVersionCodes

/**
 * 起動したときに出す更新の案内を決める（#428）
 *
 * - 閉じられないダイアログ（[UpdatePromptType.FORCE]）は、Play が `force_version_code` 以上の版に即時更新できると答えたときだけ出す。
 *   Play に聞けない端末や、直った版がまだ届いていない端末を、更新できないまま使えなくしないため。
 *   そのときは同じ文言の閉じられるダイアログ（[UpdatePromptType.FORCE_FALLBACK]）に下げ、計測でも分けて数える
 * - Play の柔軟な更新は、新しい版が出てから [FLEXIBLE_MIN_STALENESS_DAYS] 日たっても入っていない端末だけに出す。
 *   Play の自動更新がすぐ入れる端末にまで、毎回の起動で案内を出さないため。Play が日数を返さないときは出さない側に倒す
 */
object UpdatePromptPolicy {

    const val FLEXIBLE_MIN_STALENESS_DAYS = 3

    /**
     * @param currentVersionCode 今動いている版の versionCode
     * @param versionCodes Remote Config の更新を促す版の境目。取得できなかったときは初期値（どちらも 0）
     * @param availableUpdate Play が出せると答えた新しい版。新しい版がない・Play に聞けなかったときは null
     */
    fun decide(
        currentVersionCode: Long,
        versionCodes: UpdateVersionCodes,
        availableUpdate: AvailableAppUpdate?
    ): UpdatePromptType {
        val isBelowForce = currentVersionCode < versionCodes.forceVersionCode
        val isBelowMin = currentVersionCode < versionCodes.minVersionCode
        val canUpdateToForceVersion = availableUpdate != null && availableUpdate.isImmediateAllowed &&
            availableUpdate.availableVersionCode >= versionCodes.forceVersionCode
        return when {
            isBelowForce && canUpdateToForceVersion -> UpdatePromptType.FORCE
            isBelowForce -> UpdatePromptType.FORCE_FALLBACK
            isBelowMin -> UpdatePromptType.MIN
            availableUpdate != null && availableUpdate.isFlexibleAllowed &&
                (availableUpdate.stalenessDays ?: 0) >= FLEXIBLE_MIN_STALENESS_DAYS -> UpdatePromptType.FLEXIBLE

            else -> UpdatePromptType.NONE
        }
    }
}
