package com.example

import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.chrono.HijrahDate

data class UpcomingIslamicEvent(
    val id: String,
    val title: String,
    val month: Int,
    val day: Int,
    val gregorianDate: LocalDate,
    val hijriLabel: String,
    val note: String
)

object IslamicCalendar {
    val monthNames = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private data class EventDefinition(
        val id: String, val title: String, val month: Int, val day: Int, val note: String
    )

    private val eventDefinitions = listOf(
        EventDefinition("ramadan", "بداية شهر رمضان", 9, 1, "بداية شهر الصيام؛ يتأكد الموعد برؤية الهلال والإعلان الرسمي."),
        EventDefinition("laylat_al_qadr", "ليلة القدر المتوقعة", 9, 27, "ليلة 27 تاريخ متداول وليست تحديدًا قطعيًا؛ تُلتمس ليلة القدر في العشر الأواخر."),
        EventDefinition("eid_fitr", "عيد الفطر", 10, 1, "أول شوال؛ يعتمد ثبوت العيد على إعلان رؤية الهلال."),
        EventDefinition("dhul_hijjah", "بداية العشر من ذي الحجة", 12, 1, "أيام فاضلة يشرع فيها الإكثار من العمل الصالح."),
        EventDefinition("arafah", "يوم عرفة", 12, 9, "لغير الحاج صيامه مستحب وله فضل عظيم."),
        EventDefinition("eid_adha", "عيد الأضحى", 12, 10, "يوم النحر؛ يتأكد موعده بالإعلان الرسمي."),
        EventDefinition("islamic_new_year", "رأس السنة الهجرية", 1, 1, "بداية شهر محرم من العام الهجري الجديد."),
        EventDefinition("ashura", "يوم عاشوراء", 1, 10, "يستحب صيامه، والأفضل صيام يوم قبله أو بعده.")
    )

    fun hijriDate(gregorianDate: LocalDate, adjustmentDays: Int = 0): HijrahDate =
        HijrahDate.from(gregorianDate.plusDays(adjustmentDays.toLong()))

    fun ramadanLength(hijriYear: Int): Int = HijrahDate.of(hijriYear, 9, 1).lengthOfMonth()

    fun nextOccurrence(referenceDate: LocalDate, hijriMonth: Int, hijriDay: Int): LocalDate {
        require(hijriMonth in 1..12) { "Hijri month must be between 1 and 12" }
        val currentHijri = HijrahDate.from(referenceDate)
        val currentYear = currentHijri.get(ChronoField.YEAR_OF_ERA)
        val candidate = LocalDate.from(HijrahDate.of(currentYear, hijriMonth, hijriDay))
        return if (candidate.isBefore(referenceDate)) {
            LocalDate.from(HijrahDate.of(currentYear + 1, hijriMonth, hijriDay))
        } else candidate
    }

    fun upcomingEvents(referenceDate: LocalDate): List<UpcomingIslamicEvent> =
        eventDefinitions.map { event ->
            val date = nextOccurrence(referenceDate, event.month, event.day)
            UpcomingIslamicEvent(
                id = event.id, title = event.title, month = event.month, day = event.day,
                gregorianDate = date,
                hijriLabel = "${event.day} ${monthNames[event.month - 1]} هـ",
                note = event.note
            )
        }.sortedBy { it.gregorianDate }
}
