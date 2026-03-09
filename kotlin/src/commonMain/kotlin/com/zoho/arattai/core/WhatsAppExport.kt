package com.zoho.arattai.core

/**
 * Immutable container for a fully-parsed WhatsApp chat export.
 */
class WhatsAppExport(
    /**
     * The human-readable chat name derived from the export ZIP filename.
     */
    val chatName: String,
    /**
     * All messages in chronological order exactly as they appear
     * in the WhatsApp transcript file.
     */
    val allMessages: List<Message>
)
