package com.zoho.arattai.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.zoho.arattai.Message.*;
import com.zoho.arattai.core.Message.MessageType;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.mpatric.mp3agic.Mp3File;

/**
 * Stateless parser for WhatsApp chat export ZIP files.
 */
public class WhatsAppChatParser {

    /**
     * The regex pattern used to identify and parse individual WhatsApp message
     * lines.
     */
    public static final Pattern MESSAGE_PATTERN = Pattern.compile(
            "^(\\d{1,2}/\\d{1,2}/\\d{4},[\\s\\u202f\\u00a0]+\\d{1,2}:\\d{2}(?::\\d{2})?[\\s\\u202f\\u00a0]*[ap]m)\\s*-\\s*([^:]+):\\s(.*)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * The date pattern used for parsing timestamps from the chat transcript.
     */
    public static final String DATE_PATTERN = "dd/MM/yyyy, h:mm a";

    /**
     * Parses a WhatsApp chat export ZIP file.
     *
     * @param zipFilePath path to the ZIP file
     * @return the parsed export
     * @throws IOException if an error occurs during parsing
     */
    public static WhatsAppExport parse(String zipFilePath) throws IOException {
        String chatName = extractChatName(zipFilePath);
        List<Message> messages = new ArrayList<>();
        Map<String, MediaEntry> mediaFiles = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && !entry.getName().endsWith(".txt"))
                    mediaFiles.put(entry.getName(), new MediaEntry(entry.getName(), entry.getSize()));
            }
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".txt") && !entry.isDirectory()) {
                    System.out.println("Found chat file: " + entry.getName());
                    try (InputStream is = zipFile.getInputStream(entry);
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        messages = parseTranscript(reader, mediaFiles, zipFile);
                    }
                    break;
                }
            }
        }
        return new WhatsAppExport(chatName, messages);
    }

    /**
     * Parses the full transcript using the provided BufferedReader.
     *
     * @param reader     the BufferedReader for the transcript text file
     * @param mediaFiles a map of indexed media filenames and entries
     * @param zipFile    the ZipFile containing the transcript and media
     * @return a list of parsed Message objects
     * @throws IOException if an error occurs during reading
     */
    public static List<Message> parseTranscript(BufferedReader reader,
            Map<String, MediaEntry> mediaFiles, ZipFile zipFile) throws IOException {
        List<Message> messages = new ArrayList<>();
        String pending = null, line;
        while ((line = reader.readLine()) != null) {
            if (MESSAGE_PATTERN.matcher(line).matches()) {
                if (pending != null) {
                    Message msg = buildMessage(pending, mediaFiles, zipFile);
                    if (msg != null)
                        messages.add(msg);
                }
                pending = line;
            } else if (pending != null) {
                pending += "\n" + line;
            }
        }
        if (pending != null) {
            Message msg = buildMessage(pending, mediaFiles, zipFile);
            if (msg != null)
                messages.add(msg);
        }
        return messages;
    }

    /**
     * Builds a single Message object from a raw line from the transcript.
     *
     * @param rawLine    the raw string representing one message
     * @param mediaFiles a map of indexed media filenames and entries
     * @param zipFile    the ZipFile context for media extraction
     * @return a Message object (or subclass instance) or null if unparseable
     */
    public static Message buildMessage(String rawLine, Map<String, MediaEntry> mediaFiles, ZipFile zipFile) {
        Matcher m = MESSAGE_PATTERN.matcher(rawLine.split("\n")[0]);
        if (!m.matches())
            return null;
        String sender = m.group(2).trim(), content = m.group(3);
        Date timestamp = parseTimestamp(m.group(1));
        MessageType type = classifyMessage(content);
        switch (type) {
            case TEXT:
                return new TextMessage(content, sender, timestamp, MessageType.TEXT);
            case IMAGE: {
                MediaEntry info = findMedia(content, mediaFiles, "image");
                String name = info != null ? info.getName() : "image.jpg";
                int size = info != null ? (int) info.getSize() : 0, w = 0, h = 0;
                if (info != null) {
                    try {
                        ZipEntry ze = zipFile.getEntry(info.getName());
                        if (ze != null) {
                            BufferedImage img = ImageIO.read(zipFile.getInputStream(ze));
                            if (img != null) {
                                w = img.getWidth();
                                h = img.getHeight();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                return new ImageMessage(name, h, w, size, extension(name), sender, timestamp, MessageType.IMAGE);
            }
            case VIDEO: {
                MediaEntry info = findMedia(content, mediaFiles, "video");
                String name = info != null ? info.getName() : "video.mp4";
                int size = info != null ? (int) info.getSize() : 0, vw = 0, vh = 0;
                String dur = "0:00";
                if (info != null) {
                    try {
                        ZipEntry ze = zipFile.getEntry(info.getName());
                        if (ze != null) {
                            File tmp = extractToTemp(zipFile, ze, "video", extension(name));
                            dur = parseMp4Duration(tmp);
                            int[] dims = parseMp4Dimensions(tmp);
                            vw = dims[0];
                            vh = dims[1];
                            tmp.delete();
                        }
                    } catch (Exception ignored) {
                    }
                }
                return new VideoMessage(name, size, dur, extension(name), vw, vh, sender, timestamp, MessageType.VIDEO);
            }
            case AUDIO: {
                MediaEntry info = findMedia(content, mediaFiles, "audio");
                String name = info != null ? info.getName() : "audio.opus";
                int size = info != null ? (int) info.getSize() : 0;
                String dur = "0:00";
                if (info != null) {
                    try {
                        ZipEntry ze = zipFile.getEntry(info.getName());
                        if (ze != null) {
                            String ext = extension(name);
                            File tmp = extractToTemp(zipFile, ze, "audio", ext);
                            if (ext.equals("opus") || ext.equals("ogg")) {
                                dur = parseOpusDuration(tmp);
                            } else if (ext.equals("mp3")) {
                                try {
                                    Mp3File mp3 = new Mp3File(tmp);
                                    long s = mp3.getLengthInSeconds();
                                    dur = String.format("%d:%02d", s / 60, s % 60);
                                } catch (Exception i2) {
                                }
                            } else if (ext.equals("m4a") || ext.equals("aac")) {
                                dur = parseMp4Duration(tmp);
                            }
                            tmp.delete();
                        }
                    } catch (Exception ignored) {
                    }
                }
                return new AudioMessage(name, size, dur, extension(name), sender, timestamp, MessageType.AUDIO);
            }
            case DOCUMENT: {
                MediaEntry info = findMedia(content, mediaFiles, "document");
                String name = info != null ? info.getName() : "document.pdf";
                int size = info != null ? (int) info.getSize() : 0;
                return new DocumentMessage(name, extension(name), size, sender, timestamp, MessageType.DOCUMENT);
            }
            case STICKER: {
                MediaEntry info = findMedia(content, mediaFiles, "sticker");
                String name = info != null ? info.getName() : "sticker.webp";
                int size = info != null ? (int) info.getSize() : 0;
                return new StickerMessage(name, size, extension(name), sender, timestamp, MessageType.STICKER);
            }
            default:
                return null;
        }
    }

    /**
     * Classifies a message based on its string content.
     *
     * @param content the raw message content text
     * @return the determined MessageType
     */
    public static MessageType classifyMessage(String content) {
        String lc = content.toLowerCase().trim();
        if (lc.equals("<media omitted>"))
            return MessageType.AUDIO;
        if (lc.contains("you deleted this message") || lc.contains("this message was deleted"))
            return MessageType.TEXT;
        if (lc.contains("(file attached)")) {
            if (lc.contains("stk") && lc.contains(".webp"))
                return MessageType.STICKER;
            if (lc.contains(".jpg") || lc.contains(".jpeg") || lc.contains(".png") || lc.contains(".gif")
                    || lc.contains(".bmp") || lc.contains(".webp"))
                return MessageType.IMAGE;
            if (lc.contains(".mp4") || lc.contains(".avi") || lc.contains(".mov") || lc.contains(".mkv")
                    || lc.contains(".webm"))
                return MessageType.VIDEO;
            if (lc.contains(".mp3") || lc.contains(".wav") || lc.contains(".ogg") || lc.contains(".m4a")
                    || lc.contains(".aac") || lc.contains(".opus"))
                return MessageType.AUDIO;
            if (lc.contains(".pdf") || lc.contains(".doc") || lc.contains(".docx") || lc.contains(".xls")
                    || lc.contains(".xlsx") || lc.contains(".ppt") || lc.contains(".pptx") || lc.contains(".zip")
                    || lc.contains(".rar") || lc.contains(".txt"))
                return MessageType.DOCUMENT;
        }
        return MessageType.TEXT;
    }

    /**
     * Resolves a media attachment's filename from the content string.
     *
     * @param content    the raw message content
     * @param mediaFiles the index of files present in the ZIP
     * @param type       the expected category (image/video/audio, etc.)
     * @return the matching MediaEntry
     */
    public static MediaEntry findMedia(String content, Map<String, MediaEntry> mediaFiles, String type) {
        String lc = content.toLowerCase();
        for (String name : mediaFiles.keySet()) {
            String ln = name.toLowerCase();
            boolean tm;
            switch (type) {
                case "sticker":
                    tm = ln.endsWith(".webp");
                    break;
                case "image":
                    tm = ln.endsWith(".jpg") || ln.endsWith(".jpeg") || ln.endsWith(".png") || ln.endsWith(".gif")
                            || ln.endsWith(".bmp") || ln.endsWith(".webp");
                    break;
                case "video":
                    tm = ln.endsWith(".mp4") || ln.endsWith(".avi") || ln.endsWith(".mov") || ln.endsWith(".mkv")
                            || ln.endsWith(".webm");
                    break;
                case "audio":
                    tm = ln.endsWith(".mp3") || ln.endsWith(".wav") || ln.endsWith(".ogg") || ln.endsWith(".m4a")
                            || ln.endsWith(".aac") || ln.endsWith(".opus");
                    break;
                case "document":
                    tm = ln.endsWith(".pdf") || ln.endsWith(".doc") || ln.endsWith(".docx") || ln.endsWith(".xls")
                            || ln.endsWith(".xlsx") || ln.endsWith(".ppt") || ln.endsWith(".pptx")
                            || ln.endsWith(".zip") || ln.endsWith(".rar") || ln.endsWith(".txt");
                    break;
                default:
                    tm = false;
            }
            if (tm && (lc.contains(name) || lc.contains(ln)))
                return mediaFiles.get(name);
        }
        return null;
    }

    /**
     * Extracts and calculates the duration of an Opus audio file.
     *
     * @param file the temp file containing the Opus data
     * @return duration string in m:ss format
     */
    public static String parseOpusDuration(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long len = raf.length(), start = Math.max(0, len - 65536);
            raf.seek(start);
            byte[] b = new byte[(int) (len - start)];
            raf.readFully(b);
            for (int i = b.length - 4; i >= 0; i--) {
                if (b[i] == 0x4F && b[i + 1] == 0x67 && b[i + 2] == 0x67 && b[i + 3] == 0x53 && i + 13 < b.length) {
                    long g = 0;
                    for (int k = 7; k >= 0; k--)
                        g = (g << 8) | (b[i + 6 + k] & 0xFF);
                    if (g > 0) {
                        long s = g / 48000;
                        return String.format("%d:%02d", s / 60, s % 60);
                    }
                }
            }
        } catch (Exception e) {
        }
        return "0:00";
    }

    /**
     * Extracts the duration of an MP4/M4A video or audio file.
     *
     * @param file the temp file containing the MP4 data
     * @return duration string in m:ss format
     */
    public static String parseMp4Duration(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] b = new byte[(int) Math.min(raf.length(), 1024 * 1024)];
            raf.readFully(b);
            for (int i = 0; i < b.length - 20; i++) {
                if (b[i] == 'm' && b[i + 1] == 'v' && b[i + 2] == 'h' && b[i + 3] == 'd') {
                    int v = b[i + 4] & 0xFF;
                    long ts, dur;
                    if (v == 1 && i + 36 < b.length) {
                        ts = i32(b, i + 24);
                        dur = i64(b, i + 28);
                    } else if (i + 24 < b.length) {
                        ts = i32(b, i + 16);
                        dur = i32(b, i + 20);
                    } else
                        continue;
                    if (ts > 0) {
                        long s = dur / ts;
                        return String.format("%d:%02d", s / 60, s % 60);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "0:00";
    }

    /**
     * Extracts the pixel dimensions of an MP4 video file.
     *
     * @param file the temp file containing the MP4 data
     * @return an int array where [0]=width and [1]=height
     */
    public static int[] parseMp4Dimensions(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] b = new byte[(int) Math.min(raf.length(), 2 * 1024 * 1024)];
            raf.readFully(b);
            for (int i = 0; i < b.length - 20; i++) {
                if (b[i] == 't' && b[i + 1] == 'k' && b[i + 2] == 'h' && b[i + 3] == 'd') {
                    int ver = b[i + 4] & 0xFF;
                    int wo = (ver == 1) ? i + 92 : i + 80, ho = (ver == 1) ? i + 96 : i + 84;
                    if (ho + 4 <= b.length) {
                        int w = (int) (i32(b, wo) >> 16), h = (int) (i32(b, ho) >> 16);
                        if (w > 0 && h > 0)
                            return new int[] { w, h };
                    }
                }
            }
        } catch (Exception e) {
        }
        return new int[] { 0, 0 };
    }

    /**
     * Extracts a file from the ZIP archive to a temporary file on disk.
     *
     * @param zf     the source ZipFile
     * @param ze     the ZipEntry to extract
     * @param prefix temporary file prefix
     * @param ext    temporary file extension
     * @return the temporary File object
     * @throws IOException if extraction fails
     */
    public static File extractToTemp(ZipFile zf, ZipEntry ze, String prefix, String ext) throws IOException {
        File tmp = File.createTempFile(prefix, "." + ext);
        try (InputStream is = zf.getInputStream(ze)) {
            java.nio.file.Files.copy(is, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tmp;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param f the filename string
     * @return the extension (e.g., "jpg") or "unknown"
     */
    public static String extension(String f) {
        int d = f.lastIndexOf('.');
        return (d > 0 && d < f.length() - 1) ? f.substring(d + 1).toLowerCase() : "unknown";
    }

    /**
     * Sanitizes and extracts the chat name from the ZIP filename.
     *
     * @param p path to the ZIP file
     * @return clean chat name
     */
    public static String extractChatName(String p) {
        return new File(p).getName().replaceAll("\\.zip$", "").replaceAll("^WhatsApp Chat with ", "");
    }

    /**
     * Parses a timestamp string into a Date object.
     *
     * @param raw the raw timestamp string from the transcript
     * @return the parsed Date, or current date if parsing fails
     */
    public static Date parseTimestamp(String raw) {
        String clean = raw.replaceAll("[\\s\\u202f\\u00a0]+", " ").trim();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
            return sdf.parse(clean);
        } catch (ParseException ignored) {
        }
        return new Date();
    }

    /**
     * Reads a 32-bit big-endian integer from a byte array.
     *
     * @param b the byte array
     * @param o the offset to start reading from
     * @return the parsed long value
     */
    public static long i32(byte[] b, int o) {
        return ((b[o] & 0xFFL) << 24) | ((b[o + 1] & 0xFFL) << 16) | ((b[o + 2] & 0xFFL) << 8) | (b[o + 3] & 0xFFL);
    }

    /**
     * Reads a 64-bit big-endian integer from a byte array.
     *
     * @param b the byte array
     * @param o the offset to start reading from
     * @return the parsed long value
     */
    public static long i64(byte[] b, int o) {
        return (i32(b, o) << 32) | (i32(b, o + 4) & 0xFFFFFFFFL);
    }

    /**
     * Represents a media file entry in the ZIP archive.
     */
    public static class MediaEntry {
        /** Filename of the media entry. */
        private final String name;
        /** Byte size of the media entry. */
        private final long size;

        /**
         * @param n filename of the media entry
         * @param s byte size of the media entry
         */
        MediaEntry(String n, long s) {
            name = n;
            size = s;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }
    }
}
