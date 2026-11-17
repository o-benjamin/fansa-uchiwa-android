package com.fansauchiwa.data

/**
 * サーバーに頼んだあとに理由の分からない失敗をしたときの例外（通信の切断・時間切れ・応答の読み取りや端末への書き込みの失敗）。
 * サーバーが作り終えて回数を減らしたあとかもしれないので、送る前の失敗と分けて「作れたか確かめられなかった」と出す（#387）。
 * 原因は [cause]。理由つきで断られたときは `AiMaterialRejectedException` のままにして、この型で包まない
 */
class AiMaterialUnconfirmedException(cause: Throwable) :
    Exception("AI素材を頼んだあとに失敗し、作れたか確かめられなかった", cause)
