package com.example.data

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.*

object PrayerCalculator {

    enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double, val useIshaInterval: Boolean, val ishaIntervalMins: Int) {
        MWL(18.0, 17.0, false, 0),
        ISNA(15.0, 15.0, false, 0),
        EGYPT(19.5, 17.5, false, 0),
        MAKKAH(18.5, 0.0, true, 90),
        UMM_AL_QURA(18.5, 0.0, true, 90),
        KARACHI(18.0, 18.0, false, 0),
        TEHRAN(17.7, 14.0, false, 0),
        GULF(19.5, 0.0, true, 90),
        ALGERIA(18.0, 17.0, false, 0),
        TURKEY(18.0, 17.0, false, 0),
        AWQAF(18.0, 17.0, false, 0)
    }

    enum class Madhab(val shadowFactor: Int) {
        STANDARD(1), // Shafi'i, Maliki, Hanbali
        HANAFI(2)
    }

    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val imsak: String,
        val sunset: String,
        val midnight: String,
        val lastThird: String
    )

    private fun dtr(d: Double): Double = d * PI / 180.0
    private fun rtd(r: Double): Double = r * 180.0 / PI

    private fun fixHour(h: Double): Double {
        var hours = h
        while (hours < 0) hours += 24.0
        while (hours >= 24) hours -= 24.0
        return hours
    }

    private fun fixAngle(a: Double): Double {
        var angle = a
        while (angle < 0) angle += 360.0
        while (angle >= 360) angle -= 360.0
        return angle
    }

    // Calculate Julian Date
    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    fun calculateTimes(
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double, // in hours
        date: Date,
        method: CalculationMethod = CalculationMethod.MWL,
        madhab: Madhab = Madhab.STANDARD
    ): PrayerTimes {
        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val jd = getJulianDate(year, month, day) - longitude / (360.0 * 24.0)

        // Solar Declination & Equation of Time
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(dtr(g)) + 0.020 * sin(dtr(2 * g)))

        val r = 1.00014 - 0.01671 * cos(dtr(g)) - 0.00014 * cos(dtr(2 * g))
        val e = 23.439 - 0.00000036 * d

        val ra = rtd(atan2(cos(dtr(e)) * sin(dtr(l)), cos(dtr(l)))) / 15.0
        val rightAscension = fixHour(ra)

        val declination = rtd(asin(sin(dtr(e)) * sin(dtr(l))))
        val eqTime = (q / 15.0) - rightAscension

        // Mid-Day (Dhuhr)
        val midDay = fixHour(12.0 + timezoneOffset - longitude / 15.0 - eqTime)

        // Hour Angle helper
        fun hourAngle(angle: Double, sign: Int): Double {
            val f = (sin(dtr(angle)) - sin(dtr(latitude)) * sin(dtr(declination))) / (cos(dtr(latitude)) * cos(dtr(declination)))
            if (f < -1.0 || f > 1.0) return Double.NaN
            val h = rtd(acos(f)) / 15.0
            return if (sign == -1) midDay - h else midDay + h
        }

        // Sunrise and Sunset (refraction angle = -0.833)
        var sunriseHour = hourAngle(-0.833, -1)
        var sunsetHour = hourAngle(-0.833, 1)

        // Fajr
        var fajrHour = hourAngle(-method.fajrAngle, -1)

        // Isha
        var ishaHour = if (method.useIshaInterval) {
            sunsetHour + (method.ishaIntervalMins / 60.0)
        } else {
            hourAngle(-method.ishaAngle, 1)
        }

        // Asr Calculation (shadow angle)
        val gAsr = acot(madhab.shadowFactor.toDouble() + tan(dtr(abs(latitude - declination))))
        val asrAngle = -rtd(gAsr)
        var asrHour = hourAngle(asrAngle, 1)

        // High-latitude fallback: use an angle-based portion of the night.
        if (sunriseHour.isNaN()) sunriseHour = midDay - 6.0
        if (sunsetHour.isNaN()) sunsetHour = midDay + 6.0
        val estimatedFajr = sunriseHour - 1.5
        val nextFajr = fajrHour.takeUnless { it.isNaN() } ?: estimatedFajr
        val nightDurationToNextFajr = (24.0 - sunsetHour) + nextFajr
        val nightPortion = { angle: Double -> (angle / 60.0).coerceIn(0.0, 0.5) }
        if (fajrHour.isNaN()) fajrHour = sunriseHour - nightDurationToNextFajr * nightPortion(method.fajrAngle)
        if (ishaHour.isNaN()) ishaHour = sunsetHour + nightDurationToNextFajr * nightPortion(if (method.useIshaInterval) 18.0 else method.ishaAngle)
        if (asrHour.isNaN()) asrHour = midDay + 3.0

        // Calculate Midnight & Last Third of Night
        // Night begins at Sunset (sunsetHour) and ends at tomorrow's Fajr (fajrHour + 24.0)
        val nightDuration = (fajrHour + 24.0) - sunsetHour
        val midnightHour = sunsetHour + (nightDuration / 2.0)
        val lastThirdHour = sunsetHour + (nightDuration * 2.0 / 3.0)

        // Format
        fun formatTime(hour: Double): String {
            val h = fixHour(hour)
            val totalMinutes = kotlin.math.round(h * 60.0).toInt()
            val formattedHour = (totalMinutes / 60) % 24
            val mins = totalMinutes % 60
            return String.format("%02d:%02d", formattedHour, mins)
        }

        return PrayerTimes(
            fajr = formatTime(fajrHour),
            sunrise = formatTime(sunriseHour),
            dhuhr = formatTime(midDay),
            asr = formatTime(asrHour),
            maghrib = formatTime(sunsetHour), // Maghrib starts exactly at sunset
            isha = formatTime(ishaHour),
            imsak = formatTime(fajrHour - 10.0 / 60.0), // Imsak is 10 minutes before Fajr
            sunset = formatTime(sunsetHour),
            midnight = formatTime(midnightHour),
            lastThird = formatTime(lastThirdHour)
        )
    }

    private fun acot(x: Double): Double = atan(1.0 / x)

    // Moon phase calculation: returns a value from 0.0 (New Moon) to 1.0 (back to New Moon), 0.5 is Full Moon
    fun calculateMoonPhase(date: Date): Double {
        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val jd = getJulianDate(year, month, day)
        // Known reference: New moon on Jan 6, 2000 (JD 2451549.5)
        // Synodic Month (New Moon to New Moon) average: 29.530588853 days
        val diff = jd - 2451549.5
        val cycles = diff / 29.530588853
        val phase = cycles - floor(cycles)
        return phase
    }

    fun getMoonPhaseName(phase: Double, lang: String): String {
        return when (lang) {
            "ar" -> when {
                phase < 0.03 -> "محاق"
                phase < 0.22 -> "هلال متزايد"
                phase < 0.28 -> "تربيع أول"
                phase < 0.47 -> "أحدب متزايد"
                phase < 0.53 -> "بدر كامل"
                phase < 0.72 -> "أحدب متناقص"
                phase < 0.78 -> "تربيع أخير"
                else -> "هلال متناقص"
            }
            "fr" -> when {
                phase < 0.03 -> "Nouvelle Lune"
                phase < 0.22 -> "Premier Croissant"
                phase < 0.28 -> "Premier Quartier"
                phase < 0.47 -> "Gibbeuse Croissante"
                phase < 0.53 -> "Pleine Lune"
                phase < 0.72 -> "Gibbeuse Décroissante"
                phase < 0.78 -> "Dernier Quartier"
                else -> "Dernier Croissant"
            }
            "tr" -> when {
                phase < 0.03 -> "Yeni Ay"
                phase < 0.22 -> "Hilal"
                phase < 0.28 -> "İlk Dördün"
                phase < 0.47 -> "Şişkin Ay"
                phase < 0.53 -> "Dolunay"
                phase < 0.72 -> "Şişkin Ay (Azalan)"
                phase < 0.78 -> "Son Dördün"
                else -> "Hilal (Azalan)"
            }
            else -> when { // "en", default
                phase < 0.03 -> "New Moon"
                phase < 0.22 -> "Waxing Crescent"
                phase < 0.28 -> "First Quarter"
                phase < 0.47 -> "Waxing Gibbous"
                phase < 0.53 -> "Full Moon"
                phase < 0.72 -> "Waning Gibbous"
                phase < 0.78 -> "Last Quarter"
                else -> "Waning Crescent"
            }
        }
    }

    // Hijri date mathematical calculation using the tabular Islamic calendar (Umm Al-Qura approximate or standard 30-year cycle)
    data class HijriDate(val year: Int, val month: Int, val day: Int, val monthName: String)

    fun getHijriDate(date: Date, lang: String): HijriDate {
        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val jd = getJulianDate(year, month, day)
        // Convert Julian Date to Hijri tabular calendar
        val l = jd - 1948439.5 + 10632.0
        val n = floor((l - 1.0) / 10631.0).toInt()
        val lOffset = l - 10631.0 * n - 1.0
        val z = floor((lOffset + 354.0) / 354.36667).toInt()
        val zCal = if (z > 30) 30 else z
        val j = lOffset - floor(354.36667 * zCal) + 1

        val hYear = 30 * n + zCal
        var hMonth = ceil(j / 29.5).toInt()
        if (hMonth > 12) hMonth = 12
        val hDay = (j - floor(29.5 * (hMonth - 1))).toInt()

        val hMonthsAr = listOf(
            "محرّم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
        )
        val hMonthsEn = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
            "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        val hMonthsFr = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
            "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )

        val monthName = when (lang) {
            "ar" -> hMonthsAr.getOrNull(hMonth - 1) ?: "رمضان"
            "fr" -> hMonthsFr.getOrNull(hMonth - 1) ?: "Ramadan"
            else -> hMonthsEn.getOrNull(hMonth - 1) ?: "Ramadan"
        }

        return HijriDate(hYear, hMonth, hDay, monthName)
    }
}
