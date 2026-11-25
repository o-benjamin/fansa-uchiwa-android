package com.fansauchiwa.data

/**
 * 設定画面のいちばん下に出すアプリのバージョンと ID（#411・#387）
 *
 * @property installationId Firebase Installation ID。Remote Config の条件 `app.firebaseInstallationId` で
 * 特定のインストールだけに機能を開けるとき、条件に貼る値を画面から写せるように出す
 * （最初の使い道は AI 素材 #315 のクローズドテスト。使い方は `.agents/remote-config.md`）。取れなかったときは null
 * @property userId AI 素材のサーバーが回数を数える利用者の ID（匿名ログインの uid）。
 * 「作れたか確かめられませんでした」のあとに連絡をもらったとき、サーバーのログからその人を探して回数を戻せるように出す
 * （探し方は `functions/README.md` の generateMaterial の「困ったとき（Cloud Logging で探す）」）。記録の削除の依頼でも添えてもらう。
 * 特定商取引法に基づく表記・プライバシーポリシー・AI 素材のエラー文（`ai_material_error_unconfirmed`）などが見出しと場所を案内しているので、
 * 行を外す・見出しや場所を変えるときはそちらも直す
 * （案内している先は `.agents/firebase-auth-app-check.md` の「利用者の ID を画面に出す」）。まだ AI 素材を使っておらずログインしていなければ null
 * @property pushToken プッシュ通知（#429）の送り先の登録トークン。Firebase コンソールの「テスト メッセージを送信」に貼り、
 * この端末だけにお知らせを試し送りできるように出す（手順は `.agents/push-notifications.md`）。取れなかったときは null
 */
data class AppInfo(
    val versionName: String,
    val installationId: String?,
    val userId: String?,
    val pushToken: String? = null
)
