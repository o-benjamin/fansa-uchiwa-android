package com.fansauchiwa.data.analytics

private const val SCREEN_HOME = "home_screen"
private const val SCREEN_EDIT = "edit_screen"
private const val SCREEN_PREVIEW = "preview_screen"
private const val SCREEN_IMAGE_PREVIEW = "image_preview_screen"

private const val ACTION_TAP_HOME_NEW_CREATE = "tap_home_new_create"
private const val ACTION_TAP_HOME_ITEM_EDIT = "tap_home_item_edit"
private const val ACTION_TAP_HOME_ITEM_DELETE = "tap_home_item_delete"
private const val ACTION_TAP_HOME_ITEM_DUPLICATE = "tap_home_item_duplicate"
private const val ACTION_TAP_HOME_TEMPLATE = "tap_home_template"
private const val ACTION_TAP_EDIT_BACK = "tap_edit_back"
private const val ACTION_TAP_EDIT_BACK_DIALOG = "tap_edit_back_dialog"
private const val ACTION_TAP_EDIT_COMPLETE = "tap_edit_complete"
private const val ACTION_TAP_EDIT_UNDO_REDO = "tap_edit_undo_redo"
private const val ACTION_TAP_EDIT_DUPLICATE = "tap_edit_duplicate"
private const val ACTION_TAP_EDIT_TEXT_COLOR = "tap_edit_text_color"
private const val ACTION_TAP_EDIT_STICKER_COLOR = "tap_edit_sticker_color"
private const val ACTION_SELECT_EDIT_TEXT = "select_edit_text"
private const val ACTION_SELECT_EDIT_IMAGE = "select_edit_image"
private const val ACTION_SELECT_EDIT_STICKER = "select_edit_sticker"
private const val ACTION_SELECT_EDIT_TEXT_COLOR = "select_edit_text_color"
private const val ACTION_SELECT_EDIT_TEXT_FONT = "select_edit_text_font"
private const val ACTION_SELECT_EDIT_TEXT_WEIGHT = "select_edit_text_weight"
private const val ACTION_SELECT_EDIT_STICKER_COLOR = "select_edit_sticker_color"
private const val ACTION_SELECT_EDIT_STICKER_WEIGHT = "select_edit_sticker_weight"
private const val ACTION_SELECT_EDIT_BACKGROUND_COLOR = "select_edit_background_color"
private const val ACTION_SELECT_EDIT_LAYER = "select_edit_layer"
private const val ACTION_TAP_IMAGE_PREVIEW_CONFIRM = "tap_image_preview_confirm"
private const val ACTION_TAP_PREVIEW_EXPORT = "tap_preview_export"
private const val ACTION_TAP_PREVIEW_SHARE = "tap_preview_share"
private const val ACTION_TAP_PREVIEW_GO_HOME = "tap_preview_go_home"
private const val ACTION_TAP_PREVIEW_BACK = "tap_preview_back"
private const val ACTION_AD_REWARD_SHOW = "ad_reward_show"
private const val ACTION_AD_REWARD_COMPLETE = "ad_reward_complete"
private const val ACTION_AD_REWARD_DISMISSED = "ad_reward_dismissed"
private const val ACTION_AD_INTERSTITIAL_SHOW = "ad_interstitial_show"
private const val ACTION_AD_INTERSTITIAL_DISMISSED = "ad_interstitial_dismissed"

private const val PARAM_VALUE_TEXT = "text"
private const val PARAM_VALUE_STICKER = "sticker"
private const val PARAM_VALUE_STROKE_1 = "stroke_1"
private const val PARAM_VALUE_STROKE_2 = "stroke_2"
private const val PARAM_VALUE_UCHIWA = "uchiwa"
private const val PARAM_VALUE_BACKGROUND = "background"
private const val PARAM_VALUE_DELETE = "delete"
private const val PARAM_VALUE_SAVE = "save"
private const val PARAM_VALUE_UNDO = "undo"
private const val PARAM_VALUE_REDO = "redo"

