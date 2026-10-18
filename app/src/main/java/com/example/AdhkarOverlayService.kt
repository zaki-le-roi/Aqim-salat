package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Calendar

class AdhkarOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: LinearLayout? = null
    private var adhkarText: TextView? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var currentIndex = 0
    private var currentItems: List<Adhkar> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var initialWindowX = 0
    private var initialWindowY = 0

    private data class Adhkar(val text: String, val source: String)

    private val generalAdhkar = listOf(
        Adhkar("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ", "صحيح البخاري 6406، صحيح مسلم 2694"),
        Adhkar("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "صحيح البخاري 6307"),
        Adhkar("لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "صحيح البخاري 3293، صحيح مسلم 2691"),
        Adhkar("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "صحيح البخاري 6384، صحيح مسلم 2704"),
        Adhkar("سُبْحَانَ اللَّهِ", "صحيح مسلم 2137: أحب الكلام إلى الله"),
        Adhkar("الْحَمْدُ لِلَّهِ", "صحيح مسلم 223: تملأ الميزان"),
        Adhkar("اللَّهُ أَكْبَرُ", "ذكر مشروع ثابت في الأحاديث الصحيحة"),
        Adhkar("اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى مُحَمَّدٍ", "أصل الصلاة على النبي ثابت في صحيح البخاري 3370")
    )

    private val morningAdhkar = listOf(
        Adhkar("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ", "صحيح مسلم 2723"),
        Adhkar("اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ", "سنن أبي داود 5068، وجامع الترمذي 3391")
    )

    private val eveningAdhkar = listOf(
        Adhkar("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا", "صحيح مسلم 2723"),
        Adhkar("اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ", "سنن أبي داود 5068، وجامع الترمذي 3391")
    )

    private val sleepAdhkar = listOf(
        Adhkar("بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "صحيح البخاري 6324"),
        Adhkar("اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ", "سنن أبي داود 5045، وجامع الترمذي 3398")
    )

    private val reappearRunnable = object : Runnable {
        override fun run() {
            val prefs = getSharedPreferences("floating_adhkar", MODE_PRIVATE)
            if (!prefs.getBoolean("enabled", false) || !Settings.canDrawOverlays(this@AdhkarOverlayService)) return
            showOverlay()
            scheduleRotation()
        }
    }

    private fun intervalMillis() =
        getSharedPreferences("floating_adhkar", MODE_PRIVATE)
            .getInt("interval_minutes", 30).coerceIn(5, 60) * 60_000L

    private val rotationRunnable = object : Runnable {
        override fun run() {
            val prefs = getSharedPreferences("floating_adhkar", MODE_PRIVATE)
            if (!prefs.getBoolean("enabled", false) || !prefs.getBoolean("auto_rotate", true) || overlay == null) return
            val updated = getAdhkarForCurrentTime()
            if (updated != currentItems) {
                currentItems = updated
                currentIndex = 0
            } else {
                currentIndex = (currentIndex + 1) % currentItems.size
            }
            renderCurrentAdhkar()
            // Align changes to clock boundaries: e.g. 10:00, 10:05, 10:10.
            val now = System.currentTimeMillis()
            val interval = prefs.getInt("interval_minutes", 30).coerceIn(5, 60) * 60_000L
            val delay = interval - (now % interval)
            handler.postDelayed(this, delay.coerceAtLeast(1_000L))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        startForeground(
            NOTIFICATION_ID,
            builder.setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("أقم صلاتك")
                .setContentText("الأذكار العائمة مفعّلة")
                .setOngoing(true)
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("floating_adhkar", MODE_PRIVATE)
        if (intent?.action == ACTION_STOP) {
            prefs.edit().putBoolean("enabled", false).commit()
            handler.removeCallbacks(rotationRunnable)
            handler.removeCallbacks(reappearRunnable)
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!prefs.getBoolean("enabled", false)) {
            handler.removeCallbacks(rotationRunnable)
            handler.removeCallbacks(reappearRunnable)
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            handler.removeCallbacks(rotationRunnable)
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_REFRESH) {
            removeOverlay()
        }
        handler.removeCallbacks(reappearRunnable)
        showOverlay()
        scheduleRotation()
        return START_STICKY
    }

    private fun getAdhkarForCurrentTime(): List<Adhkar> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..10 -> morningAdhkar + generalAdhkar
            in 16..19 -> eveningAdhkar + generalAdhkar
            in 20..23, in 0..3 -> sleepAdhkar + generalAdhkar
            else -> generalAdhkar
        }
    }

    private fun scheduleRotation() {
        handler.removeCallbacks(rotationRunnable)
        val prefs = getSharedPreferences("floating_adhkar", MODE_PRIVATE)
        if (prefs.getBoolean("auto_rotate", true)) {
            val interval = prefs.getInt("interval_minutes", 5).coerceIn(5, 60) * 60_000L
            val delay = interval - (System.currentTimeMillis() % interval)
            handler.postDelayed(rotationRunnable, delay.coerceAtLeast(1_000L))
        }
    }

    private fun renderCurrentAdhkar() {
        if (currentItems.isEmpty()) currentItems = getAdhkarForCurrentTime()
        val item = currentItems[currentIndex.coerceIn(0, currentItems.lastIndex)]
        adhkarText?.text = item.text
        overlay?.let { root ->
            val density = resources.displayMetrics.density
            val width = 270 * density
            windowParams?.width = width.toInt()
            windowParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
            try { windowManager?.updateViewLayout(root, windowParams) } catch (_: Exception) { }
        }
    }

    private fun showOverlay() {
        if (overlay != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density + 0.5f).toInt()
        val prefs = getSharedPreferences("floating_adhkar", MODE_PRIVATE)
        currentItems = getAdhkarForCurrentTime()
        currentIndex = currentIndex.coerceIn(0, currentItems.lastIndex)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(4), dp(10), dp(4))
            alpha = prefs.getFloat("opacity", 0.94f).coerceIn(0.55f, 1f)
            val scale = prefs.getFloat("scale", 1f).coerceIn(0.8f, 1.25f)
            scaleX = scale
            scaleY = scale
            background = GradientDrawable().apply {
                setColor(Color.rgb(250, 248, 241))
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.rgb(212, 175, 55))
            }
            elevation = dp(7).toFloat()
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        var bubbleDragging = false
        val bubble = TextView(this).apply {
            text = "💬"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(dp(4), 0, dp(4), 0)
        }
        header.addView(bubble, LinearLayout.LayoutParams(dp(30), dp(34)))
        adhkarText = TextView(this).apply {
            textSize = 13f
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.rgb(30, 48, 40))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(5), dp(2), dp(5), dp(2))
        }
        header.addView(adhkarText, LinearLayout.LayoutParams(0, dp(34), 1f))
        val close = TextView(this).apply {
            text = "×"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 100, 100))
            setOnClickListener {
                // X hides only this card. The feature remains enabled and returns after the chosen interval.
                handler.removeCallbacks(rotationRunnable)
                removeOverlay()
                handler.removeCallbacks(reappearRunnable)
                handler.postDelayed(reappearRunnable, intervalMillis())
            }
        }
        header.addView(close, LinearLayout.LayoutParams(dp(28), dp(34)))
        root.addView(header)

        renderCurrentAdhkar()

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val params = WindowManager.LayoutParams(
            dp(280),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = ((screenWidth - dp(280)) / 2).coerceAtLeast(0)
            y = dp(90)
        }
        windowParams = params

        bubble.setOnTouchListener { view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    bubbleDragging = false
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    initialWindowX = params.x
                    initialWindowY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchStartX).toInt()
                    val dy = (event.rawY - touchStartY).toInt()
                    if (kotlin.math.abs(dx) > dp(5) || kotlin.math.abs(dy) > dp(5)) bubbleDragging = true
                    if (bubbleDragging) {
                        params.x = (initialWindowX + dx).coerceIn(0, (screenWidth - dp(80)).coerceAtLeast(0))
                        params.y = (initialWindowY + dy).coerceIn(0, (screenHeight - dp(70)).coerceAtLeast(0))
                        try { windowManager?.updateViewLayout(root, params) } catch (_: Exception) { }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (bubbleDragging) {
                        view.performClick()
                    }
                    true
                }
                else -> true
            }
        }

        try {
            windowManager?.addView(root, params)
            overlay = root
        } catch (_: Exception) {
            removeOverlay()
            handler.removeCallbacks(rotationRunnable)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun removeOverlay() {
        val view = overlay
        if (view != null) {
            try { windowManager?.removeView(view) } catch (_: Exception) { }
        }
        overlay = null
        adhkarText = null
        windowParams = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "الأذكار العائمة", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "إشعار استمرار الأذكار العائمة"
                }
            )
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(rotationRunnable)
        handler.removeCallbacks(reappearRunnable)
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SHOW = "com.example.action.SHOW_ADHKAR_OVERLAY"
        const val ACTION_REFRESH = "com.example.action.REFRESH_ADHKAR_OVERLAY"
        const val ACTION_STOP = "com.example.action.STOP_ADHKAR_OVERLAY"
        private const val CHANNEL_ID = "floating_adhkar_channel"
        private const val NOTIFICATION_ID = 4107
    }
}
