package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.components.doodles.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Mulberry32(private var a: Long) {
    fun next(): Float {
        a = (a + 0x6D2B79F5L) and 0xFFFFFFFFL
        var t = (a xor (a ushr 15)) * (1L or a) and 0xFFFFFFFFL
        t = (t + ((t xor (t ushr 7)) * (61L or t) and 0xFFFFFFFFL)) xor t and 0xFFFFFFFFL
        return (((t xor (t ushr 14)) and 0xFFFFFFFFL).toFloat()) / 4294967296f
    }
}

private fun seedFromString(str: String): Long {
    var h = 0L
    for (ch in str) {
        h = ((31 * h + ch.code.toLong()) and 0xFFFFFFFFL)
    }
    return h
}

@Composable
fun ProduceDoodle(
    symbolId: String?,
    archetype: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val seed = remember(name) { seedFromString(name) }
    val fpSeed = remember(name) { seedFromString("$name::fingerprint") }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val scaleFactor = this.size.width / 100f
            drawUniversalProduceDoodle(
                symbolId = symbolId,
                archetype = archetype,
                name = name,
                seed = seed,
                fpSeed = fpSeed,
                scale = scaleFactor
            )
        }
    }
}

private fun DrawScope.drawUniversalProduceDoodle(
    symbolId: String?,
    archetype: String,
    name: String,
    seed: Long,
    fpSeed: Long,
    scale: Float
) {
    val botanicalPalette = ProduceColorResolver.resolveColors(name, archetype, symbolId)

    if (symbolId != null && hasBespokeSymbol(symbolId)) {
        drawBespokeSymbol(symbolId, scale, botanicalPalette)
        return
    }

    // Seeded Procedural Generator with botanical precision
    val rng = Mulberry32(seed)
    val fp = Mulberry32(fpSeed)

    val deg = (fp.next() - 0.5f) * 12f
    val scaleAdjust = 0.96f + fp.next() * 0.08f

    rotate(degrees = deg, pivot = Offset(50f * scale, 54f * scale)) {
        scale(scaleX = scaleAdjust, scaleY = scaleAdjust, pivot = Offset(50f * scale, 54f * scale)) {
            drawArchetypeProcedural(archetype, rng, botanicalPalette, scale)
        }
    }
}

private fun hasBespokeSymbol(id: String): Boolean {
    return when (id) {
        "d-apple", "d-carrot", "d-strawberry", "d-avocado", "d-broccoli", "d-lemon",
        "d-tomato", "d-banana", "d-pepper", "d-grape", "d-onion", "d-kiwi",
        "d-mango", "d-pineapple", "d-papaya", "d-guava", "d-passionfruit", "d-dragonfruit",
        "d-jackfruit", "d-durian", "d-rambutan", "d-mangosteen", "d-starfruit", "d-coconut",
        "d-blueberry", "d-raspberry", "d-blackberry", "d-cranberry", "d-potato", "d-sweetpotato",
        "d-beet", "d-radish", "d-peach", "d-cherry", "d-plum", "d-apricot",
        "d-pomegranate", "d-fig", "d-persimmon", "d-spinach", "d-kale", "d-lettuce",
        "d-cabbage", "d-cauliflower", "d-orange", "d-grapefruit", "d-lime", "d-eggplant",
        "d-pumpkin", "d-zucchini", "d-cucumber", "d-greenbean", "d-pea", "d-pear",
        "d-garlic", "d-watermelon", "d-cantaloupe", "d-celery", "d-basil", "d-mushroom",
        "d-nori", "d-sprout", "d-corn", "d-asparagus", "d-ginger", "d-turmeric", "d-artichoke" -> true
        else -> false
    }
}

