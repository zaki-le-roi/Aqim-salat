package com.example.adhan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R

class AdhanPlaybackService : Service() {
    private var player: MediaPlayer? = null
    private var prayerName: String = "الصلاة"
    private var prayerKey: String = ""
    private var attempt = 0
    private var fallbackShown = false
    private var muadhinOverride: String? = null
    private var lastFailureReason: String = "سبب غير معروف"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        prayerKey = intent?.getStringExtra(EXTRA_PRAYER_KEY) ?: ""
        muadhinOverride = intent?.getStringExtra(EXTRA_MUADHIN_OVERRIDE)
        Log.e(TAG, "start prayer=$prayerName key=$prayerKey muadhin=" + selectedMuadhinCode() + " name=" + selectedMuadhinName() + " override=" + (muadhinOverride ?: "none"))
        attempt = 0
        fallbackShown = false
        createPlaybackChannel()

        val notification = NotificationCompat.Builder(this, "adhan_playback")
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
        playSelectedLocal()
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
                ).apply { description = "حالة تشغيل الأذان" }
            )
        }
    }

    private fun selectedResourceId(): Int {
        val prefs = getSharedPreferences("audio_preferences", Context.MODE_PRIVATE)
        val selected = selectedMuadhinCode()
        return when (selected) {
            "MADINAH" -> R.raw.adhan_madinah
            "ALAFASY" -> R.raw.adhan_alafasy
            "FAJR_SPECIAL" -> if (prayerKey == "Fajr") R.raw.adhan_fajr else R.raw.adhan_makkah
            else -> R.raw.adhan_makkah
        }
    }

    private fun selectedMuadhinCode(): String {
        if (muadhinOverride != null) return muadhinOverride!!
        val prefs = getSharedPreferences("audio_preferences", Context.MODE_PRIVATE)
        return prefs.getString("muadhin_${prayerKey}", prefs.getString("default_muadhin", "MAKKAH")) ?: "MAKKAH"
    }

    private fun selectedMuadhinName(): String = when (selectedMuadhinCode()) {
        "MADINAH" -> "أذان المدينة المنورة"
        "ALAFASY" -> "أذان مشاري العفاسي"
        "FAJR_SPECIAL" -> "أذان الفجر الخاص"
        else -> "أذان مكة المكرمة"
    }

    private fun selectedExternalUrl(): String {
        val selected = selectedMuadhinCode()
        return when (selected) {
            "MADINAH" -> MADINAH_ATHAN_URL
            "ALAFASY" -> ALAFASY_ATHAN_URL
            "FAJR_SPECIAL" -> if (prayerKey == "Fajr") FAJR_ATHAN_URL else MAKKAH_ATHAN_URL
            else -> MAKKAH_ATHAN_URL
        }
    }

    private fun playSelectedLocal() {
        startPlayback(attempt = 0, resourceId = selectedResourceId())
    }

    private fun startPlayback(attempt: Int, resourceId: Int? = null, externalUrl: String? = null) {
        releasePlayer()
        this.attempt = attempt
        try {
            val next = MediaPlayer()
            player = next
            next.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            if (resourceId != null) {
                next.setDataSource(this, Uri.parse("android.resource://$packageName/$resourceId"))
            } else {
                next.setDataSource(externalUrl ?: selectedExternalUrl())
            }
            next.setOnPreparedListener { mp ->
                if (player === mp) mp.start() else mp.release()
            }
            next.setOnCompletionListener { mp ->
                if (player === mp) {
                    releasePlayer()
                    stopSelf()
                }
            }
            next.setOnErrorListener { mp, what, extra ->
                lastFailureReason = "MediaPlayer what=$what extra=$extra"
                Log.e(TAG, "MediaPlayer onError what=$what extra=$extra attempt=" + this.attempt + " muadhin=" + selectedMuadhinCode() + " resourceId=" + (resourceId?.toString() ?: "none") + " url=" + (externalUrl ?: "local"))
                if (player === mp) {
                    releasePlayer()
                    when (this.attempt) {
                        0 -> startPlayback(attempt = 1, resourceId = R.raw.adhan_makkah)
                        1 -> startPlayback(attempt = 2, externalUrl = selectedExternalUrl())
                        else -> {
                            showFallbackOnce()
                            stopSelf()
                        }
                    }
                } else {
                    mp.release()
                }
                true
            }
            next.prepareAsync()
        } catch (e: Exception) {
            lastFailureReason = (e.message ?: e.javaClass.simpleName).take(140)
            Log.e(TAG, "playback exception attempt=$attempt muadhin=" + selectedMuadhinCode() + " resourceId=" + (resourceId?.toString() ?: "none") + " reason=" + lastFailureReason, e)
            releasePlayer()
            when (attempt) {
                0 -> startPlayback(attempt = 1, resourceId = R.raw.adhan_makkah)
                1 -> startPlayback(attempt = 2, externalUrl = selectedExternalUrl())
                else -> {
                    showFallbackOnce()
                    stopSelf()
                }
            }
        }
    }

    private fun releasePlayer() {
        val old = player
        player = null
        if (old != null) {
            runCatching { if (old.isPlaying) old.stop() }
            runCatching { old.release() }
        }
    }

    private fun showFallbackOnce() {
        if (fallbackShown) return
        fallbackShown = true
        AdhanAlarmReceiver().showFallbackForService(this, prayerName, lastFailureReason)
    }

    override fun onDestroy() {
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 9401
        private const val MAKKAH_ATHAN_URL = "https://www.islamcan.com/audio/adhan/azan1.mp3"
        private const val MADINAH_ATHAN_URL = "https://www.islamcan.com/audio/adhan/azan2.mp3"
        private const val ALAFASY_ATHAN_URL = "https://www.islamcan.com/audio/adhan/azan8.mp3"
        private const val FAJR_ATHAN_URL = "https://download.tvquran.com/download/selections/180/58b0dac02106f.mp3"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_KEY = "prayer_key"
        const val EXTRA_MUADHIN_OVERRIDE = "muadhin_override"
        private const val TAG = "AdhanDiag"
    }
}
