package com.fansauchiwa

import android.os.Build
import android.os.Bundle
import androidx.navigation.NavType
import java.io.Serializable

class SerializableEnumNavType<T>(
    private val type: Class<T>
) : NavType<T?>(true) where T : Enum<T>, T : Serializable {

    override val name: String = type.name

    override fun get(bundle: Bundle, key: String): T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        bundle.getSerializable(key, type)
    } else {
        @Suppress("DEPRECATION")
        bundle.getSerializable(key) as? T
    }

    override fun parseValue(value: String): T = java.lang.Enum.valueOf(type, value)

    override fun put(bundle: Bundle, key: String, value: T?) {
        bundle.putSerializable(key, value)
    }

    override fun serializeAsValue(value: T?): String = value?.name.orEmpty()
}
