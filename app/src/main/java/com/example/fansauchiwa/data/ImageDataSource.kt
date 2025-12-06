package com.example.fansauchiwa.data

import android.net.Uri

interface ImageDataSource {
    fun save(uri: Uri)
}