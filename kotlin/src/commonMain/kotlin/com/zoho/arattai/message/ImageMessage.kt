package com.zoho.arattai.message

import com.zoho.arattai.core.Message
import com.zoho.arattai.core.Message.Type

/**
 * Represents a photo or image attachment parsed from a WhatsApp chat export.
 */
class ImageMessage(
    /** The filename of the image as stored inside the export ZIP. */
    val name: String,
    /** The vertical dimension of the image in pixels, or 0 if unknown. */
    val height: Int,
    /** The horizontal dimension of the image in pixels, or 0 if unknown. */
    val width: Int,
    /** The uncompressed file size of the image in bytes. */
    val size: Int,
    /** The lowercase file extension that identifies the image format. */
    val extension: String,
    sender: String,
    timestamp: Long,
    type: Type
) : Message(sender, timestamp, type)
