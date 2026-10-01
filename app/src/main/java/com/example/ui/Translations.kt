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
        "language" to "لغة التطبيق", "arabic" to "العربية", "english" to "English",
        "theme" to "سمة التطبيق", "light" to "فاتح", "dark" to "داكن", "auto" to "تلقائي",
        "ramadan" to "رمضان", "continue_reading" to "مواصلة القراءة",
        "bookmark_saved" to "تم الحفظ", "bookmark_removed" to "تمت إزالة الحفظ",
        "search" to "بحث", "back" to "رجوع", "close" to "إغلاق", "open" to "فتح",
        "nearby_mosques" to "المساجد القريبة", "distance" to "المسافة", "directions" to "الاتجاهات",
        "walk" to "مشياً", "drive" to "بالسيارة", "favorite" to "المفضلة",
        "location" to "الموقع", "refresh" to "تحديث", "no_results" to "لا توجد نتائج",
        "loading" to "جارٍ التحميل...", "retry" to "إعادة المحاولة"
    )

    private val en = ar.toMutableMap().apply {
        putAll(mapOf(
            "slogan" to "Answer the call and keep your prayers",
            "home" to "Home", "quran" to "Quran", "hadith" to "Hadith",
            "tasbih" to "Tasbih", "adhkar" to "Adhkar", "settings" to "Settings",
            "names_of_allah" to "Names of Allah", "prayer_times" to "Prayer Times",
            "next_prayer" to "Next Prayer", "remaining" to "Remaining",
            "current_prayer" to "Current Prayer", "hijri_date" to "Hijri Date",
            "calculation_method" to "Prayer Calculation", "madhab" to "Madhab",
            "gps_location" to "Device Location", "manual_location" to "Manual Location",
            "search_surah" to "Search for a surah...", "search_hadith" to "Search hadith...",
            "search_dhikr" to "Search adhkar...", "bookmarks" to "Bookmarks",
            "fajr" to "Fajr", "sunrise" to "Sunrise", "dhuhr" to "Dhuhr",
            "asr" to "Asr", "maghrib" to "Maghrib", "isha" to "Isha",
            "imsak" to "Imsak", "midnight" to "Midnight", "lastThird" to "Last third of night",
            "tasbih_goal" to "Goal", "tasbih_count" to "Count", "tasbih_reset" to "Reset",
            "add_counter" to "Add counter", "name" to "Name", "save" to "Save", "cancel" to "Cancel",
            "streak" to "Streak", "play_athan" to "Play Adhan", "stop_athan" to "Stop Adhan",
            "prayer_tracker" to "Prayer tracker", "log_prompt" to "Log your prayer",
            "prayed_on_time" to "Prayed on time", "prayed_late" to "Prayed late", "missed" to "Missed",
            "not_yet" to "Not yet", "ramadan_countdown" to "Ramadan countdown", "days" to "days",
            "fasting_tracker" to "Fasting tracker", "qibla" to "Qibla", "qibla_desc" to "Point your phone toward the Kaaba",
            "kaaba_distance" to "Distance to Kaaba", "tools" to "Islamic Services",
            "daily_inspiration" to "Verse of the day", "daily_hadith" to "Hadith of the day", "daily_dua" to "Dua of the day",
            "reminders" to "Adhan reminders", "athan_voice" to "Adhan voice", "snooze" to "Snooze",
            "language" to "App language", "arabic" to "العربية", "english" to "English",
            "theme" to "App theme", "light" to "Light", "dark" to "Dark", "auto" to "System",
            "ramadan" to "Ramadan", "continue_reading" to "Continue reading",
            "bookmark_saved" to "Saved", "bookmark_removed" to "Removed",
            "search" to "Search", "back" to "Back", "close" to "Close", "open" to "Open",
            "nearby_mosques" to "Nearby mosques", "distance" to "Distance", "directions" to "Directions",
            "walk" to "Walk", "drive" to "Drive", "favorite" to "Favorite",
            "location" to "Location", "refresh" to "Refresh", "no_results" to "No results",
            "loading" to "Loading...", "retry" to "Retry"
        ))
    }

    fun get(key: String, lang: String): String =
        (if (lang == "en") en else ar)[key] ?: ar[key] ?: key

    fun isRtl(lang: String): Boolean = lang != "en"
}