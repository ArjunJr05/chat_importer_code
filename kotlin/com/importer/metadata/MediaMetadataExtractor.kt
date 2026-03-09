package com.importer.metadata

/**
 * Expect class for extracting media metadata.
 * Actual implementations will use platform-specific APIs.
 */
expect class MediaMetadataExtractor {
    fun getImageDimensions(imagePath: String): Pair<Int, Int>?
    fun getVideoDuration(videoPath: String): Long? // Duration in milliseconds
    fun getAudioDuration(audioPath: String): Long? // Duration in milliseconds
    // Add other media types as needed
}