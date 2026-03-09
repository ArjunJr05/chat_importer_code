package com.zoho.arattai.message

import com.zoho.arattai.core.Message
import com.zoho.arattai.core.Message.Type

/**
 * Represents a voice note or audio file attachment parsed from a WhatsApp chat
 * export.
 */
class AudioMessage(
    /** The filename of the audio file as stored inside the export ZIP. */
    val name: String,
    /** The uncompressed file size of the audio in bytes. */
    val size: Int,
    /** The playback duration of the audio clip, formatted as "m:ss". */
    val duration: String,
    /** The lowercase file extension identifying the audio format. */
    val extension: String,
    sender: String,
    timestamp: Long,
    type: Type
) : Message(sender, timestamp, type)
