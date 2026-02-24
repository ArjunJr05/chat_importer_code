package com.zoho.arattai;

import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.text.SimpleDateFormat;

import com.zoho.arattai.Message.*;
import com.zoho.arattai.core.WhatsAppChatParser;
import com.zoho.arattai.core.Message;
import com.zoho.arattai.core.WhatsAppExport;

/**
 * Command-line entry point for the WhatsApp Chat Parser application.
 *
 * <p>
 * The application prompts the user for the path to a WhatsApp export
 * {@code .zip} file, delegates parsing to
 * {@link WhatsAppChatParser#parse(String)}, and then prints two sections to
 * standard output:
 * <ol>
 * <li>A <b>category summary</b> showing how many messages of each type were
 * found (text, image, video, audio, document, sticker).</li>
 * <li>A <b>complete message listing</b> with per-message detail for every
 * message in the export.</li>
 * </ol>
 *
 * <h2>Running the application</h2>
 * 
 * <pre>
 * mvn exec:java
 * Enter the Zip file path: /path/to/WhatsApp Chat with Alice.zip
 * </pre>
 *
 * <p>
 * The path may be wrapped in double-quotes (as pasted from Windows Explorer);
 * the surrounding quotes are stripped automatically.
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see WhatsAppChatParser
 * @see WhatsAppExport
 */

public class App {

    /**
     * Date/time formatter used when printing message timestamps to the console.
     * Format: {@code dd/MM/yyyy, hh:mm:ss a} (e.g.,
     * {@code 23/02/2026, 12:25:18 am}).
     */
    public static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a");

    /**
     * Application entry point.
     *
     * <p>
     * Reads the ZIP file path from standard input, parses the export, and
     * prints the summary and full message list. Any {@link Exception} thrown
     * during parsing is caught, its message printed to {@code System.err},
     * and the stack trace is dumped for diagnostics.
     *
     * @param args command-line arguments (not used; path is collected
     *             interactively)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Message message = new ImageMessage("test.jpg", 100, 100, 500, "jpg", "Arjun", new Date(),
                Message.Type.IMAGE);

        System.out.println(message.getSender());

        try {
            System.out.println("=======================================");
            System.out.println("  WhatsApp Chat Parser - Chat Importer");
            System.out.println("=======================================\n");

            System.out.print("Enter the Zip file path: ");
            String zipPath = scanner.nextLine().trim();

            // Strip surrounding quotes if the user wrapped the path
            if (zipPath.startsWith("\"") && zipPath.endsWith("\"")) {
                zipPath = zipPath.substring(1, zipPath.length() - 1);
            }

            System.out.println("\nParsing chat file...");

            WhatsAppExport export = WhatsAppChatParser.parse(zipPath);

            if (export.getAllMessages().isEmpty()) {
                System.out.println("No messages found in the ZIP file.");
                return;
            }

            // High-level summary
            System.out.println("\n========== SUMMARY ==========");
            System.out.println("Chat Name: " + export.getChatName());
            System.out.println("Total messages parsed: " + export.getAllMessages().size());
            System.out.println("=============================\n");

            // Full chronological message list
            printAllMessages(export);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // -------------------------------------------------------------------------
    // Output helpers
    // -------------------------------------------------------------------------

    /**
     * Prints every message in the export to standard output, preceded by a
     * numbered header and followed by a separator line.
     *
     * <p>
     * Messages are printed in the chronological order returned by
     * {@link WhatsAppExport#getAllMessages()}.
     *
     * @param export the fully-parsed chat export; must not be {@code null}
     */
    public static void printAllMessages(WhatsAppExport export) {
        System.out.println("\n========== COMPLETE ARRAYLIST DATA ==========");
        System.out.println("Chat Name: " + export.getChatName());
        System.out.println("Total Messages: " + export.getAllMessages().size());
        System.out.println("=============================================\n");

        List<Message> messages = export.getAllMessages();
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            System.out.println("--- Message #" + (i + 1) + " ---");
            printMessage(msg, export.getChatName());
            System.out.println("----------------------------\n");
        }

        System.out.println("============================================\n");
    }

    /**
     * Prints the details of a single {@link Message} to standard output.
     *
     * <p>
     * The method uses {@code instanceof} checks to down-cast to the correct
     * subclass and then prints the type-specific fields (e.g., pixel dimensions
     * for images, duration for audio/video). Fields shared by all message types
     * (sender, timestamp, message type) are always printed regardless of
     * sub-type.
     *
     * @param msg      the message to print; must not be {@code null}
     * @param chatName the name of the chat, printed on each line for context
     */
    public static void printMessage(Message msg, String chatName) {
        System.out.println("Chat Name: " + chatName);
        System.out.println("Sender: " + msg.getSender());
        System.out.println("Timestamp: " + fmt(msg.getTimestamp()));
        System.out.println("Message Type: " + msg.getType());

        if (msg instanceof TextMessage) {
            TextMessage m = (TextMessage) msg;
            System.out.println("Text: " + m.getText());

        } else if (msg instanceof ImageMessage) {
            ImageMessage m = (ImageMessage) msg;
            System.out.println("Image Name: " + m.getName());
            System.out.println("Image Height: " + m.getHeight());
            System.out.println("Image Width: " + m.getWidth());
            System.out.println("Image Size: " + m.getSize() + " bytes");
            System.out.println("Image Type: " + m.getExtension());

        } else if (msg instanceof VideoMessage) {
            VideoMessage m = (VideoMessage) msg;
            System.out.println("Video Name: " + m.getName());
            System.out.println("Video Size: " + m.getSize() + " bytes");
            System.out.println("Video Duration: " + m.getDuration());
            System.out.println("Video Type: " + m.getExtension());
            System.out.println("Video Width: " + m.getWidth());
            System.out.println("Video Height: " + m.getHeight());

        } else if (msg instanceof AudioMessage) {
            AudioMessage m = (AudioMessage) msg;
            System.out.println("Audio Name: " + m.getName());
            System.out.println("Audio Size: " + m.getSize() + " bytes");
            System.out.println("Audio Duration: " + m.getDuration());
            System.out.println("Audio Type: " + m.getExtension());

        } else if (msg instanceof DocumentMessage) {
            DocumentMessage m = (DocumentMessage) msg;
            System.out.println("Document Name: " + m.getName());
            System.out.println("Document Type: " + m.getExtension());
            System.out.println("Document Size: " + m.getSize() + " bytes");

        } else if (msg instanceof StickerMessage) {
            StickerMessage m = (StickerMessage) msg;
            System.out.println("Sticker Name: " + m.getName());
            System.out.println("Sticker Type: " + m.getExtension());
            System.out.println("Sticker Size: " + m.getSize() + " bytes");
        }
    }

    /**
     * Formats a {@link Date} using {@link #DISPLAY_FORMAT} for console output.
     *
     * @param d the date to format; must not be {@code null}
     * @return the formatted date/time string (e.g.,
     *         {@code "23/02/2026, 12:25:18 am"})
     */
    public static String fmt(Date d) {
        return DISPLAY_FORMAT.format(d);
    }
}
