package com.zoho.arattai.core;

/**
 * Abstract base class for all message types in the WhatsApp chat parser system.
 */
public class Message {

    /**
     * The display name of the participant who sent this message.
     */
    public String sender;

    /**
     * The date and time at which this message was sent.
     */
    public java.util.Date timestamp;

    /**
     * The category of this message.
     */
    public MessageType messageType;

    /**
     * @param sender      the display name of the message sender
     * @param timestamp   the moment the message was sent
     * @param messageType the content category of the message
     */
    public Message(String sender, java.util.Date timestamp, MessageType messageType) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.messageType = messageType;
    }

    /**
     * Enumeration of all content categories supported by the WhatsApp chat parser.
     */
    public enum MessageType {
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
