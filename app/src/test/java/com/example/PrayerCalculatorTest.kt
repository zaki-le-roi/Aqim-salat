package com.example

import com.example.data.PrayerCalculator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PrayerCalculatorTest {
    private fun algiersDate(): java.util.Date =
        Calendar.getInstance(TimeZone.getTimeZone("Africa/Algiers")).apply {
            set(2026, Calendar.OCTOBER, 2, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

    private fun minutes(value: String): Int =
        value.substring(0, 2).toInt() * 60 + value.substring(3, 5).toInt()

    @Test
    fun algiersPrayerTimesAreOrdered() {
        val times = PrayerCalculator.calculateTimes(
            latitude = 36.7538,
            longitude = 3.0588,
            timezoneOffset = 1.0,
            date = algiersDate(),
            method = PrayerCalculator.CalculationMethod.ALGERIA,
            madhab = PrayerCalculator.Madhab.STANDARD
        )
        val values = listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha).map(::minutes)
        assertTrue("Prayer times are not chronological: " + times, values.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun hanafiAsrIsLaterThanStandard() {
        val date = algiersDate()
        val standard = PrayerCalculator.calculateTimes(36.7538, 3.0588, 1.0, date, PrayerCalculator.CalculationMethod.ALGERIA, PrayerCalculator.Madhab.STANDARD)
        val hanafi = PrayerCalculator.calculateTimes(36.7538, 3.0588, 1.0, date, PrayerCalculator.CalculationMethod.ALGERIA, PrayerCalculator.Madhab.HANAFI)
        assertTrue("Hanafi Asr=" + hanafi.asr + ", Standard Asr=" + standard.asr, minutes(hanafi.asr) > minutes(standard.asr))
    }

    @Test
    fun calculationProducesValidClockTimes() {
        val times = PrayerCalculator.calculateTimes(36.7538, 3.0588, 1.0, algiersDate(), PrayerCalculator.CalculationMethod.ALGERIA, PrayerCalculator.Madhab.STANDARD)
        listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha)
            .forEach { assertTrue("Invalid time: " + it, it.matches(Regex("\\d{2}:\\d{2}"))) }
    }
}