/**
 * AnalyticsRepository から DataSource へ渡す送信モデルです。
 *
 * UI は意味的なイベント名とパラメータだけを組み立て、
 * 実際の送信方法は Repository / DataSource 側に委譲します。
 *
 * @param name イベント名
 * @param params イベントパラメータ
 */
data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap()
)

/**
 * アプリケーションが利用する分析イベントの語彙をユースケース単位で整理した名前空間です。
 *
 * 送信モデルとは責務を分離し、Presentation 層ではこの語彙を使ってイベントを選択し、
 * Infrastructure 層では `AnalyticsEvent` を Firebase などの外部サービス向けに変換します。
 */
object AnalyticsCatalog {

    object Screen {
        const val HOME = SCREEN_HOME
        const val EDIT = SCREEN_EDIT
        const val PREVIEW = SCREEN_PREVIEW
        const val IMAGE_PREVIEW = SCREEN_IMAGE_PREVIEW
    }

    object Action {
        const val TAP_HOME_NEW_CREATE = ACTION_TAP_HOME_NEW_CREATE
        const val TAP_HOME_ITEM_EDIT = ACTION_TAP_HOME_ITEM_EDIT
        const val TAP_HOME_ITEM_DELETE = ACTION_TAP_HOME_ITEM_DELETE
        const val TAP_HOME_ITEM_DUPLICATE = ACTION_TAP_HOME_ITEM_DUPLICATE
        const val TAP_HOME_TEMPLATE = ACTION_TAP_HOME_TEMPLATE

        const val TAP_EDIT_BACK = ACTION_TAP_EDIT_BACK
        const val TAP_EDIT_BACK_DIALOG = ACTION_TAP_EDIT_BACK_DIALOG
        const val TAP_EDIT_COMPLETE = ACTION_TAP_EDIT_COMPLETE
        const val TAP_EDIT_UNDO_REDO = ACTION_TAP_EDIT_UNDO_REDO
        const val TAP_EDIT_DUPLICATE = ACTION_TAP_EDIT_DUPLICATE
        const val TAP_EDIT_TEXT_COLOR = ACTION_TAP_EDIT_TEXT_COLOR
        const val TAP_EDIT_STICKER_COLOR = ACTION_TAP_EDIT_STICKER_COLOR

        const val SELECT_EDIT_TEXT = ACTION_SELECT_EDIT_TEXT
        const val SELECT_EDIT_IMAGE = ACTION_SELECT_EDIT_IMAGE
        const val SELECT_EDIT_STICKER = ACTION_SELECT_EDIT_STICKER
        const val SELECT_EDIT_TEXT_COLOR = ACTION_SELECT_EDIT_TEXT_COLOR
        const val SELECT_EDIT_TEXT_FONT = ACTION_SELECT_EDIT_TEXT_FONT
        const val SELECT_EDIT_TEXT_WEIGHT = ACTION_SELECT_EDIT_TEXT_WEIGHT
        const val SELECT_EDIT_STICKER_COLOR = ACTION_SELECT_EDIT_STICKER_COLOR
        const val SELECT_EDIT_STICKER_WEIGHT = ACTION_SELECT_EDIT_STICKER_WEIGHT
        const val SELECT_EDIT_BACKGROUND_COLOR = ACTION_SELECT_EDIT_BACKGROUND_COLOR
        const val SELECT_EDIT_LAYER = ACTION_SELECT_EDIT_LAYER

        const val TAP_IMAGE_PREVIEW_CONFIRM = ACTION_TAP_IMAGE_PREVIEW_CONFIRM

        const val TAP_PREVIEW_EXPORT = ACTION_TAP_PREVIEW_EXPORT
        const val TAP_PREVIEW_SHARE = ACTION_TAP_PREVIEW_SHARE
        const val TAP_PREVIEW_GO_HOME = ACTION_TAP_PREVIEW_GO_HOME
        const val TAP_PREVIEW_BACK = ACTION_TAP_PREVIEW_BACK

        const val AD_REWARD_SHOW = ACTION_AD_REWARD_SHOW
        const val AD_REWARD_COMPLETE = ACTION_AD_REWARD_COMPLETE
        const val AD_REWARD_DISMISSED = ACTION_AD_REWARD_DISMISSED

