package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.NAME_TEMPLATE_FIRST_NAME_1_PLACEHOLDER_TEXT
import com.fansauchiwa.NAME_TEMPLATE_FIRST_NAME_2_PLACEHOLDER_TEXT
import com.fansauchiwa.NAME_TEMPLATE_HONORIFIC_PLACEHOLDER_TEXT
import com.fansauchiwa.NAME_TEMPLATE_LAST_NAME_PLACEHOLDER_TEXT
import com.fansauchiwa.edit.FontFamilies

/**
 * 組み込みのテンプレート。位置と縁の太さは基準の密度（[TEMPLATE_BASE_DENSITY]）の px で持ち、
 * `TemplateRepository` が端末の密度に合わせて伸ばして返す（[scaledToDensity]）。画面に出すときは直接読まず `TemplateRepository` を通す。
 */
val templateList: List<Template> = listOf(

    Template(
        id = "template_1",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = NAME_TEMPLATE_FIRST_NAME_1_PLACEHOLDER_TEXT,
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
                    text = NAME_TEMPLATE_FIRST_NAME_2_PLACEHOLDER_TEXT,
                    id = "1f97693d-6f7f-4d6d-9d96-2ca8966f4ac1",
                    offset = Offset(214.61371f, 36.934616f),
                    rotation = 15.502495f,
                    scale = 2.1579828f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 5.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 15.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = NAME_TEMPLATE_HONORIFIC_PLACEHOLDER_TEXT,
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
                    text = NAME_TEMPLATE_LAST_NAME_PLACEHOLDER_TEXT,
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
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFFFF3399),
            overallBorderWidth = 4.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = true
    ),
    Template(
        id = "template_2",
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
                    offset = Offset(-247.37503f, -12.7812805f),
                    rotation = -25.491116f,
                    scale = 0.524725f,
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
                    strokeWidth = 5.0f,
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
                ),
                Decoration.Sticker(
                    label = "crown",
                    id = "b818fade-aa85-4dcf-ab52-06c743bae10c",
                    offset = Offset(157.58398f, -217.88477f),
                    rotation = 24.121147f,
                    scale = 0.778653f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFFFF3399),
            overallBorderWidth = 4.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = true
    ),
    Template(
        id = "template_3",
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
                Decoration.Text(
                    text = "前",
                    id = "1f97693d-6f7f-4d6d-9d96-2ca8966f4ac1",
                    offset = Offset(214.61371f, 36.934616f),
                    rotation = 15.502495f,
                    scale = 2.1579828f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 5.0f,
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
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "0b03acb0-f2f9-4490-82e6-550a1d75e54c",
                    offset = Offset(202.98828f, -177.75488f),
                    rotation = 20.985054f,
                    scale = 0.8807225f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "fc04cca3-12a7-494f-834d-1ad82d97412c",
                    offset = Offset(-193.07324f, 113.33984f),
                    rotation = -22.727945f,
                    scale = 1.0232339f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFFFF3399),
            overallBorderWidth = 4.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = true
    ),
    Template(
        id = "template_4",
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
                Decoration.Text(
                    text = "前",
                    id = "1f97693d-6f7f-4d6d-9d96-2ca8966f4ac1",
                    offset = Offset(214.61371f, 36.934616f),
                    rotation = 15.502495f,
                    scale = 2.1579828f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 5.0f,
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
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "0b03acb0-f2f9-4490-82e6-550a1d75e54c",
                    offset = Offset(-237.84082f, 55.473633f),
                    rotation = -19.781012f,
                    scale = 0.5592941f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "fc04cca3-12a7-494f-834d-1ad82d97412c",
                    offset = Offset(-160.56152f, 162.4414f),
                    rotation = -20.538895f,
                    scale = 0.84335816f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                ),
                Decoration.Sticker(
                    label = "crown",
                    id = "7df712c0-fe9f-454f-8bab-5d3ff6cd4872",
                    offset = Offset(149.75977f, -202.96875f),
                    rotation = 24.444115f,
                    scale = 0.8739083f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 3.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFFFF3399),
            overallBorderWidth = 4.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = true
    ),
    Template(
        id = "template_5",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "撃",
                    id = "d05c3c8d-b8cd-4681-8d14-3344b73756c2",
                    offset = Offset(-33.60742f, -35.10742f),
                    rotation = -6.3982887f,
                    scale = 5.937686f,
                    color = DecorationColors.GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 5.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 5.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "って",
                    id = "e0075313-0134-4df7-8bd4-6a10f219679b",
                    offset = Offset(120.10547f, 127.85254f),
                    rotation = 0.0f,
                    scale = 2.1531153f,
                    color = DecorationColors.GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 9.999999f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "ab0d362a-6597-431e-a966-d90bac234720",
                    offset = Offset(218.83887f, -122.50781f),
                    rotation = 20.557663f,
                    scale = 0.81466687f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "d288c796-027d-470a-ae19-00c69cc930d9",
                    offset = Offset(-243.12012f, 87.5166f),
                    rotation = -26.363548f,
                    scale = 0.58775723f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFF00CC44),
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    Template(
        id = "template_6",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "一緒に",
                    id = "d05c3c8d-b8cd-4681-8d14-3344b73756c2",
                    offset = Offset(-85.63965f, -185.4502f),
                    rotation = 0.0f,
                    scale = 2.1009605f,
                    color = DecorationColors.GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "作って",
                    id = "e0075313-0134-4df7-8bd4-6a10f219679b",
                    offset = Offset(13.376953f, 176.53418f),
                    rotation = 0.0f,
                    scale = 2.0116758f,
                    color = DecorationColors.GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 9.999999f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ハート",
                    id = "5bb47801-51f8-4f7b-bd51-d3ccb950e771",
                    offset = Offset(5.6953125f, 0.0f),
                    rotation = 1.5258789E-5f,
                    scale = 2.802945f,
                    color = DecorationColors.GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "ab0d362a-6597-431e-a966-d90bac234720",
                    offset = Offset(198.94824f, -151.81543f),
                    rotation = 24.054016f,
                    scale = 0.96293116f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "d288c796-027d-470a-ae19-00c69cc930d9",
                    offset = Offset(-219.00684f, 132.36697f),
                    rotation = -18.086685f,
                    scale = 0.57569385f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFF00CC44),
            overallBorderWidth = 10.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    Template(
        id = "template_7",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "指",
                    id = "d05c3c8d-b8cd-4681-8d14-3344b73756c2",
                    offset = Offset(-50.68164f, -94.83887f),
                    rotation = 0.0f,
                    scale = 6.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 5.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 5.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "さして",
                    id = "e0075313-0134-4df7-8bd4-6a10f219679b",
                    offset = Offset(6.0f, 185.35449f),
                    rotation = 0.0f,
                    scale = 2.1531153f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 9.999999f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "ab0d362a-6597-431e-a966-d90bac234720",
                    offset = Offset(201.73145f, -142.90039f),
                    rotation = 20.557663f,
                    scale = 0.81466687f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "d288c796-027d-470a-ae19-00c69cc930d9",
                    offset = Offset(-252.31543f, 85.5166f),
                    rotation = -26.363548f,
                    scale = 0.58775723f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "pan_tool_alt",
                    id = "259dbafa-c2bb-47d5-b76c-91c6aebcd500",
                    offset = Offset(238.66992f, 13.0f),
                    rotation = 49.26625f,
                    scale = 1.1014731f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFFFF0000),
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),

    Template(
        id = "template_8",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "バーン",
                    id = "d05c3c8d-b8cd-4681-8d14-3344b73756c2",
                    offset = Offset(-31.291016f, -127.30762f),
                    rotation = -9.04797f,
                    scale = 3.216602f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 9.999999f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "して",
                    id = "e0075313-0134-4df7-8bd4-6a10f219679b",
                    offset = Offset(-89.38379f, 109.52832f),
                    rotation = 1.1444092E-5f,
                    scale = 2.611424f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 9.999999f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 9.999999f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "ab0d362a-6597-431e-a966-d90bac234720",
                    offset = Offset(290.79785f, -102.55469f),
                    rotation = 25.36808f,
                    scale = 0.5456756f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "d288c796-027d-470a-ae19-00c69cc930d9",
                    offset = Offset(42.45117f, 262.9375f),
                    rotation = 21.40794f,
                    scale = 0.5f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "pan_tool_alt",
                    id = "62dd2734-2f69-421a-8334-aa0b59efdfb6",
                    offset = Offset(208.2373f, 59.04785f),
                    rotation = 44.608948f,
                    scale = 1.361577f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "3f925e9d-8af9-4ee7-9078-c5e3c3ab3c9c",
                    offset = Offset(-71.4043f, -246.2539f),
                    rotation = -10.230455f,
                    scale = 0.6213915f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "db6800f8-09db-4f3b-a846-1fa1c94dadda",
                    offset = Offset(-6.350586f, -227.29004f),
                    rotation = 12.552029f,
                    scale = 0.5f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = Color(0xFFFF0000),
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),

    // template_9〜11 は保存データではなく候補の値のまま入れた（基準の密度 2.625 で確認。手順と例外は .agents/template-candidates.md）
    // ピースして
    Template(
        id = "template_9",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ピース",
                    id = "f7a43e21-1f92-4c33-aefe-e9ba950dc2d1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "して",
                    id = "2701b0f1-5693-42ef-929b-8a55c2b81470",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "hand_peace",
                    id = "9fdc8051-14a3-4050-b2e1-3fe66c5afb4f",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "68a38e94-6712-4296-bdbf-0bff598e287a",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // 手ふって
    Template(
        id = "template_10",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "手",
                    id = "4357ef8a-71ff-4425-8272-c8708ff37314",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -5.0f,
                    scale = 5.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ふって",
                    id = "1f2d7f9f-f697-4942-a034-5b9bd26148a1",
                    offset = Offset(5.0f, 150.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "waving_hand",
                    id = "d5b2a179-6434-4106-b84f-776dd62d0321",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // 投げキスして
    Template(
        id = "template_11",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "投げ",
                    id = "d1cc25df-7c41-4c75-a75e-1a3f04f1643c",
                    offset = Offset(-85.0f, -215.0f),
                    rotation = -4.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "キス",
                    id = "26d60870-a872-4bef-96d4-4d3563a2f333",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "4a76a629-5e4e-4182-af36-9bb70f718de7",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "kiss_lips",
                    id = "7ad460bb-d0ed-42f1-9f4f-f004ac0d6bae",
                    offset = Offset(115.0f, -215.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "e99c4899-22ee-4617-a541-2a8f7e9af84e",
                    offset = Offset(-130.0f, 145.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // template_12〜30 は、2回目の候補（debug ビルドのホームに出した60個）を実機で調整して保存した19個。
    // 手順は .agents/template-candidates.md。コメントの candidate_N は取り込んだ回の番号で、次の回は使い回す。
    // 候補名の半角スペースは飾り（Text）の区切り。保存しなかった候補は取り込まない
    // candidate_1（笑 って） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_12",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "笑",
                    id = "3ab1b6b6-703f-48d2-a505-14d428a3a624",
                    offset = Offset(-54.99414f, -43.26172f),
                    rotation = -5.8186874f,
                    scale = 6.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "って",
                    id = "0c920acb-75f6-40a9-99f2-5bd05d648bec",
                    offset = Offset(75.61328f, 145.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "flower",
                    id = "cdedb614-5855-4d4b-895f-598cd7be8037",
                    offset = Offset(171.88379f, -36.10254f),
                    rotation = 22.258919f,
                    scale = 1.1191599f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_2（ドヤ顔 して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_13",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ドヤ顔",
                    id = "3b74a874-17eb-4ad1-a6e9-2e30682a2d6e",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 550,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "19ead93a-1180-483a-a056-6498318a2646",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Sticker(
                    label = "crown",
                    id = "848349b7-3bbf-4ad5-b5e9-bdd562a7a5b5",
                    offset = Offset(168.63672f, 109.31836f),
                    rotation = 22.220303f,
                    scale = 0.85196435f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "d8fa9d3b-e896-4294-9a58-16812f57f486",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_4（キメ顔 して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    // フォントは MOCHIY_POP_P_ONE（P あり。MOCHIY_POP_ONE とは別）。画面の外では描けないので、実機で見た目を確認する
    Template(
        id = "template_14",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "キメ顔",
                    id = "cfbf51b2-186b-405f-b8d4-ef4915adbb64",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_P_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "fe253de3-7bed-4758-9ea2-be66c89ce896",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_P_ONE
                ),
                Decoration.Sticker(
                    label = "diamond",
                    id = "f37ab55d-eeae-45bb-944f-605ed5fc77ad",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 17.601383f,
                    scale = 0.880291f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "e4f18ad6-ad58-444e-8c7c-9d3b53fa1849",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_5（照 れて） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_15",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "照",
                    id = "f41d8958-d719-43ae-b99f-0543f058d11f",
                    offset = Offset(-63.11328f, -93.24805f),
                    rotation = -7.4432526f,
                    scale = 6.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "れて",
                    id = "cec14c3c-814b-4f12-8843-e1e810944be4",
                    offset = Offset(60.16504f, 156.72461f),
                    rotation = 0.0f,
                    scale = 2.4781437f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "b2464395-09c0-4e29-ab8c-a3746b222960",
                    offset = Offset(209.7002f, -30.72168f),
                    rotation = 22.74363f,
                    scale = 0.9741119f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_6（ほっぺ 膨らませて） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_16",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Sticker(
                    label = "balloon",
                    id = "bab49e19-a80f-48f0-944a-34193bfee281",
                    offset = Offset(225.0f, 105.0f),
                    rotation = 28.460667f,
                    scale = 0.92654824f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Text(
                    text = "ほっぺ",
                    id = "82a53dbc-de54-4e27-8c87-022159c11b2e",
                    offset = Offset(0.0f, -92.518555f),
                    rotation = 0.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Text(
                    text = "膨らませて",
                    id = "b0947a6a-9bef-4830-a986-e350db506039",
                    offset = Offset(-44.0f, 81.322266f),
                    rotation = 0.0f,
                    scale = 1.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "f097f91c-3208-4079-b1ba-832fb0fefc84",
                    offset = Offset(-215.5f, -210.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_9（寝たふり して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_17",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "寝たふり",
                    id = "7b3cbb7d-6826-4ade-8fc1-260be07198c4",
                    offset = Offset(26.276367f, -89.0f),
                    rotation = 0.0f,
                    scale = 2.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Text(
                    text = "して",
                    id = "d0194222-f1ca-409f-9921-341ac95b250b",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Sticker(
                    label = "brightness_2",
                    id = "dcde95de-75e2-460c-99e4-565c5e55adc1",
                    offset = Offset(152.84375f, 131.84375f),
                    rotation = 19.124165f,
                    scale = 0.926558f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "606528c6-9c3f-446d-bc39-9907289f312c",
                    offset = Offset(-207.7334f, -210.84961f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.PURPLE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_11（こっち 見て） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_18",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "こっち",
                    id = "4b72d7be-0598-42a6-94c9-b238214c867d",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.669489f,
                    scale = 3.251131f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "見て",
                    id = "8f89c64c-1d97-4fb9-87a6-c24e43d62465",
                    offset = Offset(-67.0f, 114.0f),
                    rotation = 0.0f,
                    scale = 2.4320772f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "emphasis_marks",
                    id = "8bf6342f-862e-4800-b55e-9679b512a77f",
                    offset = Offset(158.30762f, 122.43555f),
                    rotation = 24.759792f,
                    scale = 1.0455152f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "ccc25d75-a24f-450f-a464-80e6c57d4cd3",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "eb248c7b-1cca-4f21-95c5-5aac37f2e8e0",
                    offset = Offset(-89.53516f, -207.96484f),
                    rotation = 22.75113f,
                    scale = 0.5f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_14（好き って言って） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_19",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Sticker(
                    label = "heart",
                    id = "51304aed-0d5f-4aca-bc8a-5314c00e3a21",
                    offset = Offset(225.0f, 115.0f),
                    rotation = 19.123425f,
                    scale = 0.7504852f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Text(
                    text = "好き",
                    id = "0cde86b1-4017-4ff3-8df1-5e618e570a44",
                    offset = Offset(0.0f, -90.0f),
                    rotation = 0.0f,
                    scale = 3.3338163f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "って言って",
                    id = "f1098d5b-a03d-4268-acda-77f43ed55723",
                    offset = Offset(-77.61426f, 97.30664f),
                    rotation = 0.0f,
                    scale = 1.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "b3cf4649-1a94-4b63-9a4d-8acf3d427264",
                    offset = Offset(-219.89355f, -200.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_15（かわいい って言って） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_20",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "かわいい",
                    id = "f602630d-badd-4cf6-9827-f7c7a1948395",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "って言って",
                    id = "713bf49b-3634-4960-8a11-44af2c84c4ac",
                    offset = Offset(-68.60156f, 82.021484f),
                    rotation = 0.0f,
                    scale = 1.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 550,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "ribbon",
                    id = "c3fd0962-d4f4-4bde-b06c-0bd6c533c87a",
                    offset = Offset(258.0f, 49.844727f),
                    rotation = 17.434208f,
                    scale = 0.96437097f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "flower",
                    id = "5dcc8cfd-e5cd-43f0-b9e0-37d1ba3699ee",
                    offset = Offset(-216.0f, -200.59863f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_16（褒 めて） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_21",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "褒",
                    id = "68526592-9630-4b8f-aff9-9418f54d2e09",
                    offset = Offset(-71.43359f, -84.43359f),
                    rotation = -5.2203026f,
                    scale = 5.980259f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "めて",
                    id = "051b9502-e618-457a-b548-a154c35fc159",
                    offset = Offset(8.426758f, 168.13965f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "thumb_up",
                    id = "2734708d-5a17-4e05-83cb-2b0bd3a8df25",
                    offset = Offset(193.90723f, -39.185547f),
                    rotation = 15.0f,
                    scale = 1.0f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_22（投げ チュー して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_22",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "投げ",
                    id = "6cb959e7-2085-45b8-b406-aad1579eaaa0",
                    offset = Offset(-116.76367f, -210.36133f),
                    rotation = 0.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "チュー",
                    id = "98fd72fc-3578-4eb1-b1c3-473c9d69c660",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -9.531712f,
                    scale = 3.4700344f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "4ae926ac-45bd-4ad1-9a22-ec808b223df6",
                    offset = Offset(96.34668f, 163.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "kiss_lips",
                    id = "a4947d6f-bdd4-4d9b-b72b-b7b8371abc22",
                    offset = Offset(144.52051f, -194.32031f),
                    rotation = 15.364723f,
                    scale = 1.0171394f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "7b2fafa8-8404-4fc3-a839-7ba90eec7630",
                    offset = Offset(-121.771484f, 166.0f),
                    rotation = -27.524931f,
                    scale = 0.6114719f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "8221d1d6-79ca-4cec-b2e9-6dd3c73ba8b7",
                    offset = Offset(184.04883f, 59.179688f),
                    rotation = 35.511024f,
                    scale = 0.5f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.WHITE.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_23（指 ハートして） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_23",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "指",
                    id = "d6ed73b2-8b88-4bd5-9bc1-c7fd2cd2532e",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -8.008293f,
                    scale = 5.9999995f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ハートして",
                    id = "f453e2a5-0b0c-4f0e-b69b-8d72620d3c1b",
                    offset = Offset(5.0f, 150.0f),
                    rotation = 0.0f,
                    scale = 1.6f,
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
                    id = "1a42026c-58d2-4321-b7c7-48176eb12950",
                    offset = Offset(201.13672f, -49.4834f),
                    rotation = 20.198845f,
                    scale = 1.1352859f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_24（手で ハート 作って） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_24",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "手で",
                    id = "11e8d171-37ce-4f46-8ad0-352ed9590115",
                    offset = Offset(-103.32617f, -203.27344f),
                    rotation = 0.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ハート",
                    id = "eb09634a-f309-44a9-8245-c76fde857e0f",
                    offset = Offset(0.0f, -30.0f),
                    rotation = 0.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "作って",
                    id = "76a24d6f-3e31-4083-aaed-34d15b39c604",
                    offset = Offset(68.0f, 148.0f),
                    rotation = 0.0f,
                    scale = 1.9959991f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "28fc7c14-f8a4-404c-9b0d-08988d27e4d5",
                    offset = Offset(125.56348f, -207.19922f),
                    rotation = 19.203812f,
                    scale = 1.0081466f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "01b9320f-abc2-439e-a030-084a49dc0903",
                    offset = Offset(-187.88574f, 161.6289f),
                    rotation = -20.0f,
                    scale = 0.7f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_27（両手 ふって） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_25",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "両手",
                    id = "38a4bc0b-6b78-4711-87d1-80a5b3a5a8be",
                    offset = Offset(0.0f, -145.58203f),
                    rotation = 0.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "ふって",
                    id = "aef17349-3490-45f6-b72f-7b3cf1ff10a2",
                    offset = Offset(-41.37793f, 88.37793f),
                    rotation = 0.0f,
                    scale = 2.3816416f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "waving_hand",
                    id = "eb023933-a22c-42d3-8daf-a4d0cbe21696",
                    offset = Offset(225.05566f, 89.680664f),
                    rotation = -6.6390915f,
                    scale = 0.7215264f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_34（猫 ポーズして） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_26",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "猫",
                    id = "a6f9dc8e-0275-4192-b117-4f2e432a92c9",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -6.509903f,
                    scale = 5.2045755f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ポーズして",
                    id = "f38a0316-2264-49a4-8ada-f8ae20eeac18",
                    offset = Offset(5.0f, 150.0f),
                    rotation = 0.0f,
                    scale = 1.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "pets",
                    id = "cfd79236-f098-465a-8301-c6daef0d458c",
                    offset = Offset(187.0f, -70.0f),
                    rotation = 20.97107f,
                    scale = 1.0306901f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_36（頭 ポンして） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_27",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "頭",
                    id = "a7c998f3-cc2f-42c1-8d8d-9c9905299d4b",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -8.638615f,
                    scale = 5.3275113f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ポンして",
                    id = "46c6d117-e1e0-4f07-a6b4-8bf21a054d6f",
                    offset = Offset(5.0f, 150.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "8bc1f6fd-9b33-4f87-a131-afdd159267b5",
                    offset = Offset(178.0f, -57.53418f),
                    rotation = 15.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_39（決め ポーズ して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_28",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "決め",
                    id = "d6076a5f-fb76-464b-b61d-7a8d67173691",
                    offset = Offset(-85.0f, -215.0f),
                    rotation = -4.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ポーズ",
                    id = "b54599fe-392c-422b-b397-14368f435920",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -8.865852f,
                    scale = 3.345117f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "して",
                    id = "66543070-001d-4820-933c-b4123cf435fa",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "crown",
                    id = "29e10eb6-a64f-47bc-8e98-646ce42cc80c",
                    offset = Offset(115.0f, -215.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "diamond",
                    id = "b719264f-4b90-41f9-b0fd-f594bbeff07e",
                    offset = Offset(-124.0f, 160.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_48（結婚 して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_29",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "結婚",
                    id = "63432786-f28e-4a5c-a828-dced53a0c0ca",
                    offset = Offset(0.0f, -86.46973f),
                    rotation = -6.741789f,
                    scale = 3.9786496f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 5.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "して",
                    id = "337c44a1-4e75-48d0-af52-986bfcac17a9",
                    offset = Offset(-73.0f, 111.793945f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "diamond",
                    id = "92f2af9d-b40f-415d-afe1-1ed2ba766e39",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 23.877674f,
                    scale = 0.93894f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "a601d4a9-672d-4109-b51d-ce22cb05f375",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    ),
    // candidate_50（ぎゅー して） を debug ビルドで調整（密度 2.625、2026-11-14 に取り込み）
    Template(
        id = "template_30",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ぎゅー",
                    id = "bb812fb3-f9fd-4a1f-b8b3-0afc8f4aa808",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -8.894028f,
                    scale = 3.1953118f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "して",
                    id = "42a04c34-a821-4e59-8c8e-3cccc7db8fae",
                    offset = Offset(-54.0f, 108.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
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
                    id = "a2403f9e-36e6-4eac-bf88-027b67473e5b",
                    offset = Offset(240.00977f, 25.0f),
                    rotation = 15.561569f,
                    scale = 0.82817173f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "cb79cb4e-9f81-49e3-82a9-b989db2cf867",
                    offset = Offset(-209.0f, -207.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 0.0f
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "bf17412b-f0b5-4a26-b4d3-a746f47c82a2",
                    offset = Offset(136.72168f, 127.381836f),
                    rotation = 17.879436f,
                    scale = 0.515603f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0x11000000),
            overallBorderColor = DecorationColors.PINK.value,
            overallBorderWidth = 5.0f,
            isOverallBorderPuffyEnabled = false
        ),
        isNameInputPlaceholderEnabled = false
    )
)

internal fun SavedUchiwa.applyTemplateMainColor(
    mainColor: Color,
    isNameInputPlaceholderEnabled: Boolean
): SavedUchiwa = copy(
    overallBorderColor = mainColor,
    decorations = decorations.map { decoration ->
        when (decoration) {
            is Decoration.Text ->
                if (!isNameInputPlaceholderEnabled) {
                    decoration.copy(color = mainColor)
                } else if (decoration.text == NAME_TEMPLATE_FIRST_NAME_1_PLACEHOLDER_TEXT || decoration.text == NAME_TEMPLATE_FIRST_NAME_2_PLACEHOLDER_TEXT) {
                    decoration.copy(color = mainColor)
                } else {
                    decoration
                }

            is Decoration.Sticker -> decoration
            is Decoration.Image -> decoration
        }
    }
)
