package com.fansauchiwa.data

/**
 * 更新を促す版の境目（#428）。Remote Config で決める。どちらも versionCode で、0 なら使わない
 *
 * @property minVersionCode これより古い版では、起動するたびに閉じられる更新のダイアログを出す（`min_version_code`）
 * @property forceVersionCode これより古い版では、閉じられない更新のダイアログを出す（`force_version_code`）。致命的な不具合のときだけ使う
 */
data class UpdateVersionCodes(
    val minVersionCode: Long = 0,
    val forceVersionCode: Long = 0
)
