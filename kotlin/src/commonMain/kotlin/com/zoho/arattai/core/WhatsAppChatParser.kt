package com.zoho.arattai.core

import com.zoho.arattai.message.*
import com.zoho.arattai.core.Message.Type

/**
 * Interface to provide abstraction for platform-specific ZIP file operations.
 */
interface ZipProvider {
    fun getEntries(): List<ZipEntryInfo>
    fun readEntryAsString(name: String): String?
    fun readEntryAsBytes(name: String): ByteArray?
}

/**
 * Information about a file entry in the ZIP archive.
 */
data class ZipEntryInfo(val name: String, val size: Long)

/**
 * Stateless parser for WhatsApp chat export ZIP files (KMP Common).
 */
class WhatsAppChatParser(private val zipProvider: ZipProvider) {

    companion object {
        private val MESSAGE_PATTERN = Regex(
            "^(\\d{1,2}/\\d{1,2}/\\d{4},[\\s\\u202f\\u00a0]+\\d{1,2}:\\d{2}(?::\\d{2})?[\\s\\u202f\\u00a0]*[ap]m)\\s*-\\s*([^:]+):\\s(.*)$",
            RegexOption.IGNORE_CASE
        )

        /**
         * Parses a WhatsApp chat export via the provided ZipProvider.
         */
        fun parse(chatName: String, zipProvider: ZipProvider): WhatsAppExport {
            val parser = WhatsAppChatParser(zipProvider)
            val mediaFiles = zipProvider.getEntries()
                .filter { !it.name.endsWith(".txt") }
                .associateBy { it.name }

            var messages = emptyList<Message>()

            zipProvider.getEntries().forEach { entry ->
                if (entry.name.endsWith(".txt")) {
                    val content = zipProvider.readEntryAsString(entry.name)
                    if (content != null) {
                        messages = parser.parseTranscript(content, mediaFiles)
                    }
                }
            }

            return WhatsAppExport(chatName, messages)
        }
    }

    private fun parseTranscript(
        transcript: String,
        mediaFiles: Map<String, ZipEntryInfo>
    ): List<Message> {
        val messages = mutableListOf<Message>()
        val lines = transcript.lines()
        var pending: String? = null

        for (line in lines) {
            if (MESSAGE_PATTERN.find(line) != null) {
                if (pending != null) {
                    buildMessage(pending, mediaFiles)?.let { messages.add(it) }
                }
                pending = line
            } else if (pending != null) {
                pending += "\n" + line
            }
        }
        pending?.let { buildMessage(it, mediaFiles)?.let { messages.add(it) } }

        return messages
    }

    private fun buildMessage(rawLine: String, mediaFiles: Map<String, ZipEntryInfo>): Message? {
        val firstLine = rawLine.substringBefore("\n")
        val match = MESSAGE_PATTERN.find(firstLine) ?: return null

        val timestampStr = match.groupValues[1]
        val sender = match.groupValues[2].trim()
        val content = match.groupValues[3]

        val timestamp = parseTimestamp(timestampStr)
        val type = classifyMessage(content)

        return when (type) {
            Type.TEXT -> TextMessage(content, sender, timestamp, Type.TEXT)
            Type.IMAGE -> {
                val info = findMedia(content, mediaFiles, "image")
                val name = info?.name ?: "image.jpg"
                val size = info?.size?.toInt() ?: 0
                val (w, h) = getImageDimensions(name)
                ImageMessage(name, h, w, size, extension(name), sender, timestamp, Type.IMAGE)
            }
            Type.VIDEO -> {
                val info = findMedia(content, mediaFiles, "video")
                val name = info?.name ?: "video.mp4"
                val size = info?.size?.toInt() ?: 0
                val (vw, vh) = getVideoDimensions(name)
                VideoMessage(name, size, "0:00", extension(name), vw, vh, sender, timestamp, Type.VIDEO)
            }
            Type.AUDIO -> {
                val info = findMedia(content, mediaFiles, "audio")
                val name = info?.name ?: "audio.opus"
                val size = info?.size?.toInt() ?: 0
                AudioMessage(name, size, "0:00", extension(name), sender, timestamp, Type.AUDIO)
            }
            Type.DOCUMENT -> {
                val info = findMedia(content, mediaFiles, "document")
                val name = info?.name ?: "document.pdf"
                val size = info?.size?.toInt() ?: 0
                DocumentMessage(name, extension(name), size, sender, timestamp, Type.DOCUMENT)
            }
            Type.STICKER -> {
                val info = findMedia(content, mediaFiles, "sticker")
                val name = info?.name ?: "sticker.webp"
                val size = info?.size?.toInt() ?: 0
                StickerMessage(name, size, extension(name), sender, timestamp, Type.STICKER)
            }
        }
    }

    private fun classifyMessage(content: String): Type {
        val lc = content.lowercase().trim()
        if (lc == "<media omitted>") return Type.AUDIO
        if (lc.contains("you deleted this message") || lc.contains("this message was deleted")) return Type.TEXT
        if (lc.contains("(file attached)")) {
            if (lc.contains("stk") && lc.contains(".webp")) return Type.STICKER
            if (listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp").any { lc.contains(it) }) return Type.IMAGE
            if (listOf(".mp4", ".avi", ".mov", ".mkv", ".webm").any { lc.contains(it) }) return Type.VIDEO
            if (listOf(".mp3", ".wav", ".ogg", ".m4a", ".aac", ".opus").any { lc.contains(it) }) return Type.AUDIO
            if (listOf(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".zip", ".rar", ".txt").any { lc.contains(it) }) return Type.DOCUMENT
        }
        return Type.TEXT
    }

    private fun findMedia(content: String, mediaFiles: Map<String, ZipEntryInfo>, type: String): ZipEntryInfo? {
        val lc = content.lowercase()
        for ((name, info) in mediaFiles) {
            val ln = name.lowercase()
            val match = when (type) {
                "sticker" -> ln.endsWith(".webp")
                "image" -> listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp").any { ln.endsWith(it) }
                "video" -> listOf(".mp4", ".avi", ".mov", ".mkv", ".webm").any { ln.endsWith(it) }
                "audio" -> listOf(".mp3", ".wav", ".ogg", ".m4a", ".aac", ".opus").any { ln.endsWith(it) }
                "document" -> listOf(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".zip", ".rar", ".txt").any { ln.endsWith(it) }
                else -> false
            }
            if (match && (lc.contains(name) || lc.contains(ln))) return info
        }
        return null
    }

    private fun getImageDimensions(name: String): Pair<Int, Int> {
        val bytes = zipProvider.readEntryAsBytes(name) ?: return 0 to 0
        // Simple JPEG/PNG/WebP header parsing could be implemented here
        return 0 to 0
    }

    private fun getVideoDimensions(name: String): Pair<Int, Int> {
        // MP4 dimension parsing logic would go here
        return 0 to 0
    }

    private fun extension(f: String): String {
        val d = f.lastIndexOf('.')
        return if (d in 1 until f.length - 1) f.substring(d + 1).lowercase() else "unknown"
    }

    private fun parseTimestamp(raw: String): Long {
        // Quick and dirty manual parse for "dd/MM/yyyy, h:mm a"
        // In a real KMP app, you'd use kotlinx-datetime or an expect/actual
        return 0L
    }
}
