package com.fansauchiwa.data

import java.time.Instant

/**
 * サーバーが購入を確かめて足した回数券（`verifyPurchase` の応答）。
 * [alreadyGranted] は、前の呼び出しで足し済みだった（応答が届かずに送り直したときなど）ことを表し、[ticketsAdded] は変わらない
 */
data class GrantedTickets(
    val ticketsAdded: Int,
    val ticketsRemaining: Int,
    val expiresAt: Instant,
    val alreadyGranted: Boolean
)
