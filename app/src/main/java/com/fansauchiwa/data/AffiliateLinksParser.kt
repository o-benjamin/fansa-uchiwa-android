package com.fansauchiwa.data

import java.net.URI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Remote Config の `affiliate_materials`（JSON）から、保存完了のダイアログに出すリンクを作る（#311）
 *
 * 形式：`{"items": [{"id": "...", "label": "...", "url": "https://www.amazon.co.jp/..."}]}`
 *
 * 設定を誤っても任意の URL を開かないよう、[ALLOWED_HOSTS] の https の URL だけを通す。
 * 項目が足りない・許可しない URL の項目は捨てる。`id` が重複した項目は先の1件だけ残す（計測の `affiliate_item` を一意にするため）。
 * 残りを先頭から [MAX_LINKS] 件まで返す。
 * 設定の手順は `.agents/remote-config.md`。
 */
object AffiliateLinksParser {

    /** Amazon アソシエイトのリンクとその短縮 URL のホスト */
    internal val ALLOWED_HOSTS = setOf("amazon.co.jp", "www.amazon.co.jp", "amzn.to", "amzn.asia")

    /** ダイアログが長くなりすぎないよう、出すのは3件まで */
    internal const val MAX_LINKS = 3

    private const val KEY_ITEMS = "items"
    private const val KEY_ID = "id"
    private const val KEY_LABEL = "label"
    private const val KEY_URL = "url"

    /**
     * @param json Remote Config の値。空（未設定）なら空のリストを返す
     * @throws IllegalArgumentException `items` が配列でないとき、または項目があるのに1件も使えないとき
     *   （どちらも設定の誤り。呼び出し側が Crashlytics に記録する）
     * @throws kotlinx.serialization.SerializationException JSON として読めないとき
     */
    fun parse(json: String): List<AffiliateLink> {
        if (json.isBlank()) return emptyList()

        val root = Json.parseToJsonElement(json) as? JsonObject
            ?: throw IllegalArgumentException("affiliate_materials がオブジェクトではない")
        val items = root[KEY_ITEMS] as? JsonArray
            ?: throw IllegalArgumentException("affiliate_materials に items の配列がない")

        val links = items
            .mapNotNull { (it as? JsonObject)?.toAffiliateLink() }
            .distinctBy { it.id }
            .take(MAX_LINKS)
        // 全項目が https でない・許可しないホストなどのとき。黙って出さないと、設定したのに出ない原因が追えない
        require(items.isEmpty() || links.isNotEmpty()) {
            "affiliate_materials の ${items.size} 件がすべて使えない（id・label・URL を確かめる）"
        }
        return links
    }

    private fun JsonObject.toAffiliateLink(): AffiliateLink? {
        val id = stringOrNull(KEY_ID)?.takeIf { it.isNotBlank() } ?: return null
        val label = stringOrNull(KEY_LABEL)?.takeIf { it.isNotBlank() } ?: return null
        val url = stringOrNull(KEY_URL)?.takeIf(::isAllowedUrl) ?: return null
        return AffiliateLink(id = id, label = label, url = url)
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim()

    private fun isAllowedUrl(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        return uri.scheme == "https" && uri.host?.lowercase() in ALLOWED_HOSTS
    }
}
