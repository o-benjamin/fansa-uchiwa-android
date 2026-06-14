package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.NAME_TEMPLATE_FIRST_NAME_1_PLACEHOLDER_TEXT
import com.fansauchiwa.NAME_TEMPLATE_FIRST_NAME_2_PLACEHOLDER_TEXT
import com.fansauchiwa.NAME_TEMPLATE_HONORIFIC_PLACEHOLDER_TEXT
import com.fansauchiwa.NAME_TEMPLATE_LAST_NAME_PLACEHOLDER_TEXT
import com.fansauchiwa.edit.FontFamilies

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
