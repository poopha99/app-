package com.example.data

data class TranslationRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originalText: String,
    val translatedText: String,
    val pinyin: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "GAME", // GAME or WORK
    val isBookmarked: Boolean = false,
    val note: String = ""
)
