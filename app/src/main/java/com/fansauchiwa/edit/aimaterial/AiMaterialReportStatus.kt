package com.fansauchiwa.edit.aimaterial

/**
 * 結果の画面での報告（#339）の進み具合。素材を捨てると [NOT_SENT] に戻る。
 * [FAILED] は押し直せる失敗、[LIMIT_REACHED] は1人1日の上限に当たって今日は送れないとき
 */
enum class AiMaterialReportStatus {
    NOT_SENT,
    SENDING,
    SENT,
    FAILED,
    LIMIT_REACHED
}
