package com.zoho.arattai.Message;

import com.zoho.arattai.Model.MessageType;
import com.zoho.arattai.core.Message;

/**
 * Represents a generic file attachment message parsed from a WhatsApp chat export.
 *
 * <p>A {@code DocumentMessage} is produced for any message whose attachment
 * filename carries one of the following extensions: {@code .pdf}, {@code .doc},
 * {@code .docx}, {@code .xls}, {@code .xlsx}, {@code .ppt}, {@code .pptx},
 * {@code .zip}, {@code .rar}, or {@code .txt}.
 *
 * <p>Unlike media messages, no content is decoded from the file itself; only
 * the metadata available in the ZIP directory entry (filename and uncompressed
 * size) is captured.
 *
 * <p>All fields are immutable ({@code private final}) and initialised at
 * construction time.
 *
 * @author  Zoho Arattai
 * @version 1.0
 * @see     Message
 * @see     MessageType#DOCUMENT
 */
public class DocumentMessage extends Message {

    /** The filename of the document as stored inside the export ZIP. */
    private final String documentName;

    /**
     * The lowercase file extension identifying the document format
     * (e.g., {@code "pdf"}, {@code "docx"}, {@code "zip"}).
     */
    private final String documentType;

    /** The uncompressed file size of the document in bytes. */
    private final int documentSize;

    /**
     * Constructs a new {@code DocumentMessage} with all metadata provided by the parser.
     *
     * @param documentName the document filename inside the ZIP; must not be {@code null}
     * @param documentType the lowercase file extension; must not be {@code null}
     * @param documentSize the file size in bytes
     * @param sender       the display name of the sender; must not be {@code null}
     * @param timestamp    the date and time the message was sent; must not be {@code null}
     * @param messageType  the type classification; expected to be {@link MessageType#DOCUMENT}
     */
    public DocumentMessage(String documentName, String documentType, int documentSize,
            String sender, java.util.Date timestamp, MessageType messageType) {
        super(sender, timestamp, messageType);
        this.documentName = documentName;
        this.documentType = documentType;
        this.documentSize = documentSize;
    }

    /**
     * Returns the filename of the document file inside the export ZIP.
     *
     * @return the document filename; never {@code null}
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * Returns the lowercase file extension identifying the document format.
     *
     * @return format extension (e.g., {@code "pdf"}, {@code "zip"}); never {@code null}
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Returns the uncompressed size of the document file.
     *
     * @return file size in bytes
     */
    public int getDocumentSize() {
        return documentSize;
    }

    /**
     * Returns the display name of the participant who sent this document.
     *
     * @return the sender name; never {@code null}
     */
    public String getDocumentSender() {
        return sender;
    }

    /**
     * Returns the date and time at which this document message was sent.
     *
     * @return the message timestamp; never {@code null}
     */
    public java.util.Date getDocumentTimestamp() {
        return timestamp;
    }

    /**
     * Returns the message type classification for this message.
     *
     * @return {@link MessageType#DOCUMENT}
     */
    public MessageType getDocumentMessageType() {
        return messageType;
    }
}
