package com.example

import com.example.ui.SkyPeriod
import com.example.ui.skyPeriodFor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertTrue

class PrayerSkyPeriodTest {
    private val fajr = 5 * 3600
    private val sunrise = 6 * 3600
    private val dhuhr = 12 * 3600
    private val asr = 15 * 3600
    private val maghrib = 18 * 3600
    private val isha = 20 * 3600

    private fun period(time: Int) = skyPeriodFor(time, fajr, sunrise, dhuhr, asr, maghrib, isha)

    @Test fun justBeforeFajrIsNight() = assertEquals(SkyPeriod.NIGHT, period(fajr - 1))
    @Test fun atFajrIsDawn() = assertEquals(SkyPeriod.DAWN, period(fajr))
    @Test fun atSunriseIsMorning() = assertEquals(SkyPeriod.MORNING, period(sunrise))
    @Test fun atDhuhrIsMidday() = assertEquals(SkyPeriod.MIDDAY, period(dhuhr))
    @Test fun atAsrIsAfternoon() = assertEquals(SkyPeriod.AFTERNOON, period(asr))
    @Test fun atMaghribIsSunset() = assertEquals(SkyPeriod.SUNSET, period(maghrib))
    @Test fun atIshaIsNight() = assertEquals(SkyPeriod.NIGHT, period(isha))
    @Test fun midnightIsNight() = assertEquals(SkyPeriod.NIGHT, period(0))

    private fun relativeLuminance(rgb: Int): Double {
        fun channel(shift: Int): Double {
            val value = ((rgb shr shift) and 0xFF) / 255.0
            return if (value <= 0.04045) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    private fun contrastRatio(foreground: Int, background: Int): Double {
        val a = relativeLuminance(foreground)
        val b = relativeLuminance(background)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }

    @Test fun neutralCreamTextContrastMeetsWcagAa() {
        assertTrue(contrastRatio(0xFF1A3D2E.toInt(), 0xFFF1EBDD.toInt()) >= 4.5)
    }

    @Test fun selectedGreenTextContrastMeetsWcagAa() {
        assertTrue(contrastRatio(0xFF1A3D2E.toInt(), 0xFFDCEBE2.toInt()) >= 4.5)
    }
}
