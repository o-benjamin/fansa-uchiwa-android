package com.fansauchiwa

import com.fansauchiwa.data.DecorationColors

data class EditScreenInputArg(
    val uchiwaId: String,
    val templateId: String? = null,
    val templateMainColor: DecorationColors? = null,
    val lastName: String? = null,
    val firstName1: String? = null,
    val firstName2: String? = null,
    val honorific: String? = null
)
