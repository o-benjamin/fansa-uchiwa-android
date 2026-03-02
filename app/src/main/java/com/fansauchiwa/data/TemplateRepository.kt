package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.R
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.ui.DecorationColors
import javax.inject.Inject

interface TemplateRepository {
    suspend fun getTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
}

class DefaultTemplateRepository @Inject constructor() : TemplateRepository {

    private val templates: List<Template> = listOf(
        Template(
            id = "template_1",
            previewImageResId = R.drawable.uchiwa_shape,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        text = "推し",
                        id = "template_1_text_1",
                        offset = Offset(0.3f, 0.4f),
                        scale = 1.5f,
                        color = DecorationColors.WHITE.value,
                        strokeColor = DecorationColors.MAGENTA.value,
                        strokeWidth = 30f,
                        font = FontFamilies.DELA_GOTHIC_ONE
                    ),
                    Decoration.Sticker(
                        label = "heart",
                        id = "template_1_sticker_1",
                        offset = Offset(0.6f, 0.2f),
                        scale = 1.2f,
                        color = DecorationColors.RED.value,
                        strokeColor = DecorationColors.WHITE.value,
                        strokeWidth = 3f
                    )
                ),
                uchiwaColor = Color(0xFFFF69B4),
                backgroundColor = Color(0xFFFFFFFF)
            )
        ),
        Template(
            id = "template_2",
            previewImageResId = R.drawable.uchiwa_shape,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        text = "大好き",
                        id = "template_2_text_1",
                        offset = Offset(0.25f, 0.35f),
                        scale = 1.3f,
                        color = DecorationColors.YELLOW.value,
                        strokeColor = DecorationColors.BLUE.value,
                        strokeWidth = 25f,
                        font = FontFamilies.MOCHIY_POP_ONE
                    )
                ),
                uchiwaColor = Color(0xFF1E90FF),
                backgroundColor = Color(0xFFFFFFFF)
            )
        ),
        Template(
            id = "template_3",
            previewImageResId = R.drawable.uchiwa_shape,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        text = "最高",
                        id = "template_3_text_1",
                        offset = Offset(0.3f, 0.3f),
                        scale = 1.4f,
                        color = DecorationColors.WHITE.value,
                        strokeColor = DecorationColors.RED.value,
                        strokeWidth = 28f,
                        font = FontFamilies.ROCKNROLL_ONE
                    ),
                    Decoration.Sticker(
                        label = "auto_awesome",
                        id = "template_3_sticker_1",
                        offset = Offset(0.1f, 0.1f),
                        scale = 1.0f,
                        color = DecorationColors.YELLOW.value,
                        strokeColor = DecorationColors.WHITE.value,
                        strokeWidth = 3f
                    ),
                    Decoration.Sticker(
                        label = "bolt",
                        id = "template_3_sticker_2",
                        offset = Offset(0.7f, 0.6f),
                        scale = 1.1f,
                        color = DecorationColors.YELLOW.value,
                        strokeColor = DecorationColors.WHITE.value,
                        strokeWidth = 3f
                    )
                ),
                uchiwaColor = Color(0xFF000000),
                backgroundColor = Color(0xFFFFFFFF)
            )
        )
    )

    override suspend fun getTemplates(): List<Template> {
        return templates
    }

    override suspend fun getTemplateById(id: String): Template? {
        return templates.find { it.id == id }
    }
}

