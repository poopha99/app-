package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class TranslationMode(val titleThai: String, val systemPrompt: String) {
    GAME(
        titleThai = "โหมดเกมเมอร์ (Gaming RPG)",
        systemPrompt = "คุณคือผู้เชี่ยวชาญแปลภาษาจีนไต้หวัน (Traditional Chinese / 繁體中文) เป็นภาษาไทยสำหรับเกมเมอร์โดยเฉพาะ " +
                "ให้แปลบทสนทนาตัวละครในเกม เควสต์ คำอธิบายสกิล อุปกรณ์ ดันเจี้ยน หรือปุ่ม UI ให้เป็นภาษาไทยที่กระชับ สละสลวย เป็นธรรมชาติแบบศัพท์เกมเมอร์ที่ใช้กันจริง " +
                "(เช่น 爆擊 -> คริติคอล, 裝備 -> สวมใส่อุปกรณ์, 強化 -> ตีบวก/อัปเกรด, 副本 -> ดันเจี้ยน, 冷卻 -> คูลดาวน์) " +
                "หากเป็นบทสนทนาเนื้อเรื่อง ให้แปลได้อารมณ์แฟนตาซี/ยุทธภพตามบรรยากาศเกม " +
                "ตอบเฉพาะคำแปลภาษาไทยเท่านั้น และสามารถใส่คำอ่านพินอิน/จู้อินในวงเล็บสั้นๆ ต่อท้ายได้หากเป็นศัพท์สำคัญ ไม่ต้องใส่คำอธิบายเยิ่นเย้อ"
    ),
    WORK(
        titleThai = "โหมดทำงาน & มัลติทาสก์ (Work & Chat)",
        systemPrompt = "คุณคือผู้ช่วยแปลภาษาจีนไต้หวัน (Traditional Chinese / 繁體中文) เป็นภาษาไทยสำหรับการทำงาน สนทนาธุรกิจ และการแชท " +
                "แปลให้สุภาพ ชัดเจน ได้ใจความ ตรงตามบริบทการทำงาน ออฟฟิศ และชีวิตประจำวันของไต้หวัน ตอบเฉพาะผลลัพธ์การแปลภาษาไทยที่กระชับ"
    )
}

object GeminiTranslationService {
    private const val TAG = "GeminiTranslation"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun translate(
        text: String,
        mode: TranslationMode = TranslationMode.GAME
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success("")
        }

        // Check local quick dictionary first if it's a known single gaming term
        val directMatch = GameDictionary.terms.find { it.traditionalChinese == trimmed }
        if (directMatch != null) {
            return@withContext Result.success("${directMatch.thai} (${directMatch.pinyin})")
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Smart local fallback for prototyping / offline gaming
            val localFallback = generateSmartOfflineTranslation(trimmed, mode)
            return@withContext Result.success(localFallback)
        }

        try {
            val jsonPayload = JSONObject().apply {
                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "ข้อความภาษาจีนไต้หวันที่จะแปล:\n$trimmed")
                            })
                        })
                    })
                })

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", mode.systemPrompt)
                        })
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topK", 20)
                    put("topP", 0.9)
                })
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string()

            if (!response.isSuccessful || responseBodyString == null) {
                Log.w(TAG, "Gemini API error code: ${response.code}, body: $responseBodyString")
                val fallback = generateSmartOfflineTranslation(trimmed, mode)
                return@withContext Result.success(fallback)
            }

            val responseJson = JSONObject(responseBodyString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val resultText = parts?.optJSONObject(0)?.optString("text")?.trim()

            if (!resultText.isNullOrEmpty()) {
                Result.success(resultText)
            } else {
                val fallback = generateSmartOfflineTranslation(trimmed, mode)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation error", e)
            val fallback = generateSmartOfflineTranslation(trimmed, mode)
            Result.success(fallback)
        }
    }

    private fun generateSmartOfflineTranslation(chineseText: String, mode: TranslationMode): String {
        // Find matching keywords from dictionary
        val matches = GameDictionary.terms.filter { chineseText.contains(it.traditionalChinese) }
        
        // Common gaming dialogue patterns
        val presetTranslations = mapOf(
            "勇者啊，請前往幽暗森林擊敗首領！" to "ผู้กล้าเอ๋ย จงมุ่งหน้าไปยังป่าทมิฬเพื่อโค่นล้มบอส!",
            "獲得傳奇裝備【熾炎龍之劍】！" to "ได้รับอุปกรณ์ระดับตำนาน 【ดาบมังกรเพลิงพิโรธ】!",
            "體力不足，請使用體力藥水或稍後再試。" to "พลังงาน (Stamina) ไม่เพียงพอ กรุณาใช้น้ำยาเพิ่มพลังงานหรือลองใหม่อีกครั้ง",
            "恭喜通關第十章【星辰之塔】！" to "ยินดีด้วย! ผ่านด่านบทที่ 10 【หอคอยแห่งดวงดาว】 สำเร็จ!",
            "暴擊率提升 15%，持續 10 秒。" to "เพิ่มอัตราคริติคอล (Crit Rate) 15% เป็นเวลา 10 วินาที",
            "這份專案報告需要今天下班前確認。" to "รายงานโปรเจกต์ฉบับนี้ต้องตรวจเช็กและยืนยันก่อนเลิกงานวันนี้ครับ",
            "會議改到下午兩點在三樓會議室。" to "การประชุมเลื่อนไปเป็นเวลา 14:00 น. ที่ห้องประชุมชั้น 3 ครับ",
            "收到，辛苦了！" to "รับทราบครับ ขอบคุณสำหรับความเหน็ดเหนื่อย/ทำได้ดีมากครับ!"
        )

        presetTranslations[chineseText]?.let { return it }

        if (matches.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("คำแปลจับคู่คำศัพท์เกม:\n")
            matches.forEach { term ->
                sb.append("• ${term.traditionalChinese} (${term.pinyin}) ➔ ${term.thai}\n")
            }
            return sb.toString().trim()
        }

        return if (mode == TranslationMode.GAME) {
            "ข้อความเกม: $chineseText (เชื่อมต่อเครือข่ายเพื่อแปลแบบเต็มประโยคผ่าน AI)"
        } else {
            "ข้อความงาน: $chineseText (เชื่อมต่อเครือข่ายเพื่อแปลแบบเต็มประโยคผ่าน AI)"
        }
    }
}
