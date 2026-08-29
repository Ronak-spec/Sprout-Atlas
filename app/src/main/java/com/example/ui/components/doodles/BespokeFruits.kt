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

fun DrawScope.drawDetailedSprout(scale: Float) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk
    val leaf = SproutLeaf
    val leafDark = SproutLeafDark
    val leafLight = Color(0xFFA8D965)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // 1. Root base and delicate subterranean rootlets
    val rootBase = Path().apply {
        moveTo(sx(46f), sy(84f))
        cubicTo(sx(44f), sy(88f), sx(48f), sy(94f), sx(50f), sy(96f))
        cubicTo(sx(52f), sy(94f), sx(56f), sy(88f), sx(54f), sy(84f))
        close()
    }
    drawPath(rootBase, Color(0xFFDCC89E), style = Fill)
    drawPath(rootBase, ink, style = thinStroke)

    val rootlets = Path().apply {
        moveTo(sx(50f), sy(94f)); lineTo(sx(45f), sy(98f))
        moveTo(sx(50f), sy(94f)); lineTo(sx(55f), sy(98f))
        moveTo(sx(50f), sy(90f)); lineTo(sx(40f), sy(92f))
        moveTo(sx(50f), sy(90f)); lineTo(sx(60f), sy(92f))
    }
    drawPath(rootlets, Color(0xFFB5A078), style = Stroke(width = 1.6f * scale, cap = StrokeCap.Round))

    // 2. Rising curved green hypocotyl stem
    val stem = Path().apply {
        moveTo(sx(50f), sy(88f))
        cubicTo(sx(48f), sy(68f), sx(52f), sy(50f), sx(50f), sy(26f))
    }
    drawPath(stem, leafDark, style = Stroke(width = 4.2f * scale, cap = StrokeCap.Round))
    drawPath(stem, ink, style = thinStroke)

    // 3. Left Cotyledon Leaf
    val leafL = Path().apply {
        moveTo(sx(50f), sy(48f))
        cubicTo(sx(32f), sy(46f), sx(18f), sy(34f), sx(16f), sy(16f))
        cubicTo(sx(38f), sy(16f), sx(51f), sy(28f), sx(52f), sy(46f))
        close()
    }
    drawPath(leafL, leaf, style = Fill)
    val leafLHighlight = Path().apply {
        moveTo(sx(50f), sy(48f))
        cubicTo(sx(38f), sy(36f), sx(28f), sy(24f), sx(16f), sy(16f))
        cubicTo(sx(36f), sy(16f), sx(48f), sy(26f), sx(50f), sy(44f))
        close()
    }
    drawPath(leafLHighlight, leafLight, style = Fill)
    drawPath(leafL, ink, style = lineStroke)

    // Left Leaf Midrib & veins
    val leafLVeins = Path().apply {
        moveTo(sx(50f), sy(46f)); quadraticTo(sx(34f), sy(32f), sx(18f), sy(18f))
        moveTo(sx(42f), sy(36f)); lineTo(sx(34f), sy(42f))
        moveTo(sx(34f), sy(28f)); lineTo(sx(26f), sy(34f))
        moveTo(sx(38f), sy(30f)); lineTo(sx(36f), sy(20f))
    }
    drawPath(leafLVeins, leafDark, style = thinStroke)

    // 4. Right Cotyledon Leaf
    val leafR = Path().apply {
        moveTo(sx(50f), sy(48f))
        cubicTo(sx(68f), sy(46f), sx(82f), sy(34f), sx(84f), sy(16f))
        cubicTo(sx(62f), sy(16f), sx(49f), sy(28f), sx(48f), sy(46f))
        close()
    }
    drawPath(leafR, leaf, style = Fill)
    val leafRHighlight = Path().apply {
        moveTo(sx(50f), sy(48f))
        cubicTo(sx(62f), sy(36f), sx(72f), sy(24f), sx(84f), sy(16f))
        cubicTo(sx(64f), sy(16f), sx(52f), sy(26f), sx(50f), sy(44f))
        close()
    }
    drawPath(leafRHighlight, leafLight, style = Fill)
    drawPath(leafR, ink, style = lineStroke)

    // Right Leaf Midrib & veins
    val leafRVeins = Path().apply {
        moveTo(sx(50f), sy(46f)); quadraticTo(sx(66f), sy(32f), sx(82f), sy(18f))
        moveTo(sx(58f), sy(36f)); lineTo(sx(66f), sy(42f))
        moveTo(sx(66f), sy(28f)); lineTo(sx(74f), sy(34f))
        moveTo(sx(62f), sy(30f)); lineTo(sx(64f), sy(20f))
    }
    drawPath(leafRVeins, leafDark, style = thinStroke)

    // 5. Delicate glistening dewdrop highlight
    drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5f * scale, center = Offset(sx(26f), sy(22f)))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.0f * scale, center = Offset(sx(74f), sy(22f)))
}

