package com.zoho.arattai.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zoho.arattai.Message.*;
import com.zoho.arattai.Model.MessageType;

/**
 * Immutable container for a fully-parsed WhatsApp chat export.
 *
 * <p>
 * A {@code WhatsAppExport} instance is created by
 * {@link com.zoho.arattai.Parser.WhatsAppChatParser#parse(String)} after it has
 * read the export ZIP, discovered the chat transcript, and converted every line
 * into a typed {@link Message} subclass. Callers obtain the data through the
 * typed accessor methods below rather than by inspecting the raw message list.
 *
 * <h2>Typical usage</h2>
 * 
 * <pre>{@code
 * WhatsAppExport export = WhatsAppChatParser.parse("/path/to/export.zip");
 * System.out.println("Chat: " + export.getChatName());
 * export.printCategorySummary();
 *
 * for (AudioMessage audio : export.getAudioMessages()) {
 *     System.out.println(audio.getAudioName() + " – " + audio.getAudioDuration());
 * }
 * }</pre>
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see com.zoho.arattai.Parser.WhatsAppChatParser
 * @see Message
 * @see MessageType
 */
public class WhatsAppExport {

    /**
     * The human-readable chat name derived from the export ZIP filename.
     * The prefix {@code "WhatsApp Chat with "} and the {@code .zip} extension
     * are stripped automatically by the parser.
     */
    private final String chatName;

    /**
     * All messages in chronological order exactly as they appear
     * in the WhatsApp transcript file.
     */
    private List<Message> messages = new ArrayList<>();

    /**
     * Constructs an empty {@code WhatsAppExport} for the given chat.
     * The message list is populated afterwards by the parser via
     * {@link #setAllMessages(List)}.
     *
     * @param chatName the display name of this chat; must not be {@code null}
     */
    public WhatsAppExport(String chatName) {
        this.chatName = chatName;
    }

    /**
     * Replaces the entire message list with the supplied list.
     * Called once by the parser after it has processed the full transcript.
     *
     * @param messages the ordered list of parsed messages; must not be {@code null}
     */
    public void setAllMessages(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Appends a single message to the end of the list.
     * Use this method when building the export incrementally.
     *
     * @param message the message to append; must not be {@code null}
     */
    public void addMessage(Message message) {
        this.messages.add(message);
    }

    /**
     * Returns the complete, ordered list of all messages in this export.
     *
     * @return an unmodifiable view of the message list; never {@code null}
     */
    public List<Message> getAllMessages() {
        return messages;
    }

    /**
     * Returns the display name of the chat.
     *
     * @return the chat name; never {@code null}
     */
    public String getChatName() {
        return chatName;
    }

    /**
     * Returns the total number of parsed messages.
     *
     * @return message count, zero or more
     */
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Returns all plain-text messages in this export, preserving their
     * original order.
     *
     * @return a new list containing only {@link TextMessage} instances;
     *         never {@code null}, may be empty
     */
    public List<TextMessage> getTextMessages() {
        return messages.stream()
                .filter(m -> m instanceof TextMessage)
                .map(m -> (TextMessage) m)
                .collect(Collectors.toList());
    }

    /**
     * Returns all image messages in this export, preserving their
     * original order.
     *
     * @return a new list containing only {@link ImageMessage} instances;
     *         never {@code null}, may be empty
     */
    public List<ImageMessage> getImageMessages() {
        return messages.stream()
                .filter(m -> m instanceof ImageMessage)
                .map(m -> (ImageMessage) m)
                .collect(Collectors.toList());
    }

    /**
     * Returns all video messages in this export, preserving their
     * original order.
     *
     * @return a new list containing only {@link VideoMessage} instances;
     *         never {@code null}, may be empty
     */
    public List<VideoMessage> getVideoMessages() {
        return messages.stream()
                .filter(m -> m instanceof VideoMessage)
                .map(m -> (VideoMessage) m)
                .collect(Collectors.toList());
    }

    /**
     * Returns all audio messages in this export, preserving their
     * original order.
     *
     * @return a new list containing only {@link AudioMessage} instances;
     *         never {@code null}, may be empty
     */
    public List<AudioMessage> getAudioMessages() {
        return messages.stream()
                .filter(m -> m instanceof AudioMessage)
                .map(m -> (AudioMessage) m)
                .collect(Collectors.toList());
    }

    /**
     * Returns all document/file-attachment messages in this export,
     * preserving their original order.
     *
     * @return a new list containing only {@link DocumentMessage} instances;
     *         never {@code null}, may be empty
     */
    public List<DocumentMessage> getDocumentMessages() {
        return messages.stream()
                .filter(m -> m instanceof DocumentMessage)
                .map(m -> (DocumentMessage) m)
                .collect(Collectors.toList());
    }

    /**
     * Returns all animated-sticker messages in this export, preserving
     * their original order.
     *
     * @return a new list containing only {@link StickerMessage} instances;
     *         never {@code null}, may be empty
     */
    public List<StickerMessage> getStickerMessages() {
        return messages.stream()
                .filter(m -> m instanceof StickerMessage)
                .map(m -> (StickerMessage) m)
                .collect(Collectors.toList());
    }

    /**
     * Returns all messages that match the given {@link MessageType}.
     *
     * @param type the message category to filter by; must not be {@code null}
     * @return a new list of matching {@link Message} instances in original order;
     *         never {@code null}, may be empty
     */
    public List<Message> getMessagesByType(MessageType type) {
        return messages.stream()
                .filter(m -> m.messageType == type)
                .collect(Collectors.toList());
    }

    /**
     * Prints a formatted category summary to standard output.
     *
     * <p>
     * The output includes the chat name, total message count, and individual
     * counts for each of the six supported message categories (TEXT, IMAGE, VIDEO,
     * AUDIO, DOCUMENT, STICKER).
     */
    public void printCategorySummary() {
        System.out.println("\n======== MESSAGE CATEGORIES ========");
        System.out.println("Chat Name: " + chatName);
        System.out.println("Total Messages: " + getMessageCount());
        System.out.println("-----------------------------------");
        System.out.println("Text Messages:     " + getTextMessages().size());
        System.out.println("Image Messages:    " + getImageMessages().size());
        System.out.println("Video Messages:    " + getVideoMessages().size());
        System.out.println("Audio Messages:    " + getAudioMessages().size());
        System.out.println("Document Messages: " + getDocumentMessages().size());
        System.out.println("Sticker Messages:  " + getStickerMessages().size());
        System.out.println("====================================\n");
    }
}
