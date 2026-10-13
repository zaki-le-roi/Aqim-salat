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
 * Checks public GitHub Releases for a newer APK and uses Android's official package
 * installer. Android still requires the user to confirm installation.
 */
class ApkUpdateChecker(private val activity: Activity) {
    fun checkForUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = (URL("https://api.github.com/repos/zaki-le-roi/Aqim-salat/releases/latest")
                    .openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "Aqim-Salat-Android")
                }
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
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
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(activity)
                        .setTitle("تحديث جديد لتطبيق أقم صلاتك")
                        .setMessage("يتوفر الإصدار $latestTag. هل تريد تنزيله وتثبيته؟ لن تحتاج إلى حذف التطبيق.")
                        .setNegativeButton("لاحقًا", null)
                        .setPositiveButton("تحديث") { _, _ -> downloadAndInstall(downloadUrl, latestTag) }
                        .show()
                }
            } catch (_: Exception) {
                // Offline, rate-limited, or no public release: keep the app working normally.
            }
        }
    }

    private fun downloadAndInstall(url: String, version: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                }
                val apk = File(activity.cacheDir, "updates/Aqim-Salat-$version.apk")
                apk.parentFile?.mkdirs()
                connection.inputStream.use { input -> apk.outputStream().use { output -> input.copyTo(output) } }
                connection.disconnect()
                withContext(Dispatchers.Main) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        !activity.packageManager.canRequestPackageInstalls()) {
                        Toast.makeText(activity, "اسمح للتطبيق بتثبيت التحديثات من هذا المصدر، ثم افتح التطبيق مجددًا.", Toast.LENGTH_LONG).show()
                        val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${activity.packageName}"))
                        activity.startActivity(settingsIntent)
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
                    Toast.makeText(activity, "تعذر تنزيل التحديث. تحقق من الاتصال وحاول لاحقًا.", Toast.LENGTH_LONG).show()
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
