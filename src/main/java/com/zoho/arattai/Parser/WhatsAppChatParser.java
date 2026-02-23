package com.zoho.arattai.Parser;

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
import com.zoho.arattai.Model.MessageType;
import com.zoho.arattai.core.Message;
import com.zoho.arattai.core.WhatsAppExport;

/**
 * Stateless parser for WhatsApp chat export ZIP files.
 *
 * <p>
 * Call {@link #parse(String)} with the path to a {@code .zip} file exported
 * from WhatsApp. The parser indexes all media entries in the archive and then
 * reads the chat transcript line by line, producing a
 * {@link com.zoho.arattai.core.WhatsAppExport}
 * containing typed {@link com.zoho.arattai.core.Message} subclass instances.
 *
 * @author Zoho Arattai
 * @version 1.0
 */
public class WhatsAppChatParser {

    private static final Pattern MESSAGE_PATTERN = Pattern.compile(
            "^(\\d{1,2}/\\d{1,2}/\\d{4},\\s+\\d{1,2}:\\d{2}.?[ap]m)\\s*-\\s*([^:]+):\\s(.*)$");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.ENGLISH);

    /**
     * Parses a WhatsApp chat export ZIP file into a structured
     * {@link com.zoho.arattai.core.WhatsAppExport}.
     *
     * @param zipFilePath the absolute path to the export {@code .zip} file
     * @return a {@link com.zoho.arattai.core.WhatsAppExport} with all parsed
     *         messages
     * @throws IOException if the file cannot be read or is not a valid ZIP
     */
    public static WhatsAppExport parse(String zipFilePath) throws IOException {
        String chatName = extractChatName(zipFilePath);
        WhatsAppExport export = new WhatsAppExport(chatName);
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
                        export.setAllMessages(parseTranscript(reader, mediaFiles, zipFilePath));
                    }
                    break;
                }
            }
        }
        return export;
    }

    private static List<Message> parseTranscript(BufferedReader reader,
            Map<String, MediaEntry> mediaFiles, String zipFilePath) throws IOException {
        List<Message> messages = new ArrayList<>();
        String pending = null, line;
        while ((line = reader.readLine()) != null) {
            if (MESSAGE_PATTERN.matcher(line).matches()) {
                if (pending != null) {
                    Message msg = buildMessage(pending, mediaFiles, zipFilePath);
                    if (msg != null)
                        messages.add(msg);
                }
                pending = line;
            } else if (pending != null) {
                pending += "\n" + line;
            }
        }
        if (pending != null) {
            Message msg = buildMessage(pending, mediaFiles, zipFilePath);
            if (msg != null)
                messages.add(msg);
        }
        return messages;
    }

    private static Message buildMessage(String rawLine, Map<String, MediaEntry> mediaFiles, String zipFilePath) {
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
                String name = info != null ? info.name : "image.jpg";
                int size = info != null ? (int) info.size : 0, w = 0, h = 0;
                if (info != null) {
                    try (ZipFile zf = new ZipFile(zipFilePath)) {
                        ZipEntry ze = zf.getEntry(info.name);
                        if (ze != null) {
                            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(zf.getInputStream(ze));
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
                String name = info != null ? info.name : "video.mp4";
                int size = info != null ? (int) info.size : 0, vw = 0, vh = 0;
                String dur = "0:00";
                if (info != null) {
                    try (ZipFile zf = new ZipFile(zipFilePath)) {
                        ZipEntry ze = zf.getEntry(info.name);
                        if (ze != null) {
                            File tmp = extractToTemp(zf, ze, "video", extension(name));
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
                String name = info != null ? info.name : "audio.opus";
                int size = info != null ? (int) info.size : 0;
                String dur = "0:00";
                if (info != null) {
                    try (ZipFile zf = new ZipFile(zipFilePath)) {
                        ZipEntry ze = zf.getEntry(info.name);
                        if (ze != null) {
                            String ext = extension(name);
                            File tmp = extractToTemp(zf, ze, "audio", ext);
                            if (ext.equals("opus") || ext.equals("ogg")) {
                                dur = parseOpusDuration(tmp);
                            } else if (ext.equals("mp3")) {
                                try {
                                    com.mpatric.mp3agic.Mp3File mp3 = new com.mpatric.mp3agic.Mp3File(tmp);
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
                String name = info != null ? info.name : "document.pdf";
                int size = info != null ? (int) info.size : 0;
                return new DocumentMessage(name, extension(name), size, sender, timestamp, MessageType.DOCUMENT);
            }
            case STICKER: {
                MediaEntry info = findMedia(content, mediaFiles, "sticker");
                String name = info != null ? info.name : "sticker.webp";
                int size = info != null ? (int) info.size : 0;
                return new StickerMessage(name, size, extension(name), sender, timestamp, MessageType.STICKER);
            }
            default:
                return null;
        }
    }

    private static MessageType classifyMessage(String content) {
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

    private static MediaEntry findMedia(String content, Map<String, MediaEntry> mediaFiles, String type) {
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

    private static String parseOpusDuration(File file) {
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

    private static String parseMp4Duration(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Read the entire file — WhatsApp MP4s place the moov atom at the END
            // of the file (tail-moov layout), so a small read cap would miss it.
            long fileLen = raf.length();
            byte[] b = new byte[(int) fileLen];
            raf.readFully(b);
            for (int i = 0; i < b.length - 20; i++) {
                if (b[i] == 'm' && b[i + 1] == 'v' && b[i + 2] == 'h' && b[i + 3] == 'd') {
                    int v = b[i + 4] & 0xFF;
                    long ts, dur;
                    // Offsets from the 'mvhd' FCC position (i):
                    // v0: +4(ver+flags) +8(create) +12(modify) +16(timescale) +20(duration)
                    // v1: +4(ver+flags) +8(create64) +16(modify64) +24(trackId?) — actually:
                    // +4(ver+flags) +8(create8) +16(modify8) +24(timescale) +28(duration8)
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
        } catch (Exception e) {
        }
        return "0:00";
    }

    private static int[] parseMp4Dimensions(File file) {
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

    private static File extractToTemp(ZipFile zf, ZipEntry ze, String prefix, String ext) throws IOException {
        File tmp = File.createTempFile(prefix, "." + ext);
        try (InputStream is = zf.getInputStream(ze)) {
            java.nio.file.Files.copy(is, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tmp;
    }

    private static String extension(String f) {
        int d = f.lastIndexOf('.');
        return (d > 0 && d < f.length() - 1) ? f.substring(d + 1).toLowerCase() : "unknown";
    }

    private static String extractChatName(String p) {
        return new File(p).getName().replaceAll("\\.zip$", "").replaceAll("^WhatsApp Chat with ", "");
    }

    private static Date parseTimestamp(String raw) {
        try {
            return DATE_FORMAT.parse(raw.replaceAll("\\s+", " ").trim());
        } catch (ParseException e) {
            return new Date();
        }
    }

    private static long i32(byte[] b, int o) {
        return ((b[o] & 0xFFL) << 24) | ((b[o + 1] & 0xFFL) << 16) | ((b[o + 2] & 0xFFL) << 8) | (b[o + 3] & 0xFFL);
    }

    private static long i64(byte[] b, int o) {
        return (i32(b, o) << 32) | (i32(b, o + 4) & 0xFFFFFFFFL);
    }

    private static class MediaEntry {
        final String name;
        final long size;

        MediaEntry(String n, long s) {
            name = n;
            size = s;
        }
    }
}