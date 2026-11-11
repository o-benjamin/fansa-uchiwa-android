package com.fansauchiwa.data

/**
 * 回数券のアイテムが Play で見つからないときの例外（Play Console で作っていない・有効でない、Play の国で売っていないなど）
 */
class TicketProductUnavailableException : Exception("回数券のアイテムが Play で見つからない")
