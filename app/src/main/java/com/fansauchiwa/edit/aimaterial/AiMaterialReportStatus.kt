package com.fansauchiwa.edit.aimaterial

/**
 * 結果の画面での報告（#339）の進み具合。素材を捨てると [NOT_SENT] に戻る
 */
enum class AiMaterialReportStatus {
    NOT_SENT,
    SENDING,
    SENT,
    FAILED
}
