package com.example

import com.example.data.PrayerCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class PrayerCalculatorTest {

    @Test
    fun calculationProducesValidClockTimes() {
        val times = PrayerCalculator.calculateTimes(
            latitude = 36.7538,
            longitude = 3.0588,
            timezoneOffset = 1.0,
            date = Date(),
            method = PrayerCalculator.CalculationMethod.ALGERIA,
            madhab = PrayerCalculator.Madhab.STANDARD
        )

        listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha)
            .forEach { assertTrue("Invalid time: $it", it.matches(Regex("\\d{2}:\\d{2}"))) }
    }

    @Test
    fun supportedMadhabsUseDistinctShadowFactors() {
        assertEquals(1, PrayerCalculator.Madhab.STANDARD.shadowFactor)
        assertEquals(2, PrayerCalculator.Madhab.HANAFI.shadowFactor)
    }

    @Test
    fun moonPhaseAlwaysStaysWithinCycle() {
        val phase = PrayerCalculator.calculateMoonPhase(Date())
        assertTrue(phase >= 0.0 && phase < 1.0)
    }
}
