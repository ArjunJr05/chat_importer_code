package com.zoho.arattai.Message;

import com.zoho.arattai.Model.MessageType;
import com.zoho.arattai.core.Message;

/**
 * Represents an animated WebP sticker message parsed from a WhatsApp chat export.
 *
 * <p>WhatsApp names every sticker file using the convention
 * {@code STK-<date>-WA<seq>.webp} (e.g., {@code STK-20260102-WA0003.webp}).
 * The parser uses this naming convention — specifically the presence of the
 * substring {@code "stk"} combined with the {@code ".webp"} extension — to
 * distinguish stickers from regular {@link ImageMessage image} attachments, which
 * can also be {@code .webp} files.
 *
 * <p>Sticker payloads are animated WebP files; no content decoding is performed
 * on them.  Only the metadata available in the ZIP directory entry (filename and
 * uncompressed size) is captured.
 *
 * <p>All fields are immutable ({@code private final}) and initialised at
 * construction time.
 *
 * @author  Zoho Arattai
 * @version 1.0
 * @see     Message
 * @see     MessageType#STICKER
 * @see     ImageMessage
 */
public class StickerMessage extends Message {

    /**
     * The filename of the sticker file as stored inside the export ZIP
     * (e.g., {@code "STK-20260102-WA0003.webp"}).
     */
    private final String stickerName;

    /** The uncompressed file size of the sticker in bytes. */
    private final int stickerSize;

    /**
     * The lowercase file extension of the sticker.
     * Always {@code "webp"} for WhatsApp stickers.
     */
    private final String stickerType;

    /**
     * Constructs a new {@code StickerMessage} with all metadata provided by the parser.
     *
     * @param stickerName the sticker filename inside the ZIP; must not be {@code null}
     * @param stickerSize the file size in bytes
     * @param stickerType the lowercase file extension (typically {@code "webp"});
     *                    must not be {@code null}
     * @param sender      the display name of the sender; must not be {@code null}
     * @param timestamp   the date and time the message was sent; must not be {@code null}
     * @param messageType the type classification; expected to be {@link MessageType#STICKER}
     */
    public StickerMessage(String stickerName, int stickerSize, String stickerType,
            String sender, java.util.Date timestamp, MessageType messageType) {
        super(sender, timestamp, messageType);
        this.stickerName = stickerName;
        this.stickerSize = stickerSize;
        this.stickerType = stickerType;
    }

    /**
     * Returns the filename of the sticker file inside the export ZIP.
     *
     * @return the sticker filename (e.g., {@code "STK-20260102-WA0003.webp"});
     *         never {@code null}
     */
    public String getStickerName() {
        return stickerName;
    }

    /**
     * Returns the uncompressed size of the sticker file.
     *
     * @return file size in bytes
     */
    public int getStickerSize() {
        return stickerSize;
    }

    /**
     * Returns the lowercase file extension identifying the sticker format.
     *
     * @return format extension — always {@code "webp"} for WhatsApp stickers;
     *         never {@code null}
     */
    public String getStickerType() {
        return stickerType;
    }

    /**
     * Returns the display name of the participant who sent this sticker.
     *
     * @return the sender name; never {@code null}
     */
    public String getStickerSender() {
        return sender;
    }

    /**
     * Returns the date and time at which this sticker message was sent.
     *
     * @return the message timestamp; never {@code null}
     */
    public java.util.Date getStickerTimestamp() {
        return timestamp;
    }

    /**
     * Returns the message type classification for this message.
     *
     * @return {@link MessageType#STICKER}
     */
    public MessageType getStickerMessageType() {
        return messageType;
    }
}
