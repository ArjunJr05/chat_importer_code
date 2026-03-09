package com.importer.model

import kotlinx.datetime.Instant

sealed class Message {
    abstract val id: String // Keep for UI uniqueness/stability
    abstract val sender: String
    abstract val timestamp: Instant
    abstract val isFromMe: Boolean // Useful for UI display
}