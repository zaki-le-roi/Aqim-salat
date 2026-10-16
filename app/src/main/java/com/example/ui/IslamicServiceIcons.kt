package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin

private val Emerald = Color(0xFF115E3F)
private val Gold = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFF5E4A8)
private val Wood = Color(0xFF8A5528)
private val WoodLight = Color(0xFFC58B45)
private val Page = Color(0xFFFFF4D5)

/** Code-drawn Islamic service artwork; no image assets or downloads are needed. */
@Composable
fun IslamicServiceIcon(type: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 100f
        withTransform({
            scale(scaleX = scaleFactor, scaleY = scaleFactor, pivot = Offset.Zero)
            translate(
                left = (size.width / scaleFactor - 100f) / 2f,
                top = (size.height / scaleFactor - 100f) / 2f
            )
        }) {
            when (type) {
                "QURAN" -> drawOpenMushaf()
                "TASBIH" -> drawWoodenTasbih()
                "HADITH" -> drawStackedBooks()
                "HISN" -> drawFortressShield()
                "QIBLA" -> drawQiblaCompass()
                else -> drawCircle(Gold, radius = 28f, center = Offset(50f, 50f), style = Stroke(4f))
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOpenMushaf() {
    drawRoundRect(Brush.linearGradient(listOf(WoodLight, Wood, Color(0xFF633A1D))),
        Offset(18f, 72f), Size(64f, 8f), CornerRadius(3f))
    drawLine(WoodLight, Offset(27f, 74f), Offset(39f, 91f), 7f, StrokeCap.Round)
    drawLine(Wood, Offset(73f, 74f), Offset(61f, 91f), 7f, StrokeCap.Round)
    drawLine(Gold.copy(alpha = 0.85f), Offset(20f, 94f), Offset(42f, 94f), 3f, StrokeCap.Round)
    drawLine(Gold.copy(alpha = 0.85f), Offset(58f, 94f), Offset(80f, 94f), 3f, StrokeCap.Round)
    val leftPage = Path().apply {
        moveTo(13f, 27f); quadraticTo(30f, 19f, 49f, 31f)
        lineTo(49f, 69f); quadraticTo(31f, 58f, 13f, 65f); close()
    }
    val rightPage = Path().apply {
        moveTo(51f, 31f); quadraticTo(69f, 19f, 87f, 27f)
        lineTo(87f, 65f); quadraticTo(68f, 58f, 51f, 69f); close()
    }
    drawPath(leftPage, Brush.linearGradient(listOf(Color(0xFFFFFBEF), Page, Color(0xFFE6C77C))))
    drawPath(rightPage, Brush.linearGradient(listOf(Color(0xFFFFFBEF), Page, Color(0xFFE6C77C))))
    drawPath(leftPage, Gold, style = Stroke(1.8f))
    drawPath(rightPage, Gold, style = Stroke(1.8f))
    drawLine(Wood, Offset(50f, 30f), Offset(50f, 70f), 2.6f, StrokeCap.Round)
    for (y in listOf(38f, 45f, 52f, 59f)) {
        drawLine(Color(0xFFB69A5C), Offset(22f, y), Offset(42f, y + 1f), 1.2f, StrokeCap.Round)
        drawLine(Color(0xFFB69A5C), Offset(58f, y + 1f), Offset(78f, y), 1.2f, StrokeCap.Round)
    }
    drawCircle(GoldLight, radius = 2.2f, center = Offset(50f, 25f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWoodenTasbih() {
    val center = Offset(47f, 47f)
    val radius = 29f
    drawCircle(WoodLight, radius = 31.5f, center = center, style = Stroke(width = 2.4f))
    for (i in 0 until 25) {
        val angle = (i * 2.0 * Math.PI / 25.0) - Math.PI / 2.0
        val x = center.x + cos(angle).toFloat() * radius
        val y = center.y + sin(angle).toFloat() * radius
        drawCircle(if (i % 3 == 0) WoodLight else Wood, radius = 5f, center = Offset(x, y))
        drawCircle(GoldLight.copy(alpha = 0.9f), radius = 1.35f, center = Offset(x - 1.1f, y - 1.4f))
    }
    drawCircle(Gold, radius = 6f, center = Offset(47f, 15f))
    drawCircle(GoldLight, radius = 2f, center = Offset(45f, 13f))
    drawLine(Gold, Offset(47f, 8f), Offset(47f, 3f), 2.5f, StrokeCap.Round)
    drawLine(WoodLight, Offset(47f, 3f), Offset(39f, 1f), 2f, StrokeCap.Round)
    drawLine(WoodLight, Offset(47f, 3f), Offset(55f, 1f), 2f, StrokeCap.Round)
    drawLine(WoodLight, Offset(47f, 3f), Offset(47f, 0f), 2f, StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStackedBooks() {
    drawRoundRect(Brush.linearGradient(listOf(Color(0xFFB77A32), Wood, Color(0xFF603A20))),
        Offset(14f, 65f), Size(72f, 20f), CornerRadius(4f))
    drawRoundRect(Brush.linearGradient(listOf(Color(0xFF2A8A64), Emerald, Color(0xFF0A3E2B))),
        Offset(9f, 43f), Size(75f, 21f), CornerRadius(4f))
    drawRoundRect(Brush.linearGradient(listOf(Color(0xFFE9D28A), Color(0xFFB99A4B), Color(0xFF866B30))),
        Offset(19f, 21f), Size(68f, 21f), CornerRadius(4f))
    drawRoundRect(Page, Offset(20f, 68f), Size(61f, 13f), CornerRadius(2f))
    drawRoundRect(Color(0xFFECE5C8), Offset(15f, 46f), Size(64f, 14f), CornerRadius(2f))
    drawRoundRect(Page, Offset(25f, 24f), Size(56f, 14f), CornerRadius(2f))
    drawRoundRect(Gold, Offset(14f, 65f), Size(9f, 20f), CornerRadius(2f))
    drawRoundRect(Gold, Offset(9f, 43f), Size(9f, 21f), CornerRadius(2f))
    drawRoundRect(Gold, Offset(19f, 21f), Size(9f, 21f), CornerRadius(2f))
    drawCircle(GoldLight, 2.2f, Offset(18.5f, 75f))
    drawCircle(GoldLight, 2.2f, Offset(13.5f, 53f))
    drawCircle(GoldLight, 2.2f, Offset(23.5f, 31f))
    for (y in listOf(28f, 32f, 36f)) {
        drawLine(Color(0xFFB99A4B), Offset(34f, y), Offset(72f, y), 1f, StrokeCap.Round)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFortressShield() {
    val shield = Path().apply {
        moveTo(50f, 8f); cubicTo(62f, 15f, 74f, 17f, 84f, 18f)
        lineTo(82f, 48f); cubicTo(80f, 67f, 66f, 82f, 50f, 91f)
        cubicTo(34f, 82f, 20f, 67f, 18f, 48f); lineTo(16f, 18f)
        cubicTo(27f, 17f, 39f, 15f, 50f, 8f); close()
    }
    drawPath(shield, Brush.linearGradient(listOf(Color(0xFF21835B), Emerald, Color(0xFF083B2A))))
    drawPath(shield, Gold, style = Stroke(width = 2.8f))
    val inner = Path().apply {
        moveTo(50f, 16f); cubicTo(61f, 22f, 70f, 24f, 76f, 25f)
        lineTo(74f, 47f); cubicTo(72f, 63f, 61f, 74f, 50f, 81f)
        cubicTo(39f, 74f, 28f, 63f, 26f, 47f); lineTo(24f, 25f)
        cubicTo(31f, 24f, 40f, 22f, 50f, 16f); close()
    }
    drawPath(inner, GoldLight.copy(alpha = 0.8f), style = Stroke(width = 1.1f))
    drawCircle(GoldLight, radius = 10f, center = Offset(62f, 36f))
    drawCircle(Emerald, radius = 8.5f, center = Offset(67f, 32f))
    drawRoundRect(Gold, Offset(31f, 54f), Size(38f, 16f), CornerRadius(2f))
    drawCircle(GoldLight, radius = 10f, center = Offset(50f, 53f))
    drawRect(Gold, Offset(40f, 53f), Size(20f, 17f))
    drawRect(Emerald, Offset(46f, 59f), Size(8f, 11f))
    drawRoundRect(GoldLight, Offset(25f, 44f), Size(4f, 26f), CornerRadius(2f))
    drawRoundRect(GoldLight, Offset(71f, 44f), Size(4f, 26f), CornerRadius(2f))
    drawCircle(GoldLight, radius = 2.2f, center = Offset(27f, 42f))
    drawCircle(GoldLight, radius = 2.2f, center = Offset(73f, 42f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawQiblaCompass() {
    val center = Offset(50f, 50f)
    drawCircle(Gold.copy(alpha = 0.25f), radius = 42f, center = center)
    drawCircle(Gold, radius = 38f, center = center, style = Stroke(width = 3.2f))
    drawCircle(GoldLight, radius = 31f, center = center, style = Stroke(width = 1.2f))
    for (i in 0 until 8) {
        val angle = i * Math.PI / 4.0 - Math.PI / 2.0
        val inner = if (i % 2 == 0) 30f else 33f
        val outer = 36f
        drawLine(GoldLight,
            Offset(50f + cos(angle).toFloat() * inner, 50f + sin(angle).toFloat() * inner),
            Offset(50f + cos(angle).toFloat() * outer, 50f + sin(angle).toFloat() * outer),
            if (i % 2 == 0) 2.2f else 1.2f, StrokeCap.Round)
    }
    val pointer = Path().apply {
        moveTo(50f, 17f); lineTo(59f, 51f); lineTo(50f, 45f); lineTo(41f, 51f); close()
    }
    drawPath(pointer, Gold)
    drawCircle(Emerald, radius = 4f, center = center)
    drawCircle(GoldLight, radius = 2f, center = center)
    drawRoundRect(Color(0xFF17231D), Offset(37f, 45f), Size(26f, 23f), CornerRadius(2f))
    drawRect(Gold, Offset(37f, 50f), Size(26f, 4f))
    drawRect(GoldLight, Offset(40f, 58f), Size(20f, 2f))
    drawCircle(GoldLight, radius = 2f, center = Offset(50f, 39f))
}
