package com.fansauchiwa.analytics

import androidx.annotation.StringRes
import com.fansauchiwa.R

/**
 * 編集を破棄した理由の選択肢（#265・一時的な調査。消し方は [DiscardReasonSurvey] の KDoc を参照）。
 *
 * @param paramValue answer_discard_reason の discard_reason に送る値。GA4 で集計するため、送り始めたら変えない
 * @param labelRes ダイアログに出す文言
 */
enum class DiscardReason(val paramValue: String, @param:StringRes val labelRes: Int) {
    NOT_AS_IMAGINED("not_as_imagined", R.string.discard_reason_not_as_imagined),
    NOT_ENOUGH_MATERIALS("not_enough_materials", R.string.discard_reason_not_enough_materials),
    NO_TIME("no_time", R.string.discard_reason_no_time),
    JUST_TRYING("just_trying", R.string.discard_reason_just_trying),
    OTHER("other", R.string.discard_reason_other),

    /** 「答えない」を押した、またはダイアログの外側・戻るキーで閉じた */
    NO_ANSWER("no_answer", R.string.discard_reason_no_answer);

    companion object {
        /** ダイアログに選択肢として並べるもの（「答えない」は別のボタンにするため除く） */
        val choices: List<DiscardReason> = entries.filter { it != NO_ANSWER }
    }
}
