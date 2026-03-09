package com.zoho.arattai.core

/**
 * Abstract base class for all message types in the WhatsApp chat parser system.
 */
abstract class Message(
    /**
     * The display name of the participant who sent this message.
     */
    val sender: String,
    /**
     * The timestamp at which this message was sent (milliseconds since epoch).
     */
    val timestamp: Long,
    /**
     * The category of this message.
     */
    val type: Type
) {
    /**
     * Enumeration of all content categories supported by the WhatsApp chat parser.
     */
    enum class Type {
        /** Plain-text message. */
        TEXT,
        /** Photo or image attachment. */
        IMAGE,
        /** Video clip attachment. */
        VIDEO,
        /** Voice note or audio file attachment. */
        AUDIO,
        /** Generic file attachment. */
        DOCUMENT,
        /** Animated WebP sticker. */
        STICKER
    }
}
