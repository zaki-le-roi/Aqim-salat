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
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class AdhkarOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: LinearLayout? = null
    private var adhkarText: TextView? = null
    private var currentIndex = 0

    private val adhkar = listOf(
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
        "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
        "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ"
    )

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
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            getSharedPreferences("floating_adhkar", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
            stopSelf()
            return START_NOT_STICKY
        }
        showOverlay()
        return START_STICKY
    }

    private fun showOverlay() {
        if (overlay != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density + 0.5f).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
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
        header.addView(TextView(this).apply {
            text = "ذِكْرٌ"
            textSize = 12f
            setTextColor(Color.rgb(15, 76, 58))
            typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(0, dp(28), 1f))
        header.addView(TextView(this).apply {
            text = "×"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 100, 100))
            setOnClickListener {
                getSharedPreferences("floating_adhkar", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
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
            text = adhkar[currentIndex]
            setPadding(dp(2), dp(5), dp(2), dp(10))
        }
        root.addView(adhkarText, LinearLayout.LayoutParams(dp(270), LinearLayout.LayoutParams.WRAP_CONTENT))

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
                currentIndex = (currentIndex + 1) % adhkar.size
                adhkarText?.text = adhkar[currentIndex]
            }
        }
        root.addView(next, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)))

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            dp(300),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(90)
        }

        try {
            windowManager?.addView(root, params)
            overlay = root
        } catch (_: Exception) {
            removeOverlay()
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
        windowManager = null
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
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SHOW = "com.example.action.SHOW_ADHKAR_OVERLAY"
        const val ACTION_STOP = "com.example.action.STOP_ADHKAR_OVERLAY"
        private const val CHANNEL_ID = "floating_adhkar_channel"
        private const val NOTIFICATION_ID = 4107
    }
}
