package com.example

import com.example.data.PrayerCalculator
import com.example.data.PrayerTimeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

class CityTimeZoneTest {
 @Test fun worldCitiesUseSelectedZoneInsteadOfDeviceZone() {
  val cases=listOf(
   Triple("America/New_York",LocalDate.of(2026,1,15),-18000),
   Triple("Asia/Tokyo",LocalDate.of(2026,1,15),32400),
   Triple("Asia/Jakarta",LocalDate.of(2026,1,15),25200),
   Triple("Asia/Kolkata",LocalDate.of(2026,1,15),19800),
   Triple("Asia/Kathmandu",LocalDate.of(2026,1,15),20700),
   Triple("Australia/Sydney",LocalDate.of(2026,1,15),39600),
   Triple("Europe/Oslo",LocalDate.of(2026,1,15),3600)
  )
  val device=ZoneId.of("Africa/Algiers")
  cases.forEach{(id,date,expected)->
   val zone=ZoneId.of(id)
   assertNotEquals(device.id,zone.id)
   assertEquals(id,expected,zone.rules.getOffset(date.atTime(12,0).atZone(zone).toInstant()).totalSeconds)
   val times=PrayerCalculator.calculateTimes(35.0,10.0,date,zone)
   assertEquals(zone,times.fajrDateTime.zone)
   assertEquals(date,times.dhuhrDateTime.toLocalDate())
  }
 }
 @Test fun newYorkSpringDstOffsetChangesByDate() {
  val zone=ZoneId.of("America/New_York")
  val before=LocalDate.of(2026,3,7)
  val after=LocalDate.of(2026,3,8)
  assertEquals(-18000,zone.rules.getOffset(before.atTime(12,0).atZone(zone).toInstant()).totalSeconds)
  assertEquals(-14400,zone.rules.getOffset(after.atTime(12,0).atZone(zone).toInstant()).totalSeconds)
  val a=PrayerCalculator.calculateTimes(40.7128,-74.0060,before,zone)
  val b=PrayerCalculator.calculateTimes(40.7128,-74.0060,after,zone)
  assertEquals(before,a.fajrDateTime.toLocalDate())
  assertEquals(after,b.fajrDateTime.toLocalDate())
 }
 @Test fun instantCountdownIsIndependentOfPhoneWallClockZone() {
  val zone=ZoneId.of("Asia/Kathmandu")
  val target=LocalDate.of(2026,5,1).atTime(18,0).atZone(zone)
  assertEquals(90L,PrayerTimeEngine.secondsUntil(target.toInstant().minusSeconds(90),target))
 }
}
