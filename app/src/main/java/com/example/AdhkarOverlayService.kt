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
    private var sourceText: TextView? = null
    private var currentIndex = 0
    private var currentItems: List<Adhkar> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var initialWindowX = 0
    private var initialWindowY = 0

    private data class Adhkar(val text: String, val source: String)

    private val generalAdhkar = listOf(
        Adhkar("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ", "صحيح البخاري 6406، وصحيح مسلم 2694"),
        Adhkar("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "صحيح البخاري 6307"),
        Adhkar("لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "صحيح البخاري 3293، وصحيح مسلم 2691"),
        Adhkar("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "صحيح البخاري 6384، وصحيح مسلم 2704")
    )

    private val morningAdhkar = listOf(
        Adhkar("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ", "صحيح مسلم 2723"),
        Adhkar("اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ", "سنن أبي داود 5068، وجامع الترمذي 3391")
    )

    private val eveningAdhkar = listOf(
        Adhkar("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا", "صحيح مسلم 2723"),
        Adhkar("اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ", "سنن أبي داود 5068، وجامع الترمذي 3391")
    )

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
            val minutes = prefs.getInt("interval_minutes", 10).coerceIn(5, 60)
            handler.postDelayed(this, minutes * 60_000L)
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
        if (intent?.action == ACTION_STOP) {
            getSharedPreferences("floating_adhkar", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
            handler.removeCallbacks(rotationRunnable)
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            getSharedPreferences("floating_adhkar", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
            handler.removeCallbacks(rotationRunnable)
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_REFRESH) removeOverlay()
        showOverlay()
        scheduleRotation()
        return START_STICKY
    }

    private fun getAdhkarForCurrentTime(): List<Adhkar> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..10 -> morningAdhkar + generalAdhkar
            in 16..19 -> eveningAdhkar + generalAdhkar
            else -> generalAdhkar
        }
    }

    private fun scheduleRotation() {
        handler.removeCallbacks(rotationRunnable)
        val prefs = getSharedPreferences("floating_adhkar", MODE_PRIVATE)
        if (prefs.getBoolean("auto_rotate", true)) {
            handler.postDelayed(rotationRunnable, prefs.getInt("interval_minutes", 10).coerceIn(5, 60) * 60_000L)
        }
    }

    private fun renderCurrentAdhkar() {
        if (currentItems.isEmpty()) currentItems = getAdhkarForCurrentTime()
        val item = currentItems[currentIndex.coerceIn(0, currentItems.lastIndex)]
        adhkarText?.text = item.text
        sourceText?.text = "المصدر: ${item.source}"
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
            setPadding(dp(14), dp(10), dp(14), dp(10))
            alpha = prefs.getFloat("opacity", 0.94f).coerceIn(0.55f, 1f)
            val scale = prefs.getFloat("scale", 1f).coerceIn(0.8f, 1.25f)
            scaleX = scale
            scaleY = scale
            background = GradientDrawable().apply {
                setColor(Color.rgb(250, 248, 241))
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), Color.rgb(212, 175, 55))
            }
            elevation = dp(8).toFloat()
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "ذِكْرٌ • اسحب لنقل البطاقة"
            textSize = 12f
            setTextColor(Color.rgb(15, 76, 58))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(title, LinearLayout.LayoutParams(0, dp(28), 1f))
        header.addView(TextView(this).apply {
            text = "×"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 100, 100))
            setOnClickListener {
                getSharedPreferences("floating_adhkar", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
                handler.removeCallbacks(rotationRunnable)
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }, LinearLayout.LayoutParams(dp(32), dp(32)))
        root.addView(header)

        adhkarText = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(30, 48, 40))
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setLineSpacing(dp(3).toFloat(), 1.0f)
            setPadding(dp(2), dp(5), dp(2), dp(6))
        }
        root.addView(adhkarText, LinearLayout.LayoutParams(dp(270), LinearLayout.LayoutParams.WRAP_CONTENT))
        sourceText = TextView(this).apply {
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(88, 100, 91))
            setPadding(dp(2), dp(0), dp(2), dp(8))
        }
        root.addView(sourceText, LinearLayout.LayoutParams(dp(270), LinearLayout.LayoutParams.WRAP_CONTENT))
        renderCurrentAdhkar()

        val next = TextView(this).apply {
            text = "الذكر التالي  ←"
            textSize = 12f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.rgb(15, 76, 58))
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnClickListener {
                currentItems = getAdhkarForCurrentTime()
                currentIndex = (currentIndex + 1) % currentItems.size
                renderCurrentAdhkar()
            }
        }
        root.addView(next, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)))

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val params = WindowManager.LayoutParams(
            dp(300),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = ((screenWidth - dp(300)) / 2).coerceAtLeast(0)
            y = dp(90)
        }

        title.setOnTouchListener { view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    initialWindowX = params.x
                    initialWindowY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (initialWindowX + (event.rawX - touchStartX).toInt()).coerceIn(0, (screenWidth - dp(100)).coerceAtLeast(0))
                    params.y = (initialWindowY + (event.rawY - touchStartY).toInt()).coerceIn(0, (screenHeight - dp(80)).coerceAtLeast(0))
                    try { windowManager?.updateViewLayout(root, params) } catch (_: Exception) { }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    true
                }
                else -> false
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
        sourceText = null
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
