package com.fansauchiwa.data.infra

interface GalleryImageDataSource {
    fun saveImageToGallery(imagePath: String): Boolean
}

