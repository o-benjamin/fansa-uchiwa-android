package com.fansauchiwa.data

import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Remote Config の `daily_prompts`（JSON）から、日付ごとのお題を作る（#359）
 *
 * 形式：`{"prompts": [{"date": "2026-10-24", "id": "20261024", "text": "ピースして"}]}`
 *
 * `id` か `date` が欠けた項目、文言が空・[MAX_TEXT_LENGTH] 文字を超える・URL を含む項目は捨てる（お題に URL を入れない決まり）。
 * 同じ日付が2件あれば先の1件を使い、あとの1件は捨てる。`notify` は通知（#361）で使う予定で、ここでは読まない。
 * 設定の手順は `.agents/remote-config.md`。
 */
object DailyPromptsParser {

    /** お題の文言の上限。うちわの真ん中に1行で置いて読める長さ。コードポイントで数える */
    const val MAX_TEXT_LENGTH = 16

    /** 文言にこれが入っていたら URL とみなして捨てる */
    private val URL_MARKERS = listOf("://", "www.")

    private const val KEY_PROMPTS = "prompts"
    private const val KEY_DATE = "date"
    private const val KEY_ID = "id"
    private const val KEY_TEXT = "text"

    /**
     * @param json Remote Config の値
     * @throws IllegalArgumentException JSON として読めない・`prompts` が配列でない・項目があるのに1件も使えないとき
     *   （どれも設定の誤り。呼び出し側が Crashlytics に記録するので、メッセージは `daily_prompts` で始める）
     */
    fun parse(json: String): DailyPromptsConfig {
        if (json.isBlank()) return DailyPromptsConfig.Unset

        val root = parseJsonOrThrow(json) as? JsonObject
            ?: throw IllegalArgumentException("daily_prompts がオブジェクトではない")
        val items = root[KEY_PROMPTS] as? JsonArray
            ?: throw IllegalArgumentException("daily_prompts に prompts の配列がない")
        if (items.isEmpty()) return DailyPromptsConfig.Stopped

        val prompts = linkedMapOf<LocalDate, DailyPrompt>()
        val droppedPositions = mutableListOf<Int>()
        items.forEachIndexed { index, item ->
            val datedPrompt = (item as? JsonObject)?.toDatedPrompt()
            // 決まりに合わない項目と、同じ日付の2件目
            if (datedPrompt == null || datedPrompt.first in prompts) {
                droppedPositions += index + 1
            } else {
                prompts[datedPrompt.first] = datedPrompt.second
            }
        }
        // すべて決まりに合わないとき。黙って控えに戻ると、配ったのに出ない原因が追えない
        require(prompts.isNotEmpty()) {
            "daily_prompts の ${items.size} 件がすべて使えない（date・id・text を確かめる）"
        }
        return DailyPromptsConfig.Scheduled(prompts, droppedPositions)
    }

    private fun parseJsonOrThrow(json: String): JsonElement =
        try {
            Json.parseToJsonElement(json)
        } catch (e: SerializationException) {
            throw IllegalArgumentException("daily_prompts を JSON として読めない", e)
        }

    private fun JsonObject.toDatedPrompt(): Pair<LocalDate, DailyPrompt>? {
        val date = stringOrNull(KEY_DATE)?.let(::parseDateOrNull) ?: return null
        val id = stringOrNull(KEY_ID)?.takeIf { it.isNotBlank() } ?: return null
        val text = stringOrNull(KEY_TEXT)?.takeIf(::isAllowedText) ?: return null
        return date to DailyPrompt(id = id, text = text)
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim()

    private fun parseDateOrNull(date: String): LocalDate? =
        try {
            LocalDate.parse(date)
        } catch (e: DateTimeParseException) {
            null
        }

    /** `DailyPromptDefaultsTest` で、同梱の控えも同じ決まりに合うかを確かめるため internal にしている */
    internal fun isAllowedText(text: String): Boolean =
        text.isNotBlank() &&
            text.codePointCount(0, text.length) <= MAX_TEXT_LENGTH &&
            URL_MARKERS.none { text.contains(it, ignoreCase = true) }
}
