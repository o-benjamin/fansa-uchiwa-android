package com.fansauchiwa.data.analytics

/**
 * [AnalyticsActions.TAP_PREVIEW_SHARE] のパラメータ（#244）
 *
 * 共有ボタンからの共有と、保存完了のダイアログからの共有を見分けるために付ける。
 * イベント名は変えないので、tap_preview_share の回数はこれまでどおり数えられる。
 */
object ShareAnalyticsParams {
    const val PARAM_ENTRY_POINT = "entry_point"

    /** プレビュー画面の共有ボタン（従来からある導線） */
    const val ENTRY_POINT_PREVIEW_BUTTON = "preview_button"

    /** 保存完了のダイアログの「SNSでシェア」 */
    const val ENTRY_POINT_AFTER_SAVE = "after_save"
}
