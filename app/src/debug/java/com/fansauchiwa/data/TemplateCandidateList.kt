package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies

// debug ビルドのホームにだけ出すテンプレートの候補。手順と注意は .agents/template-candidates.md
val templateCandidateList: List<Template> = listOf(
    // 笑って
    Template(
        id = "candidate_1",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "笑",
                    id = "candidate_1_1",
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
                    text = "って",
                    id = "candidate_1_2",
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
                    label = "flower",
                    id = "candidate_1_3",
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
    // ドヤ顔して
    Template(
        id = "candidate_2",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ドヤ顔",
                    id = "candidate_2_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "candidate_2_2",
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
                    id = "candidate_2_3",
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
                    label = "auto_awesome",
                    id = "candidate_2_4",
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
    // 変顔して
    Template(
        id = "candidate_3",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "変顔",
                    id = "candidate_3_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    text = "して",
                    id = "candidate_3_2",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "burst_bubble",
                    id = "candidate_3_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "emphasis_marks",
                    id = "candidate_3_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.RED.value,
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
    // キメ顔して
    Template(
        id = "candidate_4",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "キメ顔",
                    id = "candidate_4_1",
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
                    id = "candidate_4_2",
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
                    label = "diamond",
                    id = "candidate_4_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "candidate_4_4",
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
    // 照れて
    Template(
        id = "candidate_5",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "照",
                    id = "candidate_5_1",
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
                    text = "れて",
                    id = "candidate_5_2",
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
                    label = "heart_cute",
                    id = "candidate_5_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // ほっぺ膨らませて
    Template(
        id = "candidate_6",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ほっぺ",
                    id = "candidate_6_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    id = "candidate_6_2",
                    offset = Offset(-70.0f, 100.0f),
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
                    label = "balloon",
                    id = "candidate_6_3",
                    offset = Offset(225.0f, 105.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_6_4",
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
    // 舌ペロして
    Template(
        id = "candidate_7",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "舌ペロ",
                    id = "candidate_7_1",
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
                    id = "candidate_7_2",
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
                    label = "kiss_lips",
                    id = "candidate_7_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "ribbon",
                    id = "candidate_7_4",
                    offset = Offset(-195.0f, -195.0f),
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
    // 泣きまねして
    Template(
        id = "candidate_8",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "泣きまね",
                    id = "candidate_8_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.6f,
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
                    id = "candidate_8_2",
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
                    label = "heart_vertical",
                    id = "candidate_8_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "brightness_1",
                    id = "candidate_8_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.BLUE.value,
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
    // 寝たふりして
    Template(
        id = "candidate_9",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "寝たふり",
                    id = "candidate_9_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    id = "candidate_9_2",
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
                    id = "candidate_9_3",
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
                    label = "star",
                    id = "candidate_9_4",
                    offset = Offset(-195.0f, -195.0f),
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
    // 目線ちょうだい
    Template(
        id = "candidate_10",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "目線",
                    id = "candidate_10_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "ちょうだい",
                    id = "candidate_10_2",
                    offset = Offset(-70.0f, 100.0f),
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
                    label = "eyeglasses",
                    id = "candidate_10_3",
                    offset = Offset(165.0f, 195.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_arrow",
                    id = "candidate_10_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.RED.value,
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
    // こっち見て
    Template(
        id = "candidate_11",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "こっち",
                    id = "candidate_11_1",
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
                    text = "見て",
                    id = "candidate_11_2",
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
                    label = "emphasis_marks",
                    id = "candidate_11_3",
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
                    id = "candidate_11_4",
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
    // 見つめて
    Template(
        id = "candidate_12",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "見",
                    id = "candidate_12_1",
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
                    text = "つめて",
                    id = "candidate_12_2",
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
                    label = "winged_heart",
                    id = "candidate_12_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // 名前呼んで
    Template(
        id = "candidate_13",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "名前",
                    id = "candidate_13_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    text = "呼んで",
                    id = "candidate_13_2",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "speech_bubble",
                    id = "candidate_13_3",
                    offset = Offset(200.0f, 145.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "music_notes",
                    id = "candidate_13_4",
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
    // 好きって言って
    Template(
        id = "candidate_14",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "好き",
                    id = "candidate_14_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
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
                    id = "candidate_14_2",
                    offset = Offset(-90.0f, 100.0f),
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
                    id = "candidate_14_3",
                    offset = Offset(225.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_14_4",
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
    // かわいいって言って
    Template(
        id = "candidate_15",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "かわいい",
                    id = "candidate_15_1",
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
                    id = "candidate_15_2",
                    offset = Offset(-90.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 1.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "ribbon",
                    id = "candidate_15_3",
                    offset = Offset(225.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "flower",
                    id = "candidate_15_4",
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
    // 褒めて
    Template(
        id = "candidate_16",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "褒",
                    id = "candidate_16_1",
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
                    text = "めて",
                    id = "candidate_16_2",
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
                    label = "thumb_up",
                    id = "candidate_16_3",
                    offset = Offset(180.0f, -80.0f),
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
    // 叱って
    Template(
        id = "candidate_17",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "叱",
                    id = "candidate_17_1",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -5.0f,
                    scale = 5.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "って",
                    id = "candidate_17_2",
                    offset = Offset(5.0f, 150.0f),
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
                    label = "bolt",
                    id = "candidate_17_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // おかえりって言って
    Template(
        id = "candidate_18",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "おかえり",
                    id = "candidate_18_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.6f,
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
                    id = "candidate_18_2",
                    offset = Offset(-90.0f, 100.0f),
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
                    id = "candidate_18_3",
                    offset = Offset(225.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "candidate_18_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.LIGHT_GREEN.value,
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
    // 応援してるって言って
    Template(
        id = "candidate_19",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "応援",
                    id = "candidate_19_1",
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
                    text = "してる",
                    id = "candidate_19_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
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
                    text = "言って",
                    id = "candidate_19_3",
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
                    label = "local_fire_department",
                    id = "candidate_19_4",
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
                    label = "star_rounded",
                    id = "candidate_19_5",
                    offset = Offset(-150.0f, 145.0f),
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
    // ありがとうって言って
    Template(
        id = "candidate_20",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ありがとう",
                    id = "candidate_20_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Text(
                    text = "って言って",
                    id = "candidate_20_2",
                    offset = Offset(-70.0f, 100.0f),
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
                    label = "celebration",
                    id = "candidate_20_3",
                    offset = Offset(215.0f, 135.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_20_4",
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
    // ウインクして
    Template(
        id = "candidate_21",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ウインク",
                    id = "candidate_21_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.6f,
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
                    id = "candidate_21_2",
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
                    label = "star_rounded",
                    id = "candidate_21_3",
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
                    id = "candidate_21_4",
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
    // 投げチューして
    Template(
        id = "candidate_22",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "投げ",
                    id = "candidate_22_1",
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
                    text = "チュー",
                    id = "candidate_22_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
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
                    id = "candidate_22_3",
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
                    id = "candidate_22_4",
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
                    label = "winged_heart",
                    id = "candidate_22_5",
                    offset = Offset(-130.0f, 145.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
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
    // 指ハートして
    Template(
        id = "candidate_23",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "指",
                    id = "candidate_23_1",
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
                    text = "ハートして",
                    id = "candidate_23_2",
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
                    id = "candidate_23_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // 手でハート作って
    Template(
        id = "candidate_24",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "手で",
                    id = "candidate_24_1",
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
                    text = "ハート",
                    id = "candidate_24_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
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
                    id = "candidate_24_3",
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
                    label = "heart_cute",
                    id = "candidate_24_4",
                    offset = Offset(115.0f, -215.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "candidate_24_5",
                    offset = Offset(-160.0f, 165.0f),
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
    // 腕でハート作って
    Template(
        id = "candidate_25",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "腕で",
                    id = "candidate_25_1",
                    offset = Offset(-85.0f, -215.0f),
                    rotation = -4.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Text(
                    text = "ハート",
                    id = "candidate_25_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Text(
                    text = "作って",
                    id = "candidate_25_3",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                Decoration.Sticker(
                    label = "heart_arrow",
                    id = "candidate_25_4",
                    offset = Offset(115.0f, -215.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart",
                    id = "candidate_25_5",
                    offset = Offset(-160.0f, 145.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
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
    // バイバイして
    Template(
        id = "candidate_26",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "バイバイ",
                    id = "candidate_26_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.6f,
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
                    id = "candidate_26_2",
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
                    label = "waving_hand",
                    id = "candidate_26_3",
                    offset = Offset(160.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "candidate_26_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.LIGHT_GREEN.value,
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
    // 両手ふって
    Template(
        id = "candidate_27",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "両手",
                    id = "candidate_27_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    id = "candidate_27_2",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
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
                    id = "candidate_27_3",
                    offset = Offset(220.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "flower",
                    id = "candidate_27_4",
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
    // 敬礼して
    Template(
        id = "candidate_28",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "敬礼",
                    id = "candidate_28_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "candidate_28_2",
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
                    label = "pan_tool_alt",
                    id = "candidate_28_3",
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
                    label = "star_rounded",
                    id = "candidate_28_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.BLUE.value,
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
    // ジャンプして
    Template(
        id = "candidate_29",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ジャンプ",
                    id = "candidate_29_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 2.6f,
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
                    id = "candidate_29_2",
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
                    label = "rocket_launch",
                    id = "candidate_29_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "bolt",
                    id = "candidate_29_4",
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
    // ガッツポーズして
    Template(
        id = "candidate_30",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ガッツ",
                    id = "candidate_30_1",
                    offset = Offset(-85.0f, -215.0f),
                    rotation = -4.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "ポーズ",
                    id = "candidate_30_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "candidate_30_3",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Sticker(
                    label = "thumb_up",
                    id = "candidate_30_4",
                    offset = Offset(155.0f, -205.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "local_fire_department",
                    id = "candidate_30_5",
                    offset = Offset(-130.0f, 145.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
                    color = DecorationColors.RED.value,
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
    // 拍手して
    Template(
        id = "candidate_31",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "拍手",
                    id = "candidate_31_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
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
                    id = "candidate_31_2",
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
                    label = "celebration",
                    id = "candidate_31_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "music_notes",
                    id = "candidate_31_4",
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
    // くるっと回って
    Template(
        id = "candidate_32",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "くるっと",
                    id = "candidate_32_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    text = "回って",
                    id = "candidate_32_2",
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
                    label = "curved_arrow",
                    id = "candidate_32_3",
                    offset = Offset(180.0f, 145.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "ribbon",
                    id = "candidate_32_4",
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
    // 手招きして
    Template(
        id = "candidate_33",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "手招き",
                    id = "candidate_33_1",
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
                    id = "candidate_33_2",
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
                    label = "pan_tool_alt",
                    id = "candidate_33_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.PURPLE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_33_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.RED.value,
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
    // 猫ポーズして
    Template(
        id = "candidate_34",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "猫",
                    id = "candidate_34_1",
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
                    text = "ポーズして",
                    id = "candidate_34_2",
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
                    id = "candidate_34_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // うさ耳して
    Template(
        id = "candidate_35",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "うさ耳",
                    id = "candidate_35_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "して",
                    id = "candidate_35_2",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "ribbon",
                    id = "candidate_35_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_35_4",
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
    // 頭ポンして
    Template(
        id = "candidate_36",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "頭",
                    id = "candidate_36_1",
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
                    text = "ポンして",
                    id = "candidate_36_2",
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
                    id = "candidate_36_3",
                    offset = Offset(170.0f, -80.0f),
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
    // 顎クイして
    Template(
        id = "candidate_37",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "顎クイ",
                    id = "candidate_37_1",
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
                    id = "candidate_37_2",
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
                    label = "heart_arrow",
                    id = "candidate_37_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "candidate_37_4",
                    offset = Offset(-195.0f, -195.0f),
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
    // 目隠しして
    Template(
        id = "candidate_38",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "目隠し",
                    id = "candidate_38_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    text = "して",
                    id = "candidate_38_2",
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
                    label = "eyeglasses",
                    id = "candidate_38_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "candidate_38_4",
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
    // 決めポーズして
    Template(
        id = "candidate_39",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "決め",
                    id = "candidate_39_1",
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
                    id = "candidate_39_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
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
                    text = "して",
                    id = "candidate_39_3",
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
                    id = "candidate_39_4",
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
                    id = "candidate_39_5",
                    offset = Offset(-130.0f, 145.0f),
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
    // 腕組みして
    Template(
        id = "candidate_40",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "腕組み",
                    id = "candidate_40_1",
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
                    id = "candidate_40_2",
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
                    label = "emphasis_marks",
                    id = "candidate_40_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "candidate_40_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.LIGHT_GREEN.value,
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
    // 見つけて
    Template(
        id = "candidate_41",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "見",
                    id = "candidate_41_1",
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
                    text = "つけて",
                    id = "candidate_41_2",
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
                    label = "star_rounded",
                    id = "candidate_41_3",
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
    // 気づいて
    Template(
        id = "candidate_42",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "気づ",
                    id = "candidate_42_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "いて",
                    id = "candidate_42_2",
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
                    label = "emphasis_marks",
                    id = "candidate_42_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_42_4",
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
    // 手ふり返して
    Template(
        id = "candidate_43",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "手",
                    id = "candidate_43_1",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -5.0f,
                    scale = 5.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "ふり返して",
                    id = "candidate_43_2",
                    offset = Offset(5.0f, 150.0f),
                    rotation = 0.0f,
                    scale = 1.6f,
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
                    id = "candidate_43_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // こっち来て
    Template(
        id = "candidate_44",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "こっち",
                    id = "candidate_44_1",
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
                    text = "来て",
                    id = "candidate_44_2",
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
                    label = "pan_tool_alt",
                    id = "candidate_44_3",
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
                    label = "heart",
                    id = "candidate_44_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.RED.value,
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
    // 近くに来て
    Template(
        id = "candidate_45",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "近くに",
                    id = "candidate_45_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    text = "来て",
                    id = "candidate_45_2",
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
                    label = "winged_heart",
                    id = "candidate_45_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "candidate_45_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.LIGHT_GREEN.value,
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
    // 指さして笑って
    Template(
        id = "candidate_46",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "指さし",
                    id = "candidate_46_1",
                    offset = Offset(-85.0f, -215.0f),
                    rotation = -4.0f,
                    scale = 1.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "て",
                    id = "candidate_46_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "笑って",
                    id = "candidate_46_3",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 1.8f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "pan_tool_alt",
                    id = "candidate_46_4",
                    offset = Offset(115.0f, -215.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "flower",
                    id = "candidate_46_5",
                    offset = Offset(-150.0f, 135.0f),
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
    // 誕生日祝って
    Template(
        id = "candidate_47",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "誕生日",
                    id = "candidate_47_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "祝って",
                    id = "candidate_47_2",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "cake",
                    id = "candidate_47_3",
                    offset = Offset(180.0f, 135.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "gift",
                    id = "candidate_47_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.RED.value,
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
    // 結婚して
    Template(
        id = "candidate_48",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "結婚",
                    id = "candidate_48_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
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
                    id = "candidate_48_2",
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
                    label = "diamond",
                    id = "candidate_48_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_48_4",
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
    // 付き合って
    Template(
        id = "candidate_49",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "付き合",
                    id = "candidate_49_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
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
                    text = "って",
                    id = "candidate_49_2",
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
                    label = "heart_arrow",
                    id = "candidate_49_3",
                    offset = Offset(150.0f, 105.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "ribbon",
                    id = "candidate_49_4",
                    offset = Offset(-195.0f, -195.0f),
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
    // ぎゅーして
    Template(
        id = "candidate_50",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ぎゅー",
                    id = "candidate_50_1",
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
                    id = "candidate_50_2",
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
                    label = "heart",
                    id = "candidate_50_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.ORANGE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "candidate_50_4",
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
    // 待ってて
    Template(
        id = "candidate_51",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "待",
                    id = "candidate_51_1",
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
                    text = "ってて",
                    id = "candidate_51_2",
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
                    label = "heart_cute",
                    id = "candidate_51_3",
                    offset = Offset(170.0f, -80.0f),
                    rotation = 15.0f,
                    scale = 1.0f,
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
    // 覚えてて
    Template(
        id = "candidate_52",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "覚え",
                    id = "candidate_52_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "てて",
                    id = "candidate_52_2",
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
                    label = "star_rounded",
                    id = "candidate_52_3",
                    offset = Offset(150.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "balloon",
                    id = "candidate_52_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
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
    // 忘れないで
    Template(
        id = "candidate_53",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "忘れ",
                    id = "candidate_53_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.4f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "ないで",
                    id = "candidate_53_2",
                    offset = Offset(-90.0f, 100.0f),
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
                    label = "winged_heart",
                    id = "candidate_53_3",
                    offset = Offset(210.0f, 115.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.PURPLE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "heart_cute",
                    id = "candidate_53_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.RED.value,
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
    // また会おうって言って
    Template(
        id = "candidate_54",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "また",
                    id = "candidate_54_1",
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
                    text = "会おう",
                    id = "candidate_54_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
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
                    text = "言って",
                    id = "candidate_54_3",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 1.8f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "speech_bubble",
                    id = "candidate_54_4",
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
                    label = "heart_cute",
                    id = "candidate_54_5",
                    offset = Offset(-140.0f, 145.0f),
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
    // 一緒に歌って
    Template(
        id = "candidate_55",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "一緒に",
                    id = "candidate_55_1",
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
                    text = "歌って",
                    id = "candidate_55_2",
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
                    label = "music_notes",
                    id = "candidate_55_3",
                    offset = Offset(180.0f, 145.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.LIGHT_BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "audio_track",
                    id = "candidate_55_4",
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
    // 一緒に踊って
    Template(
        id = "candidate_56",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "一緒に",
                    id = "candidate_56_1",
                    offset = Offset(0.0f, -90.0f),
                    rotation = -4.0f,
                    scale = 3.1f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Text(
                    text = "踊って",
                    id = "candidate_56_2",
                    offset = Offset(-70.0f, 100.0f),
                    rotation = 0.0f,
                    scale = 2.3f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.MOCHIY_POP_ONE
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "candidate_56_3",
                    offset = Offset(170.0f, 145.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "music_notes",
                    id = "candidate_56_4",
                    offset = Offset(-195.0f, -195.0f),
                    rotation = -20.0f,
                    scale = 0.55f,
                    color = DecorationColors.GREEN.value,
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
    // マイク向けて
    Template(
        id = "candidate_57",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "マイク",
                    id = "candidate_57_1",
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
                    text = "向けて",
                    id = "candidate_57_2",
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
                    label = "microphone_stage",
                    id = "candidate_57_3",
                    offset = Offset(170.0f, 135.0f),
                    rotation = 14.0f,
                    scale = 1.0f,
                    color = DecorationColors.BLUE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "auto_awesome",
                    id = "candidate_57_4",
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
    // 盛り上げて
    Template(
        id = "candidate_58",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "盛り",
                    id = "candidate_58_1",
                    offset = Offset(-85.0f, -215.0f),
                    rotation = -4.0f,
                    scale = 1.9f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "上げ",
                    id = "candidate_58_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "て",
                    id = "candidate_58_3",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 2.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Sticker(
                    label = "local_fire_department",
                    id = "candidate_58_4",
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
                    label = "celebration",
                    id = "candidate_58_5",
                    offset = Offset(-130.0f, 145.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
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
    // 煽って
    Template(
        id = "candidate_59",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "煽",
                    id = "candidate_59_1",
                    offset = Offset(-60.0f, -85.0f),
                    rotation = -5.0f,
                    scale = 5.0f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 6.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 6.0f,
                    width = 900,
                    font = FontFamilies.DELA_GOTHIC_ONE
                ),
                Decoration.Text(
                    text = "って",
                    id = "candidate_59_2",
                    offset = Offset(5.0f, 150.0f),
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
                    label = "bolt",
                    id = "candidate_59_3",
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
    // もう一回やって
    Template(
        id = "candidate_60",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "もう",
                    id = "candidate_60_1",
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
                    text = "一回",
                    id = "candidate_60_2",
                    offset = Offset(0.0f, -30.0f),
                    rotation = -4.0f,
                    scale = 3.6f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 8.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 8.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Text(
                    text = "やって",
                    id = "candidate_60_3",
                    offset = Offset(70.0f, 140.0f),
                    rotation = 0.0f,
                    scale = 1.8f,
                    color = DecorationColors.PINK.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10.0f,
                    secondBorderColor = DecorationColors.BLACK.value,
                    secondBorderWidth = 10.0f,
                    width = 900,
                    font = FontFamilies.M_PLUS_ROUNDED_1C
                ),
                Decoration.Sticker(
                    label = "curved_arrow",
                    id = "candidate_60_4",
                    offset = Offset(115.0f, -215.0f),
                    rotation = 15.0f,
                    scale = 0.75f,
                    color = DecorationColors.LIGHT_GREEN.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 2.0f,
                    secondStrokeColor = DecorationColors.BLACK.value,
                    secondStrokeWidth = 2.0f
                ),
                Decoration.Sticker(
                    label = "star_rounded",
                    id = "candidate_60_5",
                    offset = Offset(-130.0f, 145.0f),
                    rotation = -20.0f,
                    scale = 0.7f,
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
    )
)
