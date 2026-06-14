package com.fansauchiwa.data

data class Template(
    val id: String,
    val savedUchiwa: SavedUchiwa,
    val isNameInputPlaceholderEnabled: Boolean = false
)
