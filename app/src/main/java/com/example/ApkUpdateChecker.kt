package com.example

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks version.json on every app launch and offers a user-confirmed APK update.
 * Android always owns the final installation confirmation; silent self-installation
 * is not available to ordinary applications.
 */
class ApkUpdateChecker(private val activity: Activity) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val preferences = activity.getSharedPreferences("apk_updates", Context.MODE_PRIVATE)
    private var receiverRegistered = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            val expectedId = preferences.getLong(KEY_DOWNLOAD_ID, -2L)
            if (completedId != expectedId) return

            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(completedId)
            manager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) return
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIndex < 0 || cursor.getInt(statusIndex) != DownloadManager.STATUS_SUCCESSFUL) {
                    preferences.edit().remove(KEY_DOWNLOAD_ID).apply()
                    showToast("تعذر تنزيل التحديث. تحقق من الاتصال وحاول مجددًا.")
                    return
                }
            }

            val apk = File(
                activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                APK_FILE_NAME
            )
            if (apk.isFile && apk.length() > 0L) {
                preferences.edit().putString(KEY_PENDING_APK, apk.absolutePath).remove(KEY_DOWNLOAD_ID).apply()
                installPendingUpdateIfAllowed()
            } else {
                showToast("اكتمل التنزيل لكن تعذر العثور على ملف التحديث.")
            }
        }
    }

    fun checkForUpdate() {
        registerDownloadReceiver()
        scope.launch {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(VERSION_JSON_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Cache-Control", "no-cache")
                    setRequestProperty("User-Agent", "Aqim-Salat-Android")
                }
                if (connection.responseCode !in 200..299) return@launch
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val release = JSONObject(json)
                val latestCode = release.optInt("versionCode", -1)
                val latestName = release.optString("versionName").takeIf { it.isNotBlank() } ?: return@launch
                val downloadUrl = release.optString("downloadUrl").takeIf { it.startsWith("https://") } ?: return@launch
                val notes = release.optString("releaseNotes", "تحسينات وإصلاحات جديدة")

                if (latestCode <= BuildConfig.VERSION_CODE) return@launch

                withContext(Dispatchers.Main) {
                    if (activity.isFinishing || activity.isDestroyed) return@withContext
                    showUpdateDialog(latestCode, latestName, notes, downloadUrl)
                }
            } catch (_: Exception) {
                // Offline or invalid metadata: keep normal app use uninterrupted.
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun showUpdateDialog(versionCode: Int, versionName: String, notes: String, downloadUrl: String) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("يتوفر تحديث جديد")
            .setMessage("أقم صلاتك $versionName\n\n$notes\n\nسيتم تنزيل التحديث ثم فتح مُثبّت Android لتأكيد التثبيت.")
            .setNegativeButton("لاحقًا", null)
            .setPositiveButton("تحديث الآن", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(EMERALD)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(GOLD)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                dialog.dismiss()
                startDownload(versionCode, versionName, downloadUrl)
            }
        }
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(EMERALD)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(GOLD)
    }

    private fun startDownload(versionCode: Int, versionName: String, downloadUrl: String) {
        try {
            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("تحديث أقم صلاتك $versionName")
                .setDescription("جارٍ تنزيل التحديث في الخلفية")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalFilesDir(
                    activity,
                    Environment.DIRECTORY_DOWNLOADS,
                    APK_FILE_NAME
                )
            val id = manager.enqueue(request)
            preferences.edit()
                .putLong(KEY_DOWNLOAD_ID, id)
                .putInt(KEY_TARGET_VERSION, versionCode)
                .apply()
            Toast.makeText(activity, "بدأ تنزيل التحديث في الخلفية", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            showToast("تعذر بدء تنزيل التحديث. حاول مرة أخرى.")
        }
    }

    /** Called on return from Android's 'install unknown apps' settings screen. */
    fun installPendingUpdateIfAllowed() {
        if (activity.isFinishing || activity.isDestroyed) return
        val path = preferences.getString(KEY_PENDING_APK, null) ?: return
        val apk = File(path)
        if (!apk.isFile || apk.length() == 0L) {
            preferences.edit().remove(KEY_PENDING_APK).apply()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()) {
            AlertDialog.Builder(activity)
                .setTitle("التحديث جاهز للتثبيت")
                .setMessage("تم تنزيل التحديث. اسمح لتطبيق أقم صلاتك بالتثبيت من هذا المصدر، ثم ارجع إلى التطبيق لإكمال التثبيت.")
                .setNegativeButton("لاحقًا", null)
                .setPositiveButton("فتح الإعدادات") { _, _ ->
                    activity.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${activity.packageName}")
                        )
                    )
                }
                .show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                apk
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            preferences.edit().remove(KEY_PENDING_APK).apply()
            activity.startActivity(installIntent)
        } catch (_: Exception) {
            showToast("تعذر فتح مُثبّت التحديث.")
        }
    }

    private fun registerDownloadReceiver() {
        if (receiverRegistered) return
        ContextCompat.registerReceiver(
            activity,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
        receiverRegistered = true
    }

    fun dispose() {
        scope.cancel()
        if (receiverRegistered) {
            try {
                activity.unregisterReceiver(downloadReceiver)
            } catch (_: IllegalArgumentException) {
                // Already unregistered by the Android lifecycle.
            }
            receiverRegistered = false
        }
    }

    private fun showToast(message: String) {
        activity.runOnUiThread {
            if (!activity.isFinishing && !activity.isDestroyed) {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
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

    private companion object {
        const val VERSION_JSON_URL = "https://raw.githubusercontent.com/zaki-le-roi/Aqim-salat/main/version.json"
        const val APK_FILE_NAME = "Aqim-Salat-update.apk"
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_PENDING_APK = "pending_apk"
        const val KEY_TARGET_VERSION = "target_version"
        const val EMERALD = 0xFF0F6B46.toInt()
        const val GOLD = 0xFFD4AF37.toInt()
    }
}
