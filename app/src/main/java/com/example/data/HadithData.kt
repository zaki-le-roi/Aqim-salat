package com.example.data

object HadithData {
    data class Hadith(
        val collection: String, // Bukhari, Muslim, Riyad as-Salihin, Nawawi
        val id: String,
        val number: String,
        val arabic: String,
        val english: String,
        val reference: String
    )

    val hadiths = listOf(
        // Sahih al-Bukhari
        Hadith(
            collection = "Sahih al-Bukhari",
            id = "bukhari_1",
            number = "1",
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            english = "The reward of deeds depends upon the intentions and every person will get the reward according to what he has intended.",
            reference = "Sahih al-Bukhari, Book 1, Hadith 1"
        ),
        Hadith(
            collection = "Sahih al-Bukhari",
            id = "bukhari_2",
            number = "13",
            arabic = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            english = "None of you will have faith until he wishes for his brother what he likes for himself.",
            reference = "Sahih al-Bukhari, Book 2, Hadith 6"
        ),
        Hadith(
            collection = "Sahih al-Bukhari",
            id = "bukhari_3",
            number = "6018",
            arabic = "الْخَالِقُ جَمِيلٌ يُحِبُّ الْجَمَالَ، وَمَنْ لاَ يَرْحَمِ النَّاسَ لاَ يَرْحَمْهُ اللَّهُ",
            english = "He who is not merciful to others, Allah will not be merciful to him.",
            reference = "Sahih al-Bukhari, Book 78, Hadith 48"
        ),
        
        // Sahih Muslim
        Hadith(
            collection = "Sahih Muslim",
            id = "muslim_1",
            number = "223",
            arabic = "الطهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ",
            english = "Purity is half of faith, and Al-Hamdulillah (praise be to Allah) fills the scale.",
            reference = "Sahih Muslim, Book 2, Hadith 1"
        ),
        Hadith(
            collection = "Sahih Muslim",
            id = "muslim_2",
            number = "2564",
            arabic = "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا",
            english = "Be mindful of Allah wherever you are, and follow up a bad deed with a good deed which will wipe it out.",
            reference = "Sahih Muslim, Book 45, Hadith 12"
        ),

        // Riyad as-Salihin
        Hadith(
            collection = "Riyad as-Salihin",
            id = "riyad_1",
            number = "1",
            arabic = "يَسِّرُوا وَلا تُعَسِّرُوا، وَبَشِّرُوا وَلا تُنَفِّرُوا",
            english = "Make things easy and do not make them difficult, cheer people up and do not drive them away.",
            reference = "Riyad as-Salihin, Book 1, Hadith 12"
        ),
        Hadith(
            collection = "Riyad as-Salihin",
            id = "riyad_2",
            number = "54",
            arabic = "الدِّينُ النَّصِيحَةُ",
            english = "The religion is sincerity (faithful advice).",
            reference = "Riyad as-Salihin, Book 1, Hadith 183"
        ),

        // 40 Nawawi
        Hadith(
            collection = "40 Nawawi",
            id = "nawawi_1",
            number = "12",
            arabic = "مِنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ",
            english = "Part of the perfection of one's Islam is his leaving that which does not concern him.",
            reference = "40 Hadith Nawawi, Hadith 12"
        ),
        Hadith(
            collection = "40 Nawawi",
            id = "nawawi_2",
            number = "15",
            arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            english = "Let him who believes in Allah and the Last Day speak good or remain silent.",
            reference = "40 Hadith Nawawi, Hadith 15"
        )
    )
}
