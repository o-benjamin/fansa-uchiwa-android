package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color

/**
 * 永続化済み、またはテンプレートとして扱ううちわのスナップショット。
 *
 * 識別子を持たない値オブジェクトとして扱い、識別子を持つ [Uchiwa] との変換責務をこの型に閉じ込める。
 * これにより、Presentation 層や Application 層が保存モデルの構造を直接知りすぎないようにする。
 */
data class SavedUchiwa(
    val decorations: List<Decoration>,
    val uchiwaColor: Color,
    val backgroundColor: Color
) {
    val appearance: Appearance
        get() = Appearance(
            uchiwaColor = uchiwaColor,
            backgroundColor = backgroundColor
        )

    fun toUchiwa(id: String): Uchiwa {
        return Uchiwa(
            id = id,
            decorations = decorations,
            uchiwaColor = appearance.uchiwaColor,
            backgroundColor = appearance.backgroundColor
        )
    }

    fun withAppearance(appearance: Appearance): SavedUchiwa {
        return copy(
            uchiwaColor = appearance.uchiwaColor,
            backgroundColor = appearance.backgroundColor
        )
    }

    companion object {
        fun fromUchiwa(uchiwa: Uchiwa): SavedUchiwa {
            return SavedUchiwa(
                decorations = uchiwa.decorations,
                uchiwaColor = uchiwa.uchiwaColor,
                backgroundColor = uchiwa.backgroundColor
            )
        }
    }

    data class Appearance(
        val uchiwaColor: Color,
        val backgroundColor: Color
    )
}
