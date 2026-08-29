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

fun DrawScope.drawDetailedLemon(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val lemonColor = palette?.primary ?: Color(0xFFFFEB3B)
    val highlightColor = palette?.secondary ?: Color(0xFFFFF59D)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Elliptical lemon with pointed apex tips
    val body = Path().apply {
        moveTo(sx(20f), sy(50f))
        cubicTo(sx(16f), sy(46f), sx(14f), sy(44f), sx(12f), sy(50f))
        cubicTo(sx(14f), sy(56f), sx(16f), sy(54f), sx(20f), sy(50f))
        cubicTo(sx(28f), sy(24f), sx(72f), sy(24f), sx(80f), sy(50f))
        cubicTo(sx(84f), sy(46f), sx(86f), sy(44f), sx(88f), sy(50f))
        cubicTo(sx(86f), sy(56f), sx(84f), sy(54f), sx(80f), sy(50f))
        cubicTo(sx(72f), sy(76f), sx(28f), sy(76f), sx(20f), sy(50f))
        close()
    }
    drawPath(body, lemonColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Highlight contour
    val highlight = Path().apply {
        moveTo(sx(28f), sy(46f))
        cubicTo(sx(34f), sy(34f), sx(66f), sy(34f), sx(72f), sy(46f))
    }
    drawPath(highlight, highlightColor.copy(alpha = 0.6f), style = Stroke(width = 4.0f * scale, cap = StrokeCap.Round))

    // Oil gland pores / stipples
    listOf(Pair(36f, 52f), Pair(48f, 48f), Pair(62f, 54f), Pair(44f, 60f), Pair(56f, 62f)).forEach { (cx, cy) ->
        drawCircle(Color(0xFFFBC02D).copy(alpha = 0.5f), radius = 1.2f * scale, center = Offset(sx(cx), sy(cy)))
    }
}

fun DrawScope.drawDetailedTomato(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val tomatoColor = palette?.primary ?: Color(0xFFE53935)
    val highlightColor = palette?.secondary ?: Color(0xFFFF8A80)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Plump glossy tomato body
    val body = Path().apply {
        moveTo(sx(50f), sy(28f))
        cubicTo(sx(74f), sy(26f), sx(84f), sy(44f), sx(82f), sy(64f))
        cubicTo(sx(80f), sy(82f), sx(66f), sy(92f), sx(50f), sy(92f))
        cubicTo(sx(34f), sy(92f), sx(20f), sy(82f), sx(18f), sy(64f))
        cubicTo(sx(16f), sy(44f), sx(26f), sy(26f), sx(50f), sy(28f))
        close()
    }
    drawPath(body, tomatoColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Glare highlight curve
    val glare = Path().apply {
        moveTo(sx(32f), sy(42f))
        cubicTo(sx(34f), sy(52f), sx(32f), sy(66f), sx(26f), sy(72f))
    }
    drawPath(glare, highlightColor.copy(alpha = 0.6f), style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = 0.85f), radius = 2.4f * scale, center = Offset(sx(34f), sy(42f)))

    // 5-pointed star calyx & woody stem
    val calyx = Path().apply {
        moveTo(sx(50f), sy(28f)); lineTo(sx(38f), sy(22f)); lineTo(sx(44f), sy(28f))
        lineTo(sx(34f), sy(34f)); lineTo(sx(44f), sy(32f))
        lineTo(sx(50f), sy(36f))
        lineTo(sx(56f), sy(32f)); lineTo(sx(66f), sy(34f))
        lineTo(sx(56f), sy(28f)); lineTo(sx(62f), sy(22f))
        lineTo(sx(50f), sy(28f))
        close()
    }
    drawPath(calyx, Color(0xFF388E3C), style = Fill)
    drawPath(calyx, ink, style = thinStroke)

    val stem = Path().apply {
        moveTo(sx(50f), sy(28f)); quadraticTo(sx(52f), sy(16f), sx(46f), sy(10f))
    }
    drawPath(stem, Color(0xFF2E7D32), style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
}

fun DrawScope.drawDetailedBanana(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val bananaColor = palette?.primary ?: Color(0xFFFFEB3B)
    val ridgeColor = palette?.accent ?: Color(0xFFFBC02D)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Crescent banana body
    val body = Path().apply {
        moveTo(sx(20f), sy(24f))
        cubicTo(sx(26f), sy(48f), sx(48f), sy(76f), sx(82f), sy(74f))
        cubicTo(sx(78f), sy(84f), sx(44f), sy(88f), sx(18f), sy(54f))
        cubicTo(sx(12f), sy(42f), sx(14f), sy(30f), sx(20f), sy(24f))
        close()
    }
    drawPath(body, bananaColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Longitudinal ridge facet
    val ridge = Path().apply {
        moveTo(sx(20f), sy(24f))
        cubicTo(sx(22f), sy(46f), sx(46f), sy(74f), sx(80f), sy(74f))
    }
    drawPath(ridge, ridgeColor, style = thinStroke)

    // Green stalk top and dark tip
    val stalk = Path().apply {
        moveTo(sx(20f), sy(24f)); lineTo(sx(24f), sy(14f))
    }
    drawPath(stalk, Color(0xFF689F38), style = Stroke(width = 4.0f * scale, cap = StrokeCap.Round))

    drawCircle(Color(0xFF5D4037), radius = 2.4f * scale, center = Offset(sx(81f), sy(74f)))
}

fun DrawScope.drawDetailedOrange(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val orangeColor = palette?.primary ?: Color(0xFFFF9800)
    val highlightColor = palette?.secondary ?: Color(0xFFFFB74D)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Vibrant orange sphere
    drawCircle(orangeColor, radius = 34f * scale, center = Offset(sx(50f), sy(52f)))
    drawCircle(highlightColor.copy(alpha = 0.5f), radius = 28f * scale, center = Offset(sx(44f), sy(46f)))
    drawCircle(ink, radius = 34f * scale, center = Offset(sx(50f), sy(52f)), style = lineStroke)

    // Oil gland pores
    listOf(Pair(38f, 44f), Pair(52f, 42f), Pair(64f, 50f), Pair(36f, 62f), Pair(54f, 64f), Pair(66f, 62f)).forEach { (cx, cy) ->
        drawCircle(Color(0xFFE65100).copy(alpha = 0.4f), radius = 1.2f * scale, center = Offset(sx(cx), sy(cy)))
    }

    // Stem button & leaf
    drawCircle(Color(0xFF388E3C), radius = 2.8f * scale, center = Offset(sx(50f), sy(20f)))

    val leaf = Path().apply {
        moveTo(sx(50f), sy(20f))
        cubicTo(sx(66f), sy(12f), sx(76f), sy(20f), sx(72f), sy(28f))
        cubicTo(sx(60f), sy(30f), sx(52f), sy(26f), sx(50f), sy(20f))
        close()
    }
    drawPath(leaf, Color(0xFF2E7D32), style = Fill)
    drawPath(leaf, ink, style = thinStroke)
}

fun DrawScope.drawDetailedPeach(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val peachColor = palette?.primary ?: Color(0xFFFFB300)
    val blushColor = palette?.accent ?: Color(0xFFE64A19)
    val sutureColor = (palette?.accent ?: Color(0xFFD84315)).copy(alpha = 0.55f)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Heart-lobed peach body with pointed tip
    val body = Path().apply {
        moveTo(sx(50f), sy(24f))
        cubicTo(sx(74f), sy(22f), sx(84f), sy(44f), sx(80f), sy(66f))
        cubicTo(sx(76f), sy(84f), sx(60f), sy(94f), sx(50f), sy(94f))
        cubicTo(sx(40f), sy(94f), sx(24f), sy(84f), sx(20f), sy(66f))
        cubicTo(sx(16f), sy(44f), sx(26f), sy(22f), sx(50f), sy(24f))
        close()
    }
    drawPath(body, peachColor, style = Fill)

    // Crimson-sunset blush side
    val blush = Path().apply {
        moveTo(sx(50f), sy(24f))
        cubicTo(sx(68f), sy(24f), sx(78f), sy(44f), sx(76f), sy(66f))
        cubicTo(sx(72f), sy(82f), sx(60f), sy(90f), sx(50f), sy(94f))
        close()
    }
    drawPath(blush, blushColor.copy(alpha = 0.55f), style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Characteristic peach cleft/suture indent
    val suture = Path().apply {
        moveTo(sx(50f), sy(24f)); quadraticTo(sx(46f), sy(54f), sx(50f), sy(94f))
    }
    drawPath(suture, sutureColor, style = thinStroke)

    // Stem and leaf
    val leaf = Path().apply {
        moveTo(sx(50f), sy(24f))
        cubicTo(sx(64f), sy(14f), sx(74f), sy(18f), sx(70f), sy(24f))
        cubicTo(sx(60f), sy(26f), sx(52f), sy(26f), sx(50f), sy(24f))
        close()
    }
    drawPath(leaf, Color(0xFF388E3C), style = Fill)
    drawPath(leaf, ink, style = thinStroke)
}

fun DrawScope.drawDetailedCherry(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 2.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.4f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val cherry1Color = palette?.primary ?: Color(0xFFB71C1C)
    val cherry2Color = palette?.secondary ?: Color(0xFFD32F2F)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Left cherry
    drawCircle(cherry1Color, radius = 17f * scale, center = Offset(sx(34f), sy(68f)))
    drawCircle(ink, radius = 17f * scale, center = Offset(sx(34f), sy(68f)), style = lineStroke)
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 3.0f * scale, center = Offset(sx(28f), sy(62f)))

    // Right cherry
    drawCircle(cherry2Color, radius = 17f * scale, center = Offset(sx(66f), sy(72f)))
    drawCircle(ink, radius = 17f * scale, center = Offset(sx(66f), sy(72f)), style = lineStroke)
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 3.0f * scale, center = Offset(sx(60f), sy(66f)))

    // Slender curved twin stems
    val stemL = Path().apply {
        moveTo(sx(34f), sy(52f)); cubicTo(sx(36f), sy(34f), sx(46f), sy(18f), sx(50f), sy(14f))
    }
    val stemR = Path().apply {
        moveTo(sx(66f), sy(56f)); cubicTo(sx(64f), sy(36f), sx(54f), sy(18f), sx(50f), sy(14f))
    }
    drawPath(stemL, Color(0xFF2E7D32), style = Stroke(width = 2.6f * scale, cap = StrokeCap.Round))
    drawPath(stemR, Color(0xFF2E7D32), style = Stroke(width = 2.6f * scale, cap = StrokeCap.Round))

    // Green junction leaf
    val leaf = Path().apply {
        moveTo(sx(50f), sy(14f))
        cubicTo(sx(64f), sy(8f), sx(74f), sy(14f), sx(70f), sy(20f))
        cubicTo(sx(58f), sy(20f), sx(52f), sy(18f), sx(50f), sy(14f))
        close()
    }
    drawPath(leaf, Color(0xFF388E3C), style = Fill)
    drawPath(leaf, ink, style = thinStroke)
}
