package com.example.data

data class DictionaryTerm(
    val traditionalChinese: String,
    val pinyin: String,
    val thai: String,
    val category: String,
    val explanation: String
)

object GameDictionary {
    val terms = listOf(
        // Combat & Stats
        DictionaryTerm("爆擊", "bào jī", "คริติคอล (Critical Hit)", "การต่อสู้", "ดาเมจรุนแรงพิเศษ มักขึ้นตัวเลขสีแดงหรือเหลือง"),
        DictionaryTerm("爆擊傷害", "bào jī shāng hài", "ความแรงคริติคอล (Crit DMG)", "การต่อสู้", "เพิ่มเปอร์เซ็นต์ความเสียหายเมื่อติดคริติคอล"),
        DictionaryTerm("爆擊率", "bào jī lǜ", "อัตราคริติคอล (Crit Rate)", "การต่อสู้", "โอกาสที่จะเกิดการโจมตีคริติคอล"),
        DictionaryTerm("冷卻時間", "lěng què shí jiān", "เวลาคูลดาวน์ (Cooldown / CD)", "การต่อสู้", "ระยะเวลารอคอยก่อนใช้สกิลได้อีกครั้ง"),
        DictionaryTerm("攻擊力", "gōng jī lì", "พลังโจมตี (ATK)", "การต่อสู้", "ค่าพลังโจมตีกายภาพหรือพื้นฐานของตัวละคร"),
        DictionaryTerm("魔法攻擊", "mó fǎ gōng jī", "พลังโจมตีเวท (MATK)", "การต่อสู้", "ค่าพลังโจมตีด้วยเวทมนตร์หรือธาตุ"),
        DictionaryTerm("防禦力", "fáng yù lì", "พลังป้องกัน (DEF)", "การต่อสู้", "ลดความเสียหายทางกายภาพที่ได้รับ"),
        DictionaryTerm("生命值", "shēng mìng zhí", "พลังชีวิต (HP)", "การต่อสู้", "แถบเลือดของตัวละคร (Health Points)"),
        DictionaryTerm("法力值", "fǎ lì zhí", "มานา / พลังจิต (MP)", "การต่อสู้", "แต้มพลังงานที่ใช้ในการร่ายเวทมนตร์"),
        DictionaryTerm("閃避", "shǎn bì", "หลบหลีก (Dodge / Evasion)", "การต่อสู้", "โอกาสที่การโจมตีของศัตรูจะไม่โดนตัวละคร"),
        DictionaryTerm("命中率", "mìng zhòng lǜ", "ความแม่นยำ (Accuracy)", "การต่อสู้", "โอกาสที่การโจมตีของเราจะโดนเป้าหมาย"),
        DictionaryTerm("穿透", "chuān tòu", "เจาะเกราะ (Armor Penetration)", "การต่อสู้", "มองข้ามพลังป้องกันของศัตรู"),
        DictionaryTerm("吸血", "xī xiě", "ดูดเลือด (Life Steal)", "การต่อสู้", "ฟื้นฟู HP ตามสัดส่วนความเสียหายที่ทำได้"),
        DictionaryTerm("霸體", "bà tǐ", "ซูเปอร์อาร์เมอร์ (Super Armor / Unstoppable)", "การต่อสู้", "สถานะไม่กระเด็น ไม่ล้ม ไม่ติดขัดจังหวะเมื่อโดนตี"),
        DictionaryTerm("眩暈", "xuàn yūn", "มึนงง (Stun)", "การต่อสู้", "ศัตรูขยับไม่ได้และไม่สามารถใช้สกิลได้ชั่วขณะ"),
        DictionaryTerm("沉默", "chén mò", "ใบ้ (Silence)", "การต่อสู้", "ไม่สามารถใช้สกิลหรือเวทมนตร์ได้"),
        DictionaryTerm("流血", "liú xiě", "เลือดออก (Bleed)", "การต่อสู้", "ลดเลือดเรื่อยๆ ต่อเนื่อง (Damage over Time)"),
        DictionaryTerm("減益效果", "jiǎn yì xiào guǒ", "ดีบัฟฟ์ (Debuff)", "การต่อสู้", "ผลลบที่ลดสถานะหรือความสามารถ"),
        DictionaryTerm("增益效果", "zēng yì xiào guǒ", "บัฟฟ์ (Buff)", "การต่อสู้", "ผลบวกที่เสริมพลังหรือความสามารถ"),

        // Quests & Dungeons
        DictionaryTerm("主線任務", "zhǔ xiàn rèn wù", "เควสต์หลัก (Main Quest)", "เควสต์ & ดันเจี้ยน", "ภารกิจเนื้อเรื่องหลักเพื่อปลดล็อกฟีเจอร์"),
        DictionaryTerm("支線任務", "zhī xiàn rèn wù", "เควสต์รอง (Side Quest)", "เควสต์ & ดันเจี้ยน", "ภารกิจเสริมเพื่อเก็บ EXP และไอเทมพิเศษ"),
        DictionaryTerm("每日任務", "měi rì rèn wù", "เควสต์รายวัน (Daily Quest)", "เควสต์ & ดันเจี้ยน", "ภารกิจประจำวัน รีเซ็ตทุกเที่ยงคืนหรือเช้า"),
        DictionaryTerm("每週任務", "měi zhōu rèn wù", "เควสต์รายสัปดาห์ (Weekly Quest)", "เควสต์ & ดันเจี้ยน", "ภารกิจที่รีเซ็ตทุกสัปดาห์"),
        DictionaryTerm("副本", "fù běn", "ดันเจี้ยน / อินสแตนซ์ (Dungeon / Instance)", "เควสต์ & ดันเจี้ยน", "พื้นที่พิเศษสำหรับล่าบอสและฟาร์มของ"),
        DictionaryTerm("團隊副本", "tuán duì fù běn", "เรดดันเจี้ยน (Raid Dungeon)", "เควสต์ & ดันเจี้ยน", "ดันเจี้ยนระดับสูงที่ต้องเล่นเป็นทีมหลายคน"),
        DictionaryTerm("首領", "shǒu lǐng", "บอส (Boss)", "เควสต์ & ดันเจี้ยน", "ศัตรูตัวหลักของด่านที่มีพลังสูงมาก"),
        DictionaryTerm("獎勵", "jiǎng lì", "ของรางวัล (Rewards)", "เควสต์ & ดันเจี้ยน", "ไอเทม เงิน หรือ EXP ที่ได้รับเมื่อทำสำเร็จ"),
        DictionaryTerm("通關", "tōng guān", "ผ่านด่าน (Stage Clear)", "เควสต์ & ดันเจี้ยน", "เอาชนะด่านหรือภารกิจสำเร็จ"),
        DictionaryTerm("掃蕩", "sǎo dàng", "กวาดล้างด่วน (Sweep / Auto-Clear)", "เควสต์ & ดันเจี้ยน", "ผ่านด่านทันทีโดยไม่ต้องเล่นซ้ำ (ใช้ตั๋วข้าม)"),

        // Items, Gacha & Equipment
        DictionaryTerm("裝備", "zhuāng bèi", "อุปกรณ์สวมใส่ (Equipment)", "อุปกรณ์ & กาชา", "อาวุธ ชุดเกราะ เครื่องประดับ"),
        DictionaryTerm("強化", "qiáng huà", "ตีบวก / อัปเกรด (Enhance / Upgrade)", "อุปกรณ์ & กาชา", "เพิ่มระดับเลเวลและค่าสเตตัสของอุปกรณ์"),
        DictionaryTerm("鍛造", "duàn zào", "คราฟต์ / ตีเหล็ก (Craft / Forge)", "อุปกรณ์ & กาชา", "สร้างอุปกรณ์ใหม่จากวัตถุดิบ"),
        DictionaryTerm("精煉", "jīng liàn", "รีไฟน์ / หลอมกลั่น (Refine)", "อุปกรณ์ & กาชา", "เพิ่มพลังแฝงหรือระดับขั้นของอาวุธ"),
        DictionaryTerm("洗練", "xǐ liàn", "รีสเตตัส (Re-roll Stats)", "อุปกรณ์ & กาชา", "สุ่มคุณสมบัติพิเศษของอุปกรณ์ใหม่"),
        DictionaryTerm("抽卡", "chōu kǎ", "สุ่มกาชา (Gacha / Pull)", "อุปกรณ์ & กาชา", "เปิดสุ่มตัวละครหรืออาวุธ"),
        DictionaryTerm("十連抽", "shí lián chōu", "สุ่ม 10 ครั้งติด (10x Pull)", "อุปกรณ์ & กาชา", "เปิดกาชา 10 ใบพร้อมกัน มักได้การันตี"),
        DictionaryTerm("保底", "bǎo dǐ", "การันตี (Pity System)", "อุปกรณ์ & กาชา", "จำนวนครั้งที่การันตีว่าจะได้ระดับสูงสุดแน่นอน"),
        DictionaryTerm("傳奇", "chuán qí", "ระดับตำนาน (Legendary)", "อุปกรณ์ & กาชา", "ระดับความหายากสูงสุดสีส้มหรือทอง"),
        DictionaryTerm("史詩", "shǐ shī", "ระดับเอปิก (Epic)", "อุปกรณ์ & กาชา", "ระดับความหายากสีม่วง"),
        DictionaryTerm("稀有", "xī yǒu", "ระดับหายาก (Rare)", "อุปกรณ์ & กาชา", "ระดับความหายากสีฟ้า"),
        DictionaryTerm("體力", "tǐ lì", "พลังงาน / สตามินา (Stamina / Energy)", "อุปกรณ์ & กาชา", "แต้มที่ต้องใช้เพื่อลงเล่นด่าน"),

        // UI & Common Actions
        DictionaryTerm("確認", "què rèn", "ยืนยัน (Confirm)", "ปุ่ม & ระบบ", "กดยืนยันการกระทำ"),
        DictionaryTerm("取消", "qǔ xiāo", "ยกเลิก (Cancel)", "ปุ่ม & ระบบ", "กดยกเลิกหรือไม่ตกลง"),
        DictionaryTerm("領取", "lǐng qǔ", "กดรับ (Claim / Receive)", "ปุ่ม & ระบบ", "กดรับของรางวัลหรือจดหมาย"),
        DictionaryTerm("一鍵領取", "yī jiàn lǐng qǔ", "รับทั้งหมดในคลิกเดียว (Claim All)", "ปุ่ม & ระบบ", "รับของรางวัลทุกชิ้นพร้อมกัน"),
        DictionaryTerm("背包", "bēi bāo", "กระเป๋า / ช่องเก็บของ (Inventory / Bag)", "ปุ่ม & ระบบ", "หน้าดูไอเทมทั้งหมดที่เรามี"),
        DictionaryTerm("信箱", "xìn xiāng", "กล่องจดหมาย (Mailbox)", "ปุ่ม & ระบบ", "รับของแจก จดหมายประกาศ หรือของชดเชย"),
        DictionaryTerm("公會", "gōng huì", "กิลด์ (Guild / Clan)", "ปุ่ม & ระบบ", "สมาคมผู้เล่นสำหรับร่วมกิจกรรมทีม"),
        DictionaryTerm("好友", "hǎo yǒu", "เพื่อน (Friends)", "ปุ่ม & ระบบ", "รายชื่อเพื่อนและส่งหัวใจ/ของขวัญ"),
        DictionaryTerm("自動戰鬥", "zì dòng zhàn dòu", "ต่อสู้อัตโนมัติ (Auto Battle)", "ปุ่ม & ระบบ", "ให้ AI เล่นและออกสกิลให้เอง"),
        DictionaryTerm("倍速", "bèi sù", "เร่งความเร็ว (Speed Up 2x/3x)", "ปุ่ม & ระบบ", "ปรับความเร็วของการต่อสู้"),

        // Work & Multitasking Chat
        DictionaryTerm("會議", "huì yì", "การประชุม (Meeting)", "การทำงาน", "นัดหมายสนทนางาน"),
        DictionaryTerm("專案", "zhuān àn", "โปรเจกต์ (Project)", "การทำงาน", "โครงการหรือชิ้นงาน"),
        DictionaryTerm("進度", "jìn dù", "ความคืบหน้า (Progress)", "การทำงาน", "สถานะการดำเนินงาน"),
        DictionaryTerm("收到", "shōu dào", "รับทราบ / ได้รับแล้ว (Received / Noted)", "การทำงาน", "คำตอบรับข้อความหรือคำสั่งงาน"),
        DictionaryTerm("確認一下", "què rèn yī xià", "ช่วยตรวจสอบ / ยืนยันหน่อย (Please check/confirm)", "การทำงาน", "ขอให้อีกฝ่ายรีเช็กข้อมูล"),
        DictionaryTerm("急件", "jí jiàn", "งานด่วน (Urgent)", "การทำงาน", "เอกสารหรืองานที่ต้องทำทันที"),
        DictionaryTerm("檔案", "dàng àn", "ไฟล์เอกสาร (File / Document)", "การทำงาน", "ไฟล์งาน ข้อมูล"),
        DictionaryTerm("辛苦了", "xīn kǔ le", "ขอบคุณสำหรับความเหน็ดเหนื่อย (Good job / Well done)", "การทำงาน", "คำทักทายให้กำลังใจเพื่อนร่วมงานหลังเลิกงาน")
    )

    fun search(query: String): List<DictionaryTerm> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return terms
        return terms.filter {
            it.traditionalChinese.lowercase().contains(q) ||
            it.pinyin.lowercase().contains(q) ||
            it.thai.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }

    fun findQuickMatch(text: String): String? {
        val trimmed = text.trim()
        val exact = terms.find { it.traditionalChinese == trimmed }
        if (exact != null) {
            return "${exact.thai} (${exact.pinyin})"
        }
        val partial = terms.filter { trimmed.contains(it.traditionalChinese) }
        if (partial.isNotEmpty()) {
            return partial.joinToString("\n") { "• ${it.traditionalChinese}: ${it.thai}" }
        }
        return null
    }
}
