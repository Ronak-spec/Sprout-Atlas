package com.example.ui.components.doodles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.*

fun DrawScope.drawDetailedPotato(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val skinColor = palette?.primary ?: Color(0xFFC69A64)
    val highlightColor = palette?.secondary ?: Color(0xFFDDB98B)
    val eyeColor = palette?.accent ?: Color(0xFF8D6E63)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Earthy potato oblong tuber
    val body = Path().apply {
        moveTo(sx(24f), sy(48f))
        cubicTo(sx(20f), sy(30f), sx(40f), sy(18f), sx(60f), sy(22f))
        cubicTo(sx(80f), sy(26f), sx(88f), sy(44f), sx(84f), sy(64f))
        cubicTo(sx(80f), sy(82f), sx(60f), sy(86f), sx(40f), sy(82f))
        cubicTo(sx(22f), sy(78f), sx(26f), sy(62f), sx(24f), sy(48f))
        close()
    }
    drawPath(body, skinColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Soft warm highlight
    val highlight = Path().apply {
        moveTo(sx(34f), sy(34f))
        cubicTo(sx(48f), sy(26f), sx(68f), sy(30f), sx(74f), sy(42f))
    }
    drawPath(highlight, highlightColor.copy(alpha = 0.55f), style = Stroke(width = 3.8f * scale, cap = StrokeCap.Round))

    // Characteristic potato "eyes" and dimples
    val eyes = listOf(
        Pair(36f, 44f), Pair(52f, 38f), Pair(68f, 48f),
        Pair(44f, 62f), Pair(62f, 66f), Pair(32f, 68f)
    )
    eyes.forEach { (cx, cy) ->
        val eyePath = Path().apply {
            moveTo(sx(cx - 3f), sy(cy)); quadraticTo(sx(cx), sy(cy + 1.8f), sx(cx + 3f), sy(cy))
        }
        drawPath(eyePath, eyeColor, style = thinStroke)
        drawCircle(eyeColor, radius = 1.0f * scale, center = Offset(sx(cx), sy(cy - 0.8f)))
    }
}

fun DrawScope.drawDetailedPumpkin(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val pumpkinColor = palette?.primary ?: Color(0xFFFF9800)
    val ribColor = palette?.accent ?: Color(0xFFE65100)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Ribbed pumpkin segments
    // Left outer lobe
    val lobeL = Path().apply {
        moveTo(sx(50f), sy(28f))
        cubicTo(sx(30f), sy(26f), sx(14f), sy(46f), sx(16f), sy(68f))
        cubicTo(sx(18f), sy(86f), sx(36f), sy(92f), sx(50f), sy(92f))
        close()
    }
    drawPath(lobeL, pumpkinColor, style = Fill)

    // Right outer lobe
    val lobeR = Path().apply {
        moveTo(sx(50f), sy(28f))
        cubicTo(sx(70f), sy(26f), sx(86f), sy(46f), sx(84f), sy(68f))
        cubicTo(sx(82f), sy(86f), sx(64f), sy(92f), sx(50f), sy(92f))
        close()
    }
    drawPath(lobeR, pumpkinColor, style = Fill)

    // Center central lobe
    val lobeC = Path().apply {
        moveTo(sx(50f), sy(26f))
        cubicTo(sx(38f), sy(26f), sx(32f), sy(46f), sx(34f), sy(68f))
        cubicTo(sx(36f), sy(86f), sx(42f), sy(92f), sx(50f), sy(92f))
        cubicTo(sx(58f), sy(92f), sx(64f), sy(86f), sx(66f), sy(68f))
        cubicTo(sx(68f), sy(46f), sx(62f), sy(26f), sx(50f), sy(26f))
        close()
    }
    drawPath(lobeC, pumpkinColor, style = Fill)

    // Rib outline grooves
    drawPath(lobeL, ink, style = thinStroke)
    drawPath(lobeR, ink, style = thinStroke)
    drawPath(lobeC, ink, style = lineStroke)

    // Rib accent shades
    val ribShades = listOf(
        Path().apply { moveTo(sx(34f), sy(30f)); quadraticTo(sx(26f), sy(60f), sx(36f), sy(90f)) },
        Path().apply { moveTo(sx(66f), sy(30f)); quadraticTo(sx(74f), sy(60f), sx(64f), sy(90f)) }
    )
    ribShades.forEach { drawPath(it, ribColor.copy(alpha = 0.5f), style = thinStroke) }

    // Thick curved wooden stem
    val stem = Path().apply {
        moveTo(sx(50f), sy(26f)); cubicTo(sx(52f), sy(16f), sx(60f), sy(10f), sx(64f), sy(10f))
    }
    drawPath(stem, Color(0xFF5D4037), style = Stroke(width = 4.2f * scale, cap = StrokeCap.Round))
    drawPath(stem, ink, style = thinStroke)
}

fun DrawScope.drawDetailedEggplant(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val eggplantColor = palette?.primary ?: Color(0xFF4A148C)
    val highlightColor = palette?.secondary ?: Color(0xFF7B1FA2)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Pear-shaped eggplant body
    val body = Path().apply {
        moveTo(sx(50f), sy(26f))
        cubicTo(sx(64f), sy(28f), sx(72f), sy(46f), sx(76f), sy(66f))
        cubicTo(sx(80f), sy(86f), sx(66f), sy(94f), sx(50f), sy(94f))
        cubicTo(sx(34f), sy(94f), sx(20f), sy(86f), sx(24f), sy(66f))
        cubicTo(sx(28f), sy(46f), sx(36f), sy(28f), sx(50f), sy(26f))
        close()
    }
    drawPath(body, eggplantColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Glossy high-contrast specular gleam
    val gleam = Path().apply {
        moveTo(sx(34f), sy(46f))
        cubicTo(sx(32f), sy(60f), sx(36f), sy(76f), sx(42f), sy(84f))
    }
    drawPath(gleam, highlightColor.copy(alpha = 0.5f), style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.4f * scale, center = Offset(sx(36f), sy(46f)))

    // 5-toothed green spiky calyx cap
    val calyx = Path().apply {
        moveTo(sx(50f), sy(26f)); lineTo(sx(36f), sy(36f)); lineTo(sx(44f), sy(26f))
        lineTo(sx(30f), sy(24f)); lineTo(sx(42f), sy(22f))
        lineTo(sx(50f), sy(20f))
        lineTo(sx(58f), sy(22f)); lineTo(sx(70f), sy(24f))
        lineTo(sx(56f), sy(26f)); lineTo(sx(64f), sy(36f))
        lineTo(sx(50f), sy(26f))
        close()
    }
    drawPath(calyx, Color(0xFF2E7D32), style = Fill)
    drawPath(calyx, ink, style = thinStroke)

    // Stalk
    val stem = Path().apply {
        moveTo(sx(50f), sy(20f)); quadraticTo(sx(52f), sy(12f), sx(46f), sy(8f))
    }
    drawPath(stem, Color(0xFF1B5E20), style = Stroke(width = 3.8f * scale, cap = StrokeCap.Round))
}

fun DrawScope.drawDetailedPea(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val podColor = palette?.primary ?: Color(0xFF689F38)
    val peaColor = palette?.secondary ?: Color(0xFF8BC34A)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Pod shell
    val pod = Path().apply {
        moveTo(sx(18f), sy(34f))
        cubicTo(sx(36f), sy(22f), sx(74f), sy(28f), sx(84f), sy(54f))
        cubicTo(sx(74f), sy(76f), sx(36f), sy(76f), sx(18f), sy(54f))
        close()
    }
    drawPath(pod, podColor, style = Fill)
    drawPath(pod, ink, style = lineStroke)

    // 3 round plump peas nestled inside pod
    val peas = listOf(Pair(34f, 48f), Pair(50f, 48f), Pair(66f, 48f))
    peas.forEach { (cx, cy) ->
        drawCircle(peaColor, radius = 8.5f * scale, center = Offset(sx(cx), sy(cy)))
        drawCircle(ink, radius = 8.5f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
        drawCircle(Color.White.copy(alpha = 0.75f), radius = 1.6f * scale, center = Offset(sx(cx - 2f), sy(cy - 2f)))
    }

    // Stem and curling tendril
    val stem = Path().apply {
        moveTo(sx(18f), sy(44f)); lineTo(sx(10f), sy(40f))
        moveTo(sx(10f), sy(40f)); quadraticTo(sx(6f), sy(30f), sx(12f), sy(24f))
    }
    drawPath(stem, Color(0xFF33691E), style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round))
}