fun DrawScope.drawDetailedApple(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk
    val leaf = palette?.leaf ?: SproutLeaf
    val leafDark = palette?.leafDark ?: SproutLeafDark

    val primaryColor = palette?.primary ?: Color(0xFFD63B2F)
    val blushColor = palette?.secondary ?: Color(0xFFF39C12).copy(alpha = 0.45f)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Apple body
    val body = Path().apply {
        moveTo(sx(50f), sy(27f))
        cubicTo(sx(68f), sy(24f), sx(84f), sy(38f), sx(82f), sy(58f))
        cubicTo(sx(80f), sy(78f), sx(65f), sy(91f), sx(50f), sy(89f))
        cubicTo(sx(35f), sy(91f), sx(18f), sy(78f), sx(18f), sy(58f))
        cubicTo(sx(16f), sy(38f), sx(32f), sy(24f), sx(50f), sy(27f))
        close()
    }
    drawPath(body, primaryColor, style = Fill)

    // Sunlit blush contour
    val blush = Path().apply {
        moveTo(sx(50f), sy(28f))
        cubicTo(sx(38f), sy(30f), sx(26f), sy(42f), sx(24f), sy(58f))
        cubicTo(sx(24f), sy(74f), sx(36f), sy(84f), sx(50f), sy(88f))
        cubicTo(sx(42f), sy(80f), sx(32f), sy(70f), sx(32f), sy(56f))
        cubicTo(sx(32f), sy(42f), sx(42f), sy(32f), sx(50f), sy(28f))
        close()
    }
    drawPath(blush, blushColor, style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Calyx indent line at top and base
    val topIndent = Path().apply {
        moveTo(sx(44f), sy(27f)); quadraticTo(sx(50f), sy(32f), sx(56f), sy(27f))
    }
    drawPath(topIndent, Color(0xFF6B1D1D).copy(alpha = 0.8f), style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round))

    // Wooden Stem with growth texture
    val stem = Path().apply {
        moveTo(sx(50f), sy(28f))
        cubicTo(sx(52f), sy(16f), sx(60f), sy(9f), sx(68f), sy(10f))
    }
    drawPath(stem, Color(0xFF5D3A1A), style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
    drawPath(stem, ink, style = Stroke(width = 1.4f * scale, cap = StrokeCap.Round))

    // Attached green Leaf with midrib vein
    val leafP = Path().apply {
        moveTo(sx(58f), sy(18f))
        cubicTo(sx(72f), sy(8f), sx(84f), sy(14f), sx(82f), sy(22f))
        cubicTo(sx(70f), sy(26f), sx(60f), sy(22f), sx(58f), sy(18f))
        close()
    }
    drawPath(leafP, leaf, style = Fill)
    drawPath(leafP, ink, style = thinStroke)
    val leafVein = Path().apply {
        moveTo(sx(58f), sy(18f)); quadraticTo(sx(70f), sy(16f), sx(82f), sy(20f))
    }
    drawPath(leafVein, leafDark, style = thinStroke)

    // Curved Specular Gleam Highlights
    val shine1 = Path().apply {
        moveTo(sx(32f), sy(38f))
        cubicTo(sx(36f), sy(46f), sx(36f), sy(58f), sx(30f), sy(68f))
    }
    drawPath(shine1, Color.White.copy(alpha = 0.65f), style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.0f * scale, center = Offset(sx(36f), sy(36f)))
}

fun DrawScope.drawDetailedAvocado(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // 1. Dark pebbled skin exterior (black/emerald)
    val body = Path().apply {
        moveTo(sx(50f), sy(12f))
        cubicTo(sx(68f), sy(14f), sx(80f), sy(32f), sx(78f), sy(54f))
        cubicTo(sx(76f), sy(78f), sx(64f), sy(92f), sx(48f), sy(92f))
        cubicTo(sx(30f), sy(90f), sx(20f), sy(74f), sx(22f), sy(52f))
        cubicTo(sx(24f), sy(30f), sx(34f), sy(12f), sx(50f), sy(12f))
        close()
    }
    drawPath(body, Color(0xFF1E3821), style = Fill)
    drawPath(body, ink, style = lineStroke)

    // Skin texture stippling
    listOf(Pair(26f, 38f), Pair(74f, 40f), Pair(26f, 68f), Pair(72f, 70f), Pair(48f, 90f)).forEach { (x, y) ->
        drawCircle(Color(0xFF325936), radius = 1.6f * scale, center = Offset(sx(x), sy(y)))
    }

    // 2. Vibrant lime flesh gradient outer zone
    val fleshOuter = Path().apply {
        moveTo(sx(50f), sy(18f))
        cubicTo(sx(64f), sy(20f), sx(74f), sy(34f), sx(72f), sy(54f))
        cubicTo(sx(70f), sy(74f), sx(60f), sy(86f), sx(48f), sy(86f))
        cubicTo(sx(34f), sy(84f), sx(26f), sy(70f), sx(28f), sy(52f))
        cubicTo(sx(30f), sy(32f), sx(38f), sy(18f), sx(50f), sy(18f))
        close()
    }
    drawPath(fleshOuter, Color(0xFF8BBF42), style = Fill)

    // 3. Creamy buttery interior zone
    val fleshInner = Path().apply {
        moveTo(sx(50f), sy(26f))
        cubicTo(sx(60f), sy(28f), sx(68f), sy(38f), sx(66f), sy(54f))
        cubicTo(sx(64f), sy(70f), sx(56f), sy(80f), sx(48f), sy(80f))
        cubicTo(sx(38f), sy(78f), sx(32f), sy(68f), sx(34f), sy(52f))
        cubicTo(sx(36f), sy(38f), sx(42f), sy(26f), sx(50f), sy(26f))
        close()
    }
    drawPath(fleshInner, Color(0xFFE9E598), style = Fill)
    drawPath(fleshOuter, ink.copy(alpha = 0.2f), style = thinStroke)

    // 4. Recessed pit socket shadow
    drawCircle(Color(0xFF5A361C).copy(alpha = 0.35f), radius = 14.5f * scale, center = Offset(sx(49f), sy(58f)))

    // 5. Polished mahogany spherical seed pit with 3D highlight
    drawCircle(Color(0xFF6E3B1F), radius = 13f * scale, center = Offset(sx(49f), sy(57f)))
    drawCircle(ink, radius = 13f * scale, center = Offset(sx(49f), sy(57f)), style = thinStroke)
    // Seed core shadow & highlight arc
    drawCircle(Color(0xFF4A2510), radius = 11.5f * scale, center = Offset(sx(50f), sy(58f)))
    drawCircle(Color(0xFF8B4D28), radius = 8f * scale, center = Offset(sx(46f), sy(54f)))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 3.2f * scale, center = Offset(sx(45f), sy(52f)))
    drawCircle(Color.White.copy(alpha = 0.5f), radius = 1.6f * scale, center = Offset(sx(48f), sy(49f)))
}

