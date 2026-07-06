package com.example.data

object AdhkarData {
    data class Dhikr(
        val category: String, // Morning, Evening, Sleep, Travel, Mosque, Food, Protection, Healing, Ramadan
        val id: String,
        val text: String,
        val translation: String,
        val reference: String,
        val countTarget: Int = 1
    )

    val adhkar = listOf(
        // Morning
        Dhikr(
            category = "Morning",
            id = "morning_1",
            text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            translation = "We have entered a new day and with it all dominion belongs to Allah. All praise is due to Allah. None has the right to be worshipped but Allah alone, who has no partner.",
            reference = "Sahih Muslim 4/2088",
            countTarget = 1
        ),
        Dhikr(
            category = "Morning",
            id = "morning_2",
            text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            translation = "Glory is to Allah and praise is to Him, by the number of His creation, by His Pleasure, by the weight of His Throne, and by the ink of His Words.",
            reference = "Sahih Muslim 4/2090",
            countTarget = 3
        ),

        // Evening
        Dhikr(
            category = "Evening",
            id = "evening_1",
            text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            translation = "We have entered the evening and with it all dominion belongs to Allah. All praise is due to Allah. None has the right to be worshipped but Allah alone, who has no partner.",
            reference = "Sahih Muslim 4/2088",
            countTarget = 1
        ),
        Dhikr(
            category = "Evening",
            id = "evening_2",
            text = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            translation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            reference = "Sahih Muslim 4/2080",
            countTarget = 3
        ),

        // Sleep
        Dhikr(
            category = "Sleep",
            id = "sleep_1",
            text = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            translation = "With Your name, my Lord, I lay my side down, and by Your leave I raise it. If You take my soul, bestow mercy upon it, and if You release it, protect it as You protect Your righteous servants.",
            reference = "Sahih al-Bukhari 11/126",
            countTarget = 1
        ),

        // Travel
        Dhikr(
            category = "Travel",
            id = "travel_1",
            text = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            translation = "Glory is to Him Who has subjected this to us, and we were not able to do it ourselves. And surely, to our Lord we are returning.",
            reference = "Sahih Muslim 2/998",
            countTarget = 1
        ),

        // Mosque
        Dhikr(
            category = "Mosque",
            id = "mosque_1",
            text = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            translation = "O Allah, open the gates of Your mercy for me (entering the mosque).",
            reference = "Sahih Muslim 1/496",
            countTarget = 1
        ),

        // Protection & Healing
        Dhikr(
            category = "Protection",
            id = "protection_1",
            text = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            translation = "In the Name of Allah, Who with His Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
            reference = "Abu Dawud 4/323",
            countTarget = 3
        ),
        Dhikr(
            category = "Healing",
            id = "healing_1",
            text = "أَذْهِبِ الْبَاسَ رَبَّ النَّاسِ، وَاشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            translation = "Take away the disease, O Lord of the people. Heal, for You are the Healer. There is no cure but Your cure, a cure that leaves no illness.",
            reference = "Sahih al-Bukhari 10/206",
            countTarget = 1
        )
    )
}
