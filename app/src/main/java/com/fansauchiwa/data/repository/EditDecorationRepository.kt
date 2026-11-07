package com.fansauchiwa.data.repository

import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.edit.FontFamilies
import javax.inject.Inject
import kotlin.math.min

interface EditDecorationRepository {
    fun createText(font: FontFamilies): Decoration.Text

    /**
     * 今日のお題から作るとき（#360）、白紙のうちわの真ん中に置く文字を作る。ふつうの文字と同じく直せるし、消せる
     */
    fun createDailyPromptText(text: String): Decoration.Text

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

    override fun createDailyPromptText(text: String): Decoration.Text {
        return createText(DAILY_PROMPT_FONT).copy(
            text = text,
            scale = dailyPromptTextScale(text)
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

// お題の文字のフォント。かなと漢字がそろっていて、角の丸いやわらかい形のもの
private val DAILY_PROMPT_FONT = FontFamilies.M_PLUS_ROUNDED_1C

// 編集画面の文字の大きさのまま、横長のうちわの幅に収まる文字数の目安（推測。実機では確かめていない）。
// お題は16文字まで届く（DailyPromptsParser.MAX_TEXT_LENGTH）ので、これより長いときは縮めてはみ出さないようにする
private const val DAILY_PROMPT_CHARS_AT_FULL_SCALE = 10

// 文字数は DailyPromptsParser と同じくコードポイントで数える（絵文字1つを2文字と数えて縮めすぎないため）
private fun dailyPromptTextScale(text: String): Float =
    min(1f, DAILY_PROMPT_CHARS_AT_FULL_SCALE.toFloat() / text.codePointCount(0, text.length))
