package com.fansauchiwa.data.repository

import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EditDecorationRepositoryTest {

    private class FakeUuidProvider(
        private val ids: ArrayDeque<String>
    ) : UuidProvider {
        override fun generate(): String = ids.removeFirst()
    }

    @Test
    fun createText_generatesTextDecorationWithProvidedFont() {
        val repository = EditDecorationRepositoryImpl(
            uuidProvider = FakeUuidProvider(ArrayDeque(listOf("text-id")))
        )

        val decoration = repository.createText(FontFamilies.HACHI_MARU_POP)

        assertEquals("text-id", decoration.id)
        assertEquals(FontFamilies.HACHI_MARU_POP, decoration.font)
    }

    @Test
    fun createSticker_generatesStickerDecorationWithProvidedLabel() {
        val repository = EditDecorationRepositoryImpl(
            uuidProvider = FakeUuidProvider(ArrayDeque(listOf("sticker-id")))
        )

        val decoration = repository.createSticker("heart")

        assertEquals("sticker-id", decoration.id)
        assertEquals("heart", decoration.label)
    }

    @Test
    fun createImage_generatesImageDecorationWithProvidedImageId() {
        val repository = EditDecorationRepositoryImpl(
            uuidProvider = FakeUuidProvider(ArrayDeque(listOf("image-decoration-id")))
        )

        val decoration = repository.createImage("image-id")

        assertEquals("image-decoration-id", decoration.id)
        assertEquals("image-id", decoration.imageId)
    }

    @Test
    fun moveDecorations_reordersUsingDisplayedLayerIndexes() {
        val repository = EditDecorationRepositoryImpl(
            uuidProvider = FakeUuidProvider(ArrayDeque())
        )
        val text = Decoration.Text(
            id = "text-id",
            font = FontFamilies.HACHI_MARU_POP
        )
        val sticker = Decoration.Sticker(
            id = "sticker-id",
            label = "heart"
        )
        val image = Decoration.Image(
            id = "image-id",
            imageId = "source-image-id"
        )

        val moved = repository.moveDecorations(
            decorations = listOf(text, sticker, image),
            fromIndex = 0,
            toIndex = 2
        )

        assertEquals(listOf(image, text, sticker), moved)
    }

    @Test
    fun moveDecorations_sameIndexes_returnsOriginalInstance() {
        val repository = EditDecorationRepositoryImpl(
            uuidProvider = FakeUuidProvider(ArrayDeque())
        )
        val decorations = listOf(
            Decoration.Text(
                id = "text-id",
                font = FontFamilies.HACHI_MARU_POP
            )
        )

        val moved = repository.moveDecorations(
            decorations = decorations,
            fromIndex = 0,
            toIndex = 0
        )

        assertSame(decorations, moved)
    }
}
