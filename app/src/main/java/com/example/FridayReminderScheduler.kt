package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object FridayReminderScheduler {
    private const val REQUEST_CODE = 7401
    private const val CHANNEL_ID = "friday_reminder"

    fun schedule(context: Context, zoneId: String) {
        val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
        context.getSharedPreferences("friday_reminder", Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", true).putString("zone_id", zone.id).apply()
        val now = ZonedDateTime.now(zone)
        var next = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .withHour(9).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusWeeks(1)
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), pendingIntent(context))
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context))
        context.getSharedPreferences("friday_reminder", Context.MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
    }

    internal fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences("friday_reminder", Context.MODE_PRIVATE)
        if (prefs.getBoolean("enabled", false)) {
            schedule(context, prefs.getString("zone_id", ZoneId.systemDefault().id) ?: ZoneId.systemDefault().id)
        }
    }

    private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
        context, REQUEST_CODE, Intent(context, FridayReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    internal fun channelId() = CHANNEL_ID
}

class FridayReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("friday_reminder", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel(FridayReminderScheduler.channelId(), "تذكير الجمعة", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val openApp = PendingIntent.getActivity(
            context, 7402, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, FridayReminderScheduler.channelId())
            .setSmallIcon(R.drawable.ic_notification_adhan)
            .setContentTitle("تذكير يوم الجمعة")
            .setContentText("لا تنسَ سنن الجمعة والإكثار من الصلاة على النبي محمد صلى الله عليه وسلم.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("تذكير بقراءة سورة الكهف، والاغتسال والتبكير إلى الصلاة، والإكثار من الصلاة على النبي محمد صلى الله عليه وسلم."))
            .setContentIntent(openApp).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        manager.notify(7401, notification)
        FridayReminderScheduler.scheduleNext(context)
    }
}
