package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * Edit 画面の presentation 層で使用するセマンティクスキーを集約する。
 *
 * セマンティクスは Compose UI 固有の関心事なので、このファイルに閉じ込めて
 * Screen / ViewModel / Domain へ責務が漏れないようにする。
 */
object EditSemanticsKeys {
    private const val BORDER_COLOR_NAME = "Edit.BorderColor"

    /**
     * 選択中アイテムのボーダー色を UI テストへ公開するためのキー。
     */
    val BorderColor = SemanticsPropertyKey<Color>(BORDER_COLOR_NAME)
}

/**
 * 既存の UI テスト互換のために公開しているエイリアス。
 */
val BorderColorKey: SemanticsPropertyKey<Color> = EditSemanticsKeys.BorderColor

/**
 * 選択中アイテムのボーダー色をセマンティクスへ公開するための拡張プロパティ。
 */
var SemanticsPropertyReceiver.borderColor by BorderColorKey
