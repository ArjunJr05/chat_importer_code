package com.zoho.arattai.Message;

import com.zoho.arattai.Model.MessageType;
import com.zoho.arattai.core.Message;

/**
 * Represents a video clip attachment parsed from a WhatsApp chat export.
 *
 * <p>
 * A {@code VideoMessage} is produced for any message whose attachment filename
 * carries one of the following extensions: {@code .mp4}, {@code .avi},
 * {@code .mov}, {@code .mkv}, or {@code .webm}.
 *
 * <p>
 * <b>Duration extraction:</b> the playback duration is parsed from the MP4
 * container's {@code mvhd} (Movie Header) box directly in Java — no external
 * tools such as FFmpeg are required. If parsing fails (e.g., the file is
 * corrupt or in an unsupported container), the duration defaults to
 * {@code "0:00"}.
 *
 * <p>
 * <b>Pixel dimensions:</b> width and height are set to {@code 0} in the
 * current implementation. They can be populated in a future revision by
 * reading the {@code tkhd} (Track Header) box from the video track.
 *
 * <p>
 * All fields are immutable ({@code private final}) and initialised at
 * construction time.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see Message
 * @see MessageType#VIDEO
 */
public class VideoMessage extends Message {

    /** The filename of the video file as stored inside the export ZIP. */
    private final String videoName;

    /** The uncompressed file size of the video in bytes. */
    private final int videoSize;

    /**
     * The playback duration of the video, formatted as {@code "m:ss"}
     * (e.g., {@code "2:45"}). Defaults to {@code "0:00"} when extraction fails.
     */
    private final String videoDuration;

    /**
     * The lowercase file extension identifying the video container format
     * (e.g., {@code "mp4"}, {@code "mkv"}).
     */
    private final String videoType;

    /**
     * The horizontal resolution of the video in pixels, or {@code 0} if unknown.
     */
    private final int videoWidth;

    /**
     * The vertical resolution of the video in pixels, or {@code 0} if unknown.
     */
    private final int videoHeight;

    /**
     * Constructs a new {@code VideoMessage} with all metadata provided by the
     * parser.
     *
     * @param videoName     the video filename inside the ZIP; must not be
     *                      {@code null}
     * @param videoSize     the file size in bytes
     * @param videoDuration the playback duration as {@code "m:ss"}; must not be
     *                      {@code null}
     * @param videoType     the lowercase file extension; must not be {@code null}
     * @param videoWidth    the video width in pixels ({@code 0} if undetectable)
     * @param videoHeight   the video height in pixels ({@code 0} if undetectable)
     * @param sender        the display name of the sender; must not be {@code null}
     * @param timestamp     the date and time the message was sent; must not be
     *                      {@code null}
     * @param messageType   the type classification; expected to be
     *                      {@link MessageType#VIDEO}
     */
    public VideoMessage(String videoName, int videoSize, String videoDuration, String videoType,
            int videoWidth, int videoHeight,
            String sender, java.util.Date timestamp, MessageType messageType) {
        super(sender, timestamp, messageType);
        this.videoName = videoName;
        this.videoSize = videoSize;
        this.videoDuration = videoDuration;
        this.videoType = videoType;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    /**
     * Returns the filename of the video file inside the export ZIP.
     *
     * @return the video filename; never {@code null}
     */
    public String getVideoName() {
        return videoName;
    }

    /**
     * Returns the uncompressed size of the video file.
     *
     * @return file size in bytes
     */
    public int getVideoSize() {
        return videoSize;
    }

    /**
     * Returns the playback duration of the video.
     *
     * @return duration string in {@code "m:ss"} format (e.g., {@code "2:45"}),
     *         or {@code "0:00"} if extraction failed; never {@code null}
     */
    public String getVideoDuration() {
        return videoDuration;
    }

    /**
     * Returns the lowercase file extension identifying the container format.
     *
     * @return format extension (e.g., {@code "mp4"}, {@code "mov"}); never
     *         {@code null}
     */
    public String getVideoType() {
        return videoType;
    }

    /**
     * Returns the horizontal resolution of the video in pixels.
     *
     * @return video width, or {@code 0} if the dimensions could not be determined
     */
    public int getVideoWidth() {
        return videoWidth;
    }

    /**
     * Returns the vertical resolution of the video in pixels.
     *
     * @return video height, or {@code 0} if the dimensions could not be determined
     */
    public int getVideoHeight() {
        return videoHeight;
    }

    /**
     * Returns the display name of the participant who sent this video.
     *
     * @return the sender name; never {@code null}
     */
    public String getVideoSender() {
        return sender;
    }

    /**
     * Returns the date and time at which this video message was sent.
     *
     * @return the message timestamp; never {@code null}
     */
    public java.util.Date getVideoTimestamp() {
        return timestamp;
    }

    /**
     * Returns the message type classification for this message.
     *
     * @return {@link MessageType#VIDEO}
     */
    public MessageType getVideoMessageType() {
        return messageType;
    }
}
