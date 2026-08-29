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

fun DrawScope.drawDetailedStrawberry(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val berryColor = palette?.primary ?: Color(0xFFE52D27)
    val seedColor = palette?.accent ?: Color(0xFFFFD54F)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Strawberry heart-shaped body
    val body = Path().apply {
        moveTo(sx(50f), sy(26f))
        cubicTo(sx(74f), sy(24f), sx(84f), sy(46f), sx(76f), sy(68f))
        cubicTo(sx(68f), sy(84f), sx(54f), sy(94f), sx(50f), sy(94f))
        cubicTo(sx(46f), sy(94f), sx(32f), sy(84f), sx(24f), sy(68f))
        cubicTo(sx(16f), sy(46f), sx(26f), sy(24f), sx(50f), sy(26f))
        close()
    }
    drawPath(body, berryColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Sunlit blush on left side
    val blush = Path().apply {
        moveTo(sx(32f), sy(34f))
        cubicTo(sx(26f), sy(48f), sx(28f), sy(64f), sx(38f), sy(76f))
    }
    drawPath(blush, Color.White.copy(alpha = 0.4f), style = Stroke(width = 3.0f * scale, cap = StrokeCap.Round))

    // Tiny golden achene seeds with tiny indent dots
    val achenes = listOf(
        Pair(38f, 38f), Pair(50f, 36f), Pair(62f, 38f),
        Pair(30f, 50f), Pair(44f, 48f), Pair(58f, 48f), Pair(70f, 50f),
        Pair(36f, 62f), Pair(50f, 60f), Pair(64f, 62f),
        Pair(42f, 74f), Pair(56f, 74f),
        Pair(48f, 84f)
    )
    achenes.forEach { (cx, cy) ->
        drawCircle(Color(0xFF5A1010).copy(alpha = 0.4f), radius = 1.6f * scale, center = Offset(sx(cx + 0.5f), sy(cy + 0.8f)))
        drawCircle(seedColor, radius = 1.3f * scale, center = Offset(sx(cx), sy(cy)))
    }

    // 5-pointed green calyx star cap
    val calyx = Path().apply {
        moveTo(sx(50f), sy(24f)); lineTo(sx(36f), sy(18f)); lineTo(sx(44f), sy(24f))
        lineTo(sx(28f), sy(28f)); lineTo(sx(42f), sy(28f))
        lineTo(sx(50f), sy(32f))
        lineTo(sx(58f), sy(28f)); lineTo(sx(72f), sy(28f))
        lineTo(sx(56f), sy(24f)); lineTo(sx(64f), sy(18f))
        lineTo(sx(50f), sy(24f))
        close()
    }
    drawPath(calyx, Color(0xFF388E3C), style = Fill)
    drawPath(calyx, ink, style = thinStroke)

    // Little top stem
    val stem = Path().apply {
        moveTo(sx(50f), sy(24f)); quadraticBezierTo(sx(52f), sy(14f), sx(46f), sy(8f))
    }
    drawPath(stem, Color(0xFF2E7D32), style = Stroke(width = 3.0f * scale, cap = StrokeCap.Round))
}

fun DrawScope.drawDetailedKiwi(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val fleshColor = palette?.primary ?: Color(0xFF86C138)
    val coreColor = if (palette?.variantTag == "golden_kiwi") Color(0xFFFFF9C4) else Color(0xFFF1F8E9)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Fuzzy brown exterior skin rim
    drawCircle(Color(0xFF7A5731), radius = 38f * scale, center = Offset(sx(50f), sy(50f)))
    drawCircle(ink, radius = 38f * scale, center = Offset(sx(50f), sy(50f)), style = lineStroke)

    // Vibrant chartreuse / golden kiwi flesh
    drawCircle(fleshColor, radius = 34.5f * scale, center = Offset(sx(50f), sy(50f)))

    // Creamy pale center core
    drawCircle(coreColor, radius = 11f * scale, center = Offset(sx(50f), sy(50f)))
    drawCircle(ink.copy(alpha = 0.2f), radius = 11f * scale, center = Offset(sx(50f), sy(50f)), style = thinStroke)

    // Radiating white ray lines and tiny black seeds
    for (i in 0 until 12) {
        val angle = (i * 30.0 * Math.PI / 180.0).toFloat()
        val cosA = kotlin.math.cos(angle)
        val sinA = kotlin.math.sin(angle)

        val r1 = 11.5f
        val r2 = 25f
        val ray = Path().apply {
            moveTo(sx(50f + r1 * cosA), sy(50f + r1 * sinA))
            lineTo(sx(50f + r2 * cosA), sy(50f + r2 * sinA))
        }
        drawPath(ray, Color.White.copy(alpha = 0.45f), style = Stroke(width = 1.2f * scale, cap = StrokeCap.Round))

        // Tiny black seed along ray
        val rSeed = 18f
        drawCircle(Color(0xFF1E1E1E), radius = 1.6f * scale, center = Offset(sx(50f + rSeed * cosA), sy(50f + rSeed * sinA)))
    }
}

fun DrawScope.drawDetailedDragonfruit(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val skinColor = palette?.primary ?: Color(0xFFE91E63)
    val fleshColor = if (palette?.variantTag == "red_dragonfruit") Color(0xFFC2185B) else Color(0xFFFAFAFA)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Bright magenta / yellow dragonfruit body
    val body = Path().apply {
        moveTo(sx(50f), sy(18f))
        cubicTo(sx(76f), sy(22f), sx(82f), sy(46f), sx(78f), sy(70f))
        cubicTo(sx(74f), sy(88f), sx(60f), sy(94f), sx(50f), sy(94f))
        cubicTo(sx(40f), sy(94f), sx(26f), sy(88f), sx(22f), sy(70f))
        cubicTo(sx(18f), sy(46f), sx(24f), sy(22f), sx(50f), sy(18f))
        close()
    }
    drawPath(body, skinColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Green leafy flame scale bracts
    val scales = listOf(
        Path().apply { moveTo(sx(24f), sy(36f)); quadraticTo(sx(12f), sy(30f), sx(10f), sy(20f)); quadraticTo(sx(20f), sy(26f), sx(30f), sy(32f)); close() },
        Path().apply { moveTo(sx(76f), sy(36f)); quadraticTo(sx(88f), sy(30f), sx(90f), sy(20f)); quadraticTo(sx(80f), sy(26f), sx(70f), sy(32f)); close() },
        Path().apply { moveTo(sx(22f), sy(64f)); quadraticTo(sx(10f), sy(60f), sx(8f), sy(50f)); quadraticTo(sx(18f), sy(54f), sx(26f), sy(60f)); close() },
        Path().apply { moveTo(sx(78f), sy(64f)); quadraticTo(sx(90f), sy(60f), sx(92f), sy(50f)); quadraticTo(sx(82f), sy(54f), sx(74f), sy(60f)); close() },
        Path().apply { moveTo(sx(50f), sy(18f)); quadraticTo(sx(50f), sy(6f), sx(44f), sy(2f)); quadraticTo(sx(54f), sy(8f), sx(54f), sy(18f)); close() }
    )
    scales.forEach { s ->
        drawPath(s, Color(0xFF8BC34A), style = Fill)
        drawPath(s, ink, style = thinStroke)
    }

    // Inner cross section / pulp window
    val pulp = Path().apply {
        moveTo(sx(50f), sy(32f))
        cubicTo(sx(68f), sy(34f), sx(72f), sy(52f), sx(68f), sy(70f))
        cubicTo(sx(64f), sy(82f), sx(56f), sy(86f), sx(50f), sy(86f))
        cubicTo(sx(44f), sy(86f), sx(36f), sy(82f), sx(32f), sy(70f))
        cubicTo(sx(28f), sy(52f), sx(32f), sy(34f), sx(50f), sy(32f))
        close()
    }
    drawPath(pulp, fleshColor, style = Fill)
    drawPath(pulp, ink.copy(alpha = 0.3f), style = thinStroke)

    // Tiny black speckled seeds
    val seeds = listOf(
        Pair(42f, 44f), Pair(54f, 42f), Pair(48f, 52f), Pair(38f, 58f),
        Pair(58f, 56f), Pair(46f, 66f), Pair(54f, 72f), Pair(42f, 76f)
    )
    val dotColor = if (palette?.variantTag == "red_dragonfruit") Color.White else Color(0xFF212121)
    seeds.forEach { (cx, cy) ->
        drawCircle(dotColor, radius = 1.3f * scale, center = Offset(sx(cx), sy(cy)))
    }
}

fun DrawScope.drawDetailedPineapple(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Golden oval body
    val body = Path().apply {
        moveTo(sx(50f), sy(38f))
        cubicTo(sx(74f), sy(40f), sx(80f), sy(60f), sx(74f), sy(82f))
        cubicTo(sx(68f), sy(94f), sx(58f), sy(96f), sx(50f), sy(96f))
        cubicTo(sx(42f), sy(96f), sx(32f), sy(94f), sx(26f), sy(82f))
        cubicTo(sx(20f), sy(60f), sx(26f), sy(40f), sx(50f), sy(38f))
        close()
    }
    drawPath(body, Color(0xFFFFB300), style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Crosshatch diamond pattern
    val grid = listOf(
        Pair(Pair(32f, 50f), Pair(68f, 86f)),
        Pair(Pair(26f, 64f), Pair(58f, 96f)),
        Pair(Pair(44f, 40f), Pair(74f, 70f)),
        Pair(Pair(68f, 50f), Pair(32f, 86f)),
        Pair(Pair(74f, 64f), Pair(42f, 96f)),
        Pair(Pair(56f, 40f), Pair(26f, 70f))
    )
    grid.forEach { (p1, p2) ->
        val line = Path().apply {
            moveTo(sx(p1.first), sy(p1.second))
            lineTo(sx(p2.first), sy(p2.second))
        }
        drawPath(line, Color(0xFFE65100), style = thinStroke)
    }

    // Scale eye dots
    listOf(Pair(50f, 52f), Pair(38f, 64f), Pair(62f, 64f), Pair(50f, 76f), Pair(38f, 86f), Pair(62f, 86f)).forEach { (cx, cy) ->
        drawCircle(Color(0xFF8D6E63), radius = 1.8f * scale, center = Offset(sx(cx), sy(cy)))
    }

    // Spiky crown leaves
    val crown = Path().apply {
        moveTo(sx(50f), sy(38f)); lineTo(sx(38f), sy(22f)); lineTo(sx(44f), sy(34f))
        lineTo(sx(32f), sy(12f)); lineTo(sx(44f), sy(26f))
        lineTo(sx(50f), sy(4f)); lineTo(sx(56f), sy(26f))
        lineTo(sx(68f), sy(12f)); lineTo(sx(56f), sy(34f))
        lineTo(sx(62f), sy(22f)); lineTo(sx(50f), sy(38f))
        close()
    }
    drawPath(crown, Color(0xFF2E7D32), style = Fill)
    drawPath(crown, ink, style = thinStroke)
}

fun DrawScope.drawDetailedBlueberry(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val berryColor = palette?.primary ?: Color(0xFF283593)
    val bloomColor = palette?.secondary ?: Color(0xFF7986CB)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Midnight indigo sphere
    drawCircle(berryColor, radius = 34f * scale, center = Offset(sx(50f), sy(52f)))
    // Soft powdery blue bloom crescent
    drawCircle(bloomColor.copy(alpha = 0.55f), radius = 30f * scale, center = Offset(sx(46f), sy(48f)))
    drawCircle(berryColor, radius = 28f * scale, center = Offset(sx(52f), sy(54f)))
    drawCircle(ink, radius = 34f * scale, center = Offset(sx(50f), sy(52f)), style = lineStroke)

    // 5-pointed crown calyx star at apex
    val calyx = Path().apply {
        moveTo(sx(50f), sy(24f)); lineTo(sx(44f), sy(28f)); lineTo(sx(38f), sy(26f))
        lineTo(sx(40f), sy(32f)); lineTo(sx(36f), sy(38f))
        lineTo(sx(44f), sy(38f)); lineTo(sx(50f), sy(44f))
        lineTo(sx(56f), sy(38f)); lineTo(sx(64f), sy(38f))
        lineTo(sx(60f), sy(32f)); lineTo(sx(62f), sy(26f))
        lineTo(sx(56f), sy(28f))
        close()
    }
    drawPath(calyx, Color(0xFF1A237E), style = Fill)
    drawPath(calyx, ink, style = thinStroke)

    // Specular shine
    drawCircle(Color.White.copy(alpha = 0.7f), radius = 2.4f * scale, center = Offset(sx(34f), sy(46f)))
}

fun DrawScope.drawDetailedGrape(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 2.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.4f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val grapeColor = palette?.primary ?: Color(0xFF6A1B9A)
    val highlightColor = palette?.secondary ?: Color(0xFF9C27B0)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Clustered grape berries
    val grapes = listOf(
        Pair(40f, 38f), Pair(60f, 38f),
        Pair(30f, 52f), Pair(50f, 50f), Pair(70f, 52f),
        Pair(40f, 66f), Pair(60f, 66f),
        Pair(50f, 80f),
        Pair(50f, 92f)
    )
    grapes.forEach { (cx, cy) ->
        drawCircle(grapeColor, radius = 12f * scale, center = Offset(sx(cx), sy(cy)))
        drawCircle(highlightColor.copy(alpha = 0.5f), radius = 9f * scale, center = Offset(sx(cx - 2f), sy(cy - 2f)))
        drawCircle(ink, radius = 12f * scale, center = Offset(sx(cx), sy(cy)), style = lineStroke)
        drawCircle(Color.White.copy(alpha = 0.7f), radius = 1.8f * scale, center = Offset(sx(cx - 3f), sy(cy - 3f)))
    }

    // Woody vine stalk with curling tendril
    val stem = Path().apply {
        moveTo(sx(50f), sy(28f)); lineTo(sx(50f), sy(12f))
        moveTo(sx(50f), sy(16f)); quadraticTo(sx(66f), sy(10f), sx(72f), sy(18f))
    }
    drawPath(stem, Color(0xFF5D4037), style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))

    // Vine leaf
    val leaf = Path().apply {
        moveTo(sx(50f), sy(18f))
        cubicTo(sx(32f), sy(10f), sx(24f), sy(20f), sx(32f), sy(28f))
        cubicTo(sx(42f), sy(28f), sx(46f), sy(24f), sx(50f), sy(18f))
        close()
    }
    drawPath(leaf, Color(0xFF388E3C), style = Fill)
    drawPath(leaf, ink, style = thinStroke)
}
