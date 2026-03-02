package com.fansauchiwa.data.repository

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
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
            previewImageResId = R.drawable.template_1,
            savedUchiwa = SavedUchiwa(
                decorations = listOf(
                    Decoration.Text(
                        text = "大好き",
                        id = "template_2_text_1",
                        offset = Offset(-124.754555f, -139.34021f),
                        rotation = -19.011398f,
                        scale = 1.9864473f,
                        color = DecorationColors.YELLOW.value,
                        strokeColor = DecorationColors.BLUE.value,
                        strokeWidth = 25.0f,
                        secondBorderColor = DecorationColors.WHITE.value,
                        secondBorderWidth = 0.0f,
                        width = 900,
                        font = FontFamilies.MOCHIY_POP_ONE
                    ),
                    Decoration.Text(
                        text = "あいしてる",
                        id = "3a6bd08c-b334-4d72-9db0-d59a3bfde73e",
                        offset = Offset(41.6437f, 128.98508f),
                        rotation = 15.709348f,
                        scale = 1.5701048f,
                        color = DecorationColors.WHITE.value,
                        strokeColor = DecorationColors.MAGENTA.value,
                        strokeWidth = 30.0f,
                        secondBorderColor = DecorationColors.WHITE.value,
                        secondBorderWidth = 0.0f,
                        width = 900,
                        font = FontFamilies.M_PLUS_1P
                    )
                ),
                uchiwaColor = Color(0xFF1E90FF),
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

