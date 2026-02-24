package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.MessageType;
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
 * Pixel dimensions ({@link #getImageWidth()} / {@link #getImageHeight()}) are
 * extracted at parse time by decoding the image via
 * {@code javax.imageio.ImageIO}
 * directly from the ZIP entry stream. If decoding fails for any reason
 * (unsupported
 * format, corrupted data, etc.) the dimensions default to {@code 0}.
 *
 * <p>
 * All fields are immutable ({@code private final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see MessageType#IMAGE
 * @see StickerMessage
 */
public class ImageMessage extends Message {

    /** The filename of the image as stored inside the export ZIP. */
    private final String imageName;

    /** The vertical dimension of the image in pixels, or {@code 0} if unknown. */
    private final int imageHeight;

    /** The horizontal dimension of the image in pixels, or {@code 0} if unknown. */
    private final int imageWidth;

    /** The uncompressed file size of the image in bytes. */
    private final int imageSize;

    /**
     * The lowercase file extension that identifies the image format
     * (e.g., {@code "jpg"}, {@code "png"}, {@code "webp"}).
     */
    private final String imageType;

    /**
     * Constructs a new {@code ImageMessage} with all metadata provided by the
     * parser.
     *
     * @param imageName   the image filename inside the ZIP; must not be
     *                    {@code null}
     * @param imageHeight the image height in pixels ({@code 0} if undetectable)
     * @param imageWidth  the image width in pixels ({@code 0} if undetectable)
     * @param imageSize   the file size in bytes
     * @param imageType   the lowercase file extension; must not be {@code null}
     * @param sender      the display name of the sender; must not be {@code null}
     * @param timestamp   the date and time the message was sent; must not be
     *                    {@code null}
     * @param messageType the type classification; expected to be
     *                    {@link MessageType#IMAGE}
     */
    public ImageMessage(String imageName, int imageHeight, int imageWidth, int imageSize,
            String imageType, String sender, java.util.Date timestamp, MessageType messageType) {
        super(sender, timestamp, messageType);
        this.imageName = imageName;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.imageSize = imageSize;
        this.imageType = imageType;
    }

    /**
     * Returns the filename of the image file inside the export ZIP.
     *
     * @return the image filename; never {@code null}
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Returns the height of the image in pixels.
     *
     * @return image height, or {@code 0} if the dimensions could not be determined
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Returns the width of the image in pixels.
     *
     * @return image width, or {@code 0} if the dimensions could not be determined
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Returns the uncompressed size of the image file.
     *
     * @return file size in bytes
     */
    public int getImageSize() {
        return imageSize;
    }

    /**
     * Returns the lowercase file extension that identifies the image format.
     *
     * @return image format extension (e.g., {@code "jpg"}, {@code "png"}); never
     *         {@code null}
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * Returns the display name of the participant who sent this image.
     *
     * @return the sender name; never {@code null}
     */
    public String getImageSender() {
        return sender;
    }

    /**
     * Returns the date and time at which this image message was sent.
     *
     * @return the message timestamp; never {@code null}
     */
    public java.util.Date getImageTimestamp() {
        return timestamp;
    }

    /**
     * Returns the message type classification for this message.
     *
     * @return {@link MessageType#IMAGE}
     */
    public MessageType getImageMessageType() {
        return messageType;
    }
}
