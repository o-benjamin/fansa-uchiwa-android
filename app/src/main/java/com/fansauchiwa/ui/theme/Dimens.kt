package com.fansauchiwa.ui.theme

import androidx.compose.ui.unit.dp

/**
 * アプリ全体で使用するサイズ定数。
 *
 * 原則として 8-Point Grid System（8の倍数）を適用する。
 * 細かい調整が必要な箇所（テキスト周りなど）は 4 の倍数を許容する。
 * 外部 SDK の仕様など変更不可な固定値については、その旨をコメントで明記する。
 *
 * このオブジェクトはプレゼンテーション層の定数定義のみを担い、
 * ビジネスロジックや ViewModel の責務は一切持たない。（PDS 遵守）
 */
object Dimens {

    // ---- Stroke / Border ----

    /** ガイドライン・境界線など最細のストローク */
    val StrokeThin = 1.dp

    /** 強調ボーダーの線幅 */
    val StrokeThick = 2.dp

    /** プログレスインジケーターなど太い線幅 */
    val StrokeExtraThick = 4.dp

    // ---- Spacing (8-Point Grid) ----

    /** 4dp: テキスト周りや細かい調整に使用する最小余白 */
    val SpacingXxs = 4.dp

    /** 8dp: 標準的な小余白 */
    val SpacingXs = 8.dp

    /** 12dp: グリッド要素間の中間余白 */
    val SpacingS = 12.dp

    /** 16dp: 標準余白 */
    val SpacingM = 16.dp

    /** 24dp: セクション間・やや広い余白 */
    val SpacingL = 24.dp

    /** 32dp: ページ内パディングなど広い余白 */
    val SpacingXl = 32.dp

    /** 48dp: 大きなセクション余白 */
    val SpacingXxl = 48.dp

    /** 64dp: 画面レベルの大きな余白 */
    val SpacingXxxl = 64.dp

    // ---- Corner Radius ----

    /** 小角丸: カード・チップなど */
    val CornerS = 8.dp

    /** 中角丸: ダイアログ・入力バー・カラープレビューなど */
    val CornerM = 16.dp

    /** 大角丸: 画像・サムネイルなど */
    val CornerL = 24.dp

    /**
     * FAB 専用角丸: [FabSize] の半径に合わせた値。
     * FAB サイズ (56dp) の半分として設計上の意図をもつ固定値。
     */
    val CornerFab = 28.dp

    /** 最大角丸: タイムラインカードなど大きめの丸み */
    val CornerXl = 32.dp

    // ---- Icon Size ----

    /** XS アイコン: 小さなオーバーレイ・リストアイコンなど */
    val IconXs = 16.dp

    /** S アイコン: Material 標準サイズ */
    val IconS = 24.dp

    /** M アイコン: サムネイル付きアイコンなど */
    val IconM = 32.dp

    /** L アイコン: 装飾ハンドルアイコン・スライダーラベルなど */
    val IconL = 36.dp

    // ---- Component / Touch Target Size ----

    /** 標準タッチターゲット / ローディングインジケーター */
    val ComponentM = 48.dp

    /** FAB・主要アクションボタンの高さ */
    val FabSize = 56.dp

    // ---- Thumbnail / Image Size ----

    /** デコレーション画像のデフォルトサイズ */
    val ImageDefault = 64.dp

    /** タイムラインサムネイル (小) */
    val ThumbnailS = 96.dp

    /** カラーピッカーのプレビュー幅 */
    val ColorPreviewWidth = 104.dp

    /** ダイアログ内グリッドサムネイルの高さ */
    val ThumbnailDialogHeight = 112.dp

    /** 画像デコレーションのキャンバス表示サイズ */
    val ImageDecorationDefault = 120.dp

    /** グリッドセル幅 (標準) */
    val GridCellWidth = 128.dp

    /** グリッドセル幅 (ワイド) / ホーム画面カード幅 */
    val GridCellWidthWide = 152.dp

    // ---- Layout / Screen ----

    /** タイムライン FAB の最大幅 */
    val FabMaxWidth = 240.dp

    /** ダイアログ内グリッドの高さ */
    val DialogGridHeight = 280.dp

    /** ホーム画面空状態の最小高さ */
    val HomeEmptyMinHeight = 300.dp

    // ---- Ad Banner ----

    /**
     * AdMob バナー広告の標準高さ。
     * Google AdMob SDK の仕様に基づく固定値のため、8-Point Grid の例外として許容する。
     */
    val AdBannerHeight = 50.dp
}
