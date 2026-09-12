package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.GameDictionary
import com.example.data.GeminiTranslationService
import com.example.data.TranslationMode
import com.example.data.TranslationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FloatingGameTranslateService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private lateinit var repository: TranslationRepository

    // Floating Views
    private var floatingBubbleView: View? = null
    private var floatingHudView: View? = null
    private var floatingScanBoxView: View? = null
    private var floatingQuickBannerView: View? = null

    // Layout Params
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var hudParams: WindowManager.LayoutParams
    private lateinit var scanBoxParams: WindowManager.LayoutParams
    private lateinit var quickBannerParams: WindowManager.LayoutParams

    private var isHudOpen = false
    private var isScanBoxOpen = false

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!repository.autoClipboard.value) return@OnPrimaryClipChangedListener
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()?.trim()
                if (!text.isNullOrEmpty() && containsChineseCharacters(text)) {
                    showQuickFloatingBanner(text)
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = TranslationRepository.getInstance(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundServiceWithNotification()

        if (Settings.canDrawOverlays(this)) {
            initFloatingBubble()
            initFloatingHud()
            initFloatingScanBox()
            initFloatingQuickBanner()
            repository.setOverlayActive(true)

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.addPrimaryClipChangedListener(clipboardListener)
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TRANSLATE_TEXT -> {
                val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
                if (text.isNotEmpty()) {
                    showQuickFloatingBanner(text)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        repository.setOverlayActive(false)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.removePrimaryClipChangedListener(clipboardListener)

        removeViewSafely(floatingBubbleView)
        removeViewSafely(floatingHudView)
        removeViewSafely(floatingScanBoxView)
        removeViewSafely(floatingQuickBannerView)
        isRunning = false
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "game_translate_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ระบบแปลเกมไต้หวันเรียลไทม์",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "แสดงปุ่มลอยสำหรับแปลเกมและทำงานแบบเรียลไทม์"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FloatingGameTranslateService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🎮 ปุ่มแปลเกมไต้หวันกำลังทำงาน")
            .setContentText("แตะเพื่อเปิดแอพ หรือแตะปุ่มลอยบนหน้าจอเกมเพื่อแปลสด")
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_app_icon, "ปิดปุ่มลอย", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        isRunning = true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingBubble() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            dpToPx(58),
            dpToPx(58),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 300
        }

        val bubbleContainer = FrameLayout(this).apply {
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(Color.parseColor("#00F0FF"), Color.parseColor("#7928CA"))
                orientation = GradientDrawable.Orientation.TL_BR
                setStroke(dpToPx(2), Color.WHITE)
            }
            background = bgDrawable
            elevation = dpToPx(8).toFloat()

            val iv = ImageView(context).apply {
                setImageResource(R.drawable.ic_app_icon)
                layoutParams = FrameLayout.LayoutParams(dpToPx(40), dpToPx(40)).apply {
                    gravity = Gravity.CENTER
                }
            }
            addView(iv)
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        bubbleContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams.x
                    initialY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isMoving = true
                    }
                    bubbleParams.x = initialX + dx
                    bubbleParams.y = initialY + dy
                    windowManager.updateViewLayout(bubbleContainer, bubbleParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoving) {
                        toggleHud()
                    }
                    true
                }
                else -> false
            }
        }

        floatingBubbleView = bubbleContainer
        windowManager.addView(bubbleContainer, bubbleParams)
    }

    private fun initFloatingHud() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        hudParams = WindowManager.LayoutParams(
            dpToPx(340),
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 150
        }
    }

    private fun toggleHud() {
        if (isHudOpen) {
            hideHud()
        } else {
            showHud()
        }
    }

    private fun showHud() {
        if (isHudOpen) return
        val hud = createHudLayout()
        floatingHudView = hud
        try {
            windowManager.addView(hud, hudParams)
            isHudOpen = true
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun hideHud() {
        if (!isHudOpen) return
        removeViewSafely(floatingHudView)
        floatingHudView = null
        isHudOpen = false
    }

    private fun createHudLayout(): View {
        val context = this
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(16).toFloat()
                setColor(Color.parseColor("#181B26"))
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = bg
            setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
            elevation = dpToPx(12).toFloat()
        }

        // Header Row
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(context).apply {
            text = "⚡ แปลเกม & ทำงานสด (ไต้หวัน ➔ ไทย)"
            setTextColor(Color.parseColor("#00F0FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            paint.isFakeBoldText = true
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val closeBtn = Button(context).apply {
            text = "✕"
            setTextColor(Color.parseColor("#94A3B8"))
            setBackgroundColor(Color.TRANSPARENT)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setOnClickListener { hideHud() }
        }
        header.addView(title)
        header.addView(closeBtn)
        root.addView(header)

        // Input Field
        val input = EditText(context).apply {
            hint = "วางข้อความจีนไต้หวัน (繁體中文)..."
            setHintTextColor(Color.parseColor("#64748B"))
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            val inputBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#0F172A"))
                setStroke(dpToPx(1), Color.parseColor("#1E293B"))
            }
            background = inputBg
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
                bottomMargin = dpToPx(8)
            }
        }
        root.addView(input)

        // Action Buttons Row
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val pasteTranslateBtn = Button(context).apply {
            text = "📋 วางคลิปบอร์ด"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val btnBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#334155"))
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(38), 1f).apply {
                marginEnd = dpToPx(6)
            }
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                if (text.isNotEmpty()) {
                    input.setText(text)
                }
            }
        }

        val toggleScanBtn = Button(context).apply {
            text = if (isScanBoxOpen) "ปิดกรอบสแกน" else "🎯 กรอบสแกน"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val btnBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#475569"))
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(38), 1f).apply {
                marginEnd = dpToPx(6)
            }
            setOnClickListener {
                toggleScanBox()
                text = if (isScanBoxOpen) "ปิดกรอบสแกน" else "🎯 กรอบสแกน"
            }
        }

        val translateBtn = Button(context).apply {
            text = "⚡ แปลทันที"
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            paint.isFakeBoldText = true
            val btnBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#00F0FF"))
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(38), 1.2f)
        }

        actionsRow.addView(pasteTranslateBtn)
        actionsRow.addView(toggleScanBtn)
        actionsRow.addView(translateBtn)
        root.addView(actionsRow)

        // Loading bar
        val progressBar = ProgressBar(context).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(24)
            ).apply {
                topMargin = dpToPx(6)
            }
        }
        root.addView(progressBar)

        // Result Scroll Container
        val resultScrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(130)
            ).apply {
                topMargin = dpToPx(8)
            }
        }
        val resultText = TextView(context).apply {
            text = "ผลลัพธ์คำแปลจะแสดงที่นี่..."
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            val resBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#0F172A"))
                setStroke(dpToPx(1), Color.parseColor("#1E293B"))
            }
            background = resBg
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
        }
        resultScrollView.addView(resultText)
        root.addView(resultScrollView)

        translateBtn.setOnClickListener {
            val query = input.text.toString().trim()
            if (query.isEmpty()) return@setOnClickListener

            progressBar.visibility = View.VISIBLE
            resultText.text = "กำลังแปลภาษาจีนไต้หวัน..."
            resultText.setTextColor(Color.parseColor("#00F0FF"))

            serviceScope.launch {
                val result = GeminiTranslationService.translate(query, repository.selectedMode.value)
                val translated = result.getOrDefault("ไม่สามารถแปลได้ในขณะนี้")
                progressBar.visibility = View.GONE
                resultText.text = translated
                resultText.setTextColor(Color.parseColor("#F8FAFC"))
                repository.addTranslation(query, translated)
            }
        }

        return root
    }

    private fun initFloatingScanBox() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        scanBoxParams = WindowManager.LayoutParams(
            dpToPx(320),
            dpToPx(160),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 100
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun toggleScanBox() {
        if (isScanBoxOpen) {
            removeViewSafely(floatingScanBoxView)
            floatingScanBoxView = null
            isScanBoxOpen = false
        } else {
            val scanBox = createScanBoxLayout()
            floatingScanBoxView = scanBox
            try {
                windowManager.addView(scanBox, scanBoxParams)
                isScanBoxOpen = true
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createScanBoxLayout(): View {
        val context = this
        val frame = FrameLayout(context).apply {
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#2200F0FF"))
                setStroke(dpToPx(2), Color.parseColor("#00F0FF"), dpToPx(6).toFloat(), dpToPx(4).toFloat())
            }
            background = bg
        }

        val tagView = TextView(context).apply {
            text = "🎯 กรอบสแกนข้อความบทสนทนาเกม / เอกสาร"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            val tagBg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor("#CC0F172A"))
            }
            background = tagBg
            setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                setMargins(dpToPx(6), dpToPx(6), 0, 0)
            }
        }
        frame.addView(tagView)

        val quickAction = Button(context).apply {
            text = "⚡ แปลบทสนทนาจุดนี้"
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            paint.isFakeBoldText = true
            val btnBg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor("#00F0FF"))
            }
            background = btnBg
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(32)
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, dpToPx(8), dpToPx(8))
            }
            setOnClickListener {
                // In full device mode, trigger OCR/simulation on active dialog
                showQuickFloatingBanner("勇者啊，請前往幽暗森林擊敗首領！")
            }
        }
        frame.addView(quickAction)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        frame.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = scanBoxParams.x
                    initialY = scanBoxParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    scanBoxParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    scanBoxParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(frame, scanBoxParams)
                    true
                }
                else -> false
            }
        }

        return frame
    }

    private fun initFloatingQuickBanner() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        quickBannerParams = WindowManager.LayoutParams(
            dpToPx(340),
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 70
        }
    }

    private fun showQuickFloatingBanner(chineseText: String) {
        serviceScope.launch {
            removeViewSafely(floatingQuickBannerView)

            val context = this@FloatingGameTranslateService
            val banner = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(14).toFloat()
                    setColor(Color.parseColor("#F20F172A"))
                    setStroke(dpToPx(1.5f), Color.parseColor("#00F0FF"))
                }
                background = bg
                setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10))
                elevation = dpToPx(14).toFloat()
            }

            val header = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val title = TextView(context).apply {
                text = "⚡ แปลเกมสด (Live Subtitle)"
                setTextColor(Color.parseColor("#00F0FF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                paint.isFakeBoldText = true
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val close = TextView(context).apply {
                text = "✕"
                setTextColor(Color.parseColor("#94A3B8"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setOnClickListener { removeViewSafely(floatingQuickBannerView) }
            }
            header.addView(title)
            header.addView(close)
            banner.addView(header)

            val originalTv = TextView(context).apply {
                text = "🇨🇳 $chineseText"
                setTextColor(Color.parseColor("#CBD5E1"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(0, dpToPx(4), 0, dpToPx(2))
            }
            banner.addView(originalTv)

            val thaiTv = TextView(context).apply {
                text = "🇹🇭 กำลังแปล..."
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                paint.isFakeBoldText = true
            }
            banner.addView(thaiTv)

            floatingQuickBannerView = banner
            try {
                windowManager.addView(banner, quickBannerParams)
            } catch (e: Exception) {
                // Ignore
            }

            val result = GeminiTranslationService.translate(chineseText, repository.selectedMode.value)
            val translated = result.getOrDefault("ไม่สามารถแปลได้")
            thaiTv.text = "🇹🇭 $translated"
            repository.addTranslation(chineseText, translated)

            // Auto dismiss after 7 seconds
            withContext(Dispatchers.IO) {
                Thread.sleep(7000)
            }
            removeViewSafely(banner)
        }
    }

    private fun removeViewSafely(view: View?) {
        if (view != null && view.isAttachedToWindow) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun containsChineseCharacters(text: String): Boolean {
        for (char in text) {
            val ub = Character.UnicodeBlock.of(char)
            if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            ) {
                return true
            }
        }
        return false
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_TRANSLATE_TEXT = "com.example.service.ACTION_TRANSLATE_TEXT"
        const val EXTRA_TEXT = "extra_text"

        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, FloatingGameTranslateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingGameTranslateService::class.java)
            context.stopService(intent)
        }
    }
}
