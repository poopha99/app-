package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiTranslationService
import com.example.data.TranslationMode
import com.example.data.TranslationRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class GameScenario(
    val id: String,
    val title: String,
    val category: String,
    val speaker: String,
    val chineseDialogue: String,
    val subtitleThai: String,
    val stats: List<Pair<String, String>> = emptyList()
)

@Composable
fun GameSimulatorScreen(
    repository: TranslationRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scenarios = remember {
        listOf(
            GameScenario(
                id = "rpg_quest",
                title = "MMORPG: เควสต์เนื้อเรื่องหลัก",
                category = "เควสต์ & บทสนทนา",
                speaker = "精靈長老 (ผู้อาวุโสเอลฟ์)",
                chineseDialogue = "勇者啊，請前往幽暗森林擊敗首領，奪回被封印的熾炎寶石！",
                subtitleThai = "ผู้กล้าเอ๋ย จงมุ่งหน้าไปยังป่าทมิฬเพื่อโค่นล้มบอส และชิงอัญมณีเพลิงพิโรธที่ถูกผนึกกลับคืนมา!"
            ),
            GameScenario(
                id = "gacha_weapon",
                title = "Gacha: ดาบมังกรระดับตำนาน",
                category = "อุปกรณ์ & สเตตัส",
                speaker = "裝備屬性 (คุณสมบัติไอเทม)",
                chineseDialogue = "獲得傳奇裝備【熾炎龍之劍】！基礎攻擊力+850，暴擊率提升 15%，冷卻時間縮短 10%。",
                subtitleThai = "ได้รับอุปกรณ์ระดับตำนาน 【ดาบมังกรเพลิงพิโรธ】! พลังโจมตีพื้นฐาน +850, เพิ่มอัตราคริติคอล (Crit Rate) 15%, ลดเวลาคูลดาวน์ (CD) 10%",
                stats = listOf("基礎攻擊力" to "+850 ATK", "暴擊率" to "+15% Crit Rate", "冷卻縮短" to "-10% CD")
            ),
            GameScenario(
                id = "boss_raid",
                title = "Boss Raid: คำสั่งต่อสู้เรดบอส",
                category = "การต่อสู้ & ดันเจี้ยน",
                speaker = "隊長指令 (คำสั่งหัวหน้าปาร์ตี้)",
                chineseDialogue = "所有人注意！首領即將釋放大範圍眩暈技能，坦克開啟霸體，其他人迅速閃避！",
                subtitleThai = "ทุกคนระวัง! บอสกำลังจะปล่อยสกิลสตั๊น (Stun) วงกว้าง แทงค์เปิดซูเปอร์อาร์เมอร์ (Super Armor) ด่วน คนอื่นรีบหลบหลีก (Dodge)!"
            ),
            GameScenario(
                id = "work_chat",
                title = "Work Chat: ข้อความแชทส่งงานด่วน",
                category = "การทำงาน & มัลติทาสก์",
                speaker = "主管 (หัวหน้างาน)",
                chineseDialogue = "這份專案進度報告需要今天下班前確認，請儘速將最新檔案寄到信箱，辛苦了！",
                subtitleThai = "รายงานความคืบหน้าโปรเจกต์นี้ต้องตรวจเช็กและยืนยันก่อนเลิกงานวันนี้ รบกวนรีบส่งไฟล์ล่าสุดเข้ากล่องอีเมลด้วยครับ ขอบคุณสำหรับความเหน็ดเหนื่อย!"
            )
        )
    }

    var selectedScenario by remember { mutableStateOf(scenarios[0]) }
    var activeTranslatedSubtitle by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }

    // Floating Reticle / Target Box drag offsets
    var reticleOffsetX by remember { mutableStateOf(0f) }
    var reticleOffsetY by remember { mutableStateOf(0f) }
    var isReticleVisible by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intro Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Brush.horizontalGradient(listOf(CyberPurple, CyberCyan)), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ห้องจำลองหน้าจอเกม & แปลสด",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ทดสอบการทำงานของปุ่มลอย HUD และกรอบเล็งแปลเกม",
                            color = CyberCyan,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ลองสลับฉากเกมหรือแชทงานด้านล่าง แล้วกด '⚡ สแกนแปลบทสนทนา' หรือแตะกรอบสีฟ้าเพื่อดูซับไตเติลภาษาไทยซ้อนทับทันที",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Scenario Selector Chips
        Text(text = "เลือกฉากจำลองสถานการณ์:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            scenarios.forEach { scenario ->
                val isSelected = scenario.id == selectedScenario.id
                Surface(
                    color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberDarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CyberCyan else CyberCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedScenario = scenario
                            activeTranslatedSubtitle = null
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (scenario.id == "work_chat") Icons.Default.Work else Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = if (isSelected) CyberCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = scenario.category,
                            color = if (isSelected) CyberCyan else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Simulated Game Screen Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Brush.verticalGradient(listOf(CyberCyan, CyberPurple)), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070A12))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Game HUD Top Status Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CyberGold, CyberRed))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("75", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Lv.75 龍之勇者", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            // Mini HP bar
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.DarkGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(5.dp)
                                        .background(CyberGreen)
                                )
                            }
                        }
                    }

                    // Game Mini Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = CyberDarkSurface, shape = RoundedCornerShape(6.dp)) {
                            Text("體力: 110/120", color = CyberGold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(color = CyberDarkSurface, shape = RoundedCornerShape(6.dp)) {
                            Text("金幣: 48,200", color = CyberCyan, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Game Center Scene / Illustration area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF16192B),
                                    Color(0xFF0F172A),
                                    Color(0xFF090D16)
                                )
                            )
                        )
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Game environment visuals
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedScenario.id == "rpg_quest") Icons.Default.Shield
                            else if (selectedScenario.id == "gacha_weapon") Icons.Default.AutoAwesome
                            else if (selectedScenario.id == "work_chat") Icons.Default.Work
                            else Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedScenario.title,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "【${selectedScenario.speaker}】",
                            color = CyberGold,
                            fontSize = 12.sp
                        )

                        if (selectedScenario.stats.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedScenario.stats.forEach { stat ->
                                    Surface(
                                        color = CyberCyan.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberCyan)
                                    ) {
                                        Text(
                                            text = "${stat.first} (${stat.second})",
                                            color = CyberCyan,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Draggable Floating Reticle Box (Simulated Overlay)
                    if (isReticleVisible) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(reticleOffsetX.roundToInt(), reticleOffsetY.roundToInt()) }
                                .size(width = 240.dp, height = 75.dp)
                                .border(1.5.dp, CyberCyan, RoundedCornerShape(8.dp))
                                .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        reticleOffsetX += dragAmount.x
                                        reticleOffsetY += dragAmount.y
                                    }
                                },
                            contentAlignment = Alignment.TopStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberCyan.copy(alpha = 0.7f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎯 กรอบเล็งสแกน HUD (ลากย้ายได้)", color = CyberBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("แตะเพื่อแปล", color = CyberBlack, fontSize = 8.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Game Dialogue Box (Original Chinese Text)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131724))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 บทสนทนาในเกม (ต้นฉบับจีนไต้หวัน):",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Surface(
                                color = CyberPurple.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "繁體中文",
                                    color = CyberPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedScenario.chineseDialogue,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Floating Translation Subtitle (Overlaid on game screen)
                AnimatedVisibility(
                    visible = activeTranslatedSubtitle != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xE60F172A))
                            .border(1.5.dp, CyberCyan, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ซับไตเติลแปลสดบนหน้าจอเกม (Floating Subtitle)",
                                        color = CyberCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Thai Translation", activeTranslatedSubtitle ?: "")
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "คัดลอกคำแปลสำเร็จ", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeTranslatedSubtitle ?: "",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trigger Translate Actions Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            isTranslating = true
                            coroutineScope.launch {
                                val result = GeminiTranslationService.translate(
                                    selectedScenario.chineseDialogue,
                                    if (selectedScenario.id == "work_chat") TranslationMode.WORK else TranslationMode.GAME
                                )
                                activeTranslatedSubtitle = result.getOrDefault(selectedScenario.subtitleThai)
                                isTranslating = false
                                repository.addTranslation(
                                    selectedScenario.chineseDialogue,
                                    activeTranslatedSubtitle ?: "",
                                    if (selectedScenario.id == "work_chat") "WORK" else "GAME"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("scan_translate_btn")
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(color = CyberBlack, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("กำลังแปลสด...", color = CyberBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ สแกนแปลบทสนทนา", color = CyberBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            isReticleVisible = !isReticleVisible
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (isReticleVisible) "ซ่อนกรอบ" else "แสดงกรอบ",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
