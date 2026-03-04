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
                        text = "プロポーズ",
                        id = "f61e514b-11ce-4342-ae2d-d0d223a3d4ff",
                        offset = Offset(0.0f, -81.89173f),
                        rotation = -0.5187378f,
                        scale = 1.9523201f,
                        color = DecorationColors.WHITE.value,
                        strokeColor = DecorationColors.CYAN.value,
                        strokeWidth = 24.545456f,
                        secondBorderColor = DecorationColors.CYAN.value,
                        secondBorderWidth = 0.0f,
                        width = 900,
                        font = FontFamilies.M_PLUS_ROUNDED_1C
                    ),
                    Decoration.Sticker(
                        label = "heart",
                        id = "87a716cf-7402-4a31-9e5f-6c6011968aee",
                        offset = Offset(207.50427f, -212.45009f),
                        rotation = 23.069443f,
                        scale = 0.6699208f,
                        color = DecorationColors.MAGENTA.value,
                        strokeColor = DecorationColors.WHITE.value,
                        strokeWidth = 3.0f,
                        secondStrokeColor = DecorationColors.WHITE.value,
                        secondStrokeWidth = 0.0f
                    ),
                    Decoration.Text(
                        text = "して！",
                        id = "b8b1db2c-7cd1-4296-9921-630ab6456181",
                        offset = Offset(0.0f, 87.4123f),
                        rotation = 0.4986801f,
                        scale = 1.9638611f,
                        color = DecorationColors.WHITE.value,
                        strokeColor = DecorationColors.CYAN.value,
                        strokeWidth = 24.545456f,
                        secondBorderColor = DecorationColors.WHITE.value,
                        secondBorderWidth = 0.0f,
                        width = 900,
                        font = FontFamilies.M_PLUS_ROUNDED_1C
                    ),
                    Decoration.Sticker(
                        label = "heart",
                        id = "70e19f4f-b389-41c5-976b-2530205d85bd",
                        offset = Offset(-155.83173f, 223.11134f),
                        rotation = 31.532291f,
                        scale = 0.5f,
                        color = DecorationColors.MAGENTA.value,
                        strokeColor = DecorationColors.WHITE.value,
                        strokeWidth = 3.0f,
                        secondStrokeColor = DecorationColors.WHITE.value,
                        secondStrokeWidth = 0.0f
                    ),
                    Decoration.Sticker(
                        label = "heart",
                        id = "6cb3980b-f4f1-4cee-b213-ea84c1213c5a",
                        offset = Offset(-270.38416f, 125.276855f),
                        rotation = -15.544874f,
                        scale = 0.531031f,
                        color = DecorationColors.MAGENTA.value,
                        strokeColor = DecorationColors.WHITE.value,
                        strokeWidth = 3.0f,
                        secondStrokeColor = DecorationColors.WHITE.value,
                        secondStrokeWidth = 0.0f
                    )
                ),
                uchiwaColor = Color(0xFFFFBBFE),
                backgroundColor = Color(0x11000000)
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

