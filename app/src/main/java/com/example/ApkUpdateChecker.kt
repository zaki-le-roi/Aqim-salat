package com.example

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks public GitHub Releases for a newer signed APK.
 *
 * The APK is downloaded automatically in the background. Android's official
 * package installer still requires confirmation; ordinary apps cannot silently
 * install their own updates.
 */
class ApkUpdateChecker(private val activity: Activity) {
    fun checkForUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL("https://api.github.com/repos/zaki-le-roi/Aqim-salat/releases/latest")
                    .openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "Aqim-Salat-Android")
                }
                if (connection.responseCode !in 200..299) return@launch
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val release = JSONObject(json)
                val latestTag = release.optString("tag_name").removePrefix("v")
                if (!isNewerVersion(latestTag, BuildConfig.VERSION_NAME)) return@launch

                val assets = release.optJSONArray("assets") ?: return@launch
                var apkUrl: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name") == "Aqim-Salat.apk") {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                val downloadUrl = apkUrl ?: return@launch
                downloadAndOfferInstall(downloadUrl, latestTag)
            } catch (_: Exception) {
                // Offline, rate-limited, or no public release: keep the app working normally.
            } finally {
                connection?.disconnect()
            }
        }
    }

    private suspend fun downloadAndOfferInstall(url: String, version: String) {
        try {
            val apk = File(activity.cacheDir, "updates/Aqim-Salat-$version.apk")
            if (!apk.isFile || apk.length() == 0L) {
                var connection: HttpURLConnection? = null
                try {
                    connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 60000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "Aqim-Salat-Android")
                    }
                    if (connection.responseCode !in 200..299) return
                    apk.parentFile?.mkdirs()
                    val temporary = File(apk.parentFile, apk.name + ".download")
                    connection.inputStream.use { input ->
                        temporary.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (temporary.length() == 0L) {
                        temporary.delete()
                        return
                    }
                    if (!temporary.renameTo(apk)) {
                        temporary.copyTo(apk, overwrite = true)
                        temporary.delete()
                    }
                } finally {
                    connection?.disconnect()
                }
            }

            withContext(Dispatchers.Main) {
                if (activity.isFinishing || activity.isDestroyed) return@withContext
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    !activity.packageManager.canRequestPackageInstalls()) {
                    AlertDialog.Builder(activity)
                        .setTitle("التحديث جاهز")
                        .setMessage("تم تنزيل الإصدار $version تلقائيًا. اسمح لتطبيق أقم صلاتك بتثبيت التحديث من هذا المصدر مرة واحدة، ثم ارجع إلى التطبيق لإكمال التثبيت.")
                        .setNegativeButton("لاحقًا", null)
                        .setPositiveButton("فتح الإعدادات") { _, _ ->
                            val settingsIntent = Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${activity.packageName}")
                            )
                            activity.startActivity(settingsIntent)
                        }
                        .show()
                    return@withContext
                }

                val uri = FileProvider.getUriForFile(
                    activity, "${activity.packageName}.fileprovider", apk
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(installIntent)
            }
        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    Toast.makeText(
                        activity,
                        "تعذر تنزيل التحديث تلقائيًا. تحقق من الاتصال وحاول لاحقًا.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val a = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val b = current.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val av = a.getOrElse(i) { 0 }
            val bv = b.getOrElse(i) { 0 }
            if (av != bv) return av > bv
        }
        return false
    }
}
