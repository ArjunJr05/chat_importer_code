package com.zoho.arattai.message

import com.zoho.arattai.core.Message
import com.zoho.arattai.core.Message.Type

/**
 * Represents a generic file attachment message parsed from a WhatsApp chat
 * export.
 */
class DocumentMessage(
    /** The filename of the document as stored inside the export ZIP. */
    val name: String,
    /** The lowercase file extension identifying the document format. */
    val extension: String,
    /** The uncompressed file size of the document in bytes. */
    val size: Int,
    sender: String,
    timestamp: Long,
    type: Type
) : Message(sender, timestamp, type)
