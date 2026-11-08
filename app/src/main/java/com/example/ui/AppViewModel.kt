package com.example.ui

import android.app.Application
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.net.ConnectivityManager
import android.net.Network
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.adhan.AdhanScheduler
import com.example.widget.PrayerTimesWidgetProvider
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.time.Instant
import java.time.ZoneId
import java.time.Duration
import kotlin.math.*

// For high-fidelity location services and real geocoder addresses
import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.location.Geocoder



private data class PrayerCalculationSettings(
    val latitude: Double,
    val longitude: Double,
    val madhab: String,
    val method: String,
    val timeZoneId: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = AppDatabase.getDatabase(context)
    private val repo = AppRepository(db, context)
    private val audioPrefs = context.getSharedPreferences("audio_preferences", Context.MODE_PRIVATE)
    private val accessibilityPrefs = context.getSharedPreferences("accessibility_preferences", Context.MODE_PRIVATE)
    private val _hapticsEnabled = MutableStateFlow(accessibilityPrefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()
    private val _fontScale = MutableStateFlow(accessibilityPrefs.getFloat("font_scale", 1.0f))
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    fun setFontScale(scale: Float) {
        val normalized = scale.coerceIn(0.8f, 1.6f)
        _fontScale.value = normalized
        accessibilityPrefs.edit().putFloat("font_scale", normalized).apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        accessibilityPrefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }
    private val _defaultReciter = MutableStateFlow(
        audioPrefs.getString("default_reciter", "Mishary Al-Afasy") ?: "Mishary Al-Afasy"
    )
    val defaultReciter: StateFlow<String> = _defaultReciter.asStateFlow()
    private val _defaultMuadhin = MutableStateFlow(
        audioPrefs.getString("default_muadhin", "MAKKAH") ?: "MAKKAH"
    )
    val defaultMuadhin: StateFlow<String> = _defaultMuadhin.asStateFlow()

    // --- State Observables ---
    val madhab: StateFlow<String> = repo.appMadhab.stateIn(viewModelScope, SharingStarted.Eagerly, "STANDARD")
    val calcMethod: StateFlow<String> = repo.appCalcMethod.stateIn(viewModelScope, SharingStarted.Eagerly, "ALGERIA")
    val latitude: StateFlow<Double> = repo.appLatitude.stateIn(viewModelScope, SharingStarted.Eagerly, 36.7538)
    val longitude: StateFlow<Double> = repo.appLongitude.stateIn(viewModelScope, SharingStarted.Eagerly, 3.0588)
    val locationConfigured: StateFlow<Boolean> = repo.appLocationConfigured.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val locationName: StateFlow<String> = repo.appLocationName.stateIn(viewModelScope, SharingStarted.Eagerly, "لم يتم تحديد الموقع بعد")
    val timeZoneId: StateFlow<String> = repo.appTimeZoneId.stateIn(viewModelScope, SharingStarted.Eagerly, ZoneId.systemDefault().id)
    val athanFajrVoice: StateFlow<String> = repo.appAthanFajrVoice.stateIn(viewModelScope, SharingStarted.Eagerly, "Fajr Medina")
    val athanOtherVoice: StateFlow<String> = repo.appAthanOtherVoice.stateIn(viewModelScope, SharingStarted.Eagerly, "Makkah")
    val snoozeMinutes: StateFlow<Int> = repo.appSnoozeMinutes.stateIn(viewModelScope, SharingStarted.Eagerly, 5)
    val notificationsEnabled: StateFlow<Boolean> = repo.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val prePrayerAlertEnabled: StateFlow<Boolean> = repo.prePrayerAlertEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val prePrayerAlertMinutes: StateFlow<Int> = repo.prePrayerAlertMinutes.stateIn(viewModelScope, SharingStarted.Eagerly, 10)
    val autoSilentEnabled: StateFlow<Boolean> = repo.autoSilentEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val themeMode: StateFlow<String> = repo.appThemeMode.stateIn(viewModelScope, SharingStarted.Eagerly, "AUTO")
    val wallpaper: StateFlow<String> = repo.appWallpaper.stateIn(viewModelScope, SharingStarted.Eagerly, "DEFAULT")

    // --- Database Observables ---
    val tasbihCounters: StateFlow<List<TasbihCounter>> = repo.allTasbihCounters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = repo.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val quranHistory: StateFlow<List<QuranHistory>> = repo.quranHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allQuranNotes: StateFlow<List<QuranNote>> = repo.allQuranNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Astronomical Calculations State ---
    private val _prayerTimes = MutableStateFlow<PrayerCalculator.PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerCalculator.PrayerTimes?> = _prayerTimes.asStateFlow()

    private val _nextPrayerName = MutableStateFlow("")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    private val _nextPrayerTime = MutableStateFlow("")
    val nextPrayerTime: StateFlow<String> = _nextPrayerTime.asStateFlow()

    private val _countdownText = MutableStateFlow("00:00:00")
    val countdownText: StateFlow<String> = _countdownText.asStateFlow()

    private val _currentPrayerName = MutableStateFlow("")
    val currentPrayerName: StateFlow<String> = _currentPrayerName.asStateFlow()

    private val _moonPhase = MutableStateFlow(0.0)
    val moonPhase: StateFlow<Double> = _moonPhase.asStateFlow()

    private val _moonPhaseName = MutableStateFlow("")
    val moonPhaseName: StateFlow<String> = _moonPhaseName.asStateFlow()

    private val _hijriDateString = MutableStateFlow("")
    val hijriDateString: StateFlow<String> = _hijriDateString.asStateFlow()

    // --- Prayer tracking state ---

    // --- Daily Content Rotation (Authentic) ---
    val dailyHadith = HadithData.hadiths[0] // Intention hadith
    val dailyDua = AdhkarData.adhkar[0] // Morning adhkar as daily dua

    // --- Media Player State ---
    private var mediaPlayer: MediaPlayer? = null
    private val _isAthanPlaying = MutableStateFlow(false)
    val isAthanPlaying: StateFlow<Boolean> = _isAthanPlaying.asStateFlow()

    // --- Compass State ---
    private val _qiblaAngle = MutableStateFlow(0.0)
    val qiblaAngle: StateFlow<Double> = _qiblaAngle.asStateFlow()

    private val _distanceToKaaba = MutableStateFlow(0.0) // in km
    val distanceToKaaba: StateFlow<Double> = _distanceToKaaba.asStateFlow()

    // --- Prayer Logging State ---


    // --- Ramadan Tracking State ---


    // --- Real-time Location Tracking & Nearby Mosques ---
    private var locationCallback: LocationCallback? = null
    
    
    private val _isTrackingLocation = MutableStateFlow(false)
    val isTrackingLocation: StateFlow<Boolean> = _isTrackingLocation.asStateFlow()

    private val timezoneChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_TIMEZONE_CHANGED) {
                viewModelScope.launch {
                    if (repo.appLocationUsesDeviceZone.first()) {
                        repo.setLocation(locationName.value, latitude.value, longitude.value,
                            timeZoneId = ZoneId.systemDefault().id, usesDeviceZone = true)
                    }
                }
            }
        }
    }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            viewModelScope.launch(Dispatchers.IO) {
                if (!repo.appLocationUsesDeviceZone.first() && repo.appTimeZoneId.first().isBlank()) {
                    TimeZoneResolver.resolve(repo.appLatitude.first(), repo.appLongitude.first())?.let {
                        repo.saveTimeZoneId(it)
                        runCatching { AdhanScheduler.schedule(context) }
                        runCatching { PrayerTimesWidgetProvider.requestUpdate(context) }
                    }
                }
            }
        }
    }

    init {
        runCatching {
            ContextCompat.registerReceiver(context, timezoneChangeReceiver,
                IntentFilter(Intent.ACTION_TIMEZONE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
        }
        runCatching { context.getSystemService(ConnectivityManager::class.java)?.registerDefaultNetworkCallback(networkCallback) }
        viewModelScope.launch(Dispatchers.IO) {
            if (!repo.appLocationUsesDeviceZone.first() && repo.appTimeZoneId.first().isBlank()) {
                TimeZoneResolver.resolve(repo.appLatitude.first(), repo.appLongitude.first())?.let { repo.saveTimeZoneId(it) }
            }
        }
        // Collect coordinates and options to trigger prayer calculations dynamically
        viewModelScope.launch {
            val baseSettings = combine(latitude, longitude, madhab, calcMethod) { lat, lng, m, c ->
                PrayerCalculationSettings(lat, lng, m, c, ZoneId.systemDefault().id)
            }
            val calculationSettings = combine(baseSettings, timeZoneId) { settings, zone ->
                settings.copy(timeZoneId = zone)
            }
            combine(calculationSettings, locationConfigured) { settings, configured ->
                settings to configured
            }.collect { (settings, configured) ->
                calculateAllTimes(
                    settings.latitude,
                    settings.longitude,
                    settings.madhab,
                    settings.method,
                    configured,
                    settings.timeZoneId
                )
            }
        }

        // Countdown Timer Loop (Runs continuously, recalculating every second)
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                updateCountdown()
                delay(1000)
            }
        }

        // Prepopulate standard prophetic tasbih counters if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            val current = repo.allTasbihCounters.firstOrNull() ?: emptyList()
            if (current.isEmpty()) {
                val presets = listOf(
                    TasbihCounter(name = "سُبْحَانَ اللَّهِ", goal = 33, count = 0, lastUpdated = System.currentTimeMillis()),
                    TasbihCounter(name = "الْحَمْدُ لِلَّهِ", goal = 33, count = 0, lastUpdated = System.currentTimeMillis()),
                    TasbihCounter(name = "اللَّهُ أَكْبَرُ", goal = 34, count = 0, lastUpdated = System.currentTimeMillis()),
                    TasbihCounter(name = "لَا إِلَهَ إِلَّا اللَّهُ", goal = 100, count = 0, lastUpdated = System.currentTimeMillis()),
                    TasbihCounter(name = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", goal = 100, count = 0, lastUpdated = System.currentTimeMillis()),
                    TasbihCounter(name = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", goal = 100, count = 0, lastUpdated = System.currentTimeMillis()),
                    TasbihCounter(name = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", goal = 100, count = 0, lastUpdated = System.currentTimeMillis())
                )
                presets.forEach { repo.insertTasbihCounter(it) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationTracking() {
        if (_isTrackingLocation.value && locationCallback != null) return
        val context = getApplication<Application>().applicationContext
        
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            // Permission is requested by MainActivity. Never replace device location with IP geolocation.
            _isTrackingLocation.value = false
            return
        }
        
        _isTrackingLocation.value = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        // Fetch last known location instantly
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        updateLocationCoordinates(location.latitude, location.longitude)
                    }
                }
                .addOnFailureListener {
                    // Continue with active high-accuracy updates.
                }
        } catch (_: SecurityException) {
            _isTrackingLocation.value = false
            return
        }

        // Register location updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            15000L
        ).apply {
            setMinUpdateIntervalMillis(10000L)
        }.build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    updateLocationCoordinates(loc.latitude, loc.longitude)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                android.os.Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _isTrackingLocation.value = false
            locationCallback = null
        }
    }
    
    fun stopLocationTracking() {
        if (!_isTrackingLocation.value && locationCallback == null) return
        val context = getApplication<Application>().applicationContext
        try {
            locationCallback?.let {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.removeLocationUpdates(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            locationCallback = null
            _isTrackingLocation.value = false
        }
    }

    fun setLocation(name: String, lat: Double, lng: Double) {
        if (!lat.isFinite() || lat !in -90.0..90.0 ||
            !lng.isFinite() || lng !in -180.0..180.0 ||
            name.isBlank()
        ) return

        viewModelScope.launch {
            val zone = TimeZoneResolver.resolve(lat, lng)
            repo.setLocation(name.trim(), lat, lng, timeZoneId = zone, usesDeviceZone = false)
            runCatching { AdhanScheduler.schedule(getApplication()) }
            runCatching { PrayerTimesWidgetProvider.requestUpdate(context) }
        }
    }
    
    private fun calculateDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return r * c
    }

    private fun updateLocationCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch {
            val oldLat = latitude.value
            val oldLng = longitude.value
            val dist = calculateDist(oldLat, oldLng, lat, lng)
            // Even if GPS matches the default coordinates, mark it as confirmed on first fix.
            if (dist <= 0.05 && locationConfigured.value && repo.appLocationUsesDeviceZone.first()) return@launch

            // Persist coordinates immediately so prayer times, Qibla and mosque distance never
            // wait for reverse-geocoding to finish.
            repo.setLocation("My Location", lat, lng, timeZoneId = ZoneId.systemDefault().id, usesDeviceZone = true)
            try { AdhanScheduler.schedule(getApplication()) } catch (_: Exception) { }
            try { PrayerTimesWidgetProvider.requestUpdate(context) } catch (_: Exception) { }

            val geocoder = runCatching {
                Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
            }.getOrNull() ?: return@launch

            fun applyAddress(addresses: List<android.location.Address>) {
                val addr = addresses.firstOrNull() ?: return
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val country = addr.countryName.orEmpty()
                val countryCode = addr.countryCode.orEmpty()
                val isAlgeria = countryCode.equals("DZ", ignoreCase = true) ||
                    country.contains("Algeria", ignoreCase = true) ||
                    country.contains("الجزائر")
                val addressName = listOfNotNull(city, country.ifBlank { null })
                    .joinToString(", ")
                    .ifBlank { "My Location" }

                viewModelScope.launch {
                    repo.setLocation(addressName, lat, lng, timeZoneId = ZoneId.systemDefault().id, usesDeviceZone = true)
                    if (isAlgeria) repo.setCalcMethod("ALGERIA")
                    try { AdhanScheduler.schedule(getApplication()) } catch (_: Exception) { }
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        applyAddress(addresses)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    applyAddress(geocoder.getFromLocation(lat, lng, 1).orEmpty())
                }
            } catch (_: Exception) {
                // Coordinates are already persisted; address resolution is optional.
            }
        }
    }



    private fun calculateAllTimes(
        lat: Double,
        lng: Double,
        madhabStr: String,
        methodStr: String,
        isLocationConfigured: Boolean,
        timeZoneId: String
    ) {
        val zone = PrayerTimeEngine.zoneIdOrDevice(timeZoneId)
        val now = Instant.now()
        val localDate = PrayerTimeEngine.localDate(zone, now)

        val calcMethod = runCatching {
            PrayerCalculator.CalculationMethod.valueOf(methodStr)
        }.getOrDefault(PrayerCalculator.CalculationMethod.ALGERIA)
        val madhab = runCatching {
            PrayerCalculator.Madhab.valueOf(madhabStr)
        }.getOrDefault(PrayerCalculator.Madhab.STANDARD)

        val times = PrayerCalculator.calculateTimes(
            latitude = lat,
            longitude = lng,
            date = localDate,
            zoneId = zone,
            method = calcMethod,
            madhab = madhab
        )

        _prayerTimes.value = times

        // Calculate Qibla Math
        calculateQibla(lat, lng)

        // Calculate Moon Phase
        val phase = PrayerCalculator.calculateMoonPhase(localDate)
        _moonPhase.value = phase
        _moonPhaseName.value = PrayerCalculator.getMoonPhaseName(phase, "ar")

        // Calculate Hijri Date
        val hijri = PrayerCalculator.getHijriDate(localDate, "ar")
        _hijriDateString.value = "${hijri.day} ${hijri.monthName} ${hijri.year} هـ"
    }

    // Mecca Coordinates: Lat 21.4225, Lng 39.8262
    private fun calculateQibla(lat: Double, lng: Double) {
        val latRad = lat * PI / 180.0
        val lngRad = lng * PI / 180.0
        val meccaLat = 21.4225 * PI / 180.0
        val meccaLng = 39.8262 * PI / 180.0

        val dLng = meccaLng - lngRad

        val numerator = sin(dLng)
        val denominator = cos(latRad) * tan(meccaLat) - sin(latRad) * cos(dLng)

        var qibla = atan2(numerator, denominator) * 180.0 / PI
        if (qibla < 0) qibla += 360.0

        _qiblaAngle.value = qibla

        // Distance formula (haversine)
        val r = 6371.0 // earth radius in km
        val dLat = meccaLat - latRad
        val a = sin(dLat / 2).pow(2) + cos(latRad) * cos(meccaLat) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        _distanceToKaaba.value = r * c
    }

    private fun updateCountdown() {
        val times = _prayerTimes.value ?: return
        val zone = PrayerTimeEngine.zoneIdOrDevice(timeZoneId.value)
        val now = Instant.now()
        val localDate = PrayerTimeEngine.localDate(zone, now)
        // Rebuild today's prayer instants when the city's civil date rolls over.
        if (times.dhuhrDateTime.toLocalDate() != localDate) {
            calculateAllTimes(latitude.value, longitude.value, madhab.value, calcMethod.value,
                locationConfigured.value, timeZoneId.value)
            return
        }
        val prayers = listOf("Fajr" to times.fajrDateTime, "Dhuhr" to times.dhuhrDateTime,
            "Asr" to times.asrDateTime, "Maghrib" to times.maghribDateTime, "Isha" to times.ishaDateTime)
        val next = prayers.firstOrNull { it.second.toInstant().isAfter(now) } ?: run {
            val tomorrow = PrayerCalculator.calculateTimes(latitude.value, longitude.value, localDate.plusDays(1), zone,
                runCatching { PrayerCalculator.CalculationMethod.valueOf(calcMethod.value) }.getOrDefault(PrayerCalculator.CalculationMethod.ALGERIA),
                runCatching { PrayerCalculator.Madhab.valueOf(madhab.value) }.getOrDefault(PrayerCalculator.Madhab.STANDARD))
            "Fajr" to tomorrow.fajrDateTime
        }
        _nextPrayerName.value = next.first
        _currentPrayerName.value = prayers.lastOrNull { !it.second.toInstant().isAfter(now) }?.first ?: "Isha"
        _nextPrayerTime.value = PrayerTimeEngine.format(next.second)
        val seconds = Duration.between(now, next.second.toInstant()).seconds.coerceAtLeast(0)
        _countdownText.value = String.format(Locale.US, "%02d:%02d:%02d", seconds/3600, (seconds%3600)/60, seconds%60)
    }




    // --- Action Handlers ---

    // Toggle Prayer Status

    // Tasbih Actions
    fun createTasbih(name: String, goal: Int) {
        viewModelScope.launch {
            repo.insertTasbihCounter(
                TasbihCounter(name = name, goal = goal, count = 0, lastUpdated = System.currentTimeMillis())
            )
        }
    }

    fun incrementTasbih(counter: TasbihCounter) {
        viewModelScope.launch {
            val updated = counter.copy(
                count = counter.count + 1,
                lastUpdated = System.currentTimeMillis()
            )
            repo.updateTasbihCounter(updated)
            if (counter.hapticEnabled) {
                // If count reaches target, trigger long haptic, else short
                if (updated.count % updated.goal == 0) {
                    triggerHapticFeedback(long = true)
                } else {
                    triggerHapticFeedback(long = false)
                }
            }
        }
    }

    fun decrementTasbih(counter: TasbihCounter) {
        if (counter.count <= 0) return
        viewModelScope.launch {
            val updated = counter.copy(
                count = (counter.count - 1).coerceAtLeast(0),
                lastUpdated = System.currentTimeMillis()
            )
            repo.updateTasbihCounter(updated)
            if (counter.hapticEnabled) {
                triggerHapticFeedback(long = false)
            }
        }
    }

    fun resetTasbih(counter: TasbihCounter) {
        viewModelScope.launch {
            repo.updateTasbihCounter(
                counter.copy(count = 0, lastUpdated = System.currentTimeMillis())
            )
            triggerHapticFeedback()
        }
    }

    fun deleteTasbih(counter: TasbihCounter) {
        viewModelScope.launch {
            repo.deleteTasbihCounter(counter)
        }
    }

    // Bookmarks Actions
    fun toggleBookmark(type: String, referenceId: String, title: String, subtitle: String, arabicText: String = "", translationText: String = "") {
        viewModelScope.launch {
            val exists = bookmarks.value.any { it.type == type && it.referenceId == referenceId }
            if (exists) {
                repo.deleteBookmark(type, referenceId)
            } else {
                repo.insertBookmark(
                    Bookmark(
                        type = type,
                        referenceId = referenceId,
                        title = title,
                        subtitle = subtitle,
                        arabicText = arabicText,
                        translationText = translationText
                    )
                )
            }
            triggerHapticFeedback()
        }
    }

    fun isBookmarked(type: String, referenceId: String): Flow<Boolean> {
        return repo.isBookmarked(type, referenceId)
    }

    // Quran Progress
    fun saveLastReadSurah(surahId: Int, ayahId: Int, surahName: String) {
        viewModelScope.launch {
            repo.insertQuranHistory(
                QuranHistory(surahId = surahId, ayahId = ayahId, surahName = surahName)
            )
        }
    }

    // Settings actions
    fun setPrePrayerAlert(enabled: Boolean, minutes: Int = prePrayerAlertMinutes.value) {
        viewModelScope.launch {
            repo.setPrePrayerAlert(enabled, minutes)
            AdhanScheduler.schedule(context)
            runCatching { PrayerTimesWidgetProvider.requestUpdate(context) }
        }
    }

    fun setAutoSilentEnabled(enabled: Boolean): Boolean {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted) {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                return false
            }
        }
        viewModelScope.launch {
            repo.setAutoSilentEnabled(enabled)
            AdhanScheduler.schedule(context)
        }
        return true
    }


    fun setCalculationMethod(method: String) {
        viewModelScope.launch { repo.setCalcMethod(method); AdhanScheduler.schedule(getApplication()); runCatching { PrayerTimesWidgetProvider.requestUpdate(context) } }
    }

    fun setMadhab(madhabName: String) {
        viewModelScope.launch { repo.setMadhab(madhabName); AdhanScheduler.schedule(getApplication()); runCatching { PrayerTimesWidgetProvider.requestUpdate(context) } }
    }

    fun toggleNotifications(enabled: Boolean) {
        val appContext = getApplication<Application>().applicationContext
        viewModelScope.launch {
            repo.setNotificationsEnabled(enabled)
            AdhanScheduler.schedule(appContext)
        }
    }

    fun setAthanVoices(fajr: String, other: String) {
        viewModelScope.launch {
            repo.setAthanFajrVoice(fajr)
            repo.setAthanOtherVoice(other)
        }
    }

    fun setSnooze(minutes: Int) {
        viewModelScope.launch { repo.setSnoozeMinutes(minutes) }
    }

    fun setManualCity(city: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val zone = TimeZoneResolver.resolve(lat, lng)
            repo.setLocation(city, lat, lng, timeZoneId = zone, usesDeviceZone = false)
            AdhanScheduler.schedule(getApplication())
            runCatching { PrayerTimesWidgetProvider.requestUpdate(context) }
            triggerHapticFeedback()
        }
    }





    // --- Media Player: Athan Recitations ---
    fun playAthan() {
        if (_isAthanPlaying.value) {
            stopAthan()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.release()
                
                // Famous Athan voices URLs:
                // Let's use clean public CDN streams:
                val mPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    // Use the same locally saved Muadhin selection as scheduled prayer alarms.
                    val selectedMuadhin = _defaultMuadhin.value
                    val previewUrl = when (selectedMuadhin) {
                        "MADINAH" -> "https://www.islamcan.com/audio/adhan/azan2.mp3"
                        "ALAFASY" -> "https://www.islamcan.com/audio/adhan/azan8.mp3"
                        "FAJR_SPECIAL" -> "https://download.tvquran.com/download/selections/180/58b0dac02106f.mp3"
                        else -> "https://www.islamcan.com/audio/adhan/azan1.mp3"
                    }
                    setDataSource(previewUrl)
                    prepare()
                    start()
                }
                mediaPlayer = mPlayer
                _isAthanPlaying.value = true

                mPlayer.setOnCompletionListener {
                    _isAthanPlaying.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAthan() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _isAthanPlaying.value = false
    }

    fun setDefaultReciter(reciter: String) {
        _defaultReciter.value = reciter
        audioPrefs.edit().putString("default_reciter", reciter).apply()
    }

    fun setDefaultMuadhin(muadhin: String) {
        val supported = setOf("MAKKAH", "MADINAH", "ALAFASY", "FAJR_SPECIAL")
        val normalized = muadhin.takeIf { it in supported } ?: "MAKKAH"
        _defaultMuadhin.value = normalized
        audioPrefs.edit().putString("default_muadhin", normalized).apply()
    }

    // --- Haptic Feedback Utility ---
    private fun triggerHapticFeedback(long: Boolean = false) {
        if (!_hapticsEnabled.value) return
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (long) {
                    VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                if (long) vibrator.vibrate(300) else vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    // --- Theme & Appearance Writers ---
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repo.setThemeMode(mode)
        }
    }

    fun setWallpaper(wp: String) {
        viewModelScope.launch {
            repo.setWallpaper(wp)
        }
    }

    // --- Preferred Mosque Writers ---


    // --- Favorite Mosques Writers ---






    // --- Quran Notes Writers ---
    fun getNotesForAyah(surahId: Int, ayahId: Int): Flow<List<QuranNote>> {
        return repo.getNotesForAyah(surahId, ayahId)
    }

    fun insertQuranNote(note: QuranNote) {
        viewModelScope.launch {
            repo.insertQuranNote(note)
        }
    }

    fun deleteQuranNote(id: Int) {
        viewModelScope.launch {
            repo.deleteQuranNote(id)
        }
    }

    override fun onCleared() {
        runCatching { context.unregisterReceiver(timezoneChangeReceiver) }
        runCatching { context.getSystemService(ConnectivityManager::class.java)?.unregisterNetworkCallback(networkCallback) }
        stopLocationTracking()
        stopAthan()
        super.onCleared()
    }


}
