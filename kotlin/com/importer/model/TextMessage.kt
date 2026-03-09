package com.importer.model

import kotlinx.datetime.Instant

data class TextMessage(
    override val id: String,
    override val sender: String,
    override val timestamp: Instant,
    override val isFromMe: Boolean,
    val textContent: String
) : Message()