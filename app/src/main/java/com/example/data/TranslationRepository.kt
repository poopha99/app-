package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class TranslationRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("game_trans_prefs", Context.MODE_PRIVATE)

    private val _history = MutableStateFlow<List<TranslationRecord>>(emptyList())
    val history: StateFlow<List<TranslationRecord>> = _history.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _autoClipboard = MutableStateFlow(prefs.getBoolean("auto_clipboard", true))
    val autoClipboard: StateFlow<Boolean> = _autoClipboard.asStateFlow()

    private val _selectedMode = MutableStateFlow(
        TranslationMode.valueOf(prefs.getString("mode", TranslationMode.GAME.name) ?: TranslationMode.GAME.name)
    )
    val selectedMode: StateFlow<TranslationMode> = _selectedMode.asStateFlow()

    init {
        loadHistory()
    }

    fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }

    fun setAutoClipboard(enabled: Boolean) {
        _autoClipboard.value = enabled
        prefs.edit().putBoolean("auto_clipboard", enabled).apply()
    }

    fun setSelectedMode(mode: TranslationMode) {
        _selectedMode.value = mode
        prefs.edit().putString("mode", mode.name).apply()
    }

    fun addTranslation(original: String, translated: String, mode: String = _selectedMode.value.name): TranslationRecord {
        val record = TranslationRecord(
            originalText = original,
            translatedText = translated,
            mode = mode
        )
        val current = _history.value.toMutableList()
        current.add(0, record)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _history.value = current
        saveHistory(current)
        return record
    }

    fun toggleBookmark(id: String) {
        val current = _history.value.map {
            if (it.id == id) it.copy(isBookmarked = !it.isBookmarked) else it
        }
        _history.value = current
        saveHistory(current)
    }

    fun deleteRecord(id: String) {
        val current = _history.value.filter { it.id != id }
        _history.value = current
        saveHistory(current)
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove("history_json").apply()
    }

    private fun saveHistory(list: List<TranslationRecord>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("original", item.originalText)
                put("translated", item.translatedText)
                put("pinyin", item.pinyin)
                put("timestamp", item.timestamp)
                put("mode", item.mode)
                put("isBookmarked", item.isBookmarked)
                put("note", item.note)
            }
            array.put(obj)
        }
        prefs.edit().putString("history_json", array.toString()).apply()
    }

    private fun loadHistory() {
        val jsonStr = prefs.getString("history_json", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<TranslationRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TranslationRecord(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            originalText = obj.optString("original"),
                            translatedText = obj.optString("translated"),
                            pinyin = obj.optString("pinyin"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            mode = obj.optString("mode", "GAME"),
                            isBookmarked = obj.optBoolean("isBookmarked", false),
                            note = obj.optString("note", "")
                        )
                    )
                }
                _history.value = list
                return
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }

        // Seed with realistic initial game examples
        _history.value = listOf(
            TranslationRecord(
                originalText = "勇者啊，請前往幽暗森林擊敗首領！",
                translatedText = "ผู้กล้าเอ๋ย จงมุ่งหน้าไปยังป่าทมิฬเพื่อโค่นล้มบอส!",
                mode = "GAME",
                isBookmarked = true
            ),
            TranslationRecord(
                originalText = "獲得傳奇裝備【熾炎龍之劍】！",
                translatedText = "ได้รับอุปกรณ์ระดับตำนาน 【ดาบมังกรเพลิงพิโรธ】!",
                mode = "GAME",
                isBookmarked = true
            ),
            TranslationRecord(
                originalText = "暴擊率提升 15%，持續 10 秒。",
                translatedText = "เพิ่มอัตราคริติคอล (Crit Rate) 15% เป็นเวลา 10 วินาที",
                mode = "GAME"
            )
        )
    }

    companion object {
        @Volatile
        private var instance: TranslationRepository? = null

        fun getInstance(context: Context): TranslationRepository {
            return instance ?: synchronized(this) {
                instance ?: TranslationRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