// Procedural shape generator matching natural botanical features
private fun DrawScope.drawArchetypeProcedural(
    archetype: String,
    rng: Mulberry32,
    palette: BotanicalPalette,
    scale: Float
) {
    val lineStroke = Stroke(width = 3.0f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk
    val leaf = palette.leaf
    val leafDark = palette.leafDark
    val color = palette.primary
    val colorSecondary = palette.secondary

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    val v = (rng.next() * 3).toInt()

    when (archetype) {
        "tropical" -> {
            val v4 = (rng.next() * 4).toInt()
            when (v4) {
                0 -> {
                    // Plump tropical drupe with blush
                    val p = generateBlobPath(rng, 50f, 52f, 28f, 32f, scale)
                    drawPath(p, color, style = Fill)
                    drawPath(p, ink, style = lineStroke)
                    val blush = Path().apply {
                        moveTo(sx(40f), sy(26f))
                        quadraticBezierTo(sx(46f), sy(39f), sx(40f), sy(52f))
                    }
                    drawPath(blush, colorSecondary.copy(alpha = 0.45f), style = lineStroke)
                    val stem = Path().apply {
                        moveTo(sx(40f), sy(18f))
                        quadraticBezierTo(sx(35f), sy(12f), sx(27f), sy(11f))
                    }
                    drawPath(stem, leaf, style = thinStroke)
                }
                1 -> {
                    // Elongated tapered tropical fruit
                    val p = generateBlobPath(rng, 50f, 52f, 22f, 34f, scale)
                    drawPath(p, color, style = Fill)
                    drawPath(p, ink, style = lineStroke)
                    val stem = Path().apply {
                        moveTo(sx(60f), sy(18f))
                        quadraticBezierTo(sx(66f), sy(12f), sx(72f), sy(13f))
                    }
                    drawPath(stem, leaf, style = thinStroke)
                }
                2 -> {
                    // Crowned fruit
                    val p = generateBlobPath(rng, 50f, 58f, 30f, 25f, scale)
                    drawPath(p, color, style = Fill)
                    drawPath(p, ink, style = lineStroke)
                    for (i in 0..3) {
                        val ang = (i - 1.5f) * 15f
                        val crown = Path().apply {
                            moveTo(sx(50f), sy(34f))
                            lineTo(sx(50f + ang), sy(8f + (i % 2) * 4f))
                        }
                        drawPath(crown, leaf, style = Stroke(width = 4.2f * scale, cap = StrokeCap.Round))
                    }
                }
                else -> {
                    // Clustered tropical drupelets
                    for (i in 0..3) {
                        val rad = i * (PI.toFloat() * 2f / 4f)
                        val cx = 50f + cos(rad) * 14f
                        val cy = 54f + sin(rad) * 14f
                        drawCircle(color, radius = 12f * scale, center = Offset(sx(cx), sy(cy)))
                        drawCircle(ink, radius = 12f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
                    }
                    val stem = Path().apply {
                        moveTo(sx(50f), sy(30f))
                        quadraticBezierTo(sx(40f), sy(22f), sx(28f), sy(24f))
                    }
                    drawPath(stem, leaf, style = thinStroke)
                }
            }
        }
        "berry" -> {
            when (v) {
                0 -> {
                    // Single round berry with calyx
                    val r = 22f
                    drawCircle(color, radius = r * scale, center = Offset(sx(50f), sy(56f)))
                    drawCircle(ink, radius = r * scale, center = Offset(sx(50f), sy(56f)), style = lineStroke)
                    val calyx = Path().apply {
                        moveTo(sx(40f), sy(34f)); lineTo(sx(44f), sy(26f)); lineTo(sx(50f), sy(34f))
                        lineTo(sx(56f), sy(26f)); lineTo(sx(60f), sy(34f))
                    }
                    drawPath(calyx, ink, style = thinStroke)
                    drawCircle(Color.White.copy(alpha = 0.6f), radius = 2.4f * scale, center = Offset(sx(40f), sy(48f)))
                }
                1 -> {
                    // Cluster of drupelets
                    val spots = listOf(
                        Pair(50f, 34f), Pair(38f, 44f), Pair(62f, 44f),
                        Pair(44f, 56f), Pair(56f, 56f), Pair(50f, 68f)
                    )
                    val stem = Path().apply {
                        moveTo(sx(50f), sy(20f))
                        quadraticBezierTo(sx(40f), sy(12f), sx(32f), sy(20f))
                    }
                    drawPath(stem, leaf, style = thinStroke)
                    spots.forEach { (cx, cy) ->
                        drawCircle(color, radius = 9f * scale, center = Offset(sx(cx), sy(cy)))
                        drawCircle(ink, radius = 9f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
                    }
                }
                else -> {
                    // Ribbed berries cluster
                    val stem = Path().apply {
                        moveTo(sx(50f), sy(24f)); lineTo(sx(38f), sy(38f))
                        moveTo(sx(50f), sy(24f)); lineTo(sx(62f), sy(38f))
                    }
                    drawPath(stem, leaf, style = thinStroke)
                    listOf(Pair(38f, 50f), Pair(62f, 50f), Pair(50f, 66f)).forEach { (cx, cy) ->
                        drawOval(color, topLeft = Offset(sx(cx - 14f), sy(cy - 16f)), size = Size(28f * scale, 32f * scale))
                        drawOval(ink, topLeft = Offset(sx(cx - 14f), sy(cy - 16f)), size = Size(28f * scale, 32f * scale), style = thinStroke)
                    }
                }
            }
        }
        "root" -> {
            when (v) {
                0 -> {
                    // Tapered taproot with horizontal ridges
                    val leaves = Path().apply {
                        moveTo(sx(44f), sy(20f)); lineTo(sx(38f), sy(4f))
                        moveTo(sx(50f), sy(22f)); lineTo(sx(52f), sy(2f))
                        moveTo(sx(56f), sy(24f)); lineTo(sx(64f), sy(8f))
                    }
                    drawPath(leaves, leaf, style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))
                    val root = Path().apply {
                        moveTo(sx(48f), sy(26f))
                        cubicTo(sx(64f), sy(30f), sx(72f), sy(44f), sx(66f), sy(62f))
                        cubicTo(sx(60f), sy(82f), sx(46f), sy(92f), sx(40f), sy(88f))
                        cubicTo(sx(34f), sy(84f), sx(40f), sy(68f), sx(44f), sy(56f))
                        cubicTo(sx(48f), sy(44f), sx(40f), sy(32f), sx(48f), sy(26f))
                        close()
                    }
                    drawPath(root, color, style = Fill)
                    drawPath(root, ink, style = lineStroke)
                    val r1 = Path().apply { moveTo(sx(42f), sy(44f)); lineTo(sx(58f), sy(44f)) }
                    val r2 = Path().apply { moveTo(sx(44f), sy(60f)); lineTo(sx(54f), sy(60f)) }
                    drawPath(r1, ink.copy(alpha = 0.35f), style = thinStroke)
                    drawPath(r2, ink.copy(alpha = 0.35f), style = thinStroke)
                }
                1 -> {
                    // Globular bulb root
                    val root = Path().apply {
                        moveTo(sx(50f), sy(26f))
                        cubicTo(sx(68f), sy(26f), sx(78f), sy(40f), sx(76f), sy(58f))
                        cubicTo(sx(74f), sy(76f), sx(62f), sy(86f), sx(50f), sy(86f))
                        cubicTo(sx(38f), sy(86f), sx(26f), sy(76f), sx(24f), sy(58f))
                        cubicTo(sx(22f), sy(40f), sx(32f), sy(26f), sx(50f), sy(26f))
                        close()
                    }
                    drawPath(root, color, style = Fill)
                    drawPath(root, ink, style = lineStroke)
                    val tail = Path().apply { moveTo(sx(50f), sy(86f)); lineTo(sx(48f), sy(96f)) }
                    drawPath(tail, ink, style = lineStroke)
                    val leaves = Path().apply {
                        moveTo(sx(44f), sy(26f)); lineTo(sx(36f), sy(8f))
                        moveTo(sx(56f), sy(26f)); lineTo(sx(64f), sy(8f))
                    }
                    drawPath(leaves, leaf, style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))
                }
                else -> {
                    // Oblong tuber
                    val tuber = Path().apply {
                        moveTo(sx(24f), sy(52f))
                        cubicTo(sx(20f), sy(34f), sx(42f), sy(22f), sx(60f), sy(26f))
                        cubicTo(sx(78f), sy(30f), sx(84f), sy(48f), sx(82f), sy(64f))
                        cubicTo(sx(78f), sy(80f), sx(58f), sy(84f), sx(40f), sy(80f))
                        close()
                    }
                    drawPath(tuber, color, style = Fill)
                    drawPath(tuber, ink, style = lineStroke)
                    drawCircle(ink.copy(alpha = 0.5f), radius = 1.6f * scale, center = Offset(sx(40f), sy(44f)))
                    drawCircle(ink.copy(alpha = 0.5f), radius = 1.6f * scale, center = Offset(sx(60f), sy(56f)))
                }
            }
        }
        "stone" -> {
            when (v) {
                0 -> {
                    // Heart-shaped stone fruit with cleft
                    val fruit = Path().apply {
                        moveTo(sx(50f), sy(24f))
                        cubicTo(sx(72f), sy(22f), sx(82f), sy(44f), sx(78f), sy(66f))
                        cubicTo(sx(74f), sy(84f), sx(58f), sy(92f), sx(50f), sy(92f))
                        cubicTo(sx(42f), sy(92f), sx(26f), sy(84f), sx(22f), sy(66f))
                        cubicTo(sx(18f), sy(44f), sx(28f), sy(22f), sx(50f), sy(24f))
                        close()
                    }
                    drawPath(fruit, color, style = Fill)
                    val cleft = Path().apply {
                        moveTo(sx(50f), sy(24f)); quadraticBezierTo(sx(46f), sy(54f), sx(50f), sy(92f))
                    }
                    drawPath(cleft, ink.copy(alpha = 0.4f), style = thinStroke)
                    drawPath(fruit, ink, style = lineStroke)
                    val stem = Path().apply { moveTo(sx(50f), sy(24f)); lineTo(sx(52f), sy(14f)) }
                    drawPath(stem, leafDark, style = lineStroke)
                }
                1 -> {
                    // Twin stone cherries
                    drawCircle(color, radius = 16f * scale, center = Offset(sx(36f), sy(66f)))
                    drawCircle(color, radius = 16f * scale, center = Offset(sx(64f), sy(70f)))
                    drawCircle(ink, radius = 16f * scale, center = Offset(sx(36f), sy(66f)), style = lineStroke)
                    drawCircle(ink, radius = 16f * scale, center = Offset(sx(64f), sy(70f)), style = lineStroke)
                    val stem1 = Path().apply { moveTo(sx(36f), sy(50f)); cubicTo(sx(38f), sy(34f), sx(48f), sy(18f), sx(50f), sy(14f)) }
                    val stem2 = Path().apply { moveTo(sx(64f), sy(54f)); cubicTo(sx(62f), sy(36f), sx(52f), sy(18f), sx(50f), sy(14f)) }
                    drawPath(stem1, leafDark, style = thinStroke)
                    drawPath(stem2, leafDark, style = thinStroke)
                }
                else -> {
                    // Single round plum
                    drawCircle(color, radius = 28f * scale, center = Offset(sx(50f), sy(54f)))
                    drawCircle(ink, radius = 28f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
                    val suture = Path().apply {
                        moveTo(sx(50f), sy(26f)); quadraticBezierTo(sx(42f), sy(54f), sx(50f), sy(82f))
                    }
                    drawPath(suture, ink.copy(alpha = 0.35f), style = thinStroke)
                    val stem = Path().apply { moveTo(sx(50f), sy(26f)); lineTo(sx(50f), sy(16f)) }
                    drawPath(stem, leafDark, style = lineStroke)
                }
            }
        }
        "citrus" -> {
            when (v) {
                0 -> {
                    // Round orange / citrus
                    drawCircle(color, radius = 30f * scale, center = Offset(sx(50f), sy(54f)))
                    drawCircle(ink, radius = 30f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
                    val leafP = Path().apply {
                        moveTo(sx(50f), sy(24f))
                        cubicTo(sx(62f), sy(14f), sx(74f), sy(20f), sx(70f), sy(28f))
                        cubicTo(sx(60f), sy(30f), sx(52f), sy(26f), sx(50f), sy(24f))
                        close()
                    }
                    drawPath(leafP, leaf, style = Fill)
                    drawPath(leafP, ink, style = thinStroke)
                }
                1 -> {
                    // Pointed lemon / lime oval
                    val lemon = Path().apply {
                        moveTo(sx(20f), sy(50f))
                        cubicTo(sx(26f), sy(26f), sx(74f), sy(26f), sx(80f), sy(50f))
                        cubicTo(sx(74f), sy(74f), sx(26f), sy(74f), sx(20f), sy(50f))
                        close()
                    }
                    drawPath(lemon, color, style = Fill)
                    drawPath(lemon, ink, style = lineStroke)
                }
                else -> {
                    // Sliced citrus wheel
                    drawCircle(color, radius = 30f * scale, center = Offset(sx(50f), sy(54f)))
                    drawCircle(ink, radius = 30f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
                    drawCircle(Color.White.copy(alpha = 0.8f), radius = 25f * scale, center = Offset(sx(50f), sy(54f)), style = Stroke(width = 3.5f * scale))
                    drawCircle(ink, radius = 25f * scale, center = Offset(sx(50f), sy(54f)), style = thinStroke)
                    for (i in 0..5) {
                        val ang = i * 60.0 * PI / 180.0
                        val ray = Path().apply {
                            moveTo(sx(50f), sy(54f))
                            lineTo(sx(50f + cos(ang).toFloat() * 25f), sy(54f + sin(ang).toFloat() * 25f))
                        }
                        drawPath(ray, ink, style = thinStroke)
                    }
                }
            }
        }
        "leafy" -> {
            when (v) {
                0 -> {
                    // Layered leaves
                    val stem = Path().apply { moveTo(sx(50f), sy(90f)); lineTo(sx(50f), sy(36f)) }
                    drawPath(stem, leafDark, style = lineStroke)
                    val leaf1 = Path().apply {
                        moveTo(sx(50f), sy(60f))
                        cubicTo(sx(32f), sy(56f), sx(22f), sy(38f), sx(24f), sy(20f))
                        cubicTo(sx(42f), sy(24f), sx(52f), sy(38f), sx(50f), sy(60f))
                        close()
                    }
                    drawPath(leaf1, color, style = Fill)
                    drawPath(leaf1, ink, style = thinStroke)
                    val leaf2 = Path().apply {
                        moveTo(sx(50f), sy(50f))
                        cubicTo(sx(68f), sy(46f), sx(78f), sy(28f), sx(76f), sy(10f))
                        cubicTo(sx(58f), sy(14f), sx(48f), sy(28f), sx(50f), sy(50f))
                        close()
                    }
                    drawPath(leaf2, colorSecondary, style = Fill)
                    drawPath(leaf2, ink, style = thinStroke)
                }
                1 -> {
                    // Head of lettuce/cabbage
                    val p = generateBlobPath(rng, 50f, 54f, 28f, 26f, scale)
                    drawPath(p, color, style = Fill)
                    drawPath(p, ink, style = lineStroke)
                    val leafLine1 = Path().apply {
                        moveTo(sx(30f), sy(34f)); quadraticBezierTo(sx(50f), sy(48f), sx(70f), sy(34f))
                    }
                    val leafLine2 = Path().apply {
                        moveTo(sx(26f), sy(56f)); quadraticBezierTo(sx(50f), sy(72f), sx(74f), sy(56f))
                    }
                    drawPath(leafLine1, leafDark, style = thinStroke)
                    drawPath(leafLine2, leafDark, style = thinStroke)
                }
                else -> {
                    // Broad ribbed leaf
                    val stem = Path().apply { moveTo(sx(50f), sy(92f)); lineTo(sx(50f), sy(14f)) }
                    drawPath(stem, leafDark, style = lineStroke)
                    val blade = Path().apply {
                        moveTo(sx(50f), sy(14f))
                        cubicTo(sx(26f), sy(24f), sx(18f), sy(50f), sx(32f), sy(76f))
                        cubicTo(sx(42f), sy(82f), sx(48f), sy(86f), sx(50f), sy(88f))
                        cubicTo(sx(52f), sy(86f), sx(58f), sy(82f), sx(68f), sy(76f))
                        cubicTo(sx(82f), sy(50f), sx(74f), sy(24f), sx(50f), sy(14f))
                        close()
                    }
                    drawPath(blade, color, style = Fill)
                    drawPath(blade, ink, style = lineStroke)
                }
            }
        }
        "crucifer" -> {
            when (v) {
                0 -> {
                    // Broccoli / Cauliflower floret cluster
                    val stalk = Path().apply {
                        moveTo(sx(44f), sy(60f)); lineTo(sx(40f), sy(90f))
                        lineTo(sx(60f), sy(90f)); lineTo(sx(56f), sy(60f))
                        close()
                    }
                    drawPath(stalk, Color(0xFFC8E6C9), style = Fill)
                    drawPath(stalk, ink, style = lineStroke)
                    listOf(
                        Pair(50f, 32f), Pair(36f, 44f), Pair(64f, 44f),
                        Pair(42f, 54f), Pair(58f, 54f)
                    ).forEach { (cx, cy) ->
                        drawCircle(color, radius = 13f * scale, center = Offset(sx(cx), sy(cy)))
                        drawCircle(ink, radius = 13f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
                    }
                }
                else -> {
                    // Cabbage head
                    drawCircle(color, radius = 30f * scale, center = Offset(sx(50f), sy(54f)))
                    drawCircle(ink, radius = 30f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
                    drawCircle(leafDark.copy(alpha = 0.4f), radius = 20f * scale, center = Offset(sx(50f), sy(54f)), style = thinStroke)
                }
            }
        }
        "nightshade" -> {
            when (v) {
                0 -> {
                    // Tomato / round nightshade
                    drawCircle(color, radius = 28f * scale, center = Offset(sx(50f), sy(56f)))
                    drawCircle(ink, radius = 28f * scale, center = Offset(sx(50f), sy(56f)), style = lineStroke)
                    val star = Path().apply {
                        moveTo(sx(50f), sy(28f)); lineTo(sx(44f), sy(22f)); lineTo(sx(50f), sy(20f))
                        lineTo(sx(56f), sy(22f)); lineTo(sx(50f), sy(28f))
                    }
                    drawPath(star, leaf, style = Fill)
                    drawPath(star, ink, style = thinStroke)
                }
                1 -> {
                    // Pepper / elongated nightshade
                    val pep = Path().apply {
                        moveTo(sx(50f), sy(24f))
                        cubicTo(sx(68f), sy(26f), sx(74f), sy(46f), sx(70f), sy(68f))
                        cubicTo(sx(66f), sy(84f), sx(56f), sy(92f), sx(50f), sy(92f))
                        cubicTo(sx(44f), sy(92f), sx(34f), sy(84f), sx(30f), sy(68f))
                        cubicTo(sx(26f), sy(46f), sx(32f), sy(26f), sx(50f), sy(24f))
                        close()
                    }
                    drawPath(pep, color, style = Fill)
                    drawPath(pep, ink, style = lineStroke)
                    val stem = Path().apply { moveTo(sx(50f), sy(24f)); quadraticBezierTo(sx(54f), sy(14f), sx(48f), sy(8f)) }
                    drawPath(stem, leaf, style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
                }
                else -> {
                    // Eggplant / pear shape
                    val egg = Path().apply {
                        moveTo(sx(50f), sy(26f))
                        cubicTo(sx(64f), sy(28f), sx(72f), sy(46f), sx(76f), sy(66f))
                        cubicTo(sx(80f), sy(86f), sx(66f), sy(94f), sx(50f), sy(94f))
                        cubicTo(sx(34f), sy(94f), sx(20f), sy(86f), sx(24f), sy(66f))
                        cubicTo(sx(28f), sy(46f), sx(36f), sy(28f), sx(50f), sy(26f))
                        close()
                    }
                    drawPath(egg, color, style = Fill)
                    drawPath(egg, ink, style = lineStroke)
                    val calyx = Path().apply {
                        moveTo(sx(50f), sy(26f)); lineTo(sx(38f), sy(34f)); lineTo(sx(44f), sy(26f))
                        lineTo(sx(56f), sy(26f)); lineTo(sx(62f), sy(34f)); lineTo(sx(50f), sy(26f))
                    }
                    drawPath(calyx, leaf, style = Fill)
                    drawPath(calyx, ink, style = thinStroke)
                }
            }
        }
        "gourd", "melon" -> {
            val rx = 28f
            val ry = 24f
            drawOval(color, topLeft = Offset(sx(50f - rx), sy(54f - ry)), size = Size(rx * 2 * scale, ry * 2 * scale))
            drawOval(ink, topLeft = Offset(sx(50f - rx), sy(54f - ry)), size = Size(rx * 2 * scale, ry * 2 * scale), style = lineStroke)
            val stripes = Path().apply {
                moveTo(sx(36f), sy(32f)); quadraticBezierTo(sx(38f), sy(54f), sx(36f), sy(76f))
                moveTo(sx(50f), sy(28f)); lineTo(sx(50f), sy(80f))
                moveTo(sx(64f), sy(32f)); quadraticBezierTo(sx(62f), sy(54f), sx(64f), sy(76f))
            }
            drawPath(stripes, ink.copy(alpha = 0.4f), style = thinStroke)
            val stem = Path().apply {
                moveTo(sx(50f), sy(28f)); lineTo(sx(48f), sy(14f))
            }
            drawPath(stem, leafDark, style = lineStroke)
        }
        "pod" -> {
            val pod = Path().apply {
                moveTo(sx(14f), sy(50f))
                cubicTo(sx(14f), sy(34f), sx(26f), sy(24f), sx(50f), sy(24f))
                cubicTo(sx(74f), sy(24f), sx(86f), sy(34f), sx(86f), sy(50f))
                cubicTo(sx(86f), sy(66f), sx(74f), sy(76f), sx(50f), sy(76f))
                cubicTo(sx(26f), sy(76f), sx(14f), sy(66f), sx(14f), sy(50f))
                close()
            }
            drawPath(pod, color, style = Fill)
            drawPath(pod, ink, style = lineStroke)
            listOf(28f, 44f, 60f, 74f).forEach { cx ->
                drawCircle(colorSecondary, radius = 7f * scale, center = Offset(sx(cx), sy(50f)))
                drawCircle(ink, radius = 7f * scale, center = Offset(sx(cx), sy(50f)), style = thinStroke)
            }
        }
        "mushroom" -> {
            val stem = Path().apply {
                moveTo(sx(40f), sy(52f)); lineTo(sx(38f), sy(88f))
                lineTo(sx(62f), sy(88f)); lineTo(sx(60f), sy(52f))
            }
            drawPath(stem, Color(0xFFF4ECD8), style = Fill)
            drawPath(stem, ink, style = lineStroke)
            val cap = Path().apply {
                moveTo(sx(22f), sy(46f))
                cubicTo(sx(22f), sy(26f), sx(36f), sy(16f), sx(50f), sy(16f))
                cubicTo(sx(64f), sy(16f), sx(78f), sy(26f), sx(78f), sy(46f))
                close()
            }
            drawPath(cap, color, style = Fill)
            drawPath(cap, ink, style = lineStroke)
        }
        "herb", "stem" -> {
            val stem = Path().apply {
                moveTo(sx(50f), sy(92f)); lineTo(sx(50f), sy(24f))
            }
            drawPath(stem, leafDark, style = lineStroke)
            listOf(34f, 52f, 70f).forEach { y ->
                val left = Path().apply {
                    moveTo(sx(50f), sy(y))
                    quadraticBezierTo(sx(32f), sy(y - 6f), sx(30f), sy(y + 8f))
                    quadraticBezierTo(sx(44f), sy(y + 10f), sx(50f), sy(y))
                    close()
                }
                drawPath(left, color, style = Fill)
                drawPath(left, ink, style = thinStroke)
                val right = Path().apply {
                    moveTo(sx(50f), sy(y))
                    quadraticBezierTo(sx(68f), sy(y - 6f), sx(70f), sy(y + 8f))
                    quadraticBezierTo(sx(56f), sy(y + 10f), sx(50f), sy(y))
                    close()
                }
                drawPath(right, color, style = Fill)
                drawPath(right, ink, style = thinStroke)
            }
        }
        else -> {
            val p = generateBlobPath(rng, 50f, 54f, 26f, 28f, scale)
            drawPath(p, color, style = Fill)
            drawPath(p, ink, style = lineStroke)
            val stem = Path().apply {
                moveTo(sx(50f), sy(26f))
                quadraticBezierTo(sx(56f), sy(16f), sx(62f), sy(14f))
            }
            drawPath(stem, leaf, style = lineStroke)
        }
    }
}

private fun generateBlobPath(
    rng: Mulberry32,
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    scale: Float,
    pointsCount: Int = 8
): Path {
    val pts = mutableListOf<Pair<Float, Float>>()
    for (i in 0 until pointsCount) {
        val ang = (i.toFloat() / pointsCount) * (PI.toFloat() * 2f)
        val jitter = 1f + (rng.next() - 0.5f) * 0.3f
        val px = cx + cos(ang) * rx * jitter
        val py = cy + sin(ang) * ry * jitter
        pts.add(Pair(px * scale, py * scale))
    }

    val path = Path()
    val last = pts.last()
    val first = pts.first()
    val midStart = Pair((last.first + first.first) / 2f, (last.second + first.second) / 2f)
    path.moveTo(midStart.first, midStart.second)

    for (i in 0 until pointsCount) {
        val cur = pts[i]
        val nxt = pts[(i + 1) % pointsCount]
        val mid = Pair((cur.first + nxt.first) / 2f, (cur.second + nxt.second) / 2f)
        path.quadraticBezierTo(cur.first, cur.second, mid.first, mid.second)
    }
    path.close()
    return path
}

// 61 Bespoke Signature Doodles with enhanced botanical details & vibrant colors
private fun DrawScope.drawBespokeSymbol(id: String, scale: Float, palette: BotanicalPalette) {
    val lineStroke = Stroke(width = 3.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 2.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ink = SproutInk
    val leaf = palette.leaf
    val leafDark = palette.leafDark

    fun sx(x: Float) = x * scale
    fun sy(y: Float) = y * scale

    when (id) {
        "d-sprout" -> drawDetailedSprout(scale)
        "d-apple" -> drawDetailedApple(scale, palette)
        "d-carrot" -> drawDetailedCarrot(scale, palette)
        "d-strawberry" -> drawDetailedStrawberry(scale, palette)
        "d-avocado" -> drawDetailedAvocado(scale, palette)
        "d-broccoli" -> drawDetailedBroccoli(scale, palette)
        "d-watermelon" -> drawDetailedWatermelon(scale, palette)
        "d-kiwi" -> drawDetailedKiwi(scale, palette)
        "d-dragonfruit" -> drawDetailedDragonfruit(scale, palette)
        "d-pineapple" -> drawDetailedPineapple(scale, palette)
        "d-blueberry" -> drawDetailedBlueberry(scale, palette)
        "d-grape" -> drawDetailedGrape(scale, palette)
        "d-onion" -> drawDetailedOnion(scale, palette)
        "d-garlic" -> drawDetailedGarlic(scale, palette)
        "d-pepper" -> drawDetailedPepper(scale, palette)
        "d-mushroom" -> drawDetailedMushroom(scale, palette)
        "d-lemon" -> drawDetailedLemon(scale, palette)
        "d-tomato" -> drawDetailedTomato(scale, palette)
        "d-banana" -> drawDetailedBanana(scale, palette)
        "d-orange" -> drawDetailedOrange(scale, palette)
        "d-peach" -> drawDetailedPeach(scale, palette)
        "d-cherry" -> drawDetailedCherry(scale, palette)
        "d-potato" -> drawDetailedPotato(scale, palette)
        "d-pumpkin" -> drawDetailedPumpkin(scale, palette)
        "d-eggplant" -> drawDetailedEggplant(scale, palette)
        "d-pea" -> drawDetailedPea(scale, palette)
        "d-mango" -> {
            val mango = Path().apply {
                moveTo(sx(50f), sy(14f))
                cubicTo(sx(70f), sy(14f), sx(84f), sy(30f), sx(82f), sy(52f))
                cubicTo(sx(80f), sy(76f), sx(60f), sy(92f), sx(46f), sy(90f))
                cubicTo(sx(30f), sy(88f), sx(16f), sy(68f), sx(18f), sy(46f))
                cubicTo(sx(20f), sy(24f), sx(34f), sy(14f), sx(50f), sy(14f))
                close()
            }
            drawPath(mango, palette.primary, style = Fill)
            val blush = Path().apply {
                moveTo(sx(56f), sy(18f))
                cubicTo(sx(74f), sy(24f), sx(82f), sy(42f), sx(78f), sy(60f))
                cubicTo(sx(74f), sy(78f), sx(58f), sy(88f), sx(46f), sy(86f))
            }
            drawPath(blush, palette.accent.copy(alpha = 0.65f), style = Fill)
            drawPath(mango, ink, style = lineStroke)
            val stem = Path().apply {
                moveTo(sx(46f), sy(14f)); quadraticBezierTo(sx(44f), sy(8f), sx(36f), sy(8f))
            }
            drawPath(stem, Color(0xFF5D4037), style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))
        }
        "d-papaya" -> {
            val papaya = Path().apply {
                moveTo(sx(50f), sy(12f))
                cubicTo(sx(40f), sy(12f), sx(32f), sy(22f), sx(32f), sy(34f))
                cubicTo(sx(32f), sy(46f), sx(24f), sy(52f), sx(22f), sy(64f))
                cubicTo(sx(20f), sy(80f), sx(34f), sy(94f), sx(50f), sy(94f))
                cubicTo(sx(66f), sy(94f), sx(80f), sy(80f), sx(78f), sy(64f))
                cubicTo(sx(76f), sy(52f), sx(68f), sy(46f), sx(68f), sy(34f))
                cubicTo(sx(68f), sy(22f), sx(60f), sy(12f), sx(50f), sy(12f))
                close()
            }
            drawPath(papaya, palette.primary, style = Fill)
            drawPath(papaya, ink, style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(12f)); lineTo(sx(50f), sy(4f)) }
            drawPath(stem, SproutLeafDark, style = Stroke(width = 4f * scale, cap = StrokeCap.Round))
        }
        "d-guava" -> {
            val guava = Path().apply {
                moveTo(sx(50f), sy(16f))
                cubicTo(sx(70f), sy(16f), sx(82f), sy(32f), sx(82f), sy(54f))
                cubicTo(sx(82f), sy(74f), sx(68f), sy(92f), sx(50f), sy(92f))
                cubicTo(sx(32f), sy(92f), sx(18f), sy(74f), sx(18f), sy(54f))
                cubicTo(sx(18f), sy(32f), sx(30f), sy(16f), sx(50f), sy(16f))
                close()
            }
            drawPath(guava, palette.primary, style = Fill)
            drawPath(guava, ink, style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(16f)); lineTo(sx(50f), sy(6f)) }
            drawPath(stem, SproutLeafDark, style = thinStroke)
            drawCircle(palette.accent, radius = 1.6f * scale, center = Offset(sx(42f), sy(46f)))
            drawCircle(palette.accent, radius = 1.6f * scale, center = Offset(sx(58f), sy(42f)))
        }
        "d-passionfruit" -> {
            drawCircle(palette.primary, radius = 34f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 34f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            val stem = Path().apply {
                moveTo(sx(50f), sy(20f)); quadraticBezierTo(sx(48f), sy(10f), sx(42f), sy(6f))
            }
            drawPath(stem, Color(0xFF2E7D32), style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round))
        }
        "d-jackfruit" -> {
            val jack = Path().apply {
                moveTo(sx(50f), sy(10f))
                cubicTo(sx(70f), sy(10f), sx(84f), sy(26f), sx(84f), sy(50f))
                cubicTo(sx(84f), sy(76f), sx(70f), sy(94f), sx(50f), sy(94f))
                cubicTo(sx(30f), sy(94f), sx(16f), sy(76f), sx(16f), sy(50f))
                cubicTo(sx(16f), sy(26f), sx(30f), sy(10f), sx(50f), sy(10f))
                close()
            }
            drawPath(jack, palette.primary, style = Fill)
            drawPath(jack, ink, style = lineStroke)
            listOf(
                Pair(36f, 30f), Pair(50f, 24f), Pair(64f, 30f),
                Pair(28f, 46f), Pair(44f, 42f), Pair(58f, 42f), Pair(72f, 46f),
                Pair(34f, 62f), Pair(50f, 58f), Pair(66f, 62f)
            ).forEach { (cx, cy) ->
                drawCircle(Color(0xFF33691E), radius = 2.2f * scale, center = Offset(sx(cx), sy(cy)))
            }
        }
        "d-durian" -> {
            drawCircle(palette.primary, radius = 30f * scale, center = Offset(sx(50f), sy(52f)))
            drawCircle(ink, radius = 30f * scale, center = Offset(sx(50f), sy(52f)), style = lineStroke)
            val spikes = listOf(
                Path().apply { moveTo(sx(50f), sy(22f)); lineTo(sx(46f), sy(12f)); lineTo(sx(54f), sy(12f)); close() },
                Path().apply { moveTo(sx(68f), sy(26f)); lineTo(sx(74f), sy(18f)); lineTo(sx(78f), sy(26f)); close() },
                Path().apply { moveTo(sx(80f), sy(42f)); lineTo(sx(90f), sy(38f)); lineTo(sx(88f), sy(46f)); close() },
                Path().apply { moveTo(sx(80f), sy(62f)); lineTo(sx(90f), sy(66f)); lineTo(sx(88f), sy(58f)); close() },
                Path().apply { moveTo(sx(68f), sy(78f)); lineTo(sx(74f), sy(86f)); lineTo(sx(78f), sy(78f)); close() },
                Path().apply { moveTo(sx(50f), sy(82f)); lineTo(sx(46f), sy(92f)); lineTo(sx(54f), sy(92f)); close() },
                Path().apply { moveTo(sx(32f), sy(78f)); lineTo(sx(26f), sy(86f)); lineTo(sx(22f), sy(78f)); close() },
                Path().apply { moveTo(sx(20f), sy(62f)); lineTo(sx(10f), sy(66f)); lineTo(sx(12f), sy(58f)); close() }
            )
            spikes.forEach { spike ->
                drawPath(spike, Color(0xFF9E9D24), style = Fill)
                drawPath(spike, ink, style = thinStroke)
            }
        }
        "d-rambutan" -> {
            drawCircle(palette.primary, radius = 26f * scale, center = Offset(sx(50f), sy(52f)))
            drawCircle(ink, radius = 26f * scale, center = Offset(sx(50f), sy(52f)), style = lineStroke)
            val hairs = Path().apply {
                moveTo(sx(50f), sy(26f)); lineTo(sx(50f), sy(14f))
                moveTo(sx(64f), sy(30f)); lineTo(sx(72f), sy(20f))
                moveTo(sx(74f), sy(44f)); lineTo(sx(86f), sy(38f))
                moveTo(sx(76f), sy(58f)); lineTo(sx(88f), sy(60f))
                moveTo(sx(68f), sy(72f)); lineTo(sx(76f), sy(82f))
                moveTo(sx(54f), sy(78f)); lineTo(sx(56f), sy(90f))
                moveTo(sx(38f), sy(78f)); lineTo(sx(34f), sy(90f))
                moveTo(sx(26f), sy(70f)); lineTo(sx(16f), sy(78f))
            }
            drawPath(hairs, palette.accent, style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round))
        }
        "d-mangosteen" -> {
            drawCircle(palette.primary, radius = 30f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 30f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            val calyx = Path().apply {
                moveTo(sx(50f), sy(24f))
                cubicTo(sx(46f), sy(20f), sx(42f), sy(20f), sx(40f), sy(24f))
                cubicTo(sx(36f), sy(20f), sx(32f), sy(22f), sx(34f), sy(28f))
                cubicTo(sx(28f), sy(26f), sx(26f), sy(30f), sx(30f), sy(34f))
                close()
            }
            drawPath(calyx, SproutLeaf, style = Fill)
            drawPath(calyx, ink, style = thinStroke)
        }
        "d-starfruit" -> {
            val star = Path().apply {
                moveTo(sx(50f), sy(8f))
                lineTo(sx(60f), sy(38f)); lineTo(sx(92f), sy(38f))
                lineTo(sx(66f), sy(56f)); lineTo(sx(76f), sy(88f))
                lineTo(sx(50f), sy(68f)); lineTo(sx(24f), sy(88f))
                lineTo(sx(34f), sy(56f)); lineTo(sx(8f), sy(38f))
                lineTo(sx(40f), sy(38f))
                close()
            }
            drawPath(star, palette.primary, style = Fill)
            drawPath(star, ink, style = lineStroke)
        }
        "d-coconut" -> {
            drawCircle(palette.primary, radius = 32f * scale, center = Offset(sx(50f), sy(52f)))
            drawCircle(ink, radius = 32f * scale, center = Offset(sx(50f), sy(52f)), style = lineStroke)
            drawCircle(palette.accent, radius = 2.8f * scale, center = Offset(sx(44f), sy(50f)))
            drawCircle(palette.accent, radius = 2.8f * scale, center = Offset(sx(56f), sy(50f)))
            drawCircle(palette.accent, radius = 2.8f * scale, center = Offset(sx(50f), sy(60f)))
        }
        "d-raspberry" -> {
            val stem = Path().apply {
                moveTo(sx(50f), sy(20f))
                cubicTo(sx(44f), sy(12f), sx(34f), sy(12f), sx(32f), sy(20f))
            }
            drawPath(stem, leaf, style = thinStroke)
            listOf(
                Pair(50f, 34f), Pair(38f, 42f), Pair(62f, 42f),
                Pair(44f, 56f), Pair(56f, 56f), Pair(50f, 70f)
            ).forEach { (cx, cy) ->
                drawCircle(palette.primary, radius = 9f * scale, center = Offset(sx(cx), sy(cy)))
                drawCircle(ink, radius = 9f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
                drawCircle(Color.White.copy(alpha = 0.5f), radius = 2f * scale, center = Offset(sx(cx - 2f), sy(cy - 2f)))
            }
        }
        "d-blackberry" -> {
            val stem = Path().apply {
                moveTo(sx(50f), sy(20f))
                cubicTo(sx(44f), sy(12f), sx(34f), sy(12f), sx(32f), sy(20f))
            }
            drawPath(stem, leaf, style = thinStroke)
            listOf(
                Pair(50f, 34f), Pair(38f, 42f), Pair(62f, 42f),
                Pair(44f, 56f), Pair(56f, 56f), Pair(50f, 70f)
            ).forEach { (cx, cy) ->
                drawCircle(palette.primary, radius = 9f * scale, center = Offset(sx(cx), sy(cy)))
                drawCircle(ink, radius = 9f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 2f * scale, center = Offset(sx(cx - 2f), sy(cy - 2f)))
            }
        }
        "d-cranberry" -> {
            drawCircle(palette.primary, radius = 26f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 26f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            drawCircle(ink, radius = 2.4f * scale, center = Offset(sx(50f), sy(26f)))
        }
        "d-sweetpotato" -> {
            val sp = Path().apply {
                moveTo(sx(24f), sy(50f))
                cubicTo(sx(20f), sy(36f), sx(34f), sy(24f), sx(50f), sy(26f))
                cubicTo(sx(68f), sy(24f), sx(82f), sy(34f), sx(80f), sy(50f))
                cubicTo(sx(84f), sy(62f), sx(74f), sy(76f), sx(58f), sy(74f))
                cubicTo(sx(48f), sy(82f), sx(32f), sy(78f), sx(28f), sy(66f))
                close()
            }
            drawPath(sp, palette.primary, style = Fill)
            drawPath(sp, ink, style = lineStroke)
        }
        "d-beet" -> {
            val beet = Path().apply {
                moveTo(sx(50f), sy(30f))
                cubicTo(sx(66f), sy(30f), sx(76f), sy(42f), sx(74f), sy(56f))
                cubicTo(sx(72f), sy(72f), sx(62f), sy(82f), sx(50f), sy(82f))
                cubicTo(sx(38f), sy(82f), sx(28f), sy(72f), sx(26f), sy(56f))
                close()
            }
            drawPath(beet, palette.primary, style = Fill)
            drawPath(beet, ink, style = lineStroke)
            val tail = Path().apply { moveTo(sx(50f), sy(82f)); lineTo(sx(48f), sy(94f)) }
            drawPath(tail, ink, style = lineStroke)
            val leaves = Path().apply {
                moveTo(sx(44f), sy(30f)); lineTo(sx(38f), sy(12f))
                moveTo(sx(50f), sy(30f)); lineTo(sx(50f), sy(8f))
                moveTo(sx(56f), sy(30f)); lineTo(sx(62f), sy(12f))
            }
            drawPath(leaves, palette.leafDark, style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
        }
        "d-radish" -> {
            val radish = Path().apply {
                moveTo(sx(50f), sy(34f))
                cubicTo(sx(64f), sy(34f), sx(72f), sy(46f), sx(68f), sy(60f))
                cubicTo(sx(64f), sy(76f), sx(58f), sy(82f), sx(50f), sy(82f))
                cubicTo(sx(42f), sy(82f), sx(36f), sy(76f), sx(32f), sy(60f))
                close()
            }
            drawPath(radish, palette.primary, style = Fill)
            drawPath(radish, ink, style = lineStroke)
            val leaves = Path().apply {
                moveTo(sx(42f), sy(34f)); lineTo(sx(34f), sy(14f))
                moveTo(sx(50f), sy(34f)); lineTo(sx(50f), sy(10f))
                moveTo(sx(58f), sy(34f)); lineTo(sx(66f), sy(14f))
            }
            drawPath(leaves, palette.leaf, style = Stroke(width = 3.4f * scale, cap = StrokeCap.Round))
        }
        "d-plum" -> {
            val plum = Path().apply {
                moveTo(sx(50f), sy(24f))
                cubicTo(sx(64f), sy(24f), sx(74f), sy(38f), sx(74f), sy(56f))
                cubicTo(sx(74f), sy(74f), sx(62f), sy(88f), sx(50f), sy(88f))
                cubicTo(sx(38f), sy(88f), sx(26f), sy(74f), sx(26f), sy(56f))
                close()
            }
            drawPath(plum, palette.primary, style = Fill)
            drawPath(plum, ink, style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(24f)); lineTo(sx(50f), sy(14f)) }
            drawPath(stem, leaf, style = lineStroke)
        }
        "d-apricot" -> {
            val apricot = Path().apply {
                moveTo(sx(50f), sy(26f))
                cubicTo(sx(64f), sy(26f), sx(72f), sy(40f), sx(72f), sy(56f))
                cubicTo(sx(72f), sy(72f), sx(62f), sy(86f), sx(50f), sy(86f))
                cubicTo(sx(38f), sy(86f), sx(28f), sy(72f), sx(28f), sy(56f))
                close()
            }
            drawPath(apricot, palette.primary, style = Fill)
            drawPath(apricot, ink, style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(26f)); lineTo(sx(50f), sy(16f)) }
            drawPath(stem, leaf, style = lineStroke)
        }
        "d-pomegranate" -> {
            val pom = Path().apply {
                moveTo(sx(50f), sy(28f))
                cubicTo(sx(66f), sy(28f), sx(78f), sy(42f), sx(78f), sy(58f))
                cubicTo(sx(78f), sy(76f), sx(64f), sy(90f), sx(50f), sy(90f))
                cubicTo(sx(36f), sy(90f), sx(22f), sy(76f), sx(22f), sy(58f))
                close()
            }
            drawPath(pom, palette.primary, style = Fill)
            drawPath(pom, ink, style = lineStroke)
            val crown = Path().apply {
                moveTo(sx(42f), sy(28f)); lineTo(sx(38f), sy(16f))
                lineTo(sx(44f), sy(20f)); lineTo(sx(50f), sy(12f))
                lineTo(sx(56f), sy(20f)); lineTo(sx(62f), sy(16f)); lineTo(sx(58f), sy(28f))
            }
            drawPath(crown, ink, style = thinStroke)
        }
        "d-fig" -> {
            val fig = Path().apply {
                moveTo(sx(50f), sy(16f))
                cubicTo(sx(46f), sy(16f), sx(44f), sy(22f), sx(46f), sy(28f))
                cubicTo(sx(30f), sy(32f), sx(22f), sy(46f), sx(24f), sy(62f))
                cubicTo(sx(26f), sy(80f), sx(38f), sy(90f), sx(50f), sy(90f))
                cubicTo(sx(62f), sy(90f), sx(74f), sy(80f), sx(76f), sy(62f))
                close()
            }
            drawPath(fig, palette.primary, style = Fill)
            drawPath(fig, ink, style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(16f)); lineTo(sx(50f), sy(8f)) }
            drawPath(stem, leaf, style = lineStroke)
        }
        "d-persimmon" -> {
            drawOval(palette.primary, topLeft = Offset(sx(20f), sy(32f)), size = Size(60f * scale, 52f * scale))
            drawOval(ink, topLeft = Offset(sx(20f), sy(32f)), size = Size(60f * scale, 52f * scale), style = lineStroke)
            val calyx = Path().apply {
                moveTo(sx(50f), sy(30f)); lineTo(sx(40f), sy(18f)); lineTo(sx(50f), sy(20f)); lineTo(sx(60f), sy(18f)); close()
            }
            drawPath(calyx, leaf, style = Fill)
            drawPath(calyx, ink, style = thinStroke)
        }
        "d-spinach" -> {
            val stem = Path().apply { moveTo(sx(50f), sy(90f)); lineTo(sx(50f), sy(50f)) }
            drawPath(stem, leafDark, style = lineStroke)
            val left = Path().apply {
                moveTo(sx(50f), sy(60f))
                cubicTo(sx(34f), sy(56f), sx(24f), sy(40f), sx(26f), sy(22f))
                cubicTo(sx(42f), sy(26f), sx(52f), sy(40f), sx(50f), sy(60f))
                close()
            }
            drawPath(left, palette.primary, style = Fill)
            drawPath(left, ink, style = thinStroke)
            val right = Path().apply {
                moveTo(sx(50f), sy(55f))
                cubicTo(sx(66f), sy(50f), sx(76f), sy(34f), sx(74f), sy(18f))
                cubicTo(sx(58f), sy(22f), sx(48f), sy(36f), sx(50f), sy(55f))
                close()
            }
            drawPath(right, palette.primary, style = Fill)
            drawPath(right, ink, style = thinStroke)
        }
        "d-kale" -> {
            val stem = Path().apply { moveTo(sx(50f), sy(90f)); lineTo(sx(50f), sy(46f)) }
            drawPath(stem, leafDark, style = Stroke(width = 3.4f * scale, cap = StrokeCap.Round))
            val body = Path().apply {
                moveTo(sx(50f), sy(50f))
                cubicTo(sx(30f), sy(48f), sx(16f), sy(32f), sx(18f), sy(12f))
                cubicTo(sx(44f), sy(22f), sx(46f), sy(30f), sx(50f), sy(38f))
                cubicTo(sx(54f), sy(30f), sx(56f), sy(22f), sx(82f), sy(12f))
                cubicTo(sx(84f), sy(32f), sx(70f), sy(48f), sx(50f), sy(50f))
                close()
            }
            drawPath(body, palette.primary, style = Fill)
            drawPath(body, ink, style = thinStroke)
        }
        "d-lettuce" -> {
            val body = Path().apply {
                moveTo(sx(50f), sy(20f))
                cubicTo(sx(70f), sy(20f), sx(84f), sy(36f), sx(82f), sy(54f))
                cubicTo(sx(88f), sy(60f), sx(84f), sy(72f), sx(74f), sy(74f))
                cubicTo(sx(74f), sy(84f), sx(62f), sy(90f), sx(50f), sy(88f))
                cubicTo(sx(38f), sy(90f), sx(26f), sy(84f), sx(26f), sy(74f))
                close()
            }
            drawPath(body, palette.primary, style = Fill)
            drawPath(body, ink, style = lineStroke)
        }
        "d-cabbage" -> {
            drawCircle(palette.primary, radius = 32f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 32f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            drawCircle(palette.accent.copy(alpha = 0.4f), radius = 22f * scale, center = Offset(sx(50f), sy(54f)), style = thinStroke)
            drawCircle(palette.accent.copy(alpha = 0.4f), radius = 12f * scale, center = Offset(sx(50f), sy(54f)), style = thinStroke)
        }
        "d-cauliflower" -> {
            listOf(
                Pair(50f, 50f), Pair(36f, 48f), Pair(64f, 48f),
                Pair(42f, 62f), Pair(58f, 62f), Pair(50f, 70f)
            ).forEach { (cx, cy) ->
                drawCircle(palette.primary, radius = 11f * scale, center = Offset(sx(cx), sy(cy)))
                drawCircle(ink, radius = 11f * scale, center = Offset(sx(cx), sy(cy)), style = thinStroke)
            }
            val leafCradle = Path().apply {
                moveTo(sx(30f), sy(42f)); cubicTo(sx(24f), sy(34f), sx(30f), sy(24f), sx(40f), sy(24f))
                moveTo(sx(70f), sy(42f)); cubicTo(sx(76f), sy(34f), sx(70f), sy(24f), sx(60f), sy(24f))
            }
            drawPath(leafCradle, leaf, style = lineStroke)
        }
        "d-grapefruit" -> {
            drawCircle(palette.primary, radius = 32f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 32f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(22f)); lineTo(sx(50f), sy(12f)) }
            drawPath(stem, leaf, style = thinStroke)
        }
        "d-lime" -> {
            drawCircle(palette.primary, radius = 26f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 26f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(28f)); lineTo(sx(50f), sy(18f)) }
            drawPath(stem, leafDark, style = thinStroke)
        }
        "d-zucchini" -> {
            val body = Path().apply {
                moveTo(sx(18f), sy(62f))
                cubicTo(sx(14f), sy(50f), sx(24f), sy(30f), sx(40f), sy(22f))
                cubicTo(sx(58f), sy(12f), sx(82f), sy(18f), sx(86f), sy(32f))
                cubicTo(sx(90f), sy(44f), sx(78f), sy(56f), sx(62f), sy(66f))
                close()
            }
            drawPath(body, palette.primary, style = Fill)
            drawPath(body, ink, style = lineStroke)
        }
        "d-cucumber" -> {
            val body = Path().apply {
                moveTo(sx(14f), sy(58f))
                cubicTo(sx(10f), sy(46f), sx(22f), sy(30f), sx(38f), sy(24f))
                cubicTo(sx(56f), sy(16f), sx(82f), sy(20f), sx(88f), sy(34f))
                close()
            }
            drawPath(body, palette.primary, style = Fill)
            drawPath(body, ink, style = lineStroke)
        }
        "d-greenbean" -> {
            val pod1 = Path().apply {
                moveTo(sx(20f), sy(30f))
                cubicTo(sx(16f), sy(44f), sx(20f), sy(62f), sx(32f), sy(78f))
                cubicTo(sx(40f), sy(88f), sx(52f), sy(88f), sx(54f), sy(78f))
                close()
            }
            drawPath(pod1, palette.primary, style = Fill)
            drawPath(pod1, ink, style = lineStroke)
            val pod2 = Path().apply {
                moveTo(sx(46f), sy(22f))
                cubicTo(sx(42f), sy(36f), sx(46f), sy(54f), sx(58f), sy(70f))
                cubicTo(sx(66f), sy(80f), sx(78f), sy(80f), sx(80f), sy(70f))
                close()
            }
            drawPath(pod2, palette.primary, style = Fill)
            drawPath(pod2, ink, style = lineStroke)
        }
        "d-pear" -> {
            val pear = Path().apply {
                moveTo(sx(50f), sy(16f))
                cubicTo(sx(54f), sy(20f), sx(54f), sy(26f), sx(50f), sy(30f))
                cubicTo(sx(64f), sy(34f), sx(74f), sy(48f), sx(72f), sy(64f))
                cubicTo(sx(70f), sy(82f), sx(58f), sy(92f), sx(50f), sy(92f))
                cubicTo(sx(42f), sy(92f), sx(30f), sy(82f), sx(28f), sy(64f))
                close()
            }
            drawPath(pear, palette.primary, style = Fill)
            drawPath(pear, ink, style = lineStroke)
            val stem = Path().apply { moveTo(sx(50f), sy(16f)); lineTo(sx(52f), sy(8f)) }
            drawPath(stem, leafDark, style = lineStroke)
        }
        "d-cantaloupe" -> {
            drawCircle(palette.primary, radius = 32f * scale, center = Offset(sx(50f), sy(54f)))
            drawCircle(ink, radius = 32f * scale, center = Offset(sx(50f), sy(54f)), style = lineStroke)
            val netting = Path().apply {
                moveTo(sx(22f), sy(44f)); quadraticBezierTo(sx(50f), sy(34f), sx(78f), sy(44f))
                moveTo(sx(20f), sy(58f)); quadraticBezierTo(sx(50f), sy(68f), sx(80f), sy(58f))
                moveTo(sx(50f), sy(22f)); lineTo(sx(50f), sy(86f))
            }
            drawPath(netting, ink.copy(alpha = 0.35f), style = thinStroke)
        }
        "d-celery" -> {
            val stalks = Path().apply {
                moveTo(sx(34f), sy(90f)); lineTo(sx(30f), sy(34f))
                moveTo(sx(50f), sy(90f)); lineTo(sx(50f), sy(30f))
                moveTo(sx(66f), sy(90f)); lineTo(sx(70f), sy(34f))
            }
            drawPath(stalks, palette.secondary, style = Stroke(width = 7f * scale, cap = StrokeCap.Round))
            val tops = Path().apply {
                moveTo(sx(30f), sy(34f)); cubicTo(sx(24f), sy(24f), sx(28f), sy(14f), sx(36f), sy(12f))
                moveTo(sx(50f), sy(30f)); cubicTo(sx(48f), sy(20f), sx(54f), sy(10f), sx(60f), sy(10f))
                moveTo(sx(70f), sy(34f)); cubicTo(sx(76f), sy(24f), sx(72f), sy(14f), sx(64f), sy(12f))
            }
            drawPath(tops, leaf, style = thinStroke)
        }
        "d-basil" -> {
            val stem = Path().apply { moveTo(sx(50f), sy(92f)); lineTo(sx(50f), sy(24f)) }
            drawPath(stem, leafDark, style = thinStroke)
            listOf(Pair(34f, 44f), Pair(52f, 62f)).forEach { (y1, y2) ->
                val left = Path().apply {
                    moveTo(sx(50f), sy(y1))
                    cubicTo(sx(40f), sy(y1 - 4f), sx(32f), sy(y1), sx(30f), sy(y2))
                    cubicTo(sx(40f), sy(y2 + 2f), sx(48f), sy(y1 + 8f), sx(50f), sy(y1))
                    close()
                }
                drawPath(left, palette.primary, style = Fill)
                drawPath(left, ink, style = thinStroke)
                val right = Path().apply {
                    moveTo(sx(50f), sy(y1))
                    cubicTo(sx(60f), sy(y1 - 4f), sx(68f), sy(y1), sx(70f), sy(y2))
                    cubicTo(sx(60f), sy(y2 + 2f), sx(52f), sy(y1 + 8f), sx(50f), sy(y1))
                    close()
                }
                drawPath(right, palette.primary, style = Fill)
                drawPath(right, ink, style = thinStroke)
            }
        }
        "d-nori" -> {
            val nori = Path().apply {
                moveTo(sx(20f), sy(20f))
                cubicTo(sx(30f), sy(30f), sx(20f), sy(42f), sx(30f), sy(52f))
                cubicTo(sx(40f), sy(62f), sx(26f), sy(72f), sx(36f), sy(84f))
                cubicTo(sx(46f), sy(94f), sx(60f), sy(90f), sx(66f), sy(78f))
                cubicTo(sx(74f), sy(64f), sx(62f), sy(56f), sx(70f), sy(44f))
                close()
            }
            drawPath(nori, palette.primary, style = Fill)
            drawPath(nori, ink, style = lineStroke)
        }
        "d-corn" -> {
            val ear = Path().apply {
                moveTo(sx(50f), sy(16f))
                cubicTo(sx(66f), sy(18f), sx(72f), sy(40f), sx(68f), sy(68f))
                cubicTo(sx(64f), sy(88f), sx(56f), sy(94f), sx(50f), sy(94f))
                cubicTo(sx(44f), sy(94f), sx(36f), sy(88f), sx(32f), sy(68f))
                cubicTo(sx(28f), sy(40f), sx(34f), sy(18f), sx(50f), sy(16f))
                close()
            }
            drawPath(ear, palette.primary, style = Fill)
            drawPath(ear, ink, style = lineStroke)
            // Corn kernel grid lines
            listOf(30f, 44f, 58f, 72f).forEach { y ->
                val line = Path().apply { moveTo(sx(36f), sy(y)); lineTo(sx(64f), sy(y)) }
                drawPath(line, palette.accent.copy(alpha = 0.5f), style = thinStroke)
            }
            // Green husks
            val huskL = Path().apply {
                moveTo(sx(34f), sy(90f)); cubicTo(sx(20f), sy(70f), sx(22f), sy(40f), sx(30f), sy(30f))
            }
            val huskR = Path().apply {
                moveTo(sx(66f), sy(90f)); cubicTo(sx(80f), sy(70f), sx(78f), sy(40f), sx(70f), sy(30f))
            }
            drawPath(huskL, leaf, style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
            drawPath(huskR, leaf, style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
        }
        "d-asparagus" -> {
            val spear = Path().apply {
                moveTo(sx(44f), sy(92f)); lineTo(sx(44f), sy(36f))
                lineTo(sx(50f), sy(14f))
                lineTo(sx(56f), sy(36f)); lineTo(sx(56f), sy(92f))
                close()
            }
            drawPath(spear, palette.primary, style = Fill)
            drawPath(spear, ink, style = lineStroke)
            // Tip scales
            listOf(24f, 34f, 46f, 58f).forEach { y ->
                val tip = Path().apply {
                    moveTo(sx(44f), sy(y)); lineTo(sx(50f), sy(y - 4f)); lineTo(sx(56f), sy(y))
                }
                drawPath(tip, palette.accent, style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round))
            }
        }
        "d-ginger" -> {
            val rhizome = Path().apply {
                moveTo(sx(30f), sy(60f))
                cubicTo(sx(20f), sy(40f), sx(36f), sy(24f), sx(50f), sy(30f))
                cubicTo(sx(64f), sy(18f), sx(80f), sy(32f), sx(74f), sy(54f))
                cubicTo(sx(84f), sy(68f), sx(72f), sy(88f), sx(54f), sy(84f))
                cubicTo(sx(38f), sy(92f), sx(26f), sy(78f), sx(30f), sy(60f))
                close()
            }
            drawPath(rhizome, palette.primary, style = Fill)
            drawPath(rhizome, ink, style = lineStroke)
            val rings = Path().apply {
                moveTo(sx(36f), sy(44f)); lineTo(sx(48f), sy(44f))
                moveTo(sx(56f), sy(40f)); lineTo(sx(68f), sy(44f))
                moveTo(sx(42f), sy(66f)); lineTo(sx(60f), sy(68f))
            }
            drawPath(rings, palette.accent.copy(alpha = 0.6f), style = thinStroke)
        }
        "d-turmeric" -> {
            val root = Path().apply {
                moveTo(sx(26f), sy(52f))
                cubicTo(sx(24f), sy(32f), sx(46f), sy(24f), sx(62f), sy(32f))
                cubicTo(sx(78f), sy(40f), sx(82f), sy(60f), sx(74f), sy(76f))
                cubicTo(sx(60f), sy(88f), sx(38f), sy(84f), sx(28f), sy(72f))
                close()
            }
            drawPath(root, palette.primary, style = Fill)
            drawPath(root, ink, style = lineStroke)
            val segment = Path().apply {
                moveTo(sx(40f), sy(38f)); lineTo(sx(54f), sy(76f))
            }
            drawPath(segment, palette.accent, style = thinStroke)
        }
        "d-artichoke" -> {
            // Layered artichoke bract scales
            val base = Path().apply {
                moveTo(sx(30f), sy(60f))
                cubicTo(sx(26f), sy(36f), sx(74f), sy(36f), sx(70f), sy(60f))
                cubicTo(sx(68f), sy(84f), sx(32f), sy(84f), sx(30f), sy(60f))
                close()
            }
            drawPath(base, palette.primary, style = Fill)
            drawPath(base, ink, style = lineStroke)
            listOf(
                Pair(50f, 32f), Pair(40f, 44f), Pair(60f, 44f),
                Pair(34f, 58f), Pair(50f, 56f), Pair(66f, 58f),
                Pair(42f, 72f), Pair(58f, 72f)
            ).forEach { (cx, cy) ->
                val scaleP = Path().apply {
                    moveTo(sx(cx - 7f), sy(cy + 4f)); lineTo(sx(cx), sy(cy - 6f)); lineTo(sx(cx + 7f), sy(cy + 4f))
                }
                drawPath(scaleP, palette.accent, style = thinStroke)
            }
            val stem = Path().apply { moveTo(sx(50f), sy(84f)); lineTo(sx(50f), sy(96f)) }
            drawPath(stem, leafDark, style = Stroke(width = 4f * scale, cap = StrokeCap.Round))
        }
        else -> {
            val p = generateBlobPath(Mulberry32(seedFromString(id)), 50f, 54f, 26f, 28f, scale)
            drawPath(p, palette.primary, style = Fill)
            drawPath(p, ink, style = lineStroke)
        }
    }
}
