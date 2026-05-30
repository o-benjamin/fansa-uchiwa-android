package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.R
import com.fansauchiwa.edit.FontFamilies

val templateList: List<Template> = listOf(
    Template(
        id = "template_1",
        previewImageResId = R.drawable.template_1,
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Sticker(
                    label = "heart",
                    id = "6cb3980b-f4f1-4cee-b213-ea84c1213c5a",
                    offset = Offset(-129.53827f, 258.73358f),
                    rotation = -8.0192585f,
                    scale = 0.5826255f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Text(
                    text = "プロポーズ",
                    id = "f61e514b-11ce-4342-ae2d-d0d223a3d4ff",
                    offset = Offset(0.0f, -22.643969f),
                    rotation = 0.2539444f,
                    scale = 2.162599f,
                    color = Color(0xFFFF8AF0),
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 16.363636f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 16.363636f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "87a716cf-7402-4a31-9e5f-6c6011968aee",
                    offset = Offset(242.95407f, -176.08499f),
                    rotation = 17.2761f,
                    scale = 0.65168107f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "70e19f4f-b389-41c5-976b-2530205d85bd",
                    offset = Offset(-223.3994f, 154.48459f),
                    rotation = -13.540371f,
                    scale = 0.69355273f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Text(
                    text = "〇〇くん",
                    id = "bfe0084e-681b-4d0a-b582-7f9679174dc5",
                    offset = Offset(-46.297432f, -211.73433f),
                    rotation = 0.23916245f,
                    scale = 1.8064443f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.PINK.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "して！",
                    id = "6f4c1f9b-9760-4a98-8f88-fb6f879bf907",
                    offset = Offset(87.85379f, 164.56583f),
                    rotation = 0.1616211f,
                    scale = 1.9033979f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                )
            ),
            uchiwaColor = Color(0xFFF6D6FF),
            backgroundColor = Color(0x11000000)
        ),
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
                    secondBorderColor = DecorationColors.WHITE.value,
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
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
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
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
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
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "〇〇くん",
                    id = "20e415c0-a614-4ed7-a112-e843680f4194",
                    offset = Offset(0.0f, -183.5997f),
                    rotation = 0.24852562f,
                    scale = 1.6775169f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 30.0f,
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                )
            ),
            uchiwaColor = Color(0xFFBFE6FF),
            backgroundColor = Color(0x11000000)
        )
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
                    secondBorderColor = DecorationColors.WHITE.value,
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
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
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
                    secondBorderColor = DecorationColors.WHITE.value,
                    secondBorderWidth = 0.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                )
            ),
            uchiwaColor = Color(0xFFFFFF00),
            backgroundColor = Color(0x11000000)
        )
    )
)

private val quickTemplateStrokeColor = DecorationColors.WHITE.value
private val quickTemplateSecondBorderColor = DecorationColors.BLACK.value

internal fun SavedUchiwa.applyQuickTemplateStyle(mainColor: Color): SavedUchiwa = copy(
    decorations = decorations.map { decoration ->
        when (decoration) {
            is Decoration.Text -> decoration.copy(
                color = mainColor,
                strokeColor = quickTemplateStrokeColor,
                secondBorderColor = quickTemplateSecondBorderColor,
                secondBorderWidth = decoration.secondBorderWidth.takeIf { it > 0f }
                    ?: decoration.strokeWidth
            )

            is Decoration.Sticker -> decoration.copy(
                color = mainColor,
                strokeColor = quickTemplateStrokeColor,
                secondStrokeColor = quickTemplateSecondBorderColor,
                secondStrokeWidth = decoration.secondStrokeWidth.takeIf { it > 0f }
                    ?: decoration.strokeWidth
            )

            is Decoration.Image -> decoration
        }
    }
)
