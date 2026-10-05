package com.fansauchiwa.data

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AffiliateLinksParserTest {

    @Test
    fun parse_validItems_returnsLinksInOrder() {
        val json = """
            {"items": [
              {"id": "jumbo_uchiwa", "label": "ジャンボうちわ", "url": "https://www.amazon.co.jp/dp/B000000000?tag=test-22"},
              {"id": "mirror_sheet", "label": "ミラーシート", "url": "https://amzn.to/abc123"}
            ]}
        """.trimIndent()

        assertEquals(
            listOf(
                AffiliateLink("jumbo_uchiwa", "ジャンボうちわ", "https://www.amazon.co.jp/dp/B000000000?tag=test-22"),
                AffiliateLink("mirror_sheet", "ミラーシート", "https://amzn.to/abc123")
            ),
            AffiliateLinksParser.parse(json)
        )
    }

    @Test
    fun parse_emptyString_returnsEmptyList() {
        assertEquals(emptyList<AffiliateLink>(), AffiliateLinksParser.parse(""))
    }

    @Test
    fun parse_blankString_returnsEmptyList() {
        assertEquals(emptyList<AffiliateLink>(), AffiliateLinksParser.parse("  \n"))
    }

    @Test
    fun parse_emptyItems_returnsEmptyList() {
        assertEquals(emptyList<AffiliateLink>(), AffiliateLinksParser.parse("""{"items": []}"""))
    }

    @Test
    fun parse_malformedJson_throwsSerializationException() {
        assertThrows(SerializationException::class.java) {
            AffiliateLinksParser.parse("""{"items": [""")
        }
    }

    @Test
    fun parse_rootIsArray_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            AffiliateLinksParser.parse("""[{"id": "a", "label": "A", "url": "https://amzn.to/a"}]""")
        }
    }

    @Test
    fun parse_itemsMissing_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            AffiliateLinksParser.parse("""{"links": []}""")
        }
    }

    @Test
    fun parse_httpUrl_throwsIllegalArgumentException() {
        val json = """{"items": [{"id": "a", "label": "A", "url": "http://www.amazon.co.jp/dp/B000000000"}]}"""

        assertThrows(IllegalArgumentException::class.java) { AffiliateLinksParser.parse(json) }
    }

    @Test
    fun parse_allItemsUnusable_throwsIllegalArgumentException() {
        val json = """
            {"items": [
              {"id": "a", "label": "A", "url": "https://example.com/a"},
              {"label": "idなし", "url": "https://amzn.to/b"}
            ]}
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) { AffiliateLinksParser.parse(json) }
    }

    @Test
    fun parse_hostNotAllowed_skipsOnlyThatItem() {
        val json = """
            {"items": [
              {"id": "other", "label": "ほかのお店", "url": "https://example.com/uchiwa"},
              {"id": "lookalike", "label": "似たホスト", "url": "https://amazon.co.jp.example.com/dp/B000000000"},
              {"id": "ok", "label": "うちわ", "url": "https://amzn.asia/d/abc"}
            ]}
        """.trimIndent()

        assertEquals(
            listOf(AffiliateLink("ok", "うちわ", "https://amzn.asia/d/abc")),
            AffiliateLinksParser.parse(json)
        )
    }

    @Test
    fun parse_upperCaseHost_acceptsItem() {
        val json = """{"items": [{"id": "a", "label": "A", "url": "https://WWW.AMAZON.CO.JP/dp/B000000000"}]}"""

        assertEquals(1, AffiliateLinksParser.parse(json).size)
    }

    @Test
    fun parse_japaneseSearchQuery_acceptsItem() {
        val url = "https://www.amazon.co.jp/s?k=ミラーシート+うちわ&tag=test-22"
        val json = """{"items": [{"id": "mirror_sheet", "label": "ミラーシート", "url": "$url"}]}"""

        assertEquals(listOf(AffiliateLink("mirror_sheet", "ミラーシート", url)), AffiliateLinksParser.parse(json))
    }

    @Test
    fun parse_missingOrBlankFields_skipsItems() {
        val json = """
            {"items": [
              {"label": "idなし", "url": "https://amzn.to/a"},
              {"id": " ", "label": "idが空白", "url": "https://amzn.to/b"},
              {"id": "no_label", "url": "https://amzn.to/c"},
              {"id": "no_url", "label": "URLなし"},
              {"id": 1, "label": "idが数値", "url": "https://amzn.to/d"},
              "文字列の項目",
              {"id": "ok", "label": "うちわ", "url": "https://amzn.to/e"}
            ]}
        """.trimIndent()

        assertEquals(
            listOf(AffiliateLink("ok", "うちわ", "https://amzn.to/e")),
            AffiliateLinksParser.parse(json)
        )
    }

    @Test
    fun parse_fieldsWithSurroundingSpaces_trimsValues() {
        val json = """{"items": [{"id": " a ", "label": " うちわ ", "url": " https://amzn.to/a "}]}"""

        assertEquals(listOf(AffiliateLink("a", "うちわ", "https://amzn.to/a")), AffiliateLinksParser.parse(json))
    }

    @Test
    fun parse_duplicateIds_keepsFirst() {
        val json = """
            {"items": [
              {"id": "a", "label": "1つ目", "url": "https://amzn.to/1"},
              {"id": "a", "label": "2つ目", "url": "https://amzn.to/2"}
            ]}
        """.trimIndent()

        assertEquals(listOf(AffiliateLink("a", "1つ目", "https://amzn.to/1")), AffiliateLinksParser.parse(json))
    }

    @Test
    fun parse_moreThanMaxLinks_returnsFirstMaxLinks() {
        val items = (1..AffiliateLinksParser.MAX_LINKS + 1).joinToString(",") {
            """{"id": "item$it", "label": "材料$it", "url": "https://amzn.to/$it"}"""
        }

        val links = AffiliateLinksParser.parse("""{"items": [$items]}""")

        assertEquals((1..AffiliateLinksParser.MAX_LINKS).map { "item$it" }, links.map { it.id })
    }

    @Test
    fun parse_invalidItemsBeforeMax_countsOnlyValidItems() {
        val json = """
            {"items": [
              {"id": "bad", "label": "許可しない", "url": "https://example.com/"},
              {"id": "a", "label": "A", "url": "https://amzn.to/a"},
              {"id": "b", "label": "B", "url": "https://amzn.to/b"},
              {"id": "c", "label": "C", "url": "https://amzn.to/c"}
            ]}
        """.trimIndent()

        assertEquals(listOf("a", "b", "c"), AffiliateLinksParser.parse(json).map { it.id })
    }

    @Test
    fun parse_unknownFields_ignoresThem() {
        val json = """{"version": 2, "items": [{"id": "a", "label": "A", "url": "https://amzn.to/a", "note": "メモ"}]}"""

        assertEquals(listOf(AffiliateLink("a", "A", "https://amzn.to/a")), AffiliateLinksParser.parse(json))
    }
}
