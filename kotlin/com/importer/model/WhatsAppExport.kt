package com.importer.model

data class WhatsAppExport(
    val chatName: String,
    val messages: List<Message>
)