package com.fansauchiwa.data

interface GalleryImageDataSource {
    fun saveImageToGallery(imagePath: String): Boolean
}

