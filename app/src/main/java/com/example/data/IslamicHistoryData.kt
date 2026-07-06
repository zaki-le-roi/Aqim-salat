package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class HistoryEvent(
    val id: String,
    val day: Int,        // Gregorian day
    val month: Int,      // Gregorian month (1-12)
    val yearG: Int,       // Gregorian year
    val yearH: String,   // Hijri year (e.g. "583 هـ")
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val categoryAr: String,
    val categoryEn: String,
    val imagePrompt: String
)

object IslamicHistoryData {
    val events = listOf(
        HistoryEvent(
            id = "event_hattin",
            day = 4,
            month = 7,
            yearG = 1187,
            yearH = "583 هـ",
            titleAr = "معركة حطين الخالدة وتحرير القدس الشريف",
            titleEn = "The Battle of Hattin & Liberation of Jerusalem",
            descAr = "في مثل هذا اليوم من سنة 1187 م، قاد السلطان الناصر صلاح الدين الأيوبي جيوش المسلمين إلى نصر مؤزر في معركة حطين ضد الصليبيين، مما مهد الطريق لاستعادة بيت المقدس وتحرير المسجد الأقصى المبارك بعد عقود من الاحتلال.",
            descEn = "On this day in 1187 AD, Sultan Salah al-Din al-Ayyubi led the Muslim forces to a decisive victory in the Battle of Hattin, shattering the Crusader armies and paving the way for the liberation of Jerusalem and Al-Aqsa Mosque.",
            categoryAr = "انتصارات إسلامية",
            categoryEn = "Islamic Victories",
            imagePrompt = "Salah al-Din on horseback surveying the hills of Hattin under a golden sky"
        ),
        HistoryEvent(
            id = "event_algeria",
            day = 5,
            month = 7,
            yearG = 1962,
            yearH = "1382 هـ",
            titleAr = "استقلال الجزائر - ثورة المليون ونصف شهيد",
            titleEn = "Algerian Independence Day",
            descAr = "في مثل هذا اليوم من سنة 1962 م، نالت الجزائر استقلالها الكامل بعد 132 سنة من الاحتلال الفرنسي البغيض، بفضل ثورة التحرير المظفرة وتضحيات قوافل الشهداء الأبرار الذين قدموا أرواحهم دفاعاً عن الأرض، العرض والعقيدة الإسلامية السنيّة.",
            descEn = "On this day in 1962 AD, Algeria gained full independence after 132 years of French occupation. This historic triumph followed the glorious November Revolution and the ultimate sacrifice of over 1.5 million martyrs who gave their lives to preserve their land and Islamic identity.",
            categoryAr = "تاريخ إسلامي حديث",
            categoryEn = "Modern Islamic History",
            imagePrompt = "Historic celebration in Algiers, green and white flags flying over beautiful Ottoman-style architecture"
        ),
        HistoryEvent(
            id = "event_fatih_constantinople",
            day = 29,
            month = 5,
            yearG = 1453,
            yearH = "857 هـ",
            titleAr = "فتح القسطنطينية على يد السلطان محمد الفاتح",
            titleEn = "Conquest of Constantinople by Mehmed the Conqueror",
            descAr = "في مثل هذا اليوم، نجح السلطان العثماني الشاب محمد الثاني (الفاتح) في فتح مدينة القسطنطينية بعد حصار دام 53 يوماً، محققاً بشارة النبي محمد ﷺ: 'لتفتحن القسطنطينية فلنعم الأمير أميرها ولنعم الجيش ذلك الجيش'.",
            descEn = "On this day in 1453 AD, the young Ottoman Sultan Mehmed II (the Conqueror) successfully captured Constantinople, fulfilling the famous prophecy of Prophet Muhammad (PBUH): 'You shall conquer Constantinople. What a wonderful leader will her leader be, and what a wonderful army will that army be!'",
            categoryAr = "بشارات نبوية متحصلة",
            categoryEn = "Prophetic Fulfilments",
            imagePrompt = "Sultan Mehmed II entering Constantinople on horseback through the historic city gates"
        ),
        HistoryEvent(
            id = "event_andalusia_landing",
            day = 19,
            month = 7,
            yearG = 711,
            yearH = "92 هـ",
            titleAr = "عبور طارق بن زياد وفتح الأندلس",
            titleEn = "Tariq ibn Ziyad's Landing in Andalusia",
            descAr = "في مثل هذا اليوم، رسا البطل الإسلامي طارق بن زياد بجيشه عند مضيق جبل طارق لبدء الفتوحات الإسلامية في شبه الجزيرة الأيبيرية (الأندلس)، ممهداً لقرون من الحضارة والعلوم والازدهار الإسلامي في أوروبا.",
            descEn = "On this day in 711 AD, the great commander Tariq ibn Ziyad crossed the straits with his forces and landed near Gibraltar, beginning the Islamic conquests of the Iberian Peninsula (Andalusia) which pioneered science and civilization in Europe.",
            categoryAr = "فتوحات إسلامية",
            categoryEn = "Islamic Conquests",
            imagePrompt = "Tariq ibn Ziyad standing near the cliffs of Gibraltar looking out to sea"
        ),
        HistoryEvent(
            id = "event_azhar_construction",
            day = 22,
            month = 6,
            yearG = 972,
            yearH = "361 هـ",
            titleAr = "تأسيس وافتتاح الجامع الأزهر الشريف",
            titleEn = "Inauguration of Al-Azhar Mosque",
            descAr = "أقيمت أول صلاة جمعة في الجامع الأزهر في القاهرة عقب بنائه بأمر من المعز لدين الله، ليصبح الأزهر منبر العلم الأبرز ومنارة تعليم وحفظ الشريعة واللغة العربية في العالم الإسلامي لأكثر من ألف عام.",
            descEn = "The first Friday prayer was established at the Al-Azhar Mosque in Cairo, markign the birth of one of the oldest and most prestigious Islamic academic and spiritual institutions, safeguarding Islamic law and Arabic studies for over a millennium.",
            categoryAr = "صروح إسلامية",
            categoryEn = "Islamic Landmarks",
            imagePrompt = "The beautiful minarets and courtyard of Al-Azhar Mosque under morning light"
        ),
        HistoryEvent(
            id = "event_cordoba_mosque",
            day = 12,
            month = 4,
            yearG = 785,
            yearH = "169 هـ",
            titleAr = "بدء بناء مسجد قرطبة الكبير",
            titleEn = "Construction of the Great Mosque of Cordoba",
            descAr = "أمر الأمير الأموي عبد الرحمن الداخل ببناء الجامع الكبير في قرطبة، ليكون آية في الهندسة المعمارية الإسلامية ورمزاً لقوة وازدهار الخلافة الأموية في الأندلس.",
            descEn = "The Umayyad Emir Abd al-Rahman I ordered the construction of the Great Mosque of Cordoba, establishing an architectural marvel and a symbol of Islamic Golden Age prosperity in Europe.",
            categoryAr = "عمارة إسلامية",
            categoryEn = "Islamic Architecture",
            imagePrompt = "The red-and-white double arches of Cordoba Mosque with soft hanging lamps"
        ),
        HistoryEvent(
            id = "event_house_of_wisdom",
            day = 15,
            month = 9,
            yearG = 830,
            yearH = "215 هـ",
            titleAr = "ازدهار بيت الحكمة في بغداد منارة العلوم",
            titleEn = "Peak of the House of Wisdom in Baghdad",
            descAr = "بلغ بيت الحكمة في بغداد ذروة نشاطه العلمي والترجمي في عهد الخليفة العباسي المأمون، حيث اجتمع علماء المسلمين لترجمة المعارف الإنسانية وابتكار علوم الجبر، الفلك، والطب الحديثة.",
            descEn = "The House of Wisdom in Baghdad reached its scientific peak under Caliph al-Ma'mun, gathering top Muslim scholars who pioneered algebra, astronomy, and modern medicine.",
            categoryAr = "العصر الذهبي للعلوم",
            categoryEn = "Golden Age of Science",
            imagePrompt = "Ancient library hall filled with Islamic scrolls, brass astrolabes, and studying scholars"
        ),
        HistoryEvent(
            id = "event_badr",
            day = 13,
            month = 3,
            yearG = 624,
            yearH = "2 هـ",
            titleAr = "غزوة بدر الكبرى - يوم الفرقان",
            titleEn = "The Battle of Badr - Day of Criterion",
            descAr = "وقعت غزوة بدر الكبرى، أول معركة فاصلة في الإسلام، حيث نصر الله المؤمنين القلّة على كفار قريش، مرسخاً دعائم الدولة الإسلامية الجديدة في المدينة المنورة.",
            descEn = "The Great Battle of Badr occurred, marking the first decisive victory of early Muslims over the Quraish, establishing the foundation of the Islamic state.",
            categoryAr = "غزوات نبوية كبرى",
            categoryEn = "Major Prophetic Battles",
            imagePrompt = "A desert canyon scene under bright sunlight with protective banners"
        ),
        HistoryEvent(
            id = "event_mecca_conquest",
            day = 11,
            month = 1,
            yearG = 630,
            yearH = "8 هـ",
            titleAr = "فتح مكة المكرمة ودخول الناس في دين الله أفواجا",
            titleEn = "Conquest of Mecca",
            descAr = "دخل النبي ﷺ وجيش المسلمين مكة المكرمة فاتحين دون قتال، وحطم النبي الأصنام حول الكعبة معلناً عهد التوحيد، وعفا عن قريش قائلاً: 'اذهبوا فأنتم الطلقاء'.",
            descEn = "Prophet Muhammad (PBUH) entered Mecca peacefully with his companions, purifying the Kaaba from idols and granting amnesty to his former adversaries.",
            categoryAr = "أحداث نبوية عظمى",
            categoryEn = "Major Prophetic Milestones",
            imagePrompt = "A historic depiction of the Kaaba surrounded by peaceful companions in white"
        )
    )

    fun getEventsForDay(day: Int, month: Int): List<HistoryEvent> {
        return events.filter { it.day == day && it.month == month }
    }

    fun getAllEvents(): List<HistoryEvent> {
        return events
    }
}
