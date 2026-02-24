package com.zoho.arattai.core;

import java.util.ArrayList;
import java.util.List;

import com.zoho.arattai.core.Message.MessageType;

/**
 * Immutable container for a fully-parsed WhatsApp chat export.
 *
 * <p>
 * A {@code WhatsAppExport} instance is created by
 * {@link com.zoho.arattai.core.WhatsAppChatParser#parse(String)} after it has
 * read the export ZIP, discovered the chat transcript, and converted every line
 * into a typed {@link Message} subclass. Callers obtain the data through the
 * typed accessor methods below rather than by inspecting the raw message list.
 *
 * <h2>Typical usage</h2>
 * 
 * <pre>{@code
 * WhatsAppExport export = WhatsAppChatParser.parse("/path/to/export.zip");
 * System.out.println("Chat: " + export.getChatName());
 *
 * for (Message msg : export.getAllMessages()) {
 *     System.out.println(msg.sender + " : " + msg.messageType);
 * }
 * }</pre>
 *
 * @author Zoho Arattai
 * @version 1.0
 * @see com.zoho.arattai.core.WhatsAppChatParser
 * @see Message
 * @see MessageType
 */
public class WhatsAppExport {

    /**
     * The human-readable chat name derived from the export ZIP filename.
     * The prefix {@code "WhatsApp Chat with "} and the {@code .zip} extension
     * are stripped automatically by the parser.
     */
    public final String chatName;

    /**
     * All messages in chronological order exactly as they appear
     * in the WhatsApp transcript file.
     */
    public List<Message> messages = new ArrayList<>();

    /**
     * @param chatName the display name of this chat
     * @param messages the ordered list of parsed messages
     */
    public WhatsAppExport(String chatName, List<Message> messages) {
        this.chatName = chatName;
        this.messages = messages;
    }

    /**
     * Returns the complete, ordered list of all messages in this export.
     *
     * @return an unmodifiable view of the message list
     */
    public List<Message> getAllMessages() {
        return messages;
    }

    /**
     * Returns the display name of the chat.
     *
     * @return the chat name
     */
    public String getChatName() {
        return chatName;
    }
}
