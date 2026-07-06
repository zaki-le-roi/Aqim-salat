package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// Entities
@androidx.room.Entity(tableName = "prayer_logs")
data class PrayerLog(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val prayerName: String, // Fajr, Shuruq, Dhuhr, Asr, Maghrib, Isha
    val status: String, // PRAYED_ON_TIME, PRAYED_LATE, MISSED, NOT_YET
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "tasbih_counters")
data class TasbihCounter(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val count: Int = 0,
    val goal: Int = 33,
    val hapticEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "bookmarks")
data class Bookmark(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // QURAN, HADITH, DUA, ADHKAR
    val referenceId: String, // e.g. "1:1" for Surah 1 Ayah 1, or "bukhari:10"
    val title: String,
    val subtitle: String,
    val arabicText: String = "",
    val translationText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "quran_history")
data class QuranHistory(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahId: Int,
    val ayahId: Int,
    val surahName: String,
    val timestamp: Long = System.currentTimeMillis()
)

// DAOs
@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<PrayerLog>>

    @Query("SELECT * FROM prayer_logs ORDER BY date DESC, timestamp DESC")
    fun getAllLogs(): Flow<List<PrayerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PrayerLog)

    @Update
    suspend fun updateLog(log: PrayerLog)

    @Query("DELETE FROM prayer_logs WHERE date = :date AND prayerName = :prayerName")
    suspend fun deleteLog(date: String, prayerName: String)
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_counters ORDER BY lastUpdated DESC")
    fun getAllCounters(): Flow<List<TasbihCounter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounter(counter: TasbihCounter)

    @Update
    suspend fun updateCounter(counter: TasbihCounter)

    @Delete
    suspend fun deleteCounter(counter: TasbihCounter)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE type = :type ORDER BY timestamp DESC")
    fun getBookmarksByType(type: String): Flow<List<Bookmark>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE type = :type AND referenceId = :referenceId)")
    fun isBookmarked(type: String, referenceId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE type = :type AND referenceId = :referenceId")
    suspend fun deleteBookmark(type: String, referenceId: String)
}

@Dao
interface QuranHistoryDao {
    @Query("SELECT * FROM quran_history ORDER BY timestamp DESC LIMIT 20")
    fun getHistory(): Flow<List<QuranHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: QuranHistory)
}

@androidx.room.Entity(tableName = "favorite_mosques")
data class FavoriteMosque(
    @androidx.room.PrimaryKey val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "khatmahs")
data class Khatmah(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetDays: Int = 30,
    val currentProgressPages: Int = 0,
    val totalPages: Int = 604,
    val type: String = "INDIVIDUAL", // INDIVIDUAL, FAMILY, FRIENDS
    val code: String = "",
    val membersList: String = "",
    val streak: Int = 0,
    val lastReadTime: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = true
)

