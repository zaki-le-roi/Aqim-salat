package com.example.data
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Shared city-local time engine. */
object PrayerTimeEngine {
 fun zoneIdOrDevice(id:String?):ZoneId = runCatching { id?.takeIf{it.isNotBlank()}?.let(ZoneId::of) }.getOrNull() ?: ZoneId.systemDefault()
 fun now(zone:ZoneId):ZonedDateTime = Instant.now().atZone(zone)
 fun localDate(zone:ZoneId, instant:Instant=Instant.now()):LocalDate = instant.atZone(zone).toLocalDate()
 fun offsetHours(date:LocalDate, zone:ZoneId):Double {
  val noon=date.atTime(12,0).atZone(zone)
  return zone.rules.getOffset(noon.toInstant()).totalSeconds/3600.0
 }
 fun atLocalMinute(date:LocalDate, minute:Long, zone:ZoneId):ZonedDateTime = date.atStartOfDay().plusMinutes(minute).atZone(zone)
 fun format(time:ZonedDateTime):String=time.format(DateTimeFormatter.ofPattern("HH:mm"))
 fun millis(time:ZonedDateTime):Long=time.toInstant().toEpochMilli()
 fun secondsUntil(now:Instant,target:ZonedDateTime):Long=ChronoUnit.SECONDS.between(now,target.toInstant()).coerceAtLeast(0)
}