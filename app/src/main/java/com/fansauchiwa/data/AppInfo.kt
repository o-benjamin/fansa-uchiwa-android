package com.fansauchiwa.data

/**
 * 設定画面のいちばん下に出すアプリのバージョンとインストール ID（#411）
 *
 * @property installationId Firebase Installation ID。Remote Config の条件 `app.firebaseInstallationId` で
 * 特定のインストールだけに機能を開けるとき、条件に貼る値を画面から写せるように出す
 * （最初の使い道は AI 素材 #315 のクローズドテスト。使い方は `.agents/remote-config.md`）。取れなかったときは null
 */
data class AppInfo(
    val versionName: String,
    val installationId: String?
)
