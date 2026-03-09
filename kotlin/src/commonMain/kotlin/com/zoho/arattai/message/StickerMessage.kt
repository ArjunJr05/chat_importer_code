package com.zoho.arattai.message

import com.zoho.arattai.core.Message
import com.zoho.arattai.core.Message.Type

/**
 * Represents an animated WebP sticker message parsed from a WhatsApp chat
 * export.
 */
class StickerMessage(
    /** The filename of the sticker file as stored inside the export ZIP. */
    val name: String,
    /** The uncompressed file size of the sticker in bytes. */
    val size: Int,
    /** The lowercase file extension of the sticker. Always "webp". */
    val extension: String,
    sender: String,
    timestamp: Long,
    type: Type
) : Message(sender, timestamp, type)
