package com.example.ui

object Translations {
    private val ar = mapOf(
        "slogan" to "أجب المنادي، وحافظ على الصلاة",
        "home" to "الرئيسية", "quran" to "القرآن", "hadith" to "الحديث",
        "tasbih" to "السبحة", "adhkar" to "الأذكار", "settings" to "الإعدادات",
        "names_of_allah" to "أسماء الله الحسنى", "prayer_times" to "مواقيت الصلاة",
        "next_prayer" to "الصلاة القادمة", "remaining" to "المتبقي",
        "current_prayer" to "الصلاة الحالية", "hijri_date" to "التاريخ الهجري",
        "calculation_method" to "طريقة حساب المواقيت", "madhab" to "المذهب",
        "gps_location" to "موقع الجهاز", "manual_location" to "تحديد الموقع يدويًا",
        "search_surah" to "ابحث عن سورة...", "search_hadith" to "ابحث في الأحاديث...",
        "search_dhikr" to "ابحث في الأذكار...", "bookmarks" to "المحفوظات",
        "fajr" to "الفجر", "sunrise" to "الشروق", "dhuhr" to "الظهر",
        "asr" to "العصر", "maghrib" to "المغرب", "isha" to "العشاء",
        "imsak" to "الإمساك", "midnight" to "منتصف الليل", "lastThird" to "ثلث الليل الأخير",
        "tasbih_goal" to "الهدف", "tasbih_count" to "العدد", "tasbih_reset" to "إعادة التعيين",
        "add_counter" to "إضافة سبحة", "name" to "الاسم", "save" to "حفظ", "cancel" to "إلغاء",
        "streak" to "الاستمرارية", "play_athan" to "تشغيل الأذان", "stop_athan" to "إيقاف الأذان",
        "prayer_tracker" to "سجل الصلوات", "log_prompt" to "سجّل حالة صلاتك",
        "prayed_on_time" to "صليت في وقتها", "prayed_late" to "صليت قضاءً", "missed" to "فاتتني",
        "not_yet" to "لم يحن وقتها", "ramadan_countdown" to "العد التنازلي لرمضان", "days" to "أيام",
        "fasting_tracker" to "سجل الصيام", "qibla" to "القبلة", "qibla_desc" to "وجّه الهاتف نحو الكعبة المشرفة",
        "kaaba_distance" to "المسافة إلى الكعبة", "tools" to "الخدمات الإسلامية",
        "daily_inspiration" to "آية اليوم", "daily_hadith" to "حديث اليوم", "daily_dua" to "دعاء اليوم",
        "reminders" to "تنبيهات الأذان", "athan_voice" to "صوت الأذان", "snooze" to "غفوة التنبيه",
        "language" to "لغة التطبيق", "arabic" to "العربية",
        "theme" to "سمة التطبيق", "light" to "فاتح", "dark" to "داكن", "auto" to "تلقائي",
        "ramadan" to "رمضان", "continue_reading" to "مواصلة القراءة",
        "bookmark_saved" to "تم الحفظ", "bookmark_removed" to "تمت إزالة الحفظ",
        "search" to "بحث", "back" to "رجوع", "close" to "إغلاق", "open" to "فتح",
        "nearby_mosques" to "المساجد القريبة", "distance" to "المسافة", "directions" to "الاتجاهات",
        "walk" to "مشياً", "drive" to "بالسيارة", "favorite" to "المفضلة",
        "location" to "الموقع", "refresh" to "تحديث", "no_results" to "لا توجد نتائج",
        "loading" to "جارٍ التحميل...", "retry" to "إعادة المحاولة"
    )

    fun get(key: String, lang: String): String = ar[key] ?: key

    fun isRtl(lang: String): Boolean = true
}