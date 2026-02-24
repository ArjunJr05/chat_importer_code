package com.zoho.arattai.core;

/**
 * Abstract base class for all message types in the WhatsApp chat parser system.
 */
public abstract class Message {

    /**
     * The display name of the participant who sent this message.
     */
    private String sender;

    /**
     * The date and time at which this message was sent.
     */
    private java.util.Date timestamp;

    /**
     * The category of this message.
     */
    private MessageType messageType;

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
     * Returns the name of the participant who sent this message.
     * 
     * @return the sender's display name
     */
    public String getSender() {
        return sender;
    }

    /**
     * Returns the date and time the message was sent.
     * 
     * @return the timestamp
     */
    public java.util.Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the classification type of this message.
     * 
     * @return the message type
     */
    public MessageType getMessageType() {
        return messageType;
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
