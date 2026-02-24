package com.zoho.arattai.Message;

import com.zoho.arattai.core.Message.Type;
import com.zoho.arattai.core.Message;

/**
 * Represents a voice note or audio file attachment parsed from a WhatsApp chat
 * export.
 *
 * <p>
 * An {@code AudioMessage} is produced for any message whose attachment filename
 * carries one of the following extensions: {@code .opus}, {@code .ogg},
 * {@code .mp3}, {@code .wav}, {@code .m4a}, or {@code .aac}. In addition,
 * a bare {@code "<Media omitted>"} line is also treated as an audio message
 * (matching WhatsApp's convention for voice notes that were not included in the
 * export).
 *
 * <p>
 * <b>Duration extraction strategy (format-dependent):</b>
 * <ul>
 * <li><b>Opus / OGG</b> – The last OggS page header is located and its granule
 * position is divided by the fixed Opus sample rate of 48 000 Hz.
 * No external tools are required.</li>
 * <li><b>MP3</b> – Duration is read via the mp3agic library by examining the
 * ID3 frame data and Xing / VBRI headers.</li>
 * <li><b>M4A / AAC</b> – The {@code mvhd} (Movie Header) ISOBMFF box is located
 * and the {@code duration / timescale} ratio is computed directly in Java.</li>
 * <li>All other formats default to {@code "0:00"}.</li>
 * </ul>
 *
 * <p>
 * All fields are immutable ({@code public final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see Type#AUDIO
 */
public class AudioMessage extends Message {

    /** The filename of the audio file as stored inside the export ZIP. */
    private final String name;

    /** The uncompressed file size of the audio in bytes. */
    private final int size;

    /**
     * The playback duration of the audio clip, formatted as {@code "m:ss"}
     * (e.g., {@code "0:05"}). Defaults to {@code "0:00"} when extraction fails.
     */
    private final String duration;

    /**
     * The lowercase file extension identifying the audio format
     * (e.g., {@code "opus"}, {@code "mp3"}).
     */
    private final String extension;

    /**
     * Constructs a new {@code AudioMessage} with all metadata provided by the
     * parser.
     *
     * @param name      the audio filename inside the ZIP; must not be
     *                  {@code null}
     * @param size      the file size in bytes
     * @param duration  the playback duration as {@code "m:ss"}; must not be
     *                  {@code null}
     * @param extension the lowercase file extension; must not be {@code null}
     * @param sender    the display name of the sender; must not be {@code null}
     * @param timestamp the date and time the message was sent; must not be
     *                  {@code null}
     * @param type      the type classification; expected to be
     *                  {@link Type#AUDIO}
     */
    public AudioMessage(String name, int size, String duration, String extension,
            String sender, java.util.Date timestamp, Type type) {
        super(sender, timestamp, type);
        this.name = name;
        this.size = size;
        this.duration = duration;
        this.extension = extension;
    }

    /**
     * Returns the filename of the audio file inside the export ZIP.
     *
     * @return the audio filename; never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the uncompressed size of the audio file.
     *
     * @return file size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the playback duration of the audio clip.
     *
     * @return duration string in {@code "m:ss"} format (e.g., {@code "0:05"}),
     *         or {@code "0:00"} if extraction failed; never {@code null}
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Returns the lowercase file extension identifying the audio format.
     *
     * @return format extension (e.g., {@code "opus"}, {@code "mp3"}); never
     *         {@code null}
     */
    public String getExtension() {
        return extension;
    }
}
