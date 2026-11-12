package com.fansauchiwa.edit.aimaterial

/**
 * 購入の案内に出す、前に買おうとしたときの結果（[AiMaterialPurchase.Ready.problem]）
 */
enum class AiMaterialPurchaseProblem {
    /** 支払いがまだ済んでいない（コンビニ払いなど）。済んだあとに画面を開き直すと足す */
    PENDING,

    /** サーバーが回数を足すのを断り、開き直しても足せない（ほかの利用者の購入・取り消された購入など）。連絡先を出す */
    REJECTED,

    /** Play の課金がエラーを返した（支払いはしていない） */
    PLAY_ERROR,

    /** 通信できない・サーバーが Play に聞けなかったなど。支払いが済んでいれば、画面を開き直すと足す */
    FAILED,

    /** 前の購入が Play に残っていて買えなかった。その購入は足し済みで、今回はお金も回数も動いていない。時間をおくとまた買える */
    PREVIOUS_PURCHASE_REMAINING
}
