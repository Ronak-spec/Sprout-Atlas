package com.example.ui.components.doodles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.*

fun DrawScope.drawDetailedCarrot(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val bodyColor = palette?.primary ?: Color(0xFFFF6D00)
    val highlightColor = palette?.secondary ?: Color(0xFFFFAB40).copy(alpha = 0.6f)
    val ringColor = palette?.accent ?: Color(0xFFC43E00)
    val frondColor = palette?.leaf ?: Color(0xFF2E7D32)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Lush feathery carrot tops with multi-branching foliage
    val fronds = listOf(
        Path().apply {
            moveTo(sx(44f), sy(22f)); quadraticTo(sx(36f), sy(12f), sx(28f), sy(4f))
            moveTo(sx(38f), sy(14f)); lineTo(sx(32f), sy(12f))
            moveTo(sx(42f), sy(18f)); lineTo(sx(34f), sy(18f))
        },
        Path().apply {
            moveTo(sx(50f), sy(20f)); quadraticTo(sx(50f), sy(10f), sx(50f), sy(2f))
            moveTo(sx(50f), sy(12f)); lineTo(sx(44f), sy(8f))
            moveTo(sx(50f), sy(12f)); lineTo(sx(56f), sy(8f))
        },
        Path().apply {
            moveTo(sx(56f), sy(22f)); quadraticTo(sx(64f), sy(12f), sx(72f), sy(4f))
            moveTo(sx(62f), sy(14f)); lineTo(sx(68f), sy(12f))
            moveTo(sx(58f), sy(18f)); lineTo(sx(66f), sy(18f))
        }
    )
    fronds.forEach { frond ->
        drawPath(frond, frondColor, style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round))
    }

    // Tapered carrot root body
    val body = Path().apply {
        moveTo(sx(48f), sy(24f))
        cubicTo(sx(66f), sy(26f), sx(74f), sy(42f), sx(68f), sy(62f))
        cubicTo(sx(62f), sy(82f), sx(48f), sy(94f), sx(44f), sy(94f))
        cubicTo(sx(40f), sy(94f), sx(36f), sy(84f), sx(38f), sy(68f))
        cubicTo(sx(40f), sy(52f), sx(34f), sy(30f), sx(48f), sy(24f))
        close()
    }
    drawPath(body, bodyColor, style = Fill)

    // Highlight ridge on left flank
    val highlight = Path().apply {
        moveTo(sx(44f), sy(28f))
        cubicTo(sx(38f), sy(40f), sx(42f), sy(60f), sx(44f), sy(80f))
    }
    drawPath(highlight, highlightColor.copy(alpha = 0.5f), style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
    drawPath(body, ink, style = lineStroke)

    // Distinct horizontal growth ridges
    val rings = listOf(
        Pair(44f, 38f) to Pair(62f, 38f),
        Pair(42f, 50f) to Pair(58f, 50f),
        Pair(42f, 64f) to Pair(54f, 64f),
        Pair(43f, 76f) to Pair(50f, 76f)
    )
    rings.forEach { (start, end) ->
        val ringPath = Path().apply {
            moveTo(sx(start.first), sy(start.second))
            quadraticTo(sx((start.first + end.first) / 2f), sy(start.second + 2f), sx(end.first), sy(end.second))
        }
        drawPath(ringPath, ringColor.copy(alpha = 0.6f), style = thinStroke)
    }

    // Root tip hair
    val rootHair = Path().apply {
        moveTo(sx(44f), sy(94f)); quadraticTo(sx(45f), sy(98f), sx(42f), sy(100f))
    }
    drawPath(rootHair, ink, style = thinStroke)
}

