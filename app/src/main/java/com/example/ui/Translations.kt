package com.example.ui

object Translations {

    private val translations = mapOf(
        "ar" to mapOf(
            "slogan" to "أجب المنادي. وحافظ على الصلاة.",
            "home" to "الرئيسية",
            "quran" to "القرآن",
            "hadith" to "الحديث",
            "tasbih" to "التسبيح",
            "adhkar" to "الأذكار",
            "settings" to "الإعدادات",
            "names_of_allah" to "أسماء الله",
            "prayer_times" to "مواقيت الصلاة",
            "next_prayer" to "الصلاة القادمة",
            "remaining" to "المتبقي",
            "current_prayer" to "الصلاة الحالية",
            "hijri_date" to "التاريخ الهجري",
            "calculation_method" to "طريقة الحساب",
            "madhab" to "المذهب",
            "gps_location" to "موقع GPS",
            "manual_location" to "تحديد يدوي",
            "search_surah" to "ابحث عن سورة...",
            "search_hadith" to "ابحث في الأحاديث...",
            "search_dhikr" to "ابحث في الأذكار...",
            "bookmarks" to "المحفوظات",
            "fajr" to "الفجر",
            "sunrise" to "الشروق",
            "dhuhr" to "الظهر",
            "asr" to "العصر",
            "maghrib" to "المغرب",
            "isha" to "العشاء",
            "imsak" to "الإمساك",
            "midnight" to "قيام الليل (منتصف الليل)",
            "lastThird" to "ثلث الليل الأخير (التهجد)",
            "tasbih_goal" to "الهدف",
            "tasbih_count" to "العدد",
            "tasbih_reset" to "إعادة تعيين",
            "add_counter" to "إضافة مسبحة",
            "name" to "الاسم",
            "save" to "حفظ",
            "cancel" to "إلغاء",
            "streak" to "الالتزام",
            "play_athan" to "تشغيل الأذان",
            "stop_athan" to "إيقاف",
            "prayer_tracker" to "سجل الصلوات",
            "log_prompt" to "تسجيل حالة صلاتك:",
            "prayed_on_time" to "صليت في وقتها",
            "prayed_late" to "صليت قضاءً",
            "missed" to "فاتتني",
            "not_yet" to "لم يحن بعد",
            "ramadan_countdown" to "تنازلي لرمضان",
            "days" to "أيام",
            "fasting_tracker" to "سجل الصيام",
            "qibla" to "بوصلة القبلة",
            "qibla_desc" to "وجه هاتفك باتجاه الكعبة المشرفة",
            "kaaba_distance" to "المسافة إلى الكعبة",
            "tools" to "أدوات إسلامية",
            "daily_inspiration" to "آية اليوم",
            "daily_hadith" to "حديث اليوم",
            "daily_dua" to "دعاء اليوم",
            "reminders" to "تنبيهات الأذان",
            "athan_voice" to "صوت الأذان",
            "snooze" to "غفوة المنبه",
            "language" to "لغة التطبيق",
            "ramadan" to "سجل رمضان",
            "fast_today" to "تم صيام اليوم",
            "charity_log" to "تم تسجيل الصدقة",
            "qiyam_log" to "تم تسجيل القيام",
            "streak_days" to "أيام متتالية",
            "continue_reading" to "مواصلة القراءة",
            "bookmark_saved" to "تم الحفظ",
            "bookmark_removed" to "تم إزالة الحفظ"
        )
    )

    fun get(key: String, lang: String): String {
        return translations["ar"]?.get(key) ?: key
    }

    fun isRtl(lang: String): Boolean {
        return true
    }
}
