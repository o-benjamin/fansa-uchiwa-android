package com.fansauchiwa

import com.fansauchiwa.data.DecorationColors
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EditScreenInputArg(
    val uchiwaId: String,
    val templateId: String? = null,
    val templateMainColor: DecorationColors? = null,
    val lastName: String? = null,
    val firstName1: String? = null,
    val firstName2: String? = null,
    val honorific: String? = null
) {
    fun toRouteArgument(): String = Json.encodeToString(this)

    companion object {
        fun fromRouteArgument(value: String): EditScreenInputArg = runCatching {
            Json.decodeFromString<EditScreenInputArg>(value)
        }.getOrElse {
            Json.decodeFromString<EditScreenInputArg>(
                URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
            )
        }
    }
}
