package com.fansauchiwa.data

/**
 * 理由つきで断ったときの例外。理由のない失敗（通信できない・時間切れなど）はこの例外にしない
 */
class AiMaterialRejectedException(val reason: AiMaterialRejectReason) :
    Exception("AI素材の生成を断られた: ${reason.serverValue}")
