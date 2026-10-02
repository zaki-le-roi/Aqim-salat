package com.example

import com.example.data.PrayerCalculator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PrayerCalculatorTest {

    @Test
    fun algiersPrayerTimesAreOrdered() {
        val tz = TimeZone.getTimeZone("Africa/Algiers")
        val date = Calendar.getInstance(tz).apply {
            set(2026, Calendar.OCTOBER, 2, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val times = PrayerCalculator.calculateTimes(
            latitude = 36.7538,
            longitude = 3.0588,
            timezoneOffset = tz.getOffset(date.time) / 3600000.0,
            date = date,
            method = PrayerCalculator.CalculationMethod.ALGERIA,
            madhab = PrayerCalculator.Madhab.STANDARD
        )

        val values = listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha)
            .map { it.replace(":", "").toInt() }

        assertTrue("Unexpected order: $values", values.zipWithNext().all { (a, b) -> a < b })
        assertTrue(times.fajr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.isha.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun hanafiAsrIsLaterThanStandard() {
        val date = Calendar.getInstance(TimeZone.getTimeZone("Africa/Algiers")).apply {
            set(2026, Calendar.OCTOBER, 2, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val standard = PrayerCalculator.calculateTimes(
            36.7538, 3.0588, 1.0, date,
            PrayerCalculator.CalculationMethod.ALGERIA,
            PrayerCalculator.Madhab.STANDARD
        )
        val hanafi = PrayerCalculator.calculateTimes(
            36.7538, 3.0588, 1.0, date,
            PrayerCalculator.CalculationMethod.ALGERIA,
            PrayerCalculator.Madhab.HANAFI
        )

        assertTrue("Standard=${standard.asr}, Hanafi=${hanafi.asr}", hanafi.asr.replace(":", "").toInt() > standard.asr.replace(":", "").toInt())
    }
}