        const val AD_INTERSTITIAL_SHOW = ACTION_AD_INTERSTITIAL_SHOW
        const val AD_INTERSTITIAL_DISMISSED = ACTION_AD_INTERSTITIAL_DISMISSED
    }

    object ParamValue {
        object EditTextTarget {
            const val TEXT = PARAM_VALUE_TEXT
            const val STROKE_1 = PARAM_VALUE_STROKE_1
            const val STROKE_2 = PARAM_VALUE_STROKE_2
        }

        object EditStickerTarget {
            const val STICKER = PARAM_VALUE_STICKER
            const val STROKE_1 = PARAM_VALUE_STROKE_1
            const val STROKE_2 = PARAM_VALUE_STROKE_2
        }

        object BackgroundColorTarget {
            const val UCHIWA = PARAM_VALUE_UCHIWA
            const val BACKGROUND = PARAM_VALUE_BACKGROUND
        }

        object BackDialogAction {
            const val DELETE = PARAM_VALUE_DELETE
            const val SAVE = PARAM_VALUE_SAVE
        }

        object UndoRedoAction {
            const val UNDO = PARAM_VALUE_UNDO
            const val REDO = PARAM_VALUE_REDO
        }
    }
}

/**
 * 既存の Presentation / ViewModel からの import を壊さないための互換エイリアスです。
 */
object AnalyticsScreens {
    const val HOME_SCREEN = AnalyticsCatalog.Screen.HOME
    const val EDIT_SCREEN = AnalyticsCatalog.Screen.EDIT
    const val PREVIEW_SCREEN = AnalyticsCatalog.Screen.PREVIEW
    const val IMAGE_PREVIEW_SCREEN = AnalyticsCatalog.Screen.IMAGE_PREVIEW
}

/**
 * 既存の Presentation / ViewModel からの import を壊さないための互換エイリアスです。
 */
object AnalyticsActions {
    const val TAP_HOME_NEW_CREATE = AnalyticsCatalog.Action.TAP_HOME_NEW_CREATE
    const val TAP_HOME_ITEM_EDIT = AnalyticsCatalog.Action.TAP_HOME_ITEM_EDIT
    const val TAP_HOME_ITEM_DELETE = AnalyticsCatalog.Action.TAP_HOME_ITEM_DELETE
    const val TAP_HOME_ITEM_DUPLICATE = AnalyticsCatalog.Action.TAP_HOME_ITEM_DUPLICATE
    const val TAP_HOME_TEMPLATE = AnalyticsCatalog.Action.TAP_HOME_TEMPLATE

    const val TAP_EDIT_BACK = AnalyticsCatalog.Action.TAP_EDIT_BACK
    const val TAP_EDIT_BACK_DIALOG = AnalyticsCatalog.Action.TAP_EDIT_BACK_DIALOG
    const val TAP_EDIT_COMPLETE = AnalyticsCatalog.Action.TAP_EDIT_COMPLETE
    const val TAP_EDIT_UNDO_REDO = AnalyticsCatalog.Action.TAP_EDIT_UNDO_REDO
    const val TAP_EDIT_DUPLICATE = AnalyticsCatalog.Action.TAP_EDIT_DUPLICATE
    const val TAP_EDIT_TEXT_COLOR = AnalyticsCatalog.Action.TAP_EDIT_TEXT_COLOR
    const val TAP_EDIT_STICKER_COLOR = AnalyticsCatalog.Action.TAP_EDIT_STICKER_COLOR

