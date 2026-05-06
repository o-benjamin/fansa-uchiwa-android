package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import androidx.room.TypeConverter
import com.fansauchiwa.edit.FontFamilies
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val decorationPersistenceCodec: DecorationPersistenceCodec = KotlinxDecorationPersistenceCodec()

    @TypeConverter
    fun decorationsFromJson(jsonString: String): List<Decoration> {
        return decorationPersistenceCodec.decode(jsonString)
    }

    @TypeConverter
    fun decorationsToJson(decorations: List<Decoration>): String {
        return decorationPersistenceCodec.encode(decorations)
    }
}

private interface DecorationPersistenceCodec {
    fun decode(jsonString: String): List<Decoration>
    fun encode(decorations: List<Decoration>): String
}

private class KotlinxDecorationPersistenceCodec(
    private val json: Json = Json
) : DecorationPersistenceCodec {
    override fun decode(jsonString: String): List<Decoration> {
        return json.decodeFromString<List<PersistedDecoration>>(jsonString)
            .map(PersistedDecoration::toDomainModel)
    }

    override fun encode(decorations: List<Decoration>): String {
        return json.encodeToString(decorations.map(Decoration::toPersistedModel))
    }
}

@Serializable
private sealed interface PersistedDecoration {
    fun toDomainModel(): Decoration
}

@Serializable
@SerialName("com.fansauchiwa.data.Decoration.Text")
private data class PersistedTextDecoration(
    val text: String = DEFAULT_DECORATION_TEXT,
    val id: String,
    val offset: PersistedOffset = PersistedOffset(),
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val color: Long = DecorationColors.WHITE.value.toColorLong(),
    val strokeColor: Long = DecorationColors.MAGENTA.value.toColorLong(),
    val strokeWidth: Float = 30f,
    val secondBorderColor: Long = DecorationColors.WHITE.value.toColorLong(),
    val secondBorderWidth: Float = 0f,
    val isPuffyEnabled: Boolean = false,
    val width: Int,
    val font: String
) : PersistedDecoration {
    override fun toDomainModel(): Decoration {
        return Decoration.Text(
            text = text,
            id = id,
            offset = offset.toDomainModel(),
            rotation = rotation,
            scale = scale,
            color = color.toComposeColor(),
            strokeColor = strokeColor.toComposeColor(),
            strokeWidth = strokeWidth,
            secondBorderColor = secondBorderColor.toComposeColor(),
            secondBorderWidth = secondBorderWidth,
            isPuffyEnabled = isPuffyEnabled,
            width = width,
            font = FontFamilies.valueOf(font)
        )
    }
}

@Serializable
@SerialName("com.fansauchiwa.data.Decoration.Sticker")
private data class PersistedStickerDecoration(
    val label: String,
    val id: String,
    val offset: PersistedOffset = PersistedOffset(),
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val color: Long = DecorationColors.MAGENTA.value.toColorLong(),
    val strokeColor: Long = DecorationColors.WHITE.value.toColorLong(),
    val strokeWidth: Float = 3f,
    val secondStrokeColor: Long = DecorationColors.WHITE.value.toColorLong(),
    val secondStrokeWidth: Float = 0f,
) : PersistedDecoration {
    override fun toDomainModel(): Decoration {
        return Decoration.Sticker(
            label = label,
            id = id,
            offset = offset.toDomainModel(),
            rotation = rotation,
            scale = scale,
            color = color.toComposeColor(),
            strokeColor = strokeColor.toComposeColor(),
            strokeWidth = strokeWidth,
            secondStrokeColor = secondStrokeColor.toComposeColor(),
            secondStrokeWidth = secondStrokeWidth
        )
    }
}

@Serializable
@SerialName("com.fansauchiwa.data.Decoration.Image")
private data class PersistedImageDecoration(
    val id: String,
    val imageId: String,
    val offset: PersistedOffset = PersistedOffset(),
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val color: Long = DecorationColors.WHITE.value.toColorLong(),
    val strokeColor: Long = DecorationColors.CYAN.value.toColorLong(),
    val strokeWidth: Float = 30f,
) : PersistedDecoration {
    override fun toDomainModel(): Decoration {
        return Decoration.Image(
            id = id,
            imageId = imageId,
            offset = offset.toDomainModel(),
            rotation = rotation,
            scale = scale,
            color = color.toComposeColor(),
            strokeColor = strokeColor.toComposeColor(),
            strokeWidth = strokeWidth
        )
    }
}

@Serializable
private data class PersistedOffset(
    val x: Float = 0f,
    val y: Float = 0f
) {
    fun toDomainModel(): Offset = Offset(x, y)
}

private fun Decoration.toPersistedModel(): PersistedDecoration {
    return when (this) {
        is Decoration.Text -> PersistedTextDecoration(
            text = text,
            id = id,
            offset = offset.toPersistedModel(),
            rotation = rotation,
            scale = scale,
            color = color.toColorLong(),
            strokeColor = strokeColor.toColorLong(),
            strokeWidth = strokeWidth,
            secondBorderColor = secondBorderColor.toColorLong(),
            secondBorderWidth = secondBorderWidth,
            isPuffyEnabled = isPuffyEnabled,
            width = width,
            font = font.name
        )

        is Decoration.Sticker -> PersistedStickerDecoration(
            label = label,
            id = id,
            offset = offset.toPersistedModel(),
            rotation = rotation,
            scale = scale,
            color = color.toColorLong(),
            strokeColor = strokeColor.toColorLong(),
            strokeWidth = strokeWidth,
            secondStrokeColor = secondStrokeColor.toColorLong(),
            secondStrokeWidth = secondStrokeWidth
        )

        is Decoration.Image -> PersistedImageDecoration(
            id = id,
            imageId = imageId,
            offset = offset.toPersistedModel(),
            rotation = rotation,
            scale = scale,
            color = color.toColorLong(),
            strokeColor = strokeColor.toColorLong(),
            strokeWidth = strokeWidth
        )
    }
}

private fun Offset.toPersistedModel(): PersistedOffset = PersistedOffset(x = x, y = y)

private fun Long.toComposeColor(): Color = Color(toULong())
