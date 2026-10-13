package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.PrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/** Home-screen widget showing today's prayer times from the same calculation settings as the app. */
class PrayerTimesWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val repository = AppRepository(AppDatabase.getDatabase(appContext), appContext)
                val locationConfigured = repository.appLocationConfigured.first()
                val latitude = repository.appLatitude.first()
                val longitude = repository.appLongitude.first()
                val method = runCatching {
                    PrayerCalculator.CalculationMethod.valueOf(repository.appCalcMethod.first())
                }.getOrDefault(PrayerCalculator.CalculationMethod.ALGERIA)
                val madhab = runCatching {
                    PrayerCalculator.Madhab.valueOf(repository.appMadhab.first())
                }.getOrDefault(PrayerCalculator.Madhab.STANDARD)

                val now = Calendar.getInstance()
                val times = if (locationConfigured) {
                    runCatching {
                        PrayerCalculator.calculateTimes(
                            latitude = latitude,
                            longitude = longitude,
                            timezoneOffset = now.timeZone.getOffset(now.timeInMillis) / 3_600_000.0,
                            date = now.time,
                            method = method,
                            madhab = madhab
                        )
                    }.getOrNull()
                } else null

                val views = RemoteViews(appContext.packageName, R.layout.widget_prayer_times)
                if (times == null) {
                    views.setTextViewText(R.id.widget_title, "مواقيت الصلاة")
                    views.setTextViewText(R.id.widget_line_one, "افتح التطبيق لتحديد موقعك")
                    views.setTextViewText(R.id.widget_line_two, "ثم حدّث مواقيت الصلاة")
                } else {
                    views.setTextViewText(R.id.widget_title, "مواقيت الصلاة اليوم")
                    views.setTextViewText(
                        R.id.widget_line_one,
                        "الفجر ${times.fajr}  •  الظهر ${times.dhuhr}  •  العصر ${times.asr}"
                    )
                    views.setTextViewText(
                        R.id.widget_line_two,
                        "المغرب ${times.maghrib}  •  العشاء ${times.isha}"
                    )
                }

                val openApp = PendingIntent.getActivity(
                    appContext,
                    8701,
                    Intent(appContext, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openApp)
                appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
            } catch (_: Exception) {
                val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
                views.setTextViewText(R.id.widget_title, "مواقيت الصلاة")
                views.setTextViewText(R.id.widget_line_one, "تعذر تحديث المواقيت")
                views.setTextViewText(R.id.widget_line_two, "اضغط لفتح التطبيق")
                val openApp = PendingIntent.getActivity(
                    context, 8701, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openApp)
                appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun requestUpdate(context: Context) {
            val intent = Intent(context, PrayerTimesWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, PrayerTimesWidgetProvider::class.java))
                )
            }
            context.sendBroadcast(intent)
        }
    }
}
