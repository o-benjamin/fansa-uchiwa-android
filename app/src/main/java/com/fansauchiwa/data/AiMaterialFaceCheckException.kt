package com.fansauchiwa.data

/**
 * 元画像に顔があるかを確かめられなかったときの例外。顔があるか分からない画像は送らないので、生成は失敗にする。
 * 原因（[cause]）は ML Kit のモジュールの取得の失敗（`BackgroundRemovalException`）や画像の読み込みの失敗で、
 * Crashlytics では背景の切り抜きの失敗と分けて見られるよう、この型で包む
 */
class AiMaterialFaceCheckException(cause: Throwable) :
    Exception("元画像の顔のチェックができなかった", cause)
