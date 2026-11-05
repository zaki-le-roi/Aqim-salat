package com.example.data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import org.json.JSONObject

/** Small on-demand coordinate lookup; no timezone database is bundled. */
object TimeZoneResolver {
 suspend fun resolve(lat:Double,lng:Double):String?=withContext(Dispatchers.IO) {
  if(!lat.isFinite()||lat !in -90.0..90.0||!lng.isFinite()||lng !in -180.0..180.0) return@withContext null
  var conn:HttpURLConnection?=null
  try {
   val connection=URL("https://timeapi.io/api/TimeZone/coordinate?latitude=$lat&longitude=$lng").openConnection() as HttpURLConnection
   conn=connection
   connection.connectTimeout=7000; connection.readTimeout=7000; connection.requestMethod="GET"
   connection.setRequestProperty("Accept","application/json")
   if(connection.responseCode !in 200..299) return@withContext null
   val id=JSONObject(connection.inputStream.bufferedReader().use{it.readText()}).optString("timeZone")
   if(id.isBlank()) null else runCatching{ZoneId.of(id).id}.getOrNull()
  } catch(_:Exception){ null } finally { conn?.disconnect() }
 }
 suspend fun refreshSavedCity(context:android.content.Context) {
  val repo=AppRepository(AppDatabase.getDatabase(context),context)
  if(repo.appLocationUsesDeviceZone.first()||repo.appTimeZoneId.first().isNotBlank()) return
  resolve(repo.appLatitude.first(),repo.appLongitude.first())?.let{repo.saveTimeZoneId(it)}
 }
}