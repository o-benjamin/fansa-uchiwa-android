package com.fansauchiwa.data

import org.junit.Assert.assertEquals
import org.junit.Test

class InAppMessageTargetTest {

    @Test
    fun fromActionUrl_appScreens_returnsEachTarget() {
        assertEquals(InAppMessageTarget.STORE, InAppMessageTarget.fromActionUrl("fansauchiwa://store"))
        assertEquals(InAppMessageTarget.HOME, InAppMessageTarget.fromActionUrl("fansauchiwa://home"))
        assertEquals(InAppMessageTarget.DAILY_PROMPT, InAppMessageTarget.fromActionUrl("fansauchiwa://daily_prompt"))
        assertEquals(InAppMessageTarget.AI_MATERIAL, InAppMessageTarget.fromActionUrl("fansauchiwa://ai_material"))
    }

    @Test
    fun fromActionUrl_appScreenWithCaseSlashQueryOrFragment_returnsTarget() {
        // コンソールで大文字や末尾の / を付けても、同じ行き先にする
        assertEquals(InAppMessageTarget.STORE, InAppMessageTarget.fromActionUrl("FansaUchiwa://Store/"))
        assertEquals(InAppMessageTarget.DAILY_PROMPT, InAppMessageTarget.fromActionUrl(" fansauchiwa://daily_prompt?from=v2.13.0 "))
        assertEquals(InAppMessageTarget.STORE, InAppMessageTarget.fromActionUrl("fansauchiwa://store#v2.13.0"))
    }

    @Test
    fun fromActionUrl_httpOrHttps_returnsWeb() {
        assertEquals(InAppMessageTarget.WEB, InAppMessageTarget.fromActionUrl("https://fansa-uchiwa.web.app/news/v2.13.0.html"))
        assertEquals(InAppMessageTarget.WEB, InAppMessageTarget.fromActionUrl("http://example.com"))
        // Play ストアの https の URL は Custom Tabs で開くので、Web として数える（ストアは fansauchiwa://store で書く）
        assertEquals(
            InAppMessageTarget.WEB,
            InAppMessageTarget.fromActionUrl("https://play.google.com/store/apps/details?id=com.fansauchiwa")
        )
    }

    @Test
    fun fromActionUrl_unknownOrEmpty_returnsOther() {
        assertEquals(InAppMessageTarget.OTHER, InAppMessageTarget.fromActionUrl("fansauchiwa://timeline"))
        assertEquals(InAppMessageTarget.OTHER, InAppMessageTarget.fromActionUrl("fansauchiwa://"))
        assertEquals(InAppMessageTarget.OTHER, InAppMessageTarget.fromActionUrl("market://details?id=com.fansauchiwa"))
        assertEquals(InAppMessageTarget.OTHER, InAppMessageTarget.fromActionUrl("store"))
        assertEquals(InAppMessageTarget.OTHER, InAppMessageTarget.fromActionUrl(""))
    }
}
