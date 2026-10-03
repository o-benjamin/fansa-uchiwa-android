package com.fansauchiwa.edit.pager

import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.DecorationTabType
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TabPageToSyncWithSelectionTest {
    private val text = Decoration.Text(id = "text-1", font = FontFamilies.NOTO_SANS_JP)
    private val sticker = Decoration.Sticker(label = "sticker", id = "sticker-1")
    private val image = Decoration.Image(id = "image-1", imageId = "img")

    @Test
    fun tabPageToSyncWithSelection_StickerSelectedOnOtherTab_ReturnsStampPage() {
        val page = tabPageToSyncWithSelection(sticker, DecorationTabType.TEXT.ordinal)

        assertEquals(DecorationTabType.STAMP.ordinal, page)
    }

    @Test
    fun tabPageToSyncWithSelection_TextSelectedOnOtherTab_ReturnsTextPage() {
        val page = tabPageToSyncWithSelection(text, DecorationTabType.STAMP.ordinal)

        assertEquals(DecorationTabType.TEXT.ordinal, page)
    }

    @Test
    fun tabPageToSyncWithSelection_ImageSelectedOnOtherTab_ReturnsImagePage() {
        val page = tabPageToSyncWithSelection(image, DecorationTabType.TEXT.ordinal)

        assertEquals(DecorationTabType.IMAGE.ordinal, page)
    }

    @Test
    fun tabPageToSyncWithSelection_SelectedOnLayerTab_ReturnsNull() {
        assertNull(tabPageToSyncWithSelection(sticker, DecorationTabType.LAYERS.ordinal))
        assertNull(tabPageToSyncWithSelection(text, DecorationTabType.LAYERS.ordinal))
        assertNull(tabPageToSyncWithSelection(image, DecorationTabType.LAYERS.ordinal))
    }

    @Test
    fun tabPageToSyncWithSelection_NothingSelected_ReturnsNull() {
        assertNull(tabPageToSyncWithSelection(null, DecorationTabType.TEXT.ordinal))
    }
}
