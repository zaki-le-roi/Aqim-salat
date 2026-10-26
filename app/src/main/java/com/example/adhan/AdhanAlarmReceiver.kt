package com.example.adhan

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_RESTORE_RINGER, false)) {
            restoreRingerModeIfNeeded(context)
            return
        }
        if (intent.getBooleanExtra(EXTRA_SILENT_START, false)) {
            startTemporarySilentMode(context)
            return
        }
        if (intent.getBooleanExtra(EXTRA_REFRESH, false)) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try { AdhanScheduler.schedule(context.applicationContext) } finally { pending.finish() }
            }
            return
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        if (intent.getBooleanExtra(EXTRA_PRE_ALERT, false)) {
            showPrePrayerNotification(context, prayerName, intent.getStringExtra(EXTRA_PRAYER_KEY).orEmpty())
            return
        }
        val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
            putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AdhanPlaybackService.EXTRA_PRAYER_KEY, intent.getStringExtra(EXTRA_PRAYER_KEY) ?: "")
        }
        runCatching { ContextCompat.startForegroundService(context, serviceIntent) }
            .onFailure { showFallbackForService(context, prayerName) }
    }

    private fun showPrePrayerNotification(context: Context, prayerName: String, prayerKey: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channelId = "pre_prayer_alerts"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "التنبيه قبل الصلاة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تذكير للاستعداد للصلاة قبل دخول وقتها"
                enableVibration(true)
            })
        }
        val openApp = PendingIntent.getActivity(context, ("pre_" + prayerKey).hashCode(), Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(("pre_" + prayerKey + System.currentTimeMillis() / 86_400_000L).hashCode(),
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.example.R.drawable.ic_notification_adhan)
                .setContentTitle("اقترب وقت صلاة $prayerName")
                .setContentText("استعد للصلاة؛ بقيت دقائق على الأذان.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(openApp)
                .build()
        )
    }
    fun showFallbackForService(context: Context, prayerName: String, reason: String = "سبب غير معروف") {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = "adhan_playback_failure_silent"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            manager?.createNotificationChannel(
                NotificationChannel(channelId, "تعذّر تشغيل الأذان", NotificationManager.IMPORTANCE_LOW).apply {
                    setSound(null, null)
                    description = "إشعار صامت عند تعذّر تحميل ملف الأذان"
                    enableVibration(false)
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
            .setContentText("تعذّر تشغيل الأذان: " + reason.take(100))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        manager?.notify(prayerName.hashCode(), notification)
    }

    private fun startTemporarySilentMode(context: Context) {
        val audio = context.getSystemService(AudioManager::class.java) ?: return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            notificationManager?.isNotificationPolicyAccessGranted != true
        ) return

        val prefs = context.getSharedPreferences("auto_silent_runtime", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("active", false)) {
            prefs.edit().putInt("original_ringer_mode", audio.ringerMode).apply()
        }
        runCatching { audio.ringerMode = AudioManager.RINGER_MODE_SILENT }
        if (audio.ringerMode != AudioManager.RINGER_MODE_SILENT) {
            prefs.edit().putBoolean("active", false).apply()
            return
        }
        prefs.edit().putBoolean("active", true).apply()

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: run {
            restoreRingerModeIfNeeded(context)
            return
        }
        val restoreIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            putExtra(EXTRA_RESTORE_RINGER, true)
        }
        val pending = PendingIntent.getBroadcast(
            context, REQUEST_RINGER_RESTORE, restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val restoreAt = System.currentTimeMillis() + SILENT_DURATION_MS
        val scheduled = runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restoreAt, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restoreAt, pending)
            }
        }.isSuccess
        if (!scheduled) restoreRingerModeIfNeeded(context)
    }

    companion object {
        fun restoreRingerModeIfNeeded(context: Context) {
            val prefs = context.getSharedPreferences("auto_silent_runtime", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("active", false)) return
            val audio = context.getSystemService(AudioManager::class.java) ?: return
            if (audio.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                val originalMode = prefs.getInt("original_ringer_mode", AudioManager.RINGER_MODE_NORMAL)
                val restored = runCatching { audio.ringerMode = originalMode }.isSuccess
                if (!restored || audio.ringerMode != originalMode) return
            }
            // Do not override a mode the user changed manually while the temporary silence was active.
            prefs.edit().putBoolean("active", false).apply()
        }

        private const val REQUEST_RINGER_RESTORE = 0x8A20
        private const val SILENT_DURATION_MS = 10 * 60 * 1000L
        const val EXTRA_SILENT_START = "auto_silent_start"
        const val EXTRA_RESTORE_RINGER = "restore_ringer_mode"
        const val EXTRA_PRAYER_KEY = "prayer_key"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_DATE = "date"
        const val EXTRA_REFRESH = "refresh"
        const val EXTRA_PRE_ALERT = "pre_prayer_alert"
    }
}
