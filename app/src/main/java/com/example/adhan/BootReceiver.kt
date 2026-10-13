package com.example.adhan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AdhanAlarmReceiver.restoreRingerModeIfNeeded(context.applicationContext)
                AdhanScheduler.schedule(context.applicationContext)
                runCatching { com.example.widget.PrayerTimesWidgetProvider.requestUpdate(context.applicationContext) }
            } finally { pending.finish() }
        }
    }
}
