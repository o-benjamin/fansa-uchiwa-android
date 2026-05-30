package com.fansauchiwa

import com.fansauchiwa.data.DecorationColors

data class EditScreenInputArg(
    val uchiwaId: String,
    val templateId: String? = null,
    val templateMainColor: DecorationColors? = null
)
