package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldLight,
    tertiary = GoldAccent,
    background = DarkSlateBackground,
    surface = DarkSlateSurface,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = DarkSlateBackground,
    onBackground = TextWhite,
    onSurface = TextWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldLight,
    tertiary = AmberAccent,
    background = LightMintBackground,
    surface = LightMintSurface,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onBackground = TextDark,
    onSurface = TextDark
  )

@Composable
fun MyApplicationTheme(
  themeMode: String = "AUTO",
  nextPrayer: String = "",
  systemDarkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    "LIGHT" -> false
    "DARK" -> true
    "AMOLED" -> true
    "AUTO" -> systemDarkTheme
    "SUNRISE_SUNSET" -> {
      val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
      hour < 6 || hour > 18
    }
    "PRAYER_ADAPTIVE" -> {
      nextPrayer == "Fajr" || nextPrayer == "Maghrib" || nextPrayer == "Isha" || nextPrayer == "Sunrise"
    }
    else -> systemDarkTheme
  }

  val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  // Dynamically adapt colors for PRAYER_ADAPTIVE or AMOLED
  val colorScheme = if (themeMode == "PRAYER_ADAPTIVE" && nextPrayer.isNotBlank()) {
    val adaptivePrimary = when (nextPrayer) {
      "Fajr" -> Color(0xFF5C6BC0) // Indigo Celestial
      "Sunrise" -> Color(0xFFFFB74D) // Sunrise Amber
      "Dhuhr" -> Color(0xFFFBC02D) // Dhuhr Golden Solar
      "Asr" -> Color(0xFF8D6E63) // Terracotta Clay
      "Maghrib" -> Color(0xFFE64A19) // Sunset Crimson
      "Isha" -> Color(0xFF3F51B5) // Royal Indigo Night
      else -> EmeraldPrimary
    }
    if (darkTheme) {
      baseScheme.copy(
        primary = adaptivePrimary,
        secondary = adaptivePrimary.copy(alpha = 0.8f),
        background = DarkSlateBackground,
        surface = DarkSlateSurface
      )
    } else {
      baseScheme.copy(
        primary = adaptivePrimary,
        secondary = adaptivePrimary.copy(alpha = 0.8f),
        background = LightMintBackground,
        surface = LightMintSurface
      )
    }
  } else if (themeMode == "AMOLED") {
    baseScheme.copy(
      background = Color.Black,
      surface = Color(0xFF0C0E0D),
      onBackground = Color.White,
      onSurface = Color.White
    )
  } else {
    baseScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
