package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * ボーダー色をセマンティクスツリーに公開するためのカスタムプロパティキー。
 * UIテストでボーダー色を検証する際に使用する。
 */
val BorderColorKey = SemanticsPropertyKey<Color>("BorderColor")

var SemanticsPropertyReceiver.borderColor by BorderColorKey

