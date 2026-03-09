package com.zoho.arattai.message

import com.zoho.arattai.core.Message
import com.zoho.arattai.core.Message.Type

/**
 * Represents a plain-text message parsed from a WhatsApp chat export.
 */
class TextMessage(
    /**
     * The raw text content of the message exactly as it appears in
     * the WhatsApp transcript (including emoji and special characters).
     */
    val text: String,
    sender: String,
    timestamp: Long,
    type: Type
) : Message(sender, timestamp, type)
