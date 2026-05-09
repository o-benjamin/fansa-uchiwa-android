package com.fansauchiwa.data

fun extractUchiwaIdFromImagePath(imagePath: String): String {
    return imagePath.substringAfterLast("/").substringBeforeLast(".png")
}
