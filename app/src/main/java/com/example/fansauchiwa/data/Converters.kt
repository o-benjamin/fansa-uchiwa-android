package com.example.fansauchiwa.data

import androidx.compose.ui.graphics.Color
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class Converters {
    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(Decoration::class) {
                subclass(Decoration.Text::class)
                subclass(Decoration.Sticker::class)
                subclass(Decoration.Image::class)
            }
        }
    }

    @TypeConverter
    fun decorationsFromJson(jsonString: String): List<Decoration> {
        return json.decodeFromString(jsonString)
    }

    @TypeConverter
    fun decorationsToJson(decorations: List<Decoration>): String {
        return json.encodeToString(decorations)
    }
}