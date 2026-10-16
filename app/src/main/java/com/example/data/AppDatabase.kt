package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    ],
    version = 6,
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

    companion object {
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM prayer_logs WHERE id NOT IN (SELECT MAX(id) FROM prayer_logs GROUP BY date, prayerName)")
                db.execSQL("DELETE FROM bookmarks WHERE id NOT IN (SELECT MAX(id) FROM bookmarks GROUP BY type, referenceId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_prayer_logs_date_prayerName ON prayer_logs(date, prayerName)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_bookmarks_type_referenceId ON bookmarks(type, referenceId)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove tables that belonged exclusively to the deleted admin console.
                listOf(
                    "admin_hadiths",
                    "admin_adhkars",
                    "admin_duas",
                    "admin_articles",
                    "admin_banners_reminders",
                    "donation_campaigns",
                    "notification_logs",
                    "admin_accounts",
                    "app_members"
                ).forEach { tableName ->
                    db.execSQL("DROP TABLE IF EXISTS $tableName")
                }
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aqim_salah_db"
                ).addMigrations(MIGRATION_4_5, MIGRATION_5_6).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
