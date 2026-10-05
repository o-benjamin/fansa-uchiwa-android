package com.fansauchiwa.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 保存完了のダイアログに出す、うちわの材料のアフィリエイトのリンク（#311）
 *
 * アプリには書かず、Remote Config の値から作る（[AffiliateLinksParser]）。
 *
 * @param id 計測に使う英小文字の名前（`tap_preview_affiliate` の `affiliate_item`）
 * @param label 画面に出す材料の名前
 * @param url 開く先。[AffiliateLinksParser] が許可したホストの https の URL だけが入る
 */
@Parcelize
data class AffiliateLink(
    val id: String,
    val label: String,
    val url: String
) : Parcelable
