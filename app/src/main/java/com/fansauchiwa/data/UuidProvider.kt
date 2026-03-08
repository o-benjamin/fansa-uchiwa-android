package com.fansauchiwa.data

import java.util.UUID
import javax.inject.Inject

interface UuidProvider {
    fun generate(): String
}

class UuidProviderImpl @Inject constructor() : UuidProvider {
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}

