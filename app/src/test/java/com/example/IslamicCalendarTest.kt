package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.chrono.HijrahDate

class IslamicCalendarTest {
    @Test
    fun nextOccurrenceIncludesTodayAndMovesForwardAfterTheEvent() {
        val ramadanStart = IslamicCalendar.nextOccurrence(LocalDate.of(2026, 1, 1), 9, 1)
        assertEquals(9, HijrahDate.from(ramadanStart).get(ChronoField.MONTH_OF_YEAR))
        assertEquals(1, HijrahDate.from(ramadanStart).get(ChronoField.DAY_OF_MONTH))
        assertEquals(ramadanStart, IslamicCalendar.nextOccurrence(ramadanStart, 9, 1))
        val followingRamadan = IslamicCalendar.nextOccurrence(ramadanStart.plusDays(1), 9, 1)
        assertTrue(followingRamadan.isAfter(ramadanStart.plusDays(1)))
        assertTrue(ChronoUnit.DAYS.between(ramadanStart, followingRamadan) > 350)
    }

    @Test
    fun eventDatesStayCorrectAcrossDifferentReferenceDates() {
        val references = listOf(LocalDate.of(2025, 3, 1), LocalDate.of(2026, 6, 20), LocalDate.of(2027, 1, 1), LocalDate.of(2030, 12, 20))
        references.forEach { reference ->
            val events = IslamicCalendar.upcomingEvents(reference)
            assertEquals(events.size, events.map { it.gregorianDate }.distinct().size)
            events.forEach { event ->
                assertTrue("Past event returned: ${event.id}", !event.gregorianDate.isBefore(reference))
                val resolved = HijrahDate.from(event.gregorianDate)
                assertEquals(event.month, resolved.get(ChronoField.MONTH_OF_YEAR))
                assertEquals(event.day, resolved.get(ChronoField.DAY_OF_MONTH))
            }
        }
    }

    @Test
    fun ramadanEidAndArafahDatesAreChronologicallyConsistent() {
        val reference = LocalDate.of(2026, 1, 1)
        val ramadan = IslamicCalendar.nextOccurrence(reference, 9, 1)
        val eidFitr = IslamicCalendar.nextOccurrence(reference, 10, 1)
        val arafah = IslamicCalendar.nextOccurrence(reference, 12, 9)
        val eidAdha = IslamicCalendar.nextOccurrence(reference, 12, 10)
        assertTrue(ramadan.isBefore(eidFitr))
        assertTrue(eidFitr.isBefore(arafah))
        assertTrue(arafah.isBefore(eidAdha))
        assertTrue(IslamicCalendar.ramadanLength(HijrahDate.from(ramadan).get(ChronoField.YEAR_OF_ERA)) in 29..30)
    }

    @Test
    fun hijriDateAdjustmentChangesTheDisplayedDate() {
        val date = LocalDate.of(2026, 3, 1)
        val original = IslamicCalendar.hijriDate(date)
        val shifted = IslamicCalendar.hijriDate(date, 1)
        assertEquals(original.plus(1, ChronoUnit.DAYS), shifted)
    }
}
