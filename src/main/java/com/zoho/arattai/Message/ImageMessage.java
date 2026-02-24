package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.Type;
import com.zoho.arattai.core.Message;

/**
 * Represents a photo or image attachment parsed from a WhatsApp chat export.
 *
 * <p>
 * An {@code ImageMessage} is produced for any message whose attachment filename
 * carries one of the following extensions: {@code .jpg}, {@code .jpeg},
 * {@code .png}, {@code .gif}, {@code .bmp}, or {@code .webp} (when the filename
 * does <em>not</em> begin with the {@code STK-} sticker prefix — see
 * {@link StickerMessage} for the sticker-specific sub-type).
 *
 * <p>
 * Pixel dimensions ({@link #getWidth()} / {@link #getHeight()}) are
 * extracted at parse time by decoding the image via
 * {@code javax.imageio.ImageIO}
 * directly from the ZIP entry stream. If decoding fails for any reason
 * (unsupported
 * format, corrupted data, etc.) the dimensions default to {@code 0}.
 *
 * <p>
 * All fields are immutable ({@code public final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see Type#IMAGE
 * @see StickerMessage
 */
public class ImageMessage extends Message {

    /** The filename of the image as stored inside the export ZIP. */
    private final String name;

    /** The vertical dimension of the image in pixels, or {@code 0} if unknown. */
    private final int height;

    /** The horizontal dimension of the image in pixels, or {@code 0} if unknown. */
    private final int width;

    /** The uncompressed file size of the image in bytes. */
    private final int size;

    /**
     * The lowercase file extension that identifies the image format
     * (e.g., {@code "jpg"}, {@code "png"}, {@code "webp"}).
     */
    private final String extension;

    /**
     * Constructs a new {@code ImageMessage} with all metadata provided by the
     * parser.
     *
     * @param name      the image filename inside the ZIP; must not be
     *                  {@code null}
     * @param height    the image height in pixels ({@code 0} if undetectable)
     * @param width     the image width in pixels ({@code 0} if undetectable)
     * @param size      the file size in bytes
     * @param extension the lowercase file extension; must not be {@code null}
     * @param sender    the display name of the sender; must not be {@code null}
     * @param timestamp the date and time the message was sent; must not be
     *                  {@code null}
     * @param type      the type classification; expected to be
     *                  {@link Type#IMAGE}
     */
    public ImageMessage(String name, int height, int width, int size,
            String extension, String sender, java.util.Date timestamp, Type type) {
        super(sender, timestamp, type);
        this.name = name;
        this.height = height;
        this.width = width;
        this.size = size;
        this.extension = extension;
    }

    /**
     * Returns the filename of the image file inside the export ZIP.
     *
     * @return the image filename; never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the height of the image in pixels.
     *
     * @return image height, or {@code 0} if the dimensions could not be determined
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width of the image in pixels.
     *
     * @return image width, or {@code 0} if the dimensions could not be determined
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the uncompressed size of the image file.
     *
     * @return file size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the lowercase file extension that identifies the image format.
     *
     * @return image format extension (e.g., {@code "jpg"}, {@code "png"}); never
     *         {@code null}
     */
    public String getExtension() {
        return extension;
    }
}
