package com.example.adhan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.PrayerCalculator
import com.example.data.PrayerTimeEngine
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AdhanScheduler {
 private const val REQUEST_REFRESH=0x7A11
 private val prayers=listOf("Fajr" to "الفجر","Dhuhr" to "الظهر","Asr" to "العصر","Maghrib" to "المغرب","Isha" to "العشاء")
 private val dateFormat=DateTimeFormatter.ISO_LOCAL_DATE
 suspend fun schedule(context:Context) {
  val app=context.applicationContext
  val repo=AppRepository(AppDatabase.getDatabase(app),app)
  val enabled=repo.notificationsEnabled.first()
  val pre=repo.prePrayerAlertEnabled.first()
  val preMinutes=repo.prePrayerAlertMinutes.first().coerceIn(10,15)
  val silent=repo.autoSilentEnabled.first()
  if(!enabled&&!pre&&!silent){cancel(app);return}
  val manager=app.getSystemService(AlarmManager::class.java)?:return
  val exact=Build.VERSION.SDK_INT<Build.VERSION_CODES.S||manager.canScheduleExactAlarms()
  cancel(app)
  val lat=repo.appLatitude.first(); val lng=repo.appLongitude.first()
  if(!repo.appLocationConfigured.first()&&lat==36.7538&&lng==3.0588)return
  val zone=PrayerTimeEngine.zoneIdOrDevice(repo.appTimeZoneId.first())
  val today=PrayerTimeEngine.localDate(zone)
  val method=runCatching{PrayerCalculator.CalculationMethod.valueOf(repo.appCalcMethod.first())}.getOrDefault(PrayerCalculator.CalculationMethod.ALGERIA)
  val madhab=runCatching{PrayerCalculator.Madhab.valueOf(repo.appMadhab.first())}.getOrDefault(PrayerCalculator.Madhab.STANDARD)
  val now=Instant.now().toEpochMilli()
  for(offset in 0..2){
   val date=today.plusDays(offset.toLong())
   val t=PrayerCalculator.calculateTimes(lat,lng,date,zone,method,madhab)
   val items=listOf("Fajr" to t.fajrDateTime,"Dhuhr" to t.dhuhrDateTime,"Asr" to t.asrDateTime,"Maghrib" to t.maghribDateTime,"Isha" to t.ishaDateTime)
   items.forEach{(key,zoned)->
    val trigger=zoned.toInstant().toEpochMilli()
    if(trigger<=now)return@forEach
    val code=(date.format(dateFormat)+key).hashCode()
    val name=prayers.first{it.first==key}.second
    val alarmIntent=Intent(app,AdhanAlarmReceiver::class.java).apply{
     putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_KEY,key);putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME,name);putExtra(AdhanAlarmReceiver.EXTRA_DATE,date.format(dateFormat))
    }
    val alarm=PendingIntent.getBroadcast(app,code,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    if(enabled)setAlarm(manager,exact,trigger,alarm) else manager.cancel(alarm)
    if(silent){
     val si=Intent(app,AdhanAlarmReceiver::class.java).apply{putExtra(AdhanAlarmReceiver.EXTRA_SILENT_START,true);putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_KEY,key);putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME,name);putExtra(AdhanAlarmReceiver.EXTRA_DATE,date.format(dateFormat))}
     setAlarm(manager,exact,trigger,PendingIntent.getBroadcast(app,code xor 0x5A71,si,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }
    if(pre){
     val at=trigger-preMinutes*60_000L
     if(at>now){
      val pi=Intent(app,AdhanAlarmReceiver::class.java).apply{putExtra(AdhanAlarmReceiver.EXTRA_PRE_ALERT,true);putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_KEY,key);putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME,name);putExtra(AdhanAlarmReceiver.EXTRA_DATE,date.format(dateFormat))}
      setAlarm(manager,exact,at,PendingIntent.getBroadcast(app,code xor 0x35A7,pi,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
     }
    }
   }
  }
  val refresh=PrayerTimeEngine.atLocalMinute(today.plusDays(1),5,zone).toInstant().toEpochMilli()
  val ri=Intent(app,AdhanAlarmReceiver::class.java).apply{putExtra(AdhanAlarmReceiver.EXTRA_REFRESH,true)}
  setAlarm(manager,exact,refresh,PendingIntent.getBroadcast(app,REQUEST_REFRESH,ri,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
 }
 private fun setAlarm(m:AlarmManager,exact:Boolean,at:Long,pi:PendingIntent){
  if(exact)m.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi) else m.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi)
 }
 fun cancel(context:Context){
  val app=context.applicationContext; val manager=app.getSystemService(AlarmManager::class.java)?:return
  // Remove alarms created by both the previous device-local implementation and city-local implementation.
  val start=LocalDate.now().minusDays(3)
  for(i in 0..9){val d=start.plusDays(i.toLong()).format(dateFormat);prayers.forEach{(key,_)->
   val code=(d+key).hashCode()
   listOf(0,0x35A7,0x5A71).forEach{suffix->
    val intent=Intent(app,AdhanAlarmReceiver::class.java)
    if(suffix==0x35A7)intent.putExtra(AdhanAlarmReceiver.EXTRA_PRE_ALERT,true)
    if(suffix==0x5A71)intent.putExtra(AdhanAlarmReceiver.EXTRA_SILENT_START,true)
    PendingIntent.getBroadcast(app,code xor suffix,intent,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)?.let{manager.cancel(it);it.cancel()}
   }
  }}
  val refresh=Intent(app,AdhanAlarmReceiver::class.java)
  PendingIntent.getBroadcast(app,REQUEST_REFRESH,refresh,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)?.let{manager.cancel(it);it.cancel()}
 }
}