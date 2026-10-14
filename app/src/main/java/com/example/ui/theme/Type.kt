package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Use the Android sans-serif family, which has broad Arabic-script support,
// without adding a remote font dependency or increasing the APK size.
private val ArabicSans = FontFamily.SansSerif

val Typography =
  Typography(
    displayLarge = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp,
      lineHeight = 40.sp,
      letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.Bold,
      fontSize = 26.sp,
      lineHeight = 34.sp,
      letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.SemiBold,
      fontSize = 22.sp,
      lineHeight = 30.sp,
      letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.SemiBold,
      fontSize = 20.sp,
      lineHeight = 28.sp,
      letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
      fontFamily = ArabicSans,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.sp,
    ),
  )
