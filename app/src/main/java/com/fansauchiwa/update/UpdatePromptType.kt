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

    // Remote Config の `force_version_code` より古い。閉じられない更新のダイアログ。致命的な不具合のときだけ
    FORCE
}
