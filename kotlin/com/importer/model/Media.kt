package com.importer.model

sealed class Media {
    data class Image(val path: String, val width: Int, val height: Int) : Media()
    data class Video(val path: String, val durationMillis: Long, val thumbnailPath: String?) : Media()
    data class Audio(val path: String, val durationMillis: Long) : Media()
    data class Document(val path: String, val fileName: String, val fileSize: Long) : Media()
    data class Sticker(val path: String) : Media()
    object Unknown : Media()
}