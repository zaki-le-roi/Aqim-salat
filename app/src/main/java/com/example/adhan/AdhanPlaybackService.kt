package com.example.adhan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R

class AdhanPlaybackService : Service() {
    private var player: MediaPlayer? = null
    private var prayerName: String = "الصلاة"
    private var prayerKey: String = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        prayerKey = intent?.getStringExtra(EXTRA_PRAYER_KEY) ?: ""
        val channelId = "adhan_playback"

        createPlaybackChannel()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_adhan)
            .setContentTitle("أذان صلاة $prayerName")
            .setContentText("حان وقت الصلاة")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= 29) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else 0
        )

        playAthan()
        return START_NOT_STICKY
    }

    private fun createPlaybackChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    "adhan_playback",
                    "تشغيل الأذان",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "حالة تشغيل الأذان"
                }
            )
        }
    }

    private fun playAthan() {
        player?.release()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            setDataSource(if (prayerKey == "Fajr") FAJR_ATHAN_URL else ATHAN_URL)
            setOnPreparedListener { it.start() }
            setOnCompletionListener { stopSelf() }
            setOnErrorListener { _, _, _ ->
                showFallbackNotification()
                stopSelf()
                true
            }
            prepareAsync()
        }
    }

    private fun showFallbackNotification() {
        val receiver = AdhanAlarmReceiver()
        receiver.showFallbackForService(this, prayerName)
    }

    override fun onDestroy() {
        player?.runCatching { stop() }
        player?.release()
        player = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 9401
        // Verified tvQuran Adhan selection: Mishary Alafasi / Adhan and Takbir.
        private const val ATHAN_URL = "https://download.tvquran.com/download/TvQuran.com__Athan/TvQuran.com__04.athan.mp3"
        // Verified dedicated Fajr Adhan with Dua.
        private const val FAJR_ATHAN_URL = "https://download.tvquran.com/download/selections/180/58b0dac02106f.mp3"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_KEY = "prayer_key"
    }
}
