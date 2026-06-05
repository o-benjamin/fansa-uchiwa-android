package com.fansauchiwa.edit

import androidx.annotation.StringRes
import com.fansauchiwa.R

enum class DecorationTabType(@StringRes val tabTextRes: Int) {
    TEXT(R.string.edit_tab_text),
    IMAGE(R.string.edit_tab_image),
    STAMP(R.string.edit_tab_sticker),
    BACKGROUND(R.string.edit_tab_overall),
    LAYERS(R.string.edit_tab_layers)
}
