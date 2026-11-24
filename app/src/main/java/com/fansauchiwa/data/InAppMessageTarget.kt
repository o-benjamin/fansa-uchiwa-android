package com.fansauchiwa.data

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）のボタンの行き先。
 * キャンペーンのボタンの URL（Firebase コンソールで入れる）から決める。書き方は `.agents/in-app-messaging.md`
 *
 * @param appHost `fansauchiwa://` のときのホスト名。キャンペーンのボタンの URL に書く値なので、公開中のキャンペーンがあるうちは変えない
 * @param analyticsValue 計測（`tap_in_app_message` の `target`）で送る値
 */
enum class InAppMessageTarget(val appHost: String?, val analyticsValue: String) {
    /** `fansauchiwa://store`。アプリが Play ストアのアプリのページを開く */
    STORE(appHost = "store", analyticsValue = "store"),

    /** `https://`・`http://`。Firebase In-App Messaging がブラウザ（Custom Tabs）で開く */
    WEB(appHost = null, analyticsValue = "web"),

    /** `fansauchiwa://home`。メッセージを閉じてホームのまま */
    HOME(appHost = "home", analyticsValue = "home"),

    /** `fansauchiwa://daily_prompt`。今日のお題のカードの「このお題で作る」と同じ */
    DAILY_PROMPT(appHost = "daily_prompt", analyticsValue = "daily_prompt"),

    /** `fansauchiwa://ai_material`。今日のお題のカードの「AIで素材を作る」と同じ */
    AI_MATERIAL(appHost = "ai_material", analyticsValue = "ai_material"),

    /** 上のどれでもない URL（書き間違い、この版が知らない行き先など）。何も開かない */
    OTHER(appHost = null, analyticsValue = "other");

    companion object {
        /**
         * アプリの画面の行き先の scheme。ほかのアプリから開かれないよう、どの Activity にも intent-filter を置かない。
         * Firebase In-App Messaging は開けるアプリのない URL をログに残すだけで開かないので、アプリのクリックのリスナーだけが開く
         */
        const val APP_SCHEME = "fansauchiwa"

        /**
         * キャンペーンのボタンの URL から行き先を決める。読めない URL・知らない行き先・空文字は [OTHER]
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

        private fun fromAppHost(host: String): InAppMessageTarget =
            entries.firstOrNull { it.appHost == host.lowercase() } ?: OTHER
    }
}
