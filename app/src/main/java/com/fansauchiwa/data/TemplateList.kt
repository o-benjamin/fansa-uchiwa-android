package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.R
import com.fansauchiwa.edit.FontFamilies

val templateList: List<Template> = listOf(

    Template(
        id = "template_1",
        previewImageResId = R.drawable.template_0,
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "名",
                    id = "4b8b6f56-9dc9-4d6b-9286-1802ea38ee99",
                    offset = Offset(-21.90332f, -49.61328f),
                    rotation = 1.5258789E-5f,
                    scale = 6.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "87a716cf-7402-4a31-9e5f-6c6011968aee",
                    offset = Offset(203.62497f, -160.78128f),
                    rotation = 27.999935f,
                    scale = 0.7149991f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "70e19f4f-b389-41c5-976b-2530205d85bd",
                    offset = Offset(-197.34862f, 125.22189f),
                    rotation = -23.5994f,
                    scale = 0.7545319f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Text(
                    text = "前",
                    id = "1f97693d-6f7f-4d6d-9d96-2ca8966f4ac1",
                    offset = Offset(214.61371f, 36.934616f),
                    rotation = 15.502495f,
                    scale = 2.1579828f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 15.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "くん",
                    id = "d9a1f557-215b-4d10-ba4d-7b850f1c3e64",
                    offset = Offset(117.23871f, 186.52739f),
                    rotation = -3.8146973E-6f,
                    scale = 1.05406f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.BLACK.value,
                    strokeWidth = 25.000002f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "みょうじ",
                    id = "bfe0084e-681b-4d0a-b582-7f9679174dc5",
                    offset = Offset(-130.99176f, -211.58784f),
                    rotation = -10.866273f,
                    scale = 1.1784211f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.BLACK.value,
                    strokeWidth = 25.000002f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFF999999),
            overallBorderColor = Color(0xFFFF3399),
            overallBorderWidth = 4.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = true
    ),
    Template(
        id = "template_2",
        previewImageResId = R.drawable.template_2,
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "投げちゅー",
                    id = "06456e39-aaeb-447d-a800-710572f82de5",
                    offset = Offset(0.0f, -14.396046f),
                    rotation = 0.26878357f,
                    scale = 2.269548f,
                    color = Color(0xFFFD95FF),
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 16.363636f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.181818f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "ceca24cb-4e22-460a-a485-4151256ba6da",
                    offset = Offset(283.17545f, -152.42485f),
                    rotation = 7.970463f,
                    scale = 0.61094713f,
                    color = Color(0xFF65E8FF),
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "7fb461f8-aa63-45fa-b27e-0f9d8a4ff11e",
                    offset = Offset(-282.82278f, -160.1804f),
                    rotation = -7.6752396f,
                    scale = 0.6007333f,
                    color = Color(0xFF69E2FF),
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Text(
                    text = "して！",
                    id = "19033388-7ab8-4072-b746-d8208ecb7ce9",
                    offset = Offset(0.0f, 173.54948f),
                    rotation = 0.15176201f,
                    scale = 1.9245204f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 30.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "みょうじ",
                    id = "20e415c0-a614-4ed7-a112-e843680f4194",
                    offset = Offset(-170.0f, -183.5997f),
                    rotation = 0.24852562f,
                    scale = 1.6775169f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 30.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "名",
                    id = "6ceda16c-25aa-4f86-82ca-137ad6112f43",
                    offset = Offset(-54.0f, -183.5997f),
                    rotation = 0.24852562f,
                    scale = 1.6775169f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 30.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "前",
                    id = "3f7f6088-78f6-4d90-9991-0be019f8fe3d",
                    offset = Offset(14.0f, -183.5997f),
                    rotation = 0.24852562f,
                    scale = 1.6775169f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 30.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "くん",
                    id = "1049fd4d-9e54-4e65-8be9-088286a95d6a",
                    offset = Offset(110.0f, -183.5997f),
                    rotation = 0.24852562f,
                    scale = 1.6775169f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 30.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                )
            ),
            uchiwaColor = Color(0xFFBFE6FF),
            backgroundColor = Color(0x11000000)
        ),
        isNameInputPlaceholderEnabled = true
    ),
    Template(
        id = "template_3",
        previewImageResId = R.drawable.template_3,
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ハート",
                    id = "94bf5a25-fd33-4362-a353-88007189b7a9",
                    offset = Offset(-14.487478f, -26.507954f),
                    rotation = 0.17004395f,
                    scale = 3.0027509f,
                    color = Color(0xFFFF9EFB),
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 16.363636f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 16.363636f,
                    width = 500,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "いっしょに",
                    id = "6fa4b9fd-e139-4cce-be0d-9b6e7d71c783",
                    offset = Offset(-18.301983f, -213.85175f),
                    rotation = -0.011987686f,
                    scale = 1.74352f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 24.545456f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 24.545456f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "つくろ？",
                    id = "bc3391c4-8e6d-46b8-b3b0-fa0838316a38",
                    offset = Offset(51.548576f, 175.76237f),
                    rotation = -0.18185043f,
                    scale = 1.7444919f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 24.545456f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 24.545456f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                )
            ),
            uchiwaColor = Color(0xFFFFFF00),
            backgroundColor = Color(0x11000000)
        )
    )
)

internal fun SavedUchiwa.applyTemplateMainColor(mainColor: Color): SavedUchiwa = copy(
    decorations = decorations.map { decoration ->
        when (decoration) {
            is Decoration.Text -> decoration.copy(
                color = mainColor
            )

            is Decoration.Sticker -> decoration
            is Decoration.Image -> decoration
        }
    }
)
