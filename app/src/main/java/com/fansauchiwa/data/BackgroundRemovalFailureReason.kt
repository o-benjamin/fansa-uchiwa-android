package com.fansauchiwa.data

/**
 * 背景透過に失敗した理由
 * @param analyticsValue Analytics の `reason` パラメータに送る値
 */
enum class BackgroundRemovalFailureReason(val analyticsValue: String) {
    /** ML Kit のモジュールをダウンロードできなかった（通信エラー・Play 開発者サービスの問題など） */
    MODULE_UNAVAILABLE("module_unavailable"),

    /** ML Kit のモジュールのダウンロードが時間内に終わらなかった（裏でダウンロードは続く） */
    MODULE_TIMEOUT("module_timeout"),

    /** 画像から被写体を見つけられなかった */
    NO_SUBJECT("no_subject"),

    /** 画像の読み込み・透過処理・保存のいずれかで失敗した */
    PROCESS_FAILED("process_failed")
}
