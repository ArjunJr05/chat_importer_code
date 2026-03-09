package com.example.importer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform