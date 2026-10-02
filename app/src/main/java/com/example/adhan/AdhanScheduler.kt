package com.example.adhan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppRepository
import com.example.data.AppDatabase
import com.example.data.PrayerCalculator
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AdhanScheduler {
    // Alarms are rebuilt after boot, timezone changes, permission changes, and settings updates.
    private const val REQUEST_REFRESH = 0x7A11

    private val prayers = listOf(
        "Fajr" to "الفجر",
        "Dhuhr" to "الظهر",
        "Asr" to "العصر",
        "Maghrib" to "المغرب",
        "Isha" to "العشاء"
    )

    suspend fun schedule(context: Context) {
        val appContext = context.applicationContext
        val repository = AppRepository(AppDatabase.getDatabase(appContext), appContext)
        val enabled = repository.notificationsEnabled.first()
        if (!enabled) {
            cancel(context)
            return
        }

        val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        cancel(context)
        val latitude = repository.appLatitude.first()
        val longitude = repository.appLongitude.first()
        val method = runCatching {
            PrayerCalculator.CalculationMethod.valueOf(repository.appCalcMethod.first())
        }.getOrDefault(PrayerCalculator.CalculationMethod.MWL)
        val madhab = runCatching {
            PrayerCalculator.Madhab.valueOf(repository.appMadhab.first())
        }.getOrDefault(PrayerCalculator.Madhab.STANDARD)

        val zone = Calendar.getInstance().timeZone
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = zone }

        for (dayOffset in 0..1) {
            val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            val date = day.time
            val times = PrayerCalculator.calculateTimes(
                latitude, longitude, zone.getOffset(date.time) / 3600000.0, date, method, madhab
            )
            val values = listOf(
                "Fajr" to times.fajr,
                "Dhuhr" to times.dhuhr,
                "Asr" to times.asr,
                "Maghrib" to times.maghrib,
                "Isha" to times.isha
            )
            values.forEach { (key, time) ->
                val parts = time.split(":")
                if (parts.size != 2) return@forEach
                val trigger = Calendar.getInstance().apply {
                    set(Calendar.YEAR, day.get(Calendar.YEAR))
                    set(Calendar.MONTH, day.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, day.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: return@apply)
                    set(Calendar.MINUTE, parts[1].toIntOrNull() ?: return@apply)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                if (trigger <= System.currentTimeMillis()) return@forEach

                val requestCode = (dateFormat.format(date) + key).hashCode()
                val intent = Intent(appContext, AdhanAlarmReceiver::class.java).apply {
                    putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_KEY, key)
                    putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, prayers.first { it.first == key }.second)
                    putExtra(AdhanAlarmReceiver.EXTRA_DATE, dateFormat.format(date))
                }
                val pending = PendingIntent.getBroadcast(
                    appContext, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (exactAllowed) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
                }
            }
        }

        val refresh = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val refreshIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            putExtra(AdhanAlarmReceiver.EXTRA_REFRESH, true)
        }
        val refreshPending = PendingIntent.getBroadcast(
            appContext, REQUEST_REFRESH, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (exactAllowed) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, refresh, refreshPending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, refresh, refreshPending)
        }
    }

    fun cancel(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(AlarmManager::class.java) ?: return
        prayers.forEach { (key, _) ->
            for (dayOffset in 0..1) {
                val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(day.time)
                val requestCode = (date + key).hashCode()
                val intent = Intent(appContext, AdhanAlarmReceiver::class.java)
                val pending = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pending?.let { alarmManager.cancel(it); it.cancel() }
            }
        }
        val refreshIntent = Intent(context, AdhanAlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context, REQUEST_REFRESH, refreshIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { alarmManager.cancel(it); it.cancel() }
    }
}
