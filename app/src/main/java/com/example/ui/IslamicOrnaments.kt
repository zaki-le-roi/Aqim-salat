package com.example.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lightweight geometric Islamic corner ornament for cards and visual headers.
 * Draws only thin, low-opacity strokes near the corners so content remains legible.
 */
fun Modifier.islamicOrnament(): Modifier = drawWithContent {
    drawContent()

    val arm = 17.dp.toPx()
    val inset = 10.dp.toPx()
    val stroke = 1.dp.toPx()
    val gold = Color(0xFFD4AF37).copy(alpha = 0.18f)

    fun corner(x: Float, y: Float, dx: Float, dy: Float) {
        val outer = Offset(x + dx * arm, y)
        val inner = Offset(x, y + dy * arm)
        val center = Offset(x + dx * arm * 0.5f, y + dy * arm * 0.5f)

        drawLine(gold, outer, center, strokeWidth = stroke)
        drawLine(gold, center, inner, strokeWidth = stroke)
        drawLine(
            gold,
            Offset(x + dx * arm * 0.22f, y + dy * arm * 0.22f),
            Offset(x + dx * arm * 0.78f, y + dy * arm * 0.78f),
            strokeWidth = stroke
        )
        drawLine(
            gold,
            Offset(x + dx * arm * 0.58f, y + dy * arm * 0.12f),
            Offset(x + dx * arm * 0.12f, y + dy * arm * 0.58f),
            strokeWidth = stroke
        )
    }

    if (size.width > arm * 2 && size.height > arm * 2) {
        corner(inset, inset, 1f, 1f)
        corner(size.width - inset, inset, -1f, 1f)
        corner(inset, size.height - inset, 1f, -1f)
        corner(size.width - inset, size.height - inset, -1f, -1f)
    }
}