fun DrawScope.drawDetailedWatermelon(scale: Float, palette: BotanicalPalette? = null) {
    val lineStroke = Stroke(width = 3.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk

    val fleshColor = palette?.primary ?: Color(0xFFE53935)

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    // Outer dark green rind
    val rindArc = Path().apply {
        moveTo(sx(12f), sy(32f))
        cubicTo(sx(24f), sy(82f), sx(76f), sy(82f), sx(88f), sy(32f))
        lineTo(sx(80f), sy(36f))
        cubicTo(sx(70f), sy(74f), sx(30f), sy(74f), sx(20f), sy(36f))
        close()
    }
    drawPath(rindArc, Color(0xFF2C5E28), style = Fill)
    drawPath(rindArc, ink, style = lineStroke)

    // Inner pale rind margin
    val whiteRind = Path().apply {
        moveTo(sx(20f), sy(36f))
        cubicTo(sx(30f), sy(74f), sx(70f), sy(74f), sx(80f), sy(36f))
        lineTo(sx(76f), sy(38f))
        cubicTo(sx(68f), sy(70f), sx(32f), sy(70f), sx(24f), sy(38f))
        close()
    }
    drawPath(whiteRind, Color(0xFFD2E6AF), style = Fill)

    // Vivid ruby-red sweet flesh
    val flesh = Path().apply {
        moveTo(sx(24f), sy(38f))
        cubicTo(sx(32f), sy(70f), sx(68f), sy(70f), sx(76f), sy(38f))
        lineTo(sx(50f), sy(16f))
        close()
    }
    drawPath(flesh, fleshColor, style = Fill)
    drawPath(flesh, ink, style = lineStroke)

    // Teardrop black seeds with specular glints
    val seedPositions = listOf(
        Pair(40f, 42f), Pair(60f, 42f), Pair(50f, 52f),
        Pair(36f, 54f), Pair(64f, 54f), Pair(44f, 64f), Pair(56f, 64f)
    )
    seedPositions.forEach { (x, y) ->
        val seedPath = Path().apply {
            moveTo(sx(x), sy(y - 3f))
            cubicTo(sx(x + 2.2f), sy(y), sx(x + 2.2f), sy(y + 3.5f), sx(x), sy(y + 4.5f))
            cubicTo(sx(x - 2.2f), sy(y + 3.5f), sx(x - 2.2f), sy(y), sx(x), sy(y - 3f))
            close()
        }
        drawPath(seedPath, Color(0xFF211515), style = Fill)
        drawCircle(Color.White.copy(alpha = 0.85f), radius = 1.0f * scale, center = Offset(sx(x - 0.7f), sy(y + 1f)))
    }
}
