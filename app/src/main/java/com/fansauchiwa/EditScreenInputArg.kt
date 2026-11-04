package com.fansauchiwa

import com.fansauchiwa.data.DecorationColors
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val editScreenInputArgJson = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
}

@Serializable
data class EditScreenInputArg(
    val uchiwaId: String,
    val templateId: String? = null,
    val templateMainColor: DecorationColors? = null,
    val lastName: String = "",
    val firstName1: String = "",
    val firstName2: String = "",
    val honorific: String = "",
    // 今日のお題から作るとき（#360）、白紙のうちわの真ん中に置く文字。テンプレートとは一緒に使わない
    val dailyPromptText: String? = null
) {
    fun toRouteArgument(): String =
        editScreenInputArgJson.encodeToString(serializer(), this)

    companion object {
        fun fromRouteArgument(value: String): EditScreenInputArg = runCatching {
            editScreenInputArgJson.decodeFromString(serializer(), value)
        }.getOrElse {
            editScreenInputArgJson.decodeFromString(
                serializer(),
                URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
            )
        }
    }
}
