package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * Edit画面で使用されるカスタムセマンティクスプロパティキーを集約したファイル。
 * UIテストでセマンティクスツリー経由で値を検証する際に使用する。
 */

/**
 * ボーダー色をセマンティクスツリーに公開するためのカスタムプロパティキー。
 * UIテストでボーダー色を検証する際に使用する。
 */
val BorderColorKey = SemanticsPropertyKey<Color>("BorderColor")

var SemanticsPropertyReceiver.borderColor by BorderColorKey

