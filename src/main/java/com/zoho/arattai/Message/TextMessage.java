package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.MessageType;
import com.zoho.arattai.core.Message;

/**
 * Represents a plain-text message parsed from a WhatsApp chat export.
 *
 * <p>
 * A {@code TextMessage} is created for any line whose content is not
 * recognised as a media attachment. This includes:
 * <ul>
 * <li>Regular conversation text.</li>
 * <li>Deleted-message placeholders
 * ({@code "You deleted this message"} /
 * {@code "This message was deleted"}).</li>
 * <li>Edited-message records that contain the annotation
 * {@code "<This message was edited>"}.</li>
 * </ul>
 *
 * <p>
 * All fields are immutable ({@code private final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see MessageType#TEXT
 */
public class TextMessage extends Message {

    /**
     * The raw text content of the message exactly as it appears in
     * the WhatsApp transcript (including emoji and special characters).
     */
    private final String text;

    /**
     * Constructs a new {@code TextMessage}.
     *
     * @param text        the text body of the message; must not be {@code null}
     * @param sender      the display name of the sender; must not be {@code null}
     * @param timestamp   the date and time the message was sent; must not be
     *                    {@code null}
     * @param messageType the type classification; expected to be
     *                    {@link MessageType#TEXT}
     */
    public TextMessage(String text, String sender, java.util.Date timestamp, MessageType messageType) {
        super(sender, timestamp, messageType);
        this.text = text;
    }

    /**
     * Returns the raw text body of this message.
     *
     * @return the message text; never {@code null}
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the display name of the participant who sent this message.
     *
     * @return the sender name; never {@code null}
     */
    public String getTextSender() {
        return sender;
    }

    /**
     * Returns the date and time at which this message was sent.
     *
     * @return the message timestamp; never {@code null}
     */
    public java.util.Date getTextTimestamp() {
        return timestamp;
    }

    /**
     * Returns the message type classification for this message.
     *
     * @return {@link MessageType#TEXT}
     */
    public MessageType getTextMessageType() {
        return messageType;
    }
}
