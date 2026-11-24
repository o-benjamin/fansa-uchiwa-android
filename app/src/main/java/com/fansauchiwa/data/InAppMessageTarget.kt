package com.fansauchiwa.data

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）のボタンの行き先。
 * キャンペーンのボタンの URL（Firebase コンソールで入れる）から決める。書き方は `.agents/in-app-messaging.md`
 *
 * @param analyticsValue 計測（`tap_in_app_message` の `target`）で送る値
 */
enum class InAppMessageTarget(val analyticsValue: String) {
    /** `fansauchiwa://store`。アプリが Play ストアのアプリのページを開く */
    STORE("store"),

    /** `https://`・`http://`。Firebase In-App Messaging がブラウザ（Custom Tabs）で開く */
    WEB("web"),

    /** `fansauchiwa://home`。メッセージを閉じてホームのまま */
    HOME("home"),

    /** `fansauchiwa://daily_prompt`。今日のお題のカードの「このお題で作る」と同じ */
    DAILY_PROMPT("daily_prompt"),

    /** `fansauchiwa://ai_material`。今日のお題のカードの「AIで素材を作る」と同じ */
    AI_MATERIAL("ai_material"),

    /** 上のどれでもない URL（書き間違い、この版が知らない行き先など）。何も開かない */
    OTHER("other");

    companion object {
        const val APP_SCHEME = "fansauchiwa"

        /**
         * アプリの画面の行き先は、ほかのアプリから開かれないよう、どの Activity にも intent-filter を置かない scheme にしている。
         * Firebase In-App Messaging は開けるアプリのない URL をログに残すだけで開かないので、アプリのクリックのリスナーだけが開く
         */
        fun fromActionUrl(actionUrl: String): InAppMessageTarget {
            // android.net.Uri はユニットテストで動かず、java.net.URI は `_` を含むホスト名を読めないので、文字列で分ける
            val trimmed = actionUrl.trim()
            if (!trimmed.contains("://")) return OTHER
            return when (trimmed.substringBefore("://").lowercase()) {
                "https", "http" -> WEB
                APP_SCHEME -> fromAppHost(trimmed.substringAfter("://").takeWhile { it !in "/?#" })
                else -> OTHER
            }
        }

        private fun fromAppHost(host: String): InAppMessageTarget = when (host.lowercase()) {
            STORE.analyticsValue -> STORE
            HOME.analyticsValue -> HOME
            DAILY_PROMPT.analyticsValue -> DAILY_PROMPT
            AI_MATERIAL.analyticsValue -> AI_MATERIAL
            else -> OTHER
        }
    }
}