    const val SELECT_EDIT_TEXT = AnalyticsCatalog.Action.SELECT_EDIT_TEXT
    const val SELECT_EDIT_IMAGE = AnalyticsCatalog.Action.SELECT_EDIT_IMAGE
    const val SELECT_EDIT_STICKER = AnalyticsCatalog.Action.SELECT_EDIT_STICKER
    const val SELECT_EDIT_TEXT_COLOR = AnalyticsCatalog.Action.SELECT_EDIT_TEXT_COLOR
    const val SELECT_EDIT_TEXT_FONT = AnalyticsCatalog.Action.SELECT_EDIT_TEXT_FONT
    const val SELECT_EDIT_TEXT_WEIGHT = AnalyticsCatalog.Action.SELECT_EDIT_TEXT_WEIGHT
    const val SELECT_EDIT_STICKER_COLOR = AnalyticsCatalog.Action.SELECT_EDIT_STICKER_COLOR
    const val SELECT_EDIT_STICKER_WEIGHT = AnalyticsCatalog.Action.SELECT_EDIT_STICKER_WEIGHT
    const val SELECT_EDIT_BACKGROUND_COLOR = AnalyticsCatalog.Action.SELECT_EDIT_BACKGROUND_COLOR
    const val SELECT_EDIT_LAYER = AnalyticsCatalog.Action.SELECT_EDIT_LAYER

    const val TAP_IMAGE_PREVIEW_CONFIRM = AnalyticsCatalog.Action.TAP_IMAGE_PREVIEW_CONFIRM

    const val TAP_PREVIEW_EXPORT = AnalyticsCatalog.Action.TAP_PREVIEW_EXPORT
    const val TAP_PREVIEW_SHARE = AnalyticsCatalog.Action.TAP_PREVIEW_SHARE
    const val TAP_PREVIEW_GO_HOME = AnalyticsCatalog.Action.TAP_PREVIEW_GO_HOME
    const val TAP_PREVIEW_BACK = AnalyticsCatalog.Action.TAP_PREVIEW_BACK

    const val AD_REWARD_SHOW = AnalyticsCatalog.Action.AD_REWARD_SHOW
    const val AD_REWARD_COMPLETE = AnalyticsCatalog.Action.AD_REWARD_COMPLETE
    const val AD_REWARD_DISMISSED = AnalyticsCatalog.Action.AD_REWARD_DISMISSED

    const val AD_INTERSTITIAL_SHOW = AnalyticsCatalog.Action.AD_INTERSTITIAL_SHOW
    const val AD_INTERSTITIAL_DISMISSED = AnalyticsCatalog.Action.AD_INTERSTITIAL_DISMISSED
}

/**
 * 既存の Presentation / ViewModel からの import を壊さないための互換エイリアスです。
 */
object EditTextTargetParams {
    const val TEXT = AnalyticsCatalog.ParamValue.EditTextTarget.TEXT
    const val PARAM_STROKE_1 = AnalyticsCatalog.ParamValue.EditTextTarget.STROKE_1
    const val PARAM_STROKE_2 = AnalyticsCatalog.ParamValue.EditTextTarget.STROKE_2
}

/**
 * 既存の Presentation / ViewModel からの import を壊さないための互換エイリアスです。
 */
object EditStickerTargetParams {
    const val STICKER = AnalyticsCatalog.ParamValue.EditStickerTarget.STICKER
    const val PARAM_STROKE_1 = AnalyticsCatalog.ParamValue.EditStickerTarget.STROKE_1
    const val PARAM_STROKE_2 = AnalyticsCatalog.ParamValue.EditStickerTarget.STROKE_2
}

/**
 * 既存の Presentation / ViewModel からの import を壊さないための互換エイリアスです。
 */
object BackGroundColorParams {
    const val PARAM_UCHIWA = AnalyticsCatalog.ParamValue.BackgroundColorTarget.UCHIWA
    const val PARAM_BACKGROUND = AnalyticsCatalog.ParamValue.BackgroundColorTarget.BACKGROUND
}

/**
 * 既存の Presentation / Screen からの import を壊さないための互換エイリアスです。
 */
object AnalyticsBackDialogActions {
    const val ACTION_DELETE = AnalyticsCatalog.ParamValue.BackDialogAction.DELETE
    const val ACTION_SAVE = AnalyticsCatalog.ParamValue.BackDialogAction.SAVE
}

/**
 * 既存の Presentation / ViewModel からの import を壊さないための互換エイリアスです。
 */
object AnalyticsUndoRedoActions {
    const val ACTION_UNDO = AnalyticsCatalog.ParamValue.UndoRedoAction.UNDO
    const val ACTION_REDO = AnalyticsCatalog.ParamValue.UndoRedoAction.REDO
}
