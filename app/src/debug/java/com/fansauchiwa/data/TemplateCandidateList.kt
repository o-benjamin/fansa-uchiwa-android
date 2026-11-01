package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies

// debug ビルドのホームにだけ出すテンプレートの候補。書き出しツールが丸ごと作り直すので、手で直した分は次の書き出しで消える。
// 手順は .agents/template-candidates.md
val templateCandidateList: List<Template> = listOf(
    // 型A 主役の語＋添え（ピースして）
    Template(
        id = "candidate_1",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "ピース",
                    id = "candidate_1_1",
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
                    id = "candidate_1_2",
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
                    id = "candidate_1_3",
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
                    id = "candidate_1_4",
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
    // 型B 1字を主役（手ふって）
    Template(
        id = "candidate_2",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "手",
                    id = "candidate_2_1",
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
                    id = "candidate_2_2",
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
                    id = "candidate_2_3",
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
    // 型C 小・大・小の3段（投げキスして）
    Template(
        id = "candidate_3",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "投げ",
                    id = "candidate_3_1",
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
                    id = "candidate_3_2",
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
                    id = "candidate_3_3",
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
                    id = "candidate_3_4",
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
                    id = "candidate_3_5",
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
    )
)
