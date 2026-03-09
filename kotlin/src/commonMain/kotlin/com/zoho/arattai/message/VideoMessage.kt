package com.zoho.arattai.message

import com.zoho.arattai.core.Message
import com.zoho.arattai.core.Message.Type

/**
 * Represents a video clip attachment parsed from a WhatsApp chat export.
 */
class VideoMessage(
    /** The filename of the video file as stored inside the export ZIP. */
    val name: String,
    /** The uncompressed file size of the video in bytes. */
    val size: Int,
    /** The playback duration of the video, formatted as "m:ss". */
    val duration: String,
    /** The lowercase file extension identifying the video container format. */
    val extension: String,
    /** The horizontal resolution of the video in pixels, or 0 if unknown. */
    val width: Int,
    /** The vertical resolution of the video in pixels, or 0 if unknown. */
    val height: Int,
    sender: String,
    timestamp: Long,
    type: Type
) : Message(sender, timestamp, type)
