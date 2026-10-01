package com.example.adhan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_REFRESH, false)) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try { AdhanScheduler.schedule(context.applicationContext) } finally { pending.finish() }
            }
            return
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
            putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayerName)
        }
        runCatching { ContextCompat.startForegroundService(context, serviceIntent) }
            .onFailure { showFallbackForService(context, prayerName) }
    }

    fun showFallbackForService(context: Context, prayerName: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = "adhan"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            manager?.createNotificationChannel(
                NotificationChannel(channelId, "الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(sound, attrs)
                    description = "تنبيهات مواقيت الصلاة"
                    enableVibration(true)
                }
            )
        }

        val openApp = PendingIntent.getActivity(
            context, prayerName.hashCode(), Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.drawable.ic_notification_adhan)
            .setContentTitle("حان وقت صلاة $prayerName")
            .setContentText("تعذر تشغيل ملف الأذان، وتم تشغيل تنبيه بديل.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        manager?.notify(prayerName.hashCode(), notification)
    }

    companion object {
        const val EXTRA_PRAYER_KEY = "prayer_key"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_DATE = "date"
        const val EXTRA_REFRESH = "refresh"
    }
}
