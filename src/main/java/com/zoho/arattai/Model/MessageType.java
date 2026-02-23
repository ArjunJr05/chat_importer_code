package com.zoho.arattai.Model;

/**
 * Enumeration of all content categories supported by the WhatsApp chat parser.
 *
 * <p>Every message parsed from a WhatsApp export is assigned exactly one
 * {@code MessageType} constant.  The parser uses file-extension heuristics
 * and WhatsApp-specific filename conventions to determine the appropriate type:
 *
 * <ul>
 *   <li>{@link #TEXT}     – plain-text conversation messages.</li>
 *   <li>{@link #IMAGE}    – photos (JPG, PNG, GIF, BMP, or bare WebP files).</li>
 *   <li>{@link #VIDEO}    – video clips (MP4, AVI, MOV, MKV, WebM).</li>
 *   <li>{@link #AUDIO}    – voice notes and audio files (MP3, WAV, OGG, M4A, AAC, Opus).</li>
 *   <li>{@link #DOCUMENT} – generic file attachments (PDF, DOCX, XLSX, ZIP, RAR, etc.).</li>
 *   <li>{@link #STICKER}  – animated WebP stickers whose filenames begin with the
 *                           {@code STK-} prefix mandated by WhatsApp.</li>
 * </ul>
 *
 * <p><b>Sticker vs. Image disambiguation:</b>  WhatsApp names every sticker file
 * {@code STK-&lt;date&gt;-WA&lt;seq&gt;.webp}.  The parser therefore only classifies a
 * {@code .webp} file as a {@code STICKER} if its name contains the literal substring
 * {@code stk}; all other {@code .webp} files are treated as plain {@code IMAGE}s.
 *
 * @author  Zoho Arattai
 * @version 1.0
 */
public enum MessageType {

    /**
     * Plain-text message.
     * Includes regular conversation text, deleted-message placeholders,
     * and edited-message records.
     */
    TEXT,

    /**
     * Photo or image attachment.
     * Supported formats: JPG, JPEG, PNG, GIF, BMP, and WebP files
     * whose names do <em>not</em> carry the {@code STK-} sticker prefix.
     */
    IMAGE,

    /**
     * Video clip attachment.
     * Supported formats: MP4, AVI, MOV, MKV, WebM.
     * Duration is extracted via a pure-Java {@code mvhd} box parser.
     */
    VIDEO,

    /**
     * Voice note or audio file attachment.
     * Supported formats: Opus, OGG, MP3, WAV, M4A, AAC.
     * Duration is extracted with format-specific pure-Java parsers
     * (Ogg granule position for Opus/OGG; {@code mvhd} box for M4A/AAC;
     * mp3agic for MP3).
     */
    AUDIO,

    /**
     * Generic file attachment.
     * Supported formats: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, ZIP, RAR, TXT.
     */
    DOCUMENT,

    /**
     * Animated WebP sticker.
     * Identified by the {@code STK-} prefix in the WhatsApp filename convention
     * (e.g., {@code STK-20260102-WA0003.webp}).
     */
    STICKER
}
