package com.zoho.arattai.Message;

import com.zoho.arattai.Model.MessageType;
import com.zoho.arattai.core.Message;

/**
 * Represents a voice note or audio file attachment parsed from a WhatsApp chat export.
 *
 * <p>An {@code AudioMessage} is produced for any message whose attachment filename
 * carries one of the following extensions: {@code .opus}, {@code .ogg},
 * {@code .mp3}, {@code .wav}, {@code .m4a}, or {@code .aac}.  In addition,
 * a bare {@code "<Media omitted>"} line is also treated as an audio message
 * (matching WhatsApp's convention for voice notes that were not included in the export).
 *
 * <p><b>Duration extraction strategy (format-dependent):</b>
 * <ul>
 *   <li><b>Opus / OGG</b> – The last OggS page header is located and its granule
 *       position is divided by the fixed Opus sample rate of 48 000 Hz.
 *       No external tools are required.</li>
 *   <li><b>MP3</b> – Duration is read via the mp3agic library by examining the
 *       ID3 frame data and Xing / VBRI headers.</li>
 *   <li><b>M4A / AAC</b> – The {@code mvhd} (Movie Header) ISOBMFF box is located
 *       and the {@code duration / timescale} ratio is computed directly in Java.</li>
 *   <li>All other formats default to {@code "0:00"}.</li>
 * </ul>
 *
 * <p>All fields are immutable ({@code private final}) and initialised at
 * construction time.
 *
 * @author  Zoho Arattai
 * @version 1.0
 * @see     Message
 * @see     MessageType#AUDIO
 */
public class AudioMessage extends Message {

    /** The filename of the audio file as stored inside the export ZIP. */
    private final String audioName;

    /** The uncompressed file size of the audio in bytes. */
    private final int audioSize;

    /**
     * The playback duration of the audio clip, formatted as {@code "m:ss"}
     * (e.g., {@code "0:05"}).  Defaults to {@code "0:00"} when extraction fails.
     */
    private final String audioDuration;

    /**
     * The lowercase file extension identifying the audio format
     * (e.g., {@code "opus"}, {@code "mp3"}).
     */
    private final String audioType;

    /**
     * Constructs a new {@code AudioMessage} with all metadata provided by the parser.
     *
     * @param audioName     the audio filename inside the ZIP; must not be {@code null}
     * @param audioSize     the file size in bytes
     * @param audioDuration the playback duration as {@code "m:ss"}; must not be {@code null}
     * @param audioType     the lowercase file extension; must not be {@code null}
     * @param sender        the display name of the sender; must not be {@code null}
     * @param timestamp     the date and time the message was sent; must not be {@code null}
     * @param messageType   the type classification; expected to be {@link MessageType#AUDIO}
     */
    public AudioMessage(String audioName, int audioSize, String audioDuration, String audioType,
            String sender, java.util.Date timestamp, MessageType messageType) {
        super(sender, timestamp, messageType);
        this.audioName = audioName;
        this.audioSize = audioSize;
        this.audioDuration = audioDuration;
        this.audioType = audioType;
    }

    /**
     * Returns the filename of the audio file inside the export ZIP.
     *
     * @return the audio filename; never {@code null}
     */
    public String getAudioName() {
        return audioName;
    }

    /**
     * Returns the uncompressed size of the audio file.
     *
     * @return file size in bytes
     */
    public int getAudioSize() {
        return audioSize;
    }

    /**
     * Returns the playback duration of the audio clip.
     *
     * @return duration string in {@code "m:ss"} format (e.g., {@code "0:05"}),
     *         or {@code "0:00"} if extraction failed; never {@code null}
     */
    public String getAudioDuration() {
        return audioDuration;
    }

    /**
     * Returns the lowercase file extension identifying the audio format.
     *
     * @return format extension (e.g., {@code "opus"}, {@code "mp3"}); never {@code null}
     */
    public String getAudioType() {
        return audioType;
    }

    /**
     * Returns the display name of the participant who sent this audio message.
     *
     * @return the sender name; never {@code null}
     */
    public String getAudioSender() {
        return sender;
    }

    /**
     * Returns the date and time at which this audio message was sent.
     *
     * @return the message timestamp; never {@code null}
     */
    public java.util.Date getAudioTimestamp() {
        return timestamp;
    }

    /**
     * Returns the message type classification for this message.
     *
     * @return {@link MessageType#AUDIO}
     */
    public MessageType getAudioMessageType() {
        return messageType;
    }
}
