package com.importer.parser

import com.importer.model.Message
import com.importer.metadata.MediaMetadataExtractor // Using the expect class
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ChatParser {
    /**
     * Parses a WhatsApp chat file and returns a flow of Message objects.
     * This allows for memory-efficient processing of large chats.
     * @param chatFilePath The absolute path to the _chat.txt file.
     * @param mediaFolderPath The absolute path to the folder containing media files.
     */
    fun parseChatFile(chatFilePath: String, mediaFolderPath: String): Flow<Message>
}

class WhatsAppChatParser(
    private val mediaMetadataExtractor: MediaMetadataExtractor
) : ChatParser {
    override fun parseChatFile(chatFilePath: String, mediaFolderPath: String): Flow<Message> = flow {
        // Implement line-by-line parsing of _chat.txt here.
        // For each line, identify message components (sender, timestamp, text, media).
        // If media is identified, use mediaMetadataExtractor to get details.
        println("Warning: WhatsAppChatParser is a placeholder. Implement chat parsing logic.")
        // emit(parsedMessage)
    }
}