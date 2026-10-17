package com.example

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
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
                connection = (URL(LATEST_RELEASE_API_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Cache-Control", "no-cache, no-store")
                    setRequestProperty("Pragma", "no-cache")
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "Aqim-Salat-Android")
                }
                if (connection.responseCode !in 200..299) return@launch
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val release = JSONObject(json)
                val tag = release.optString("tag_name").removePrefix("v")
                val versionParts = tag.split(".").mapNotNull { it.toIntOrNull() }
                if (versionParts.size < 3) return@launch
                val latestCode = versionParts[0] * 1_000_000 + versionParts[1] * 1_000 + versionParts[2]
                val latestName = tag
                val assets = release.optJSONArray("assets") ?: return@launch
                var downloadUrl: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name") == APK_ASSET_NAME) {
                        downloadUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                val safeDownloadUrl = downloadUrl?.takeIf { it.startsWith("https://") } ?: return@launch
                val notes = release.optString("body").takeIf { it.isNotBlank() } ?: "تحسينات وإصلاحات جديدة"

                // Compare with the installed app version as well as its numeric code.
                // Older builds may have a stale versionCode but the same visible version name.
                if (latestCode <= BuildConfig.VERSION_CODE) return@launch
                if (preferences.getString(KEY_DISMISSED_VERSION, null) == latestName) return@launch

                withContext(Dispatchers.Main) {
                    if (activity.isFinishing || activity.isDestroyed) return@withContext
                    showUpdateDialog(latestCode, latestName, notes, safeDownloadUrl)
                }
            } catch (_: Exception) {
                // Offline or invalid metadata: keep normal app use uninterrupted.
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun showUpdateDialog(versionCode: Int, versionName: String, notes: String, downloadUrl: String) {
        // Explicit opaque text colors prevent theme-dependent transparent message text.
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(8))
            background = GradientDrawable().apply {
                setColor(Color.rgb(250, 248, 245))
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), GOLD)
            }
        }

        val title = TextView(activity).apply {
            text = "يتوفر تحديث جديد"
            setTextColor(Color.rgb(15, 76, 58))
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = true
        }
        val message = TextView(activity).apply {
            text = "أقم صلاتك $versionName\n\n$notes\n\nسيتم تنزيل التحديث ثم فتح مُثبّت Android لتأكيد التثبيت."
            setTextColor(Color.rgb(38, 43, 40))
            textSize = 16f
            gravity = Gravity.START
            includeFontPadding = true
            setLineSpacing(dp(4).toFloat(), 1.0f)
        }
        content.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(16) })
        content.addView(message, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val dialog = AlertDialog.Builder(activity)
            .setView(content)
            .setNegativeButton("لاحقًا", null)
            .setPositiveButton("تحديث الآن", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
                preferences.edit().putString(KEY_DISMISSED_VERSION, versionName).apply()
                dialog.dismiss()
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(EMERALD)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.rgb(120, 88, 20))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                dialog.dismiss()
                startDownload(versionCode, versionName, downloadUrl)
            }
        }
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.90f).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(EMERALD)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.rgb(120, 88, 20))
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density + 0.5f).toInt()

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
            val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
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

    private companion object {
        const val LATEST_RELEASE_API_URL = "https://api.github.com/repos/zaki-le-roi/Aqim-salat/releases/latest"
        const val APK_ASSET_NAME = "Aqim-Salat.apk"
        const val APK_FILE_NAME = "Aqim-Salat-update.apk"
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_PENDING_APK = "pending_apk"
        const val KEY_DISMISSED_VERSION = "dismissed_version"
        const val EMERALD = 0xFF0F6B46.toInt()
        const val GOLD = 0xFFD4AF37.toInt()
    }
}
