package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.Type;
import com.zoho.arattai.core.Message;

/**
 * Represents an animated WebP sticker message parsed from a WhatsApp chat
 * export.
 *
 * <p>
 * WhatsApp names every sticker file using the convention
 * {@code STK-<date>-WA<seq>.webp} (e.g., {@code STK-20260102-WA0003.webp}).
 * The parser uses this naming convention — specifically the presence of the
 * substring {@code "stk"} combined with the {@code ".webp"} extension — to
 * distinguish stickers from regular {@link ImageMessage image} attachments,
 * which
 * can also be {@code .webp} files.
 *
 * <p>
 * Sticker payloads are animated WebP files; no content decoding is performed
 * on them. Only the metadata available in the ZIP directory entry (filename and
 * uncompressed size) is captured.
 *
 * <p>
 * All fields are immutable ({@code public final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see Type#STICKER
 * @see ImageMessage
 */
public class StickerMessage extends Message {

    /**
     * The filename of the sticker file as stored inside the export ZIP
     * (e.g., {@code "STK-20260102-WA0003.webp"}).
     */
    private final String name;

    /** The uncompressed file size of the sticker in bytes. */
    private final int size;

    /**
     * The lowercase file extension of the sticker.
     * Always {@code "webp"} for WhatsApp stickers.
     */
    private final String extension;

    /**
     * Constructs a new {@code StickerMessage} with all metadata provided by the
     * parser.
     *
     * @param name      the sticker filename inside the ZIP; must not be
     *                  {@code null}
     * @param size      the file size in bytes
     * @param extension the lowercase file extension (typically {@code "webp"});
     *                  must not be {@code null}
     * @param sender    the display name of the sender; must not be {@code null}
     * @param timestamp the date and time the message was sent; must not be
     *                  {@code null}
     * @param type      the type classification; expected to be
     *                  {@link Type#STICKER}
     */
    public StickerMessage(String name, int size, String extension,
            String sender, java.util.Date timestamp, Type type) {
        super(sender, timestamp, type);
        this.name = name;
        this.size = size;
        this.extension = extension;
    }

    /**
     * Returns the filename of the sticker file inside the export ZIP.
     *
     * @return the sticker filename (e.g., {@code "STK-20260102-WA0003.webp"});
     *         never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the uncompressed size of the sticker file.
     *
     * @return file size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the lowercase file extension identifying the sticker format.
     *
     * @return format extension — always {@code "webp"} for WhatsApp stickers;
     *         never {@code null}
     */
    public String getExtension() {
        return extension;
    }
}
