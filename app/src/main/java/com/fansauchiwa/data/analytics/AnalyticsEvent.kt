package com.fansauchiwa.data.analytics

/**
 * Analyticsに送信するイベントデータ
 *
 * @param name イベント名
 * @param params イベントパラメータ
 */
data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap()
)

/**
 * 画面表示イベント定数
 * Screenの表示タイミングで計測します。
 */
object AnalyticsScreens {
    const val HOME_SCREEN = "home_screen"
    const val EDIT_SCREEN = "edit_screen"
    const val PREVIEW_SCREEN = "preview_screen"
    const val IMAGE_PREVIEW_SCREEN = "image_preview_screen"
}

/**
 * ユーザー操作イベント定数
 * ユーザーのアクション（タップ、選択、完了など）を計測します。
 */
object AnalyticsActions {

    const val TAP_HOME_NEW_CREATE = "tap_home_new_create"
    const val TAP_HOME_ITEM_EDIT = "tap_home_item_edit"
    const val TAP_HOME_ITEM_DELETE = "tap_home_item_delete"

    const val TAP_EDIT_BACK = "tap_edit_back"
    const val TAP_EDIT_BACK_DIALOG = "tap_edit_back_dialog"
    const val TAP_EDIT_COMPLETE = "tap_edit_complete"
    const val TAP_EDIT_UNDO_REDO = "tap_edit_undo_redo"
    const val TAP_EDIT_TEXT_COLOR = "tap_edit_text_color"
    const val TAP_EDIT_STICKER_COLOR = "tap_edit_sticker_color"

    const val SELECT_EDIT_TEXT = "select_edit_text"
    const val SELECT_EDIT_IMAGE = "select_edit_image"
    const val SELECT_EDIT_STICKER = "select_edit_sticker"
    const val SELECT_EDIT_TEXT_COLOR = "select_edit_text_color"
    const val SELECT_EDIT_TEXT_WEIGHT = "select_edit_text_weight"
    const val SELECT_EDIT_STICKER_COLOR = "select_edit_sticker_color"
    const val SELECT_EDIT_STICKER_WEIGHT = "select_edit_sticker_weight"
    const val SELECT_EDIT_BACKGROUND_COLOR = "select_edit_background_color"
    const val SELECT_EDIT_LAYER = "select_edit_layer"

    const val TAP_IMAGE_PREVIEW_CONFIRM = "tap_image_preview_confirm"

    const val TAP_PREVIEW_EXPORT = "tap_preview_export"
    const val TAP_PREVIEW_GO_HOME = "tap_preview_go_home"
    const val TAP_PREVIEW_BACK = "tap_preview_back"

    const val AD_REWARD_SHOW = "ad_reward_show"
    const val AD_REWARD_COMPLETE = "ad_reward_complete"
    const val AD_REWARD_DISMISSED = "ad_reward_dismissed"

    const val AD_INTERSTITIAL_SHOW = "ad_interstitial_show"
    const val AD_INTERSTITIAL_DISMISSED = "ad_interstitial_dismissed"
}

/**
 * Text修正対象
 */
object EditTextTargetParams {
    const val TEXT = "text"
    const val PARAM_STROKE_1 = "stroke_1"
    const val PARAM_STROKE_2 = "stroke_2"
}

/**
 * Sticker修正対象
 */
object EditStickerTargetParams {
    const val STICKER = "sticker"
    const val PARAM_STROKE_1 = "stroke_1"
    const val PARAM_STROKE_2 = "stroke_2"
}

/**
 * 背景色修正対象
 */
object BackGroundColorParams {
    const val PARAM_UCHIWA = "uchiwa"
    const val PARAM_BACKGROUND = "background"
}

/**
 * 戻るダイアログアクション
 */
object AnalyticsBackDialogActions {
    const val ACTION_DELETE = "delete"
    const val ACTION_SAVE = "save"
}

/**
 * undo/redoアクション
 */
object AnalyticsUndoRedoActions {
    const val ACTION_UNDO = "undo"
    const val ACTION_REDO = "redo"
}