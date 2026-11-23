package com.fansauchiwa.update

/**
 * 起動したときに出す、新しい版への更新の案内（#428）
 */
enum class UpdatePromptType {
    // 案内しない
    NONE,

    // Play の柔軟な更新の画面（「あとで」を押せる。裏でダウンロードして、ホームで再起動を案内する）
    FLEXIBLE,

    // Remote Config の `min_version_code` より古い。閉じられる更新のダイアログ
    MIN,

    // `force_version_code` より古いが、直った版に即時更新できると Play で確かめられなかった。
    // 閉じられないダイアログと同じ文言の、閉じられるダイアログ（UpdatePromptPolicy で下げる）
    FORCE_FALLBACK,

    // `force_version_code` より古く、直った版に即時更新できる。閉じられない更新のダイアログ。致命的な不具合のときだけ
    FORCE
}
