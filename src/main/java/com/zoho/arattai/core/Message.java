package com.zoho.arattai.core;

import com.zoho.arattai.Model.MessageType;

/**
 * Abstract base class for all message types in the WhatsApp chat parser system.
 *
 * <p>
 * Every message parsed from a WhatsApp export ZIP carries three pieces of
 * shared metadata regardless of its content type:
 * <ul>
 * <li><b>sender</b> – the display name of the participant who sent the
 * message.</li>
 * <li><b>timestamp</b> – the exact date and time the message was
 * delivered.</li>
 * <li><b>messageType</b> – the {@link MessageType} enum constant that describes
 * the nature of the content (TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, or
 * STICKER).</li>
 * </ul>
 *
 * <p>
 * Concrete subclasses (e.g., {@code TextMessage}, {@code ImageMessage}) extend
 * this class to add media-specific attributes. Each subclass also exposes
 * type-prefixed accessors (e.g., {@code getImageSender()}) to allow callers to
 * retrieve the shared fields without requiring an explicit cast to the base
 * class.
 *
 * <p>
 * <b>Design note:</b> fields are {@code public} intentionally so that
 * subclasses within the same module can access them directly without
 * reflection.
 * If the API surface needs to be tightened in the future, introduce accessor
 * methods in this base class and tighten the visibility modifier accordingly.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see MessageType
 */
public class Message {

    /**
     * The display name of the participant who sent this message,
     * as it appears in the chat export transcript.
     */
    public String sender;

    /**
     * The date and time at which this message was sent.
     * Parsed from the WhatsApp timestamp string using the pattern
     * {@code dd/MM/yyyy, hh:mm a}.
     */
    public java.util.Date timestamp;

    /**
     * The category of this message.
     * Set at construction time by the parser and never changed afterwards.
     *
     * @see MessageType
     */
    public MessageType messageType;

    /**
     * Constructs a {@code Message} with the three shared fields that every
     * WhatsApp message carries.
     *
     * @param sender      the display name of the message sender; must not be
     *                    {@code null}
     * @param timestamp   the moment the message was sent; must not be {@code null}
     * @param messageType the content category of the message; must not be
     *                    {@code null}
     */
    public Message(String sender, java.util.Date timestamp, MessageType messageType) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.messageType = messageType;
    }
}
