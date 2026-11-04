package com.fansauchiwa.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPromptsParserTest {

    private val oct24: LocalDate = LocalDate.of(2026, 10, 24)
    private val oct25: LocalDate = LocalDate.of(2026, 10, 25)
    private val oct26: LocalDate = LocalDate.of(2026, 10, 26)

    @Test
    fun parse_validPrompts_returnsScheduledPromptsByDate() {
        val json = """
            {"notify": true, "prompts": [
              {"date": "2026-10-24", "id": "20261024", "text": "ピースして"},
              {"date": "2026-10-25", "id": "20261025", "text": "手ふって"}
            ]}
        """.trimIndent()

        assertEquals(
            DailyPromptsConfig.Scheduled(
                mapOf(
                    oct24 to DailyPrompt("20261024", "ピースして"),
                    oct25 to DailyPrompt("20261025", "手ふって")
                )
            ),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_emptyString_returnsUnset() {
        assertEquals(DailyPromptsConfig.Unset, DailyPromptsParser.parse(""))
    }

    @Test
    fun parse_blankString_returnsUnset() {
        assertEquals(DailyPromptsConfig.Unset, DailyPromptsParser.parse("  \n"))
    }

    @Test
    fun parse_emptyPrompts_returnsStopped() {
        assertEquals(DailyPromptsConfig.Stopped, DailyPromptsParser.parse("""{"prompts": []}"""))
    }

    @Test
    fun parse_fieldsWithSurroundingSpaces_trimsIdAndText() {
        val json = """{"prompts": [{"date": "2026-10-24", "id": " a ", "text": "  ピースして  "}]}"""

        assertEquals(
            DailyPromptsConfig.Scheduled(mapOf(oct24 to DailyPrompt("a", "ピースして"))),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_textAtMaxLength_keepsItem() {
        val text = "あ".repeat(DailyPromptsParser.MAX_TEXT_LENGTH)
        val json = """{"prompts": [{"date": "2026-10-24", "id": "a", "text": "$text"}]}"""

        assertEquals(
            DailyPromptsConfig.Scheduled(mapOf(oct24 to DailyPrompt("a", text))),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_textOverMaxLength_dropsItem() {
        val json = """
            {"prompts": [
              {"date": "2026-10-24", "id": "long", "text": "${"あ".repeat(DailyPromptsParser.MAX_TEXT_LENGTH + 1)}"},
              {"date": "2026-10-25", "id": "ok", "text": "手ふって"}
            ]}
        """.trimIndent()

        assertEquals(
            DailyPromptsConfig.Scheduled(mapOf(oct25 to DailyPrompt("ok", "手ふって")), droppedPositions = listOf(1)),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_singleCodePointEmojiAtMaxLength_keepsItem() {
        // 1コードポイントの絵文字は UTF-16 で2つ分だが、1文字として数える
        val text = "😀".repeat(DailyPromptsParser.MAX_TEXT_LENGTH)
        val json = """{"prompts": [{"date": "2026-10-24", "id": "a", "text": "$text"}]}"""

        assertEquals(
            DailyPromptsConfig.Scheduled(mapOf(oct24 to DailyPrompt("a", text))),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_textWithUrl_dropsItem() {
        val json = """
            {"prompts": [
              {"date": "2026-10-24", "id": "scheme", "text": "https://a.jp"},
              {"date": "2026-10-25", "id": "www", "text": "WWW.example"},
              {"date": "2026-10-26", "id": "ok", "text": "ピースして"}
            ]}
        """.trimIndent()

        assertEquals(
            DailyPromptsConfig.Scheduled(mapOf(oct26 to DailyPrompt("ok", "ピースして")), droppedPositions = listOf(1, 2)),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_itemsMissingFields_dropsThemWithPositions() {
        val json = """
            {"prompts": [
              {"date": "2026-10-20", "text": "idなし"},
              {"date": "2026-10-21", "id": "", "text": "id空"},
              {"date": "2026-10-22", "id": "no_text"},
              {"date": "2026-10-23", "id": "blank_text", "text": "  "},
              {"id": "no_date", "text": "日付なし"},
              {"date": "2026/10/24", "id": "bad_date", "text": "日付の形が違う"},
              {"date": "2026-10-25", "id": 1, "text": "idが数"},
              "文字列だけ",
              {"date": "2026-10-26", "id": "ok", "text": "ピースして"}
            ]}
        """.trimIndent()

        assertEquals(
            DailyPromptsConfig.Scheduled(
                mapOf(oct26 to DailyPrompt("ok", "ピースして")),
                droppedPositions = listOf(1, 2, 3, 4, 5, 6, 7, 8)
            ),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_duplicateDate_keepsFirstAndDropsSecond() {
        val json = """
            {"prompts": [
              {"date": "2026-10-24", "id": "first", "text": "ピースして"},
              {"date": "2026-10-24", "id": "second", "text": "手ふって"}
            ]}
        """.trimIndent()

        assertEquals(
            DailyPromptsConfig.Scheduled(mapOf(oct24 to DailyPrompt("first", "ピースして")), droppedPositions = listOf(2)),
            DailyPromptsParser.parse(json)
        )
    }

    @Test
    fun parse_allItemsUnusable_throwsIllegalArgumentException() {
        val json = """{"prompts": [{"date": "2026-10-24", "id": "a", "text": "https://a.jp"}]}"""

        assertThrows(IllegalArgumentException::class.java) { DailyPromptsParser.parse(json) }
    }

    @Test
    fun parse_missingPrompts_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) { DailyPromptsParser.parse("""{"notify": false}""") }
    }

    @Test
    fun parse_promptsNotArray_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) { DailyPromptsParser.parse("""{"prompts": {}}""") }
    }

    @Test
    fun parse_rootNotObject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) { DailyPromptsParser.parse("""[]""") }
    }

    @Test
    fun parse_malformedJson_throwsIllegalArgumentExceptionNamingParameter() {
        val error = assertThrows(IllegalArgumentException::class.java) { DailyPromptsParser.parse("""{"prompts": [""") }

        // Crashlytics で見分けられるよう、メッセージはパラメータ名で始める
        assertTrue(error.message!!.startsWith("daily_prompts"))
    }
}