fun DrawScope.drawDetailedBroccoli(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Branching central stalk
    val stalk = Path().apply {
        moveTo(sx(42f), sy(58f)); lineTo(sx(38f), sy(92f))
        lineTo(sx(62f), sy(92f)); lineTo(sx(58f), sy(58f))
        close()
    }
    drawPath(stalk, Color(0xFFC8E6C9), style = Fill)
    drawPath(stalk, ink, style = lineStroke)

    // Stalk cut lines / texture
    val stalkDetail = Path().apply {
        moveTo(sx(46f), sy(68f)); lineTo(sx(45f), sy(88f))
        moveTo(sx(54f), sy(68f)); lineTo(sx(55f), sy(88f))
    }
    drawPath(stalkDetail, Color(0xFF81C784), style = thinStroke)

    // Multi-shade dense floret cloud
    val florets = listOf(
        Triple(50f, 26f, 15f),
        Triple(34f, 36f, 13f),
        Triple(66f, 36f, 13f),
        Triple(24f, 50f, 11f),
        Triple(44f, 46f, 13f),
        Triple(56f, 46f, 13f),
        Triple(76f, 50f, 11f)
    )
    florets.forEach { (cx, cy, r) ->
        drawCircle(Color(0xFF2E7D32), radius = r * scale, center = Offset(sx(cx), sy(cy)))
        drawCircle(Color(0xFF43A047), radius = (r - 2.5f) * scale, center = Offset(sx(cx - 1.5f), sy(cy - 1.5f)))
        drawCircle(ink, radius = r * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
        // Texture stipples
        drawCircle(Color(0xFF1B5E20), radius = 1.2f * scale, center = Offset(sx(cx + 2f), sy(cy + 2f)))
        drawCircle(Color(0xFF81C784), radius = 1.0f * scale, center = Offset(sx(cx - 3f), sy(cy - 3f)))
    }
}

fun DrawScope.drawDetailedMushroom(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val capColor = palette?.primary ?: Color(0xFF8D6E63)
    val stemColor = palette?.secondary ?: Color(0xFFF5F5DC)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Stem with ring/annulus
    val stem = Path().apply {
        moveTo(sx(42f), sy(50f)); cubicTo(sx(40f), sy(68f), sx(36f), sy(84f), sx(38f), sy(90f))
        lineTo(sx(62f), sy(90f)); cubicTo(sx(64f), sy(84f), sx(60f), sy(68f), sx(58f), sy(50f))
        close()
    }
    drawPath(stem, stemColor, style = Fill)
    drawPath(stem, ink, style = lineStroke)

    // Stem ring
    val annulus = Path().apply {
        moveTo(sx(38f), sy(66f)); quadraticTo(sx(50f), sy(70f), sx(62f), sy(66f))
    }
    drawPath(annulus, ink, style = thinStroke)

    // Gills under cap
    val gills = Path().apply {
        moveTo(sx(24f), sy(52f)); quadraticTo(sx(50f), sy(58f), sx(76f), sy(52f))
    }
    drawPath(gills, Color(0xFFD7CCC8), style = Stroke(width = 4f * scale, cap = StrokeCap.Round))

    // Domed mushroom cap
    val cap = Path().apply {
        moveTo(sx(18f), sy(52f))
        cubicTo(sx(18f), sy(28f), sx(34f), sy(14f), sx(50f), sy(14f))
        cubicTo(sx(66f), sy(14f), sx(82f), sy(28f), sx(82f), sy(52f))
        cubicTo(sx(50f), sy(56f), sx(30f), sy(54f), sx(18f), sy(52f))
        close()
    }
    drawPath(cap, capColor, style = Fill)
    drawPath(cap, ink, style = lineStroke)

    // Veil specks/dots
    val spots = listOf(
        Pair(34f, 28f), Pair(50f, 22f), Pair(66f, 28f),
        Pair(26f, 40f), Pair(48f, 36f), Pair(72f, 40f)
    )
    spots.forEach { (cx, cy) ->
        drawCircle(Color(0xFFEFEBE9).copy(alpha = 0.85f), radius = 2.8f * scale, center = Offset(sx(cx), sy(cy)))
        drawCircle(ink.copy(alpha = 0.25f), radius = 2.8f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
    }
}

fun DrawScope.drawDetailedOnion(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val bulbColor = palette?.primary ?: Color(0xFFE6C687)
    val striationColor = palette?.secondary ?: Color(0xFFB8860B)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Onion bulb body
    val body = Path().apply {
        moveTo(sx(50f), sy(16f))
        cubicTo(sx(70f), sy(18f), sx(82f), sy(36f), sx(78f), sy(56f))
        cubicTo(sx(74f), sy(78f), sx(64f), sy(88f), sx(50f), sy(88f))
        cubicTo(sx(36f), sy(88f), sx(26f), sy(78f), sx(22f), sy(56f))
        cubicTo(sx(18f), sy(36f), sx(30f), sy(18f), sx(50f), sy(16f))
        close()
    }
    drawPath(body, bulbColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Papery vertical peel striations
    val striations = listOf(
        Path().apply { moveTo(sx(50f), sy(16f)); quadraticTo(sx(34f), sy(50f), sx(46f), sy(88f)) },
        Path().apply { moveTo(sx(50f), sy(16f)); lineTo(sx(50f), sy(88f)) },
        Path().apply { moveTo(sx(50f), sy(16f)); quadraticTo(sx(66f), sy(50f), sx(54f), sy(88f)) }
    )
    striations.forEach { striation ->
        drawPath(striation, striationColor.copy(alpha = 0.5f), style = thinStroke)
    }

    // Top sprout shoot
    val sprout = Path().apply {
        moveTo(sx(50f), sy(16f)); lineTo(sx(48f), sy(6f))
        moveTo(sx(50f), sy(16f)); lineTo(sx(54f), sy(8f))
    }
    drawPath(sprout, SproutLeaf, style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round))

    // Fibrous root tuft beard
    val roots = Path().apply {
        moveTo(sx(46f), sy(88f)); lineTo(sx(42f), sy(96f))
        moveTo(sx(50f), sy(88f)); lineTo(sx(50f), sy(98f))
        moveTo(sx(54f), sy(88f)); lineTo(sx(58f), sy(96f))
    }
    drawPath(roots, Color(0xFF8D6E63), style = Stroke(width = 2.0f * scale, cap = StrokeCap.Round))
}

fun DrawScope.drawDetailedGarlic(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Bulging ivory cloves
    val cloves = Path().apply {
        moveTo(sx(50f), sy(22f))
        cubicTo(sx(68f), sy(24f), sx(80f), sy(42f), sx(76f), sy(60f))
        cubicTo(sx(78f), sy(76f), sx(68f), sy(88f), sx(50f), sy(88f))
        cubicTo(sx(32f), sy(88f), sx(22f), sy(76f), sx(24f), sy(60f))
        cubicTo(sx(20f), sy(42f), sx(32f), sy(24f), sx(50f), sy(22f))
        close()
    }
    drawPath(cloves, Color(0xFFF9F7EB), style = Fill)
    drawPath(cloves, ink, style = lineStroke)

    // Clove contour lines
    val cloveLines = Path().apply {
        moveTo(sx(42f), sy(26f)); quadraticTo(sx(36f), sy(54f), sx(42f), sy(86f))
        moveTo(sx(58f), sy(26f)); quadraticTo(sx(64f), sy(54f), sx(58f), sy(86f))
    }
    drawPath(cloveLines, Color(0xFFD7CCC8), style = thinStroke)

    // Dried stalk
    val stalk = Path().apply {
        moveTo(sx(50f), sy(22f)); lineTo(sx(50f), sy(8f))
    }
    drawPath(stalk, Color(0xFFBCAAA4), style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round))
}

fun DrawScope.drawDetailedPepper(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val pepperColor = palette?.primary ?: Color(0xFFE53935)
    val lobeColor = palette?.accent ?: Color(0xFFB71C1C)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Bell pepper multi-lobed body
    val body = Path().apply {
        moveTo(sx(50f), sy(24f))
        cubicTo(sx(72f), sy(26f), sx(82f), sy(44f), sx(78f), sy(66f))
        cubicTo(sx(74f), sy(84f), sx(62f), sy(92f), sx(50f), sy(90f))
        cubicTo(sx(38f), sy(92f), sx(26f), sy(84f), sx(22f), sy(66f))
        cubicTo(sx(18f), sy(44f), sx(28f), sy(26f), sx(50f), sy(24f))
        close()
    }
    drawPath(body, pepperColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Lobe vertical indentations
    val lobes = Path().apply {
        moveTo(sx(40f), sy(26f)); quadraticTo(sx(38f), sy(58f), sx(42f), sy(88f))
        moveTo(sx(60f), sy(26f)); quadraticTo(sx(62f), sy(58f), sx(58f), sy(88f))
    }
    drawPath(lobes, lobeColor.copy(alpha = 0.6f), style = thinStroke)

    // Gloss glare highlights
    val glare = Path().apply {
        moveTo(sx(30f), sy(38f)); cubicTo(sx(28f), sy(50f), sx(30f), sy(64f), sx(34f), sy(72f))
    }
    drawPath(glare, Color.White.copy(alpha = 0.5f), style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round))

    // Thick green stem
    val stem = Path().apply {
        moveTo(sx(50f), sy(24f)); quadraticTo(sx(54f), sy(14f), sx(48f), sy(6f))
    }
    drawPath(stem, Color(0xFF2E7D32), style = Stroke(width = 4.2f * scale, cap = StrokeCap.Round))
}
