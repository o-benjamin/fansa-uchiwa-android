package com.example.fansauchiwa.data

interface GalleryImageDataSource {
    fun saveImageToGallery(imagePath: String): Boolean
}

