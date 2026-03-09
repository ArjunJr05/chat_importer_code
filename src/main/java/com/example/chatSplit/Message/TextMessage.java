package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.Type;
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
 * All fields are immutable ({@code public final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see Type#TEXT
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
     * @param text      the text body of the message; must not be {@code null}
     * @param sender    the display name of the sender; must not be {@code null}
     * @param timestamp the date and time the message was sent; must not be
     *                  {@code null}
     * @param type      the type classification; expected to be
     *                  {@link Type#TEXT}
     */
    public TextMessage(String text, String sender, java.util.Date timestamp, Type type) {
        super(sender, timestamp, type);
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

}
