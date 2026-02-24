package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.Type;
import com.zoho.arattai.core.Message;

/**
 * Represents a generic file attachment message parsed from a WhatsApp chat
 * export.
 *
 * <p>
 * A {@code DocumentMessage} is produced for any message whose attachment
 * filename carries one of the following extensions: {@code .pdf}, {@code .doc},
 * {@code .docx}, {@code .xls}, {@code .xlsx}, {@code .ppt}, {@code .pptx},
 * {@code .zip}, {@code .rar}, or {@code .txt}.
 *
 * <p>
 * Unlike media messages, no content is decoded from the file itself; only
 * the metadata available in the ZIP directory entry (filename and uncompressed
 * size) is captured.
 *
 * <p>
 * All fields are immutable ({@code public final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see MessageType#DOCUMENT
 */
public class DocumentMessage extends Message {

    /** The filename of the document as stored inside the export ZIP. */
    private final String name;

    /**
     * The lowercase file extension identifying the document format
     * (e.g., {@code "pdf"}, {@code "docx"}, {@code "zip"}).
     */
    private final String extension;

    /** The uncompressed file size of the document in bytes. */
    private final int size;

    /**
     * Constructs a new {@code DocumentMessage} with all metadata provided by the
     * parser.
     *
     * @param name        the document filename inside the ZIP; must not be
     *                    {@code null}
     * @param type        the lowercase file extension; must not be {@code null}
     * @param size        the file size in bytes
     * @param sender      the display name of the sender; must not be {@code null}
     * @param timestamp   the date and time the message was sent; must not be
     *                    {@code null}
     * @param messageType the type classification; expected to be
     *                    {@link MessageType#DOCUMENT}
     */
    public DocumentMessage(String name, String extension, int size,
            String sender, java.util.Date timestamp, Type messageType) {
        super(sender, timestamp, messageType);
        this.name = name;
        this.extension = extension;
        this.size = size;
    }

    /**
     * Returns the filename of the document file inside the export ZIP.
     *
     * @return the document filename; never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the lowercase file extension identifying the document format.
     *
     * @return format extension (e.g., {@code "pdf"}, {@code "zip"}); never
     *         {@code null}
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the uncompressed size of the document file.
     *
     * @return file size in bytes
     */
    public int getSize() {
        return size;
    }
}
