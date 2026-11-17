package com.fansauchiwa.data

/**
 * 設定画面のいちばん下に出すアプリの版と端末の ID（#411）
 *
 * @property installationId Firebase Installation ID。Remote Config の条件
 * `app.firebaseInstallationId` で入口をこの端末だけ開けるのに使う。取れなかったときは null
 */
data class AppInfo(
    val versionName: String,
    val installationId: String?
)
