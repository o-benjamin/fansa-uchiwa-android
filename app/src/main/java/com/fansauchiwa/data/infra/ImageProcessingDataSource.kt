package com.fansauchiwa.data.infra

import android.net.Uri

interface ImageProcessingDataSource {
    suspend fun removeBackground(sourceUri: Uri): Uri?
}

