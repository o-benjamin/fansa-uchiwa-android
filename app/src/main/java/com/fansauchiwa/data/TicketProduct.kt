package com.fansauchiwa.data

/**
 * 回数券（Play Console のアプリ内アイテム。#348）。
 * [formattedPrice] は Play が利用者の国の通貨で作った表示用の値段で、アプリには値段を書かない（公開後に Play の価格実験で変えるため）
 */
data class TicketProduct(val formattedPrice: String)
