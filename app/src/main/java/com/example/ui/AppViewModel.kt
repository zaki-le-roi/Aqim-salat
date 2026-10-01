package com.example.ui

import android.app.Application
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.adhan.AdhanScheduler
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// For high-fidelity location services and real geocoder addresses
import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.location.Geocoder

data class LocalMosque(
    val id: Long,
    val nameAr: String,
    val nameEn: String,
    val lat: Double,
    val lng: Double,
    val addressAr: String,
    val addressEn: String,
    val distanceKm: Double = 0.0
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = AppDatabase.getDatabase(context)
    private val repo = AppRepository(db, context)
    private val pollsPrefs = context.getSharedPreferences("local_polls", Context.MODE_PRIVATE)
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

    // --- State Observables ---
    val language: StateFlow<String> = repo.appLanguage.stateIn(viewModelScope, SharingStarted.Eagerly, "ar")
    val madhab: StateFlow<String> = repo.appMadhab.stateIn(viewModelScope, SharingStarted.Eagerly, "STANDARD")
    val calcMethod: StateFlow<String> = repo.appCalcMethod.stateIn(viewModelScope, SharingStarted.Eagerly, "MWL")
    val latitude: StateFlow<Double> = repo.appLatitude.stateIn(viewModelScope, SharingStarted.Eagerly, 21.4225)
    val longitude: StateFlow<Double> = repo.appLongitude.stateIn(viewModelScope, SharingStarted.Eagerly, 39.8262)
    val locationName: StateFlow<String> = repo.appLocationName.stateIn(viewModelScope, SharingStarted.Eagerly, "Makkah, Saudi Arabia")
    val athanFajrVoice: StateFlow<String> = repo.appAthanFajrVoice.stateIn(viewModelScope, SharingStarted.Eagerly, "Fajr Medina")
    val athanOtherVoice: StateFlow<String> = repo.appAthanOtherVoice.stateIn(viewModelScope, SharingStarted.Eagerly, "Makkah")
    val snoozeMinutes: StateFlow<Int> = repo.appSnoozeMinutes.stateIn(viewModelScope, SharingStarted.Eagerly, 5)
    val notificationsEnabled: StateFlow<Boolean> = repo.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val themeMode: StateFlow<String> = repo.appThemeMode.stateIn(viewModelScope, SharingStarted.Eagerly, "AUTO")
    val wallpaper: StateFlow<String> = repo.appWallpaper.stateIn(viewModelScope, SharingStarted.Eagerly, "DEFAULT")
    val prefMosqueId: StateFlow<String> = repo.prefMosqueId.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val prefMosqueName: StateFlow<String> = repo.prefMosqueName.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val prefMosqueLat: StateFlow<Double> = repo.prefMosqueLat.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)
    val prefMosqueLng: StateFlow<Double> = repo.prefMosqueLng.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)
    val prefMosqueAddr: StateFlow<String> = repo.prefMosqueAddr.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val prefMosqueRemind: StateFlow<Boolean> = repo.prefMosqueRemind.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // --- Database Observables ---
    val tasbihCounters: StateFlow<List<TasbihCounter>> = repo.allTasbihCounters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = repo.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val quranHistory: StateFlow<List<QuranHistory>> = repo.quranHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteMosques: StateFlow<List<FavoriteMosque>> = repo.favoriteMosques.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allKhatmahs: StateFlow<List<Khatmah>> = repo.allKhatmahs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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

    // --- Community, Polls & Fajr Tracker State ---
    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _communityPolls = MutableStateFlow<List<CommunityPoll>>(emptyList())
    val communityPolls: StateFlow<List<CommunityPoll>> = _communityPolls.asStateFlow()

    private val _fajrRecords = MutableStateFlow<List<FajrDayRecord>>(emptyList())
    val fajrRecords: StateFlow<List<FajrDayRecord>> = _fajrRecords.asStateFlow()

    // --- Daily Content Rotation (Authentic) ---
    val dailyVerse = QuranData.localAyahs[1]!![1] // Default Al-Fatihah
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
    private val _loggedPrayers = MutableStateFlow<Map<String, String>>(emptyMap())
    val loggedPrayers: StateFlow<Map<String, String>> = _loggedPrayers.asStateFlow()

    // --- Ramadan Tracking State ---
    private val _ramadanDaysRemaining = MutableStateFlow(0)
    val ramadanDaysRemaining: StateFlow<Int> = _ramadanDaysRemaining.asStateFlow()

    private val _isFastingToday = MutableStateFlow(false)
    val isFastingToday: StateFlow<Boolean> = _isFastingToday.asStateFlow()

    // --- Real-time Location Tracking & Nearby Mosques ---
    private var locationCallback: LocationCallback? = null
    
    private val _nearbyRealMosques = MutableStateFlow<List<LocalMosque>>(emptyList())
    val nearbyRealMosques: StateFlow<List<LocalMosque>> = _nearbyRealMosques.asStateFlow()
    val nearestRealMosque: StateFlow<LocalMosque?> = nearbyRealMosques.map { it.firstOrNull() }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    private val _isTrackingLocation = MutableStateFlow(false)
    val isTrackingLocation: StateFlow<Boolean> = _isTrackingLocation.asStateFlow()

    init {
        // Collect coordinates and options to trigger prayer calculations dynamically
        viewModelScope.launch {
            combine(latitude, longitude, madhab, calcMethod, language) { lat, lng, m, c, lang ->
                calculateAllTimes(lat, lng, m, c, lang)
            }.collect()
        }

        // Countdown Timer Loop (Runs continuously, recalculating every second)
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                updateCountdown()
                delay(1000)
            }
        }

        // Initialize Today's Prayer logs
        loadTodayLogs()
        calculateRamadanCountdown()
        val fastingKey = "fasting_" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        _isFastingToday.value = context.getSharedPreferences("daily_worship", Context.MODE_PRIVATE).getBoolean(fastingKey, false)
        initCommunityAndPolls()

        // Fetch initial set of real mosques from Overpass around current coordinates
        viewModelScope.launch {
            fetchRealNearbyMosques(latitude.value, longitude.value)
        }
    }

    fun detectLocationByIp() {
        _isTrackingLocation.value = true
        viewModelScope.launch(Dispatchers.IO) {
            var success = false
            try {
                val url = java.net.URL("https://ip-api.com/json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.requestMethod = "GET"
                
                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(response)
                    if (json.getString("status") == "success") {
                        val lat = json.getDouble("lat")
                        val lon = json.getDouble("lon")
                        val city = json.optString("city", "Detected Location")
                        val country = json.optString("country", "")
                        val countryCode = json.optString("countryCode", "")
                        
                        val isAlgeria = countryCode.equals("DZ", ignoreCase = true) || 
                                        country.contains("Algeria", ignoreCase = true) || 
                                        country.contains("الجزائر")
                        
                        val addressName = if (country.isNotEmpty()) "$city, $country" else city
                        
                        viewModelScope.launch(Dispatchers.Main) {
                            repo.setLocation(addressName, lat, lon)
                            fetchRealNearbyMosques(lat, lon)
                            if (isAlgeria) {
                                repo.setCalcMethod("ALGERIA")
                            }
                            _isTrackingLocation.value = false
                        }
                        success = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!success) {
                // Try fallback to ipapi.co
                try {
                    val url = java.net.URL("https://ipapi.co/json/")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                    if (conn.responseCode == 200) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(response)
                        val lat = json.getDouble("latitude")
                        val lon = json.getDouble("longitude")
                        val city = json.optString("city", "Detected Location")
                        val country = json.optString("country_name", "")
                        val countryCode = json.optString("country_code", "")
                        val isAlgeria = countryCode.equals("DZ", ignoreCase = true) || 
                                        country.contains("Algeria", ignoreCase = true) || 
                                        country.contains("الجزائر")
                        val addressName = if (country.isNotEmpty()) "$city, $country" else city
                        viewModelScope.launch(Dispatchers.Main) {
                            repo.setLocation(addressName, lat, lon)
                            fetchRealNearbyMosques(lat, lon)
                            if (isAlgeria) {
                                repo.setCalcMethod("ALGERIA")
                            }
                            _isTrackingLocation.value = false
                        }
                        success = true
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            if (!success) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isTrackingLocation.value = false
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationTracking() {
        if (_isTrackingLocation.value) return
        val context = getApplication<Application>().applicationContext
        
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            // Exact prayer times and nearby-mosque ranking require device location.
            // Never silently replace precise GPS with IP geolocation.
            runCatching {
                val settingsIntent = Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$context.packageName")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
            }
            return
        }
        
        _isTrackingLocation.value = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        // Fetch last known location instantly
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    updateLocationCoordinates(location.latitude, location.longitude)
                    _isTrackingLocation.value = false
                } else {
                    // Last location is null, trigger IP Geolocation as fallback
                    detectLocationByIp()
                }
            }.addOnFailureListener {
                // Failed, trigger IP Geolocation as fallback
                detectLocationByIp()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            detectLocationByIp()
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
        if (!_isTrackingLocation.value) return
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
        viewModelScope.launch {
            repo.setLocation(name, lat, lng)
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
            if (dist > 0.05) { // more than 50 meters
                var addressName = "Detected Location"
                try {
                    val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                                val country = addr.countryName ?: ""
                                val countryCode = addr.countryCode ?: ""
                                val isAlgeria = countryCode.equals("DZ", ignoreCase = true) || 
                                                country.contains("Algeria", ignoreCase = true) || 
                                                country.contains("الجزائر")
                                
                                addressName = if (country.isNotEmpty()) "$city, $country" else city
                                viewModelScope.launch {
                                    repo.setLocation(addressName, lat, lng)
                                    if (isAlgeria) {
                                        repo.setCalcMethod("ALGERIA")
                                    }
                                }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        val addr = addresses?.firstOrNull()
                        if (addr != null) {
                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                            val country = addr.countryName ?: ""
                            val countryCode = addr.countryCode ?: ""
                            val isAlgeria = countryCode.equals("DZ", ignoreCase = true) || 
                                            country.contains("Algeria", ignoreCase = true) || 
                                            country.contains("الجزائر")
                            
                            addressName = if (country.isNotEmpty()) "$city, $country" else city
                            viewModelScope.launch {
                                repo.setLocation(addressName, lat, lng)
                                if (isAlgeria) {
                                    repo.setCalcMethod("ALGERIA")
                                }
                            }
                        } else {
                            repo.setLocation("My Location", lat, lng)
                            AdhanScheduler.schedule(getApplication())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    repo.setLocation("My Location", lat, lng)
                }
                
                fetchRealNearbyMosques(lat, lng)
            }
        }
    }
    
    fun openMosqueNavigation(mosque: LocalMosque) {
        val uri = Uri.parse("geo:${mosque.lat},${mosque.lng}?q=${Uri.encode(mosque.nameAr)}")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun fetchRealNearbyMosques(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val queryStr = """
                    [out:json][timeout:20];
                    (
                      nwr["amenity"="place_of_worship"]["religion"="muslim"](around:12000,$lat,$lng);
                      nwr["building"="mosque"](around:10000,$lat,$lng);
                    );
                    out center tags;
                """.trimIndent()
                val encodedQuery = java.net.URLEncoder.encode(queryStr, "UTF-8")
                val endpoints = listOf(
                    "https://overpass-api.de/api/interpreter?data=",
                    "https://overpass.kumi.systems/api/interpreter?data="
                )
                var response: String? = null
                for (endpoint in endpoints) {
                    try {
                        val conn = (java.net.URL(endpoint + encodedQuery).openConnection() as java.net.HttpURLConnection).apply {
                            connectTimeout = 8000
                            readTimeout = 20000
                            requestMethod = "GET"
                            setRequestProperty("User-Agent", "Aqim-Salat/1.2 Android")
                        }
                        if (conn.responseCode == 200) {
                            response = conn.inputStream.bufferedReader().use { it.readText() }
                            conn.disconnect()
                            if (!response.isNullOrBlank()) break
                        } else {
                            conn.disconnect()
                        }
                    } catch (_: Exception) {
                        // Try the next public Overpass instance.
                    }
                }

                if (!response.isNullOrBlank()) {
                    val elements = org.json.JSONObject(response).optJSONArray("elements") ?: org.json.JSONArray()
                    val unique = LinkedHashMap<String, LocalMosque>()

                    for (i in 0 until elements.length()) {
                        val el = elements.getJSONObject(i)
                        val id = el.optLong("id", 0L)
                        if (id == 0L) continue

                        val center = el.optJSONObject("center")
                        val mLat = if (el.has("lat")) el.getDouble("lat") else center?.optDouble("lat", Double.NaN) ?: Double.NaN
                        val mLng = if (el.has("lon")) el.getDouble("lon") else center?.optDouble("lon", Double.NaN) ?: Double.NaN
                        if (mLat.isNaN() || mLng.isNaN()) continue

                        val tags = el.optJSONObject("tags")
                        val name = tags?.optString("name", "")?.trim().orEmpty()
                        val nameAr = tags?.optString("name:ar", "")?.trim().orEmpty().ifBlank {
                            if (name.isNotBlank()) name else "مسجد"
                        }
                        val nameEn = tags?.optString("name:en", "")?.trim().orEmpty().ifBlank {
                            if (name.isNotBlank()) name else "Mosque"
                        }

                        val street = tags?.optString("addr:street", "")?.trim().orEmpty()
                        val city = tags?.optString("addr:city", "")?.trim().orEmpty()
                        val suburb = tags?.optString("addr:suburb", "")?.trim().orEmpty()
                        val district = listOf(suburb, city).filter { it.isNotBlank() }.joinToString("، ")
                        val addressAr = listOf(street, district).filter { it.isNotBlank() }.joinToString("، ")
                            .ifBlank { "مسجد قريب من موقعك" }
                        val addressEn = listOf(street, district).filter { it.isNotBlank() }.joinToString(", ")
                            .ifBlank { "Mosque near your location" }

                        val key = "$mLat,$mLng"
                        unique[key] = LocalMosque(id, nameAr, nameEn, mLat, mLng, addressAr, addressEn)
                    }

                    _nearbyRealMosques.value = unique.values
                        .map { mosque -> mosque.copy(distanceKm = calculateDist(lat, lng, mosque.lat, mosque.lng)) }
                        .sortedBy { it.distanceKm }
                        .take(100)
                } else {
                    _nearbyRealMosques.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _nearbyRealMosques.value = emptyList()
            }
        }
    }

    private fun calculateAllTimes(lat: Double, lng: Double, madhabStr: String, methodStr: String, lang: String) {
        val date = Date()
        val calendar = Calendar.getInstance()
        val zone = calendar.timeZone
        val offsetHours = zone.getOffset(date.time) / 3600000.0

        val calcMethod = PrayerCalculator.CalculationMethod.valueOf(methodStr)
        val madhab = PrayerCalculator.Madhab.valueOf(madhabStr)

        val times = PrayerCalculator.calculateTimes(
            latitude = lat,
            longitude = lng,
            timezoneOffset = offsetHours,
            date = date,
            method = calcMethod,
            madhab = madhab
        )

        _prayerTimes.value = times

        // Calculate Qibla Math
        calculateQibla(lat, lng)

        // Calculate Moon Phase
        val phase = PrayerCalculator.calculateMoonPhase(date)
        _moonPhase.value = phase
        _moonPhaseName.value = PrayerCalculator.getMoonPhaseName(phase, lang)

        // Calculate Hijri Date
        val hijri = PrayerCalculator.getHijriDate(date, lang)
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
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMin = now.get(Calendar.MINUTE)
        val currentSec = now.get(Calendar.SECOND)

        val totalNowSecs = currentHour * 3600 + currentMin * 60 + currentSec

        // Convert times to seconds
        fun toSecs(t: String): Int {
            val parts = t.split(":")
            if (parts.size < 2) return 0
            return parts[0].toInt() * 3600 + parts[1].toInt() * 60
        }

        val fajrSec = toSecs(times.fajr)
        val sunriseSec = toSecs(times.sunrise)
        val dhuhrSec = toSecs(times.dhuhr)
        val asrSec = toSecs(times.asr)
        val maghribSec = toSecs(times.maghrib)
        val ishaSec = toSecs(times.isha)

        val prayers = listOf(
            "Fajr" to fajrSec,
            "Sunrise" to sunriseSec,
            "Dhuhr" to dhuhrSec,
            "Asr" to asrSec,
            "Maghrib" to maghribSec,
            "Isha" to ishaSec
        )

        // Find next prayer
        var nextName = "Fajr"
        var nextSec = fajrSec + 24 * 3600
        var currentName = "Isha"

        for (i in prayers.indices) {
            val (name, sec) = prayers[i]
            if (totalNowSecs < sec) {
                nextName = name
                nextSec = sec
                currentName = if (i == 0) "Isha" else prayers[i - 1].first
                break
            }
        }

        _nextPrayerName.value = nextName
        _currentPrayerName.value = currentName

        // Format times for display
        val nextHourStr = String.format("%02d:%02d", (nextSec % 86400) / 3600, ((nextSec % 86400) % 3600) / 60)
        _nextPrayerTime.value = nextHourStr

        // Countdown math
        val diffSecs = nextSec - totalNowSecs
        val h = diffSecs / 3600
        val m = (diffSecs % 3600) / 60
        val s = diffSecs % 60
        _countdownText.value = String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun loadTodayLogs() {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdf.format(Date())
            repo.getLogsForDate(todayStr).collect { logs ->
                val logMap = logs.associate { it.prayerName to it.status }
                _loggedPrayers.value = logMap
            }
        }
    }

    private fun calculateRamadanCountdown() {
        // Find days remaining until Ramadan
        // The tabular tabular calendar calculates the Hijri year.
        // Ramadan starts on 1st Ramadan. Let's make an robust calendar difference calculation!
        val now = Date()
        val hDate = PrayerCalculator.getHijriDate(now, "en")
        
        // Approximate tabular day distance calculation:
        val currentYear = hDate.year
        // Tabular Ramadan is Month 9.
        // If current month is Ramadan (9), and we are on day X, next Ramadan is next year month 9.
        // Each Hijri month averages 29.53 days.
        val monthsRemaining = if (hDate.month <= 9) {
            9 - hDate.month
        } else {
            12 - hDate.month + 9
        }
        val daysApprox = (monthsRemaining * 29.53).toInt() - hDate.day + 1
        _ramadanDaysRemaining.value = if (daysApprox < 0) 354 + daysApprox else daysApprox
    }

    // --- Action Handlers ---

    // Toggle Prayer Status
    fun togglePrayerStatus(prayerName: String, currentStatus: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdf.format(Date())
            
            val nextStatus = when (currentStatus) {
                "NOT_YET" -> "PRAYED_ON_TIME"
                "PRAYED_ON_TIME" -> "PRAYED_LATE"
                "PRAYED_LATE" -> "MISSED"
                else -> "NOT_YET"
            }

            if (nextStatus == "NOT_YET") {
                repo.deletePrayerLog(todayStr, prayerName)
            } else {
                repo.insertPrayerLog(
                    PrayerLog(
                        date = todayStr,
                        prayerName = prayerName,
                        status = nextStatus
                    )
                )
            }
            // Trigger haptic if supported
            triggerHapticFeedback()
        }
    }

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
    fun setAppLanguage(lang: String) {
        viewModelScope.launch { repo.setLanguage(lang) }
    }

    fun setCalculationMethod(method: String) {
        viewModelScope.launch { repo.setCalcMethod(method); AdhanScheduler.schedule(getApplication()) }
    }

    fun setMadhab(madhabName: String) {
        viewModelScope.launch { repo.setMadhab(madhabName); AdhanScheduler.schedule(getApplication()) }
    }

    fun toggleNotifications(enabled: Boolean) {
        val appContext = getApplication<Application>().applicationContext
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = appContext.getSystemService(AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                runCatching {
                    val intent = Intent(
                        android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:${appContext.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    appContext.startActivity(intent)
                }
                return
            }
        }
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
            repo.setLocation(city, lat, lng)
            AdhanScheduler.schedule(getApplication())
            triggerHapticFeedback()
        }
    }

    private val qazaPrefs by lazy { context.getSharedPreferences("qaza_prayers", Context.MODE_PRIVATE) }

    fun getQazaCount(key: String): Int = qazaPrefs.getInt(key, 0)

    fun changeQazaCount(key: String, delta: Int) {
        val current = getQazaCount(key)
        val updated = (current + delta).coerceAtLeast(0)
        qazaPrefs.edit().putInt(key, updated).apply()
        triggerHapticFeedback()
    }

    fun toggleFastingToday() {
        val key = "fasting_" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefs = context.getSharedPreferences("daily_worship", Context.MODE_PRIVATE)
        val value = !prefs.getBoolean(key, false)
        prefs.edit().putBoolean(key, value).apply()
        _isFastingToday.value = value
        triggerHapticFeedback()
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
                    // URL streaming Athan (Makkah recitation)
                    setDataSource("https://download.tvquran.com/download/TvQuran.com__Athan/TvQuran.com__04.athan.mp3")
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
    fun setPreferredMosque(id: String, name: String, lat: Double, lng: Double, addr: String) {
        viewModelScope.launch {
            repo.setPreferredMosque(id, name, lat, lng, addr)
        }
    }

    fun setPreferredMosqueRemind(enabled: Boolean) {
        viewModelScope.launch {
            repo.setPreferredMosqueRemind(enabled)
        }
    }

    // --- Favorite Mosques Writers ---
    fun insertFavoriteMosque(mosque: FavoriteMosque) {
        viewModelScope.launch {
            repo.insertFavoriteMosque(mosque)
        }
    }

    fun deleteFavoriteMosque(id: Long) {
        viewModelScope.launch {
            repo.deleteFavoriteMosque(id)
        }
    }

    fun isFavoriteMosque(id: Long): Flow<Boolean> {
        return repo.isFavoriteMosque(id)
    }

    // --- Khatmahs Writers ---
    fun insertKhatmah(khatmah: Khatmah) {
        viewModelScope.launch {
            repo.insertKhatmah(khatmah)
        }
    }

    fun updateKhatmah(khatmah: Khatmah) {
        viewModelScope.launch {
            repo.updateKhatmah(khatmah)
        }
    }

    fun deleteKhatmah(khatmah: Khatmah) {
        viewModelScope.launch {
            repo.deleteKhatmah(khatmah)
        }
    }

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

    // --- Admin StateFlows & CRUD ---
    val adminHadiths = repo.allAdminHadiths.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertAdminHadith(hadith: AdminHadith) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.insertAdminHadith(hadith) }
    fun deleteAdminHadith(id: Int) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.deleteAdminHadith(id) }

    val adminAdhkars = repo.allAdminAdhkars.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertAdminAdhkar(adhkar: AdminAdhkar) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.insertAdminAdhkar(adhkar) }
    fun deleteAdminAdhkar(id: Int) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.deleteAdminAdhkar(id) }

    val adminDuas = repo.allAdminDuas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertAdminDua(dua: AdminDua) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.insertAdminDua(dua) }
    fun deleteAdminDua(id: Int) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.deleteAdminDua(id) }

    val adminArticles = repo.allAdminArticles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertAdminArticle(article: AdminArticle) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.insertAdminArticle(article) }
    fun deleteAdminArticle(id: Int) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.deleteAdminArticle(id) }

    val adminBannersReminders = repo.allAdminBannersReminders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertAdminBannerReminder(br: AdminBannerReminder) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.insertAdminBannerReminder(br) }
    fun deleteAdminBannerReminder(id: Int) = viewModelScope.launch { if (hasAdminPermission("EDIT_CONTENT")) repo.deleteAdminBannerReminder(id) }

    val donationCampaigns = repo.allDonationCampaigns.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertDonationCampaign(campaign: DonationCampaign) = viewModelScope.launch { if (hasAdminPermission("MANAGE_DONATIONS")) repo.insertDonationCampaign(campaign) }
    fun deleteDonationCampaign(id: Int) = viewModelScope.launch { if (hasAdminPermission("MANAGE_DONATIONS")) repo.deleteDonationCampaign(id) }

    val notificationLogs = repo.allNotificationLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun insertNotificationLog(title: String, body: String, audience: String) = viewModelScope.launch {
        if (!hasAdminPermission("SEND_ALERTS")) return@launch
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        repo.insertNotificationLog(NotificationLog(title = title, body = body, audience = audience, sentTime = sdf.format(java.util.Date())))
    }

    val adminAccounts = repo.allAdminAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // App Members Flows and Management
    val allMembers = repo.allMembers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val loggedInMember = MutableStateFlow<AppMember?>(null)

    private val _adminRole = MutableStateFlow<String?>(null)
    val adminRole: StateFlow<String?> = _adminRole.asStateFlow()
    private val _adminPermissions = MutableStateFlow<Set<String>>(emptySet())
    val adminPermissions: StateFlow<Set<String>> = _adminPermissions.asStateFlow()

    private fun hasAdminPermission(permission: String): Boolean =
        _adminPermissions.value.contains("ALL") || _adminPermissions.value.contains(permission)

    fun registerMember(
        name: String,
        email: String,
        password: String,
        country: String,
        city: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || password.length < 8) {
                onFailure("أدخل الاسم والبريد وكلمة مرور من 8 أحرف على الأقل.")
                return@launch
            }
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email.trim().lowercase(), password)
                    .addOnSuccessListener { result ->
                        val userEmail = result.user?.email?.trim()?.lowercase()
                        if (userEmail.isNullOrBlank()) {
                            onFailure("تعذر إنشاء الحساب.")
                            return@addOnSuccessListener
                        }
                        viewModelScope.launch {
                            val newMember = AppMember(
                                name = name.trim(),
                                email = userEmail,
                                country = country.trim(),
                                city = city.trim(),
                                points = 150,
                                registrationDate = System.currentTimeMillis(),
                                streakDays = 1,
                                isActive = true
                            )
                            repo.insertMember(newMember)
                            loggedInMember.value = newMember
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { error ->
                        onFailure(error.message ?: "تعذر إنشاء الحساب.")
                    }
            } catch (_: Exception) {
                onFailure("خدمة العضوية غير متاحة حاليًا.")
            }
        }
    }

    fun loginMember(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email.trim().lowercase(), password)
                    .addOnSuccessListener { result ->
                        val authenticatedEmail = result.user?.email?.trim()?.lowercase()
                        val matched = allMembers.value.find { it.email.trim().lowercase() == authenticatedEmail }
                        if (matched == null) {
                            auth.signOut()
                            onFailure("الحساب موثق، لكن ملف العضوية غير موجود.")
                        } else if (!matched.isActive) {
                            auth.signOut()
                            onFailure("هذا الحساب تم تجميده من قبل الإدارة.")
                        } else {
                            loggedInMember.value = matched
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { error ->
                        onFailure(error.message ?: "تعذر تسجيل الدخول.")
                    }
            } catch (_: Exception) {
                onFailure("خدمة العضوية غير متاحة حاليًا.")
            }
        }
    }

    fun logoutMember() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
        }
        loggedInMember.value = null
    }

    fun updateMemberPoints(memberId: Int, pointsToAdd: Int) {
        if (loggedInMember.value?.id != memberId && !hasAdminPermission("ALL")) return
        viewModelScope.launch {
            val matched = allMembers.value.find { it.id == memberId }
            if (matched != null) {
                val updated = matched.copy(points = matched.points + pointsToAdd)
                repo.insertMember(updated)
                if (loggedInMember.value?.id == memberId) {
                    loggedInMember.value = updated
                }
            }
        }
    }

    fun deleteMember(id: Int) = viewModelScope.launch {
        if (!hasAdminPermission("ALL")) return@launch
        repo.deleteMember(id)
    }

    fun setMemberActiveState(id: Int, isActive: Boolean) = viewModelScope.launch {
        if (!hasAdminPermission("ALL")) return@launch
        val matched = allMembers.value.find { it.id == id }
        if (matched != null) {
            repo.insertMember(matched.copy(isActive = isActive))
        }
    }

    fun authenticateAdmin(
        emailInput: String,
        passwordInput: String,
        onSuccess: (role: String, permissions: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val email = emailInput.trim().lowercase()
        if (email.isBlank() || passwordInput.isBlank()) {
            onFailure("البريد الإلكتروني وكلمة المرور مطلوبان.")
            return
        }

        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, passwordInput)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        onFailure("تعذر التحقق من بيانات الدخول.")
                        return@addOnCompleteListener
                    }

                    val authenticatedEmail = auth.currentUser?.email?.trim()?.lowercase()
                    if (authenticatedEmail.isNullOrBlank()) {
                        auth.signOut()
                        onFailure("حساب Firebase الموثق لا يحتوي على بريد إلكتروني.")
                        return@addOnCompleteListener
                    }

                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("admins").document(authenticatedEmail).get()
                        .addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                auth.signOut()
                                onFailure("هذا الحساب موثق لكنه غير مصرح له بالدخول إلى لوحة الإدارة.")
                                return@addOnSuccessListener
                            }

                            val role = doc.getString("role")?.trim()
                            val permissions = doc.getString("permissions")?.trim()
                            val active = doc.getBoolean("isActive") ?: true

                            if (!active || role.isNullOrBlank() || permissions.isNullOrBlank()) {
                                auth.signOut()
                                onFailure("حساب الإدارة غير نشط أو أن صلاحياته غير مكتملة.")
                                return@addOnSuccessListener
                            }

                            _adminRole.value = role
                            _adminPermissions.value = permissions.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                            onSuccess(role, permissions)
                        }
                        .addOnFailureListener {
                            auth.signOut()
                            onFailure("تعذر التحقق من صلاحيات الإدارة. حاول لاحقًا.")
                        }
                }
        } catch (_: Exception) {
            onFailure("خدمة المصادقة غير متاحة حاليًا.")
        }
    }

    fun insertAdminAccount(email: String, role: String, permissions: String) = viewModelScope.launch {
        if (!hasAdminPermission("ALL")) return@launch
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || role.isBlank() || permissions.isBlank()) return@launch
        repo.insertAdminAccount(AdminAccount(email = normalizedEmail, role = role, permissions = permissions))
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val data = mapOf(
                "email" to normalizedEmail,
                "role" to role,
                "permissions" to permissions
            )
            db.collection("admins").document(normalizedEmail).set(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAdminAccount(id: Int, email: String) = viewModelScope.launch {
        if (!hasAdminPermission("ALL")) return@launch
        if (email.trim().lowercase() == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()) return@launch
        repo.deleteAdminAccount(id)
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("admins").document(email.trim().lowercase()).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Donation campaigns are loaded from the configured data source; never seed fabricated campaigns.

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        stopAthan()
    }

    fun initCommunityAndPolls() {
        // Never fabricate community members, posts, votes, or prayer history.
        _communityPosts.value = emptyList()

        // Poll questions are app content; vote state is persisted locally on this device.
        val basePolls = listOf(
            CommunityPoll(
                id = 1,
                questionAr = "ما هو أنسب وقت تفضله لقراءة وردك اليومي من القرآن الكريم؟",
                questionEn = "What is the best time you prefer to read your daily Quran portion?",
                optionsAr = listOf("بعد صلاة الفجر", "بعد صلاة العصر/المغرب", "قبل النوم في الليل", "في أوقات متفرقة خلال اليوم"),
                optionsEn = listOf("After Fajr prayer", "After Asr/Maghrib prayer", "Before sleeping at night", "At separate times during the day"),
                votes = List(4) { 0 },
                totalVotes = 0,
                votedOptionIndex = null
            ),
            CommunityPoll(
                id = 2,
                questionAr = "هل قمت بتفعيل تنبيهات الأذان والأذكار في التطبيق؟",
                questionEn = "Have you activated Athan and Adhkar notifications in the app?",
                optionsAr = listOf("نعم", "بعضها فقط", "لا", "سأفعلها الآن"),
                optionsEn = listOf("Yes", "Some of them", "No", "I will enable them now"),
                votes = List(4) { 0 },
                totalVotes = 0,
                votedOptionIndex = null
            ),
            CommunityPoll(
                id = 3,
                questionAr = "كم جزءاً أو حزباً تخطط لإتمامه في ختمتك الحالية؟",
                questionEn = "How much do you plan to complete in your current Khatmah?",
                optionsAr = listOf("جزء واحد يومياً", "نصف جزء يومياً", "حزب واحد يومياً", "أكثر من جزء يومياً"),
                optionsEn = listOf("One Juz' daily", "Half Juz' daily", "One Hizb daily", "More than one Juz' daily"),
                votes = List(4) { 0 },
                totalVotes = 0,
                votedOptionIndex = null
            )
        )
        _communityPolls.value = basePolls.map { poll ->
            val voted = pollsPrefs.getInt("poll_${poll.id}_voted", -1).takeIf { it >= 0 }
            val storedVotes = poll.optionsAr.indices.map { index ->
                pollsPrefs.getInt("poll_${poll.id}_votes_$index", 0)
            }
            val hasStoredVotes = storedVotes.any { it > 0 }
            poll.copy(
                votes = if (hasStoredVotes) storedVotes else poll.votes,
                totalVotes = if (hasStoredVotes) storedVotes.sum() else poll.totalVotes,
                votedOptionIndex = voted
            )
        }

        loadFajrRecords()
    }

    private fun loadFajrRecords() {
        viewModelScope.launch {
            repo.getAllLogs().collect { logs ->
                val byDate = logs.filter { it.prayerName == "Fajr" }.associateBy { it.date }
                val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val displayFormat = SimpleDateFormat("dd-MM", Locale.US)
                val calendar = Calendar.getInstance()
                val records = (6 downTo 0).map { offset ->
                    val day = Calendar.getInstance().apply {
                        timeInMillis = calendar.timeInMillis
                        add(Calendar.DAY_OF_YEAR, -offset)
                    }
                    val date = dayFormat.format(day.time)
                    val status = when (byDate[date]?.status) {
                        "PRAYED_ON_TIME" -> "INDIVIDUAL"
                        "PRAYED_LATE" -> "INDIVIDUAL"
                        "MISSED" -> "MISSED"
                        else -> "NOT_SET"
                    }
                    FajrDayRecord(
                        dayNameAr = SimpleDateFormat("EEEE", Locale("ar")).format(day.time),
                        dayNameEn = SimpleDateFormat("EEEE", Locale.ENGLISH).format(day.time),
                        dateString = displayFormat.format(day.time),
                        status = status
                    )
                }
                _fajrRecords.value = records
            }
        }
    }

    fun addCommunityPost(content: String) {
        val member = loggedInMember.value
        val name = member?.name ?: "مستخدم أقم صلاتك"
        val country = member?.country ?: "المدينة المنورة"
        val newPost = CommunityPost(
            id = _communityPosts.value.size + 1,
            authorName = name,
            authorCountry = country,
            content = content,
            likesCount = 0,
            isLiked = false,
            timestamp = System.currentTimeMillis()
        )
        _communityPosts.value = listOf(newPost) + _communityPosts.value
        
        // Reward user with 5 Barakah points for contributing to the community!
        member?.let {
            updateMemberPoints(it.id, 5)
        }
    }

    fun likeCommunityPost(postId: Int) {
        _communityPosts.value = _communityPosts.value.map { post ->
            if (post.id == postId) {
                if (post.isLiked) {
                    post.copy(likesCount = post.likesCount - 1, isLiked = false)
                } else {
                    post.copy(likesCount = post.likesCount + 1, isLiked = true)
                }
            } else {
                post
            }
        }
    }

    fun voteInPoll(pollId: Int, optionIndex: Int) {
        _communityPolls.value = _communityPolls.value.map { poll ->
            if (poll.id == pollId && poll.votedOptionIndex == null) {
                val newVotes = poll.votes.mapIndexed { idx, v -> if (idx == optionIndex) v + 1 else v }
                poll.copy(
                    votes = newVotes,
                    totalVotes = poll.totalVotes + 1,
                    votedOptionIndex = optionIndex
                ).also {
                    val editor = pollsPrefs.edit()
                        .putInt("poll_${poll.id}_voted", optionIndex)
                    newVotes.forEachIndexed { index, value ->
                        editor.putInt("poll_${poll.id}_votes_$index", value)
                    }
                    editor.apply()
                }
            } else {
                poll
            }
        }
    }

    fun updateFajrRecord(dateString: String, status: String) {
        _fajrRecords.value = _fajrRecords.value.map { record ->
            if (record.dateString == dateString) record.copy(status = status) else record
        }
        viewModelScope.launch {
            if (status == "NOT_SET") {
                repo.deletePrayerLog(dateString, "Fajr")
            } else {
                repo.insertPrayerLog(
                    PrayerLog(
                        date = dateString,
                        prayerName = "Fajr",
                        status = when (status) {
                            "CONGREGATION" -> "CONGREGATION"
                            "INDIVIDUAL" -> "PRAYED_ON_TIME"
                            "MISSED" -> "MISSED"
                            else -> status
                        }
                    )
                )
            }
        }
    }
}