@androidx.room.Entity(tableName = "quran_notes")
data class QuranNote(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahId: Int,
    val ayahId: Int,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

// DAOs
@Dao
interface MosqueDao {
    @Query("SELECT * FROM favorite_mosques ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteMosque>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(mosque: FavoriteMosque)

    @Query("DELETE FROM favorite_mosques WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_mosques WHERE id = :id)")
    fun isFavorite(id: Long): Flow<Boolean>
}

@Dao
interface KhatmahDao {
    @Query("SELECT * FROM khatmahs ORDER BY lastReadTime DESC")
    fun getAllKhatmahs(): Flow<List<Khatmah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhatmah(khatmah: Khatmah)

    @Update
    suspend fun updateKhatmah(khatmah: Khatmah)

    @Delete
    suspend fun deleteKhatmah(khatmah: Khatmah)
}

@Dao
interface QuranNoteDao {
    @Query("SELECT * FROM quran_notes WHERE surahId = :surahId AND ayahId = :ayahId ORDER BY timestamp DESC")
    fun getNotesForAyah(surahId: Int, ayahId: Int): Flow<List<QuranNote>>

    @Query("SELECT * FROM quran_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<QuranNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuranNote)

    @Query("DELETE FROM quran_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

// Admin and Donation Entities
@androidx.room.Entity(tableName = "admin_hadiths")
data class AdminHadith(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val collection: String,
    val number: String,
    val arabic: String,
    val english: String,
    val reference: String,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "admin_adhkars")
data class AdminAdhkar(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val title: String,
    val arabic: String,
    val english: String,
    val countGoal: Int = 33,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "admin_duas")
data class AdminDua(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val arabic: String,
    val translation: String,
    val transliteration: String,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "admin_articles")
data class AdminArticle(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val author: String = "Super Admin",
    val date: String = "",
    val category: String = "Islamic Knowledge",
    val imageUri: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "admin_banners_reminders")
data class AdminBannerReminder(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // BANNER, REMINDER, ANNOUNCEMENT, HADITH_DAY, DUA_DAY, RAMADAN_MSG, EID_MSG, EVENT
    val title: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val actionUrl: String = "",
    val scheduledTime: String = "",
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "donation_campaigns")
data class DonationCampaign(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String = "",
    val targetAmount: Double = 10000.0,
    val currentProgress: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "notification_logs")
data class NotificationLog(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val audience: String = "All Users",
    val sentTime: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "app_members")
data class AppMember(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val country: String = "Saudi Arabia",
    val city: String = "Makkah",
    val points: Int = 100,
    val registrationDate: Long = System.currentTimeMillis(),
    val streakDays: Int = 0,
    val isActive: Boolean = true
)

@androidx.room.Entity(tableName = "admin_accounts")
data class AdminAccount(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val role: String = "Super Admin",
    val permissions: String = "ALL",
    val isActive: Boolean = true
)

@Dao
interface AdminDao {
    @Query("SELECT * FROM admin_hadiths ORDER BY timestamp DESC")
    fun getAllHadiths(): Flow<List<AdminHadith>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadith(hadith: AdminHadith)

    @Query("DELETE FROM admin_hadiths WHERE id = :id")
    suspend fun deleteHadith(id: Int)

    @Query("SELECT * FROM admin_adhkars ORDER BY timestamp DESC")
    fun getAllAdhkars(): Flow<List<AdminAdhkar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkar(adhkar: AdminAdhkar)

    @Query("DELETE FROM admin_adhkars WHERE id = :id")
    suspend fun deleteAdhkar(id: Int)

    @Query("SELECT * FROM admin_duas ORDER BY timestamp DESC")
    fun getAllDuas(): Flow<List<AdminDua>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDua(dua: AdminDua)

    @Query("DELETE FROM admin_duas WHERE id = :id")
    suspend fun deleteDua(id: Int)

    @Query("SELECT * FROM admin_articles ORDER BY timestamp DESC")
    fun getAllArticles(): Flow<List<AdminArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: AdminArticle)

    @Query("DELETE FROM admin_articles WHERE id = :id")
    suspend fun deleteArticle(id: Int)

    @Query("SELECT * FROM admin_banners_reminders ORDER BY timestamp DESC")
    fun getAllBannersReminders(): Flow<List<AdminBannerReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBannerReminder(br: AdminBannerReminder)

    @Query("DELETE FROM admin_banners_reminders WHERE id = :id")
    suspend fun deleteBannerReminder(id: Int)

    @Query("SELECT * FROM donation_campaigns ORDER BY timestamp DESC")
    fun getAllDonationCampaigns(): Flow<List<DonationCampaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationCampaign(campaign: DonationCampaign)

    @Query("DELETE FROM donation_campaigns WHERE id = :id")
    suspend fun deleteDonationCampaign(id: Int)

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotificationLogs(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLog)

    @Query("SELECT * FROM admin_accounts ORDER BY id DESC")
    fun getAllAdminAccounts(): Flow<List<AdminAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminAccount(account: AdminAccount)

    @Query("DELETE FROM admin_accounts WHERE id = :id")
    suspend fun deleteAdminAccount(id: Int)

    // Member DAO methods
    @Query("SELECT * FROM app_members ORDER BY points DESC")
    fun getAllMembers(): Flow<List<AppMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: AppMember)

    @Query("DELETE FROM app_members WHERE id = :id")
    suspend fun deleteMember(id: Int)
}

// Database Room Class
@Database(
    entities = [
        PrayerLog::class,
        TasbihCounter::class,
        Bookmark::class,
        QuranHistory::class,
        FavoriteMosque::class,
        Khatmah::class,
        QuranNote::class,
        AdminHadith::class,
        AdminAdhkar::class,
        AdminDua::class,
        AdminArticle::class,
        AdminBannerReminder::class,
        DonationCampaign::class,
        NotificationLog::class,
        AdminAccount::class,
        AppMember::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun quranHistoryDao(): QuranHistoryDao
    abstract fun mosqueDao(): MosqueDao
    abstract fun khatmahDao(): KhatmahDao
    abstract fun quranNoteDao(): QuranNoteDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aqim_salah_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
