package com.fansauchiwa.data.repository

import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.edit.FontFamilies
import javax.inject.Inject

interface EditDecorationRepository {
    fun createText(font: FontFamilies): Decoration.Text

    fun createSticker(label: String): Decoration.Sticker

    fun createImage(imageId: String): Decoration.Image

    fun moveDecorations(
        decorations: List<Decoration>,
        fromIndex: Int,
        toIndex: Int
    ): List<Decoration>
}

class EditDecorationRepositoryImpl @Inject constructor(
    private val uuidProvider: UuidProvider
) : EditDecorationRepository {

    override fun createText(font: FontFamilies): Decoration.Text {
        return Decoration.Text(
            id = uuidProvider.generate(),
            font = font
        )
    }

    override fun createSticker(label: String): Decoration.Sticker {
        return Decoration.Sticker(
            label = label,
            id = uuidProvider.generate()
        )
    }

    override fun createImage(imageId: String): Decoration.Image {
        return Decoration.Image(
            id = uuidProvider.generate(),
            imageId = imageId
        )
    }

    override fun moveDecorations(
        decorations: List<Decoration>,
        fromIndex: Int,
        toIndex: Int
    ): List<Decoration> {
        if (fromIndex == toIndex || decorations.isEmpty()) return decorations

        val actualFromIndex = decorations.lastIndex - fromIndex
        val actualToIndex = decorations.lastIndex - toIndex
        val updatedDecorations = decorations.toMutableList()
        val movedDecoration = updatedDecorations.removeAt(actualFromIndex)
        updatedDecorations.add(actualToIndex, movedDecoration)
        return updatedDecorations
    }
}
