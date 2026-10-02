package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Datastore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aqim_salah_preferences")

class AppRepository(
    private val db: AppDatabase,
    private val context: Context
) {
    // DAOs
    private val prayerDao = db.prayerDao()
    private val tasbihDao = db.tasbihDao()
    private val bookmarkDao = db.bookmarkDao()
    private val quranHistoryDao = db.quranHistoryDao()
    private val mosqueDao = db.mosqueDao()
    private val khatmahDao = db.khatmahDao()
    private val quranNoteDao = db.quranNoteDao()

    // --- Prayer Logs ---
    fun getLogsForDate(date: String): Flow<List<PrayerLog>> = prayerDao.getLogsForDate(date)
    fun getAllLogs(): Flow<List<PrayerLog>> = prayerDao.getAllLogs()
    suspend fun insertPrayerLog(log: PrayerLog) = prayerDao.insertLog(log)
    suspend fun deletePrayerLog(date: String, prayerName: String) = prayerDao.deleteLog(date, prayerName)

    // --- Tasbih Counters ---
    val allTasbihCounters: Flow<List<TasbihCounter>> = tasbihDao.getAllCounters()
    suspend fun insertTasbihCounter(counter: TasbihCounter) = tasbihDao.insertCounter(counter)
    suspend fun updateTasbihCounter(counter: TasbihCounter) = tasbihDao.updateCounter(counter)
    suspend fun deleteTasbihCounter(counter: TasbihCounter) = tasbihDao.deleteCounter(counter)

    // --- Bookmarks ---
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
    fun getBookmarksByType(type: String): Flow<List<Bookmark>> = bookmarkDao.getBookmarksByType(type)
    fun isBookmarked(type: String, referenceId: String): Flow<Boolean> = bookmarkDao.isBookmarked(type, referenceId)
    suspend fun insertBookmark(bookmark: Bookmark) = bookmarkDao.insertBookmark(bookmark)
    suspend fun deleteBookmark(type: String, referenceId: String) = bookmarkDao.deleteBookmark(type, referenceId)

    // --- Quran History ---
    val quranHistory: Flow<List<QuranHistory>> = quranHistoryDao.getHistory()
    suspend fun insertQuranHistory(history: QuranHistory) = quranHistoryDao.insertHistory(history)

    // --- Favorite Mosques ---
    val favoriteMosques: Flow<List<FavoriteMosque>> = mosqueDao.getAllFavorites()
    suspend fun insertFavoriteMosque(mosque: FavoriteMosque) = mosqueDao.insertFavorite(mosque)
    suspend fun deleteFavoriteMosque(id: Long) = mosqueDao.deleteFavoriteById(id)
    fun isFavoriteMosque(id: Long): Flow<Boolean> = mosqueDao.isFavorite(id)

    // --- Khatmahs ---
    val allKhatmahs: Flow<List<Khatmah>> = khatmahDao.getAllKhatmahs()
    suspend fun insertKhatmah(khatmah: Khatmah) = khatmahDao.insertKhatmah(khatmah)
    suspend fun updateKhatmah(khatmah: Khatmah) = khatmahDao.updateKhatmah(khatmah)
    suspend fun deleteKhatmah(khatmah: Khatmah) = khatmahDao.deleteKhatmah(khatmah)

    // --- Quran Notes ---
    fun getNotesForAyah(surahId: Int, ayahId: Int): Flow<List<QuranNote>> = quranNoteDao.getNotesForAyah(surahId, ayahId)
    val allQuranNotes: Flow<List<QuranNote>> = quranNoteDao.getAllNotes()
    suspend fun insertQuranNote(note: QuranNote) = quranNoteDao.insertNote(note)
    suspend fun deleteQuranNote(id: Int) = quranNoteDao.deleteNoteById(id)

    // --- Preferences Keys ---
    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_MADHAB = stringPreferencesKey("app_madhab")
        val KEY_CALC_METHOD = stringPreferencesKey("app_calc_method")
        val KEY_LATITUDE = doublePreferencesKey("app_latitude")
        val KEY_LONGITUDE = doublePreferencesKey("app_longitude")
        val KEY_LOCATION_NAME = stringPreferencesKey("app_location_name")
        val KEY_LOCATION_CONFIGURED = booleanPreferencesKey("app_location_configured")
        val KEY_ATHAN_FAJR_VOICE = stringPreferencesKey("app_athan_fajr_voice")
        val KEY_ATHAN_OTHER_VOICE = stringPreferencesKey("app_athan_other_voice")
        val KEY_SNOOZE_MINUTES = intPreferencesKey("app_snooze_minutes")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("app_notifications_enabled")
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val KEY_WALLPAPER = stringPreferencesKey("app_wallpaper")
        val KEY_PREF_MOSQUE_ID = stringPreferencesKey("app_pref_mosque_id")
        val KEY_PREF_MOSQUE_NAME = stringPreferencesKey("app_pref_mosque_name")
        val KEY_PREF_MOSQUE_LAT = doublePreferencesKey("app_pref_mosque_lat")
        val KEY_PREF_MOSQUE_LNG = doublePreferencesKey("app_pref_mosque_lng")
        val KEY_PREF_MOSQUE_ADDR = stringPreferencesKey("app_pref_mosque_addr")
        val KEY_PREF_MOSQUE_REMIND = booleanPreferencesKey("app_pref_mosque_remind")
    }

    // --- Preference Observables ---
    val appLanguage: Flow<String> = context.dataStore.data.map { pref ->
        when (pref[KEY_LANGUAGE]) {
            "en" -> "en"
            else -> "ar"
        }
    }

    val appMadhab: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_MADHAB] ?: "STANDARD"
    }

    val appCalcMethod: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_CALC_METHOD] ?: "ALGERIA"
    }

    val appLatitude: Flow<Double> = context.dataStore.data.map { pref ->
        pref[KEY_LATITUDE] ?: 36.7538
    }

    val appLongitude: Flow<Double> = context.dataStore.data.map { pref ->
        pref[KEY_LONGITUDE] ?: 3.0588
    }

    val appLocationConfigured: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_LOCATION_CONFIGURED] ?: (pref[KEY_LATITUDE] != null && pref[KEY_LONGITUDE] != null && (pref[KEY_LATITUDE] != 36.7538 || pref[KEY_LONGITUDE] != 3.0588))
    }

    val appLocationName: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_LOCATION_NAME] ?: "الجزائر العاصمة، الجزائر"
    }

    val appAthanFajrVoice: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_ATHAN_FAJR_VOICE] ?: "Fajr Medina"
    }

    val appAthanOtherVoice: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_ATHAN_OTHER_VOICE] ?: "Makkah"
    }

    val appSnoozeMinutes: Flow<Int> = context.dataStore.data.map { pref ->
        pref[KEY_SNOOZE_MINUTES] ?: 5
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val appThemeMode: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_THEME_MODE] ?: "AUTO"
    }

    val appWallpaper: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_WALLPAPER] ?: "DEFAULT"
    }

    val prefMosqueId: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_PREF_MOSQUE_ID] ?: ""
    }

    val prefMosqueName: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_PREF_MOSQUE_NAME] ?: ""
    }

    val prefMosqueLat: Flow<Double> = context.dataStore.data.map { pref ->
        pref[KEY_PREF_MOSQUE_LAT] ?: 0.0
    }

    val prefMosqueLng: Flow<Double> = context.dataStore.data.map { pref ->
        pref[KEY_PREF_MOSQUE_LNG] ?: 0.0
    }

    val prefMosqueAddr: Flow<String> = context.dataStore.data.map { pref ->
        pref[KEY_PREF_MOSQUE_ADDR] ?: ""
    }

    val prefMosqueRemind: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_PREF_MOSQUE_REMIND] ?: true
    }

    // --- Preference Writers ---
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { pref -> pref[KEY_THEME_MODE] = mode }
    }

    suspend fun setWallpaper(wallpaper: String) {
        context.dataStore.edit { pref -> pref[KEY_WALLPAPER] = wallpaper }
    }

    suspend fun setPreferredMosque(id: String, name: String, lat: Double, lng: Double, addr: String) {
        context.dataStore.edit { pref ->
            pref[KEY_PREF_MOSQUE_ID] = id
            pref[KEY_PREF_MOSQUE_NAME] = name
            pref[KEY_PREF_MOSQUE_LAT] = lat
            pref[KEY_PREF_MOSQUE_LNG] = lng
            pref[KEY_PREF_MOSQUE_ADDR] = addr
        }
    }

    suspend fun setPreferredMosqueRemind(enabled: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_PREF_MOSQUE_REMIND] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { pref -> pref[KEY_LANGUAGE] = lang }
    }

    suspend fun setMadhab(madhab: String) {
        context.dataStore.edit { pref -> pref[KEY_MADHAB] = madhab }
    }

    suspend fun setCalcMethod(method: String) {
        context.dataStore.edit { pref -> pref[KEY_CALC_METHOD] = method }
    }

    suspend fun setLocation(name: String, lat: Double, lng: Double) {
        context.dataStore.edit { pref ->
            pref[KEY_LOCATION_NAME] = name
            pref[KEY_LOCATION_CONFIGURED] = true
            pref[KEY_LATITUDE] = lat
            pref[KEY_LONGITUDE] = lng
        }
    }

    suspend fun setAthanFajrVoice(voice: String) {
        context.dataStore.edit { pref -> pref[KEY_ATHAN_FAJR_VOICE] = voice }
    }

    suspend fun setAthanOtherVoice(voice: String) {
        context.dataStore.edit { pref -> pref[KEY_ATHAN_OTHER_VOICE] = voice }
    }

    suspend fun setSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { pref -> pref[KEY_SNOOZE_MINUTES] = minutes }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    // --- Admin and Donation Operations ---
    private val adminDao = db.adminDao()

    val allAdminHadiths: Flow<List<AdminHadith>> = adminDao.getAllHadiths()
    suspend fun insertAdminHadith(hadith: AdminHadith) = adminDao.insertHadith(hadith)
    suspend fun deleteAdminHadith(id: Int) = adminDao.deleteHadith(id)

    val allAdminAdhkars: Flow<List<AdminAdhkar>> = adminDao.getAllAdhkars()
    suspend fun insertAdminAdhkar(adhkar: AdminAdhkar) = adminDao.insertAdhkar(adhkar)
    suspend fun deleteAdminAdhkar(id: Int) = adminDao.deleteAdhkar(id)

    val allAdminDuas: Flow<List<AdminDua>> = adminDao.getAllDuas()
    suspend fun insertAdminDua(dua: AdminDua) = adminDao.insertDua(dua)
    suspend fun deleteAdminDua(id: Int) = adminDao.deleteDua(id)

    val allAdminArticles: Flow<List<AdminArticle>> = adminDao.getAllArticles()
    suspend fun insertAdminArticle(article: AdminArticle) = adminDao.insertArticle(article)
    suspend fun deleteAdminArticle(id: Int) = adminDao.deleteArticle(id)

    val allAdminBannersReminders: Flow<List<AdminBannerReminder>> = adminDao.getAllBannersReminders()
    suspend fun insertAdminBannerReminder(br: AdminBannerReminder) = adminDao.insertBannerReminder(br)
    suspend fun deleteAdminBannerReminder(id: Int) = adminDao.deleteBannerReminder(id)

    val allDonationCampaigns: Flow<List<DonationCampaign>> = adminDao.getAllDonationCampaigns()
    suspend fun insertDonationCampaign(campaign: DonationCampaign) = adminDao.insertDonationCampaign(campaign)
    suspend fun deleteDonationCampaign(id: Int) = adminDao.deleteDonationCampaign(id)

    val allNotificationLogs: Flow<List<NotificationLog>> = adminDao.getAllNotificationLogs()
    suspend fun insertNotificationLog(log: NotificationLog) = adminDao.insertNotificationLog(log)

    val allAdminAccounts: Flow<List<AdminAccount>> = adminDao.getAllAdminAccounts()
    suspend fun insertAdminAccount(account: AdminAccount) = adminDao.insertAdminAccount(account)
    suspend fun deleteAdminAccount(id: Int) = adminDao.deleteAdminAccount(id)

    // Member Operations
    val allMembers: Flow<List<AppMember>> = adminDao.getAllMembers()
    suspend fun insertMember(member: AppMember) = adminDao.insertMember(member)
    suspend fun deleteMember(id: Int) = adminDao.deleteMember(id)
}