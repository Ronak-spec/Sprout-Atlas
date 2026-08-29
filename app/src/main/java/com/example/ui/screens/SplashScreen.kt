package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

data class TouchParticle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    val isLeaf: Boolean = false
)

@Composable
fun SproutSplashScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Total snappy splash duration: ~1.9s (or tap to skip instantly)
    LaunchedEffect(Unit) {
        delay(1950)
        onFinish()
    }

    // 0.0 -> 1.0 main progression controller with smooth deceleration
    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Spring-loaded pop progression for the sprout burst
    val sproutBurstScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(320)
        sproutBurstScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Continuous loop for ambient sunrays, pollen drift & aura breathing
    val infiniteTransition = rememberInfiniteTransition(label = "sproutEcosystem")

    val ambientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambientAngle"
    )

    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    val dewGlint by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dewGlint"
    )

    // Interactive user touch particle system
    var touchParticles by remember { mutableStateOf(listOf<TouchParticle>()) }
    var particleCounter by remember { mutableLongStateOf(0L) }

    // Particle update ticker
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            if (touchParticles.isNotEmpty()) {
                touchParticles = touchParticles.mapNotNull { p ->
                    val newAlpha = p.alpha - 0.03f
                    if (newAlpha <= 0f) null
                    else p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy + 0.35f,
                        alpha = newAlpha,
                        rotation = p.rotation + 4f
                    )
                }
            }
        }
    }

    fun spawnParticlesAt(x: Float, y: Float, count: Int = 6) {
        val newOnes = (0 until count).map {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextDouble(2.0, 6.0)
            val isLeaf = Random.nextBoolean()
            val col = when (Random.nextInt(4)) {
                0 -> SproutLeaf
                1 -> SproutCitrus
                2 -> SproutLeafLight
                else -> SproutTomato
            }
            TouchParticle(
                id = particleCounter++,
                x = x,
                y = y,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                color = col,
                size = Random.nextDouble(4.0, 11.0).toFloat(),
                isLeaf = isLeaf
            )
        }
        touchParticles = (touchParticles + newOnes).takeLast(45)
    }

    val progress = transitionProgress.value
    val burst = sproutBurstScale.value

    // Stage progression timings
    val seedDropProg = (progress / 0.28f).coerceIn(0f, 1f)
    val bloomProg = ((progress - 0.20f) / 0.45f).coerceIn(0f, 1f)
    val auraRevealProg = ((progress - 0.35f) / 0.40f).coerceIn(0f, 1f)
    val textRevealProg = ((progress - 0.45f) / 0.45f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SproutPaper)
            .testTag("sprout_splash_screen")
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        spawnParticlesAt(offset.x, offset.y, 8)
                        // Tap skips directly into the app
                        onFinish()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    spawnParticlesAt(change.position.x, change.position.y, 2)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Procedural Botanical Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.40f // Optical centerpiece

            // --- A. Subtle Vintage Radial Paper Lighting ---
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFDF5),
                        SproutPaper,
                        SproutPaperCard.copy(alpha = 0.6f)
                    ),
                    center = Offset(cx, cy),
                    radius = w * 0.9f
                )
            )

            // --- B. Concentric Botanical Astrolabe & Compass Rings ---
            if (auraRevealProg > 0f) {
                val ringAlpha = (auraRevealProg * 0.28f).coerceIn(0f, 0.28f)
                val baseRadius = 115.dp.toPx()

                // Outer rotating coordinate ring
                rotate(degrees = ambientAngle * 0.35f, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = SproutLeaf.copy(alpha = ringAlpha * 0.9f),
                        center = Offset(cx, cy),
                        radius = baseRadius * burst,
                        style = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f))
                        )
                    )
                    drawCircle(
                        color = SproutCitrus.copy(alpha = ringAlpha * 0.7f),
                        center = Offset(cx, cy),
                        radius = (baseRadius + 22.dp.toPx()) * burst,
                        style = Stroke(width = 0.8.dp.toPx())
                    )

                    // 12 Astrolabe Compass Ticks
                    for (i in 0 until 12) {
                        val tickAngle = (i * 30f) * (PI / 180f)
                        val r1 = (baseRadius - 5.dp.toPx()) * burst
                        val r2 = (baseRadius + 5.dp.toPx()) * burst
                        drawLine(
                            color = SproutInk.copy(alpha = ringAlpha),
                            start = Offset(cx + (r1 * cos(tickAngle)).toFloat(), cy + (r1 * sin(tickAngle)).toFloat()),
                            end = Offset(cx + (r2 * cos(tickAngle)).toFloat(), cy + (r2 * sin(tickAngle)).toFloat()),
                            strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }

                // Inner counter-rotating ring
                rotate(degrees = -ambientAngle * 0.25f, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = SproutInk.copy(alpha = ringAlpha * 0.45f),
                        center = Offset(cx, cy),
                        radius = (baseRadius * 0.65f) * burst,
                        style = Stroke(
                            width = 0.8.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))
                        )
                    )
                }
            }

            // --- C. Golden Aura & Sunlight Bloom ---
            if (bloomProg > 0f) {
                val auraRadius = (130.dp.toPx() * breathingPulse) * bloomProg
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SproutCitrus.copy(alpha = 0.32f * bloomProg),
                            SproutLeafLight.copy(alpha = 0.20f * bloomProg),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = auraRadius
                    ),
                    center = Offset(cx, cy),
                    radius = auraRadius
                )

                // 8 Ambient Sunlight God Rays
                rotate(degrees = ambientAngle * 0.8f, pivot = Offset(cx, cy)) {
                    for (r in 0 until 8) {
                        val rayAngle = (r * 45f) * (PI / 180f)
                        val rayPath = Path().apply {
                            moveTo(cx, cy)
                            val endR = w * 0.65f
                            val spread = 0.10f
                            lineTo(
                                cx + (endR * cos(rayAngle - spread)).toFloat(),
                                cy + (endR * sin(rayAngle - spread)).toFloat()
                            )
                            lineTo(
                                cx + (endR * cos(rayAngle + spread)).toFloat(),
                                cy + (endR * sin(rayAngle + spread)).toFloat()
                            )
                            close()
                        }
                        drawPath(
                            path = rayPath,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SproutCitrus.copy(alpha = 0.055f * bloomProg),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = w * 0.55f
                            )
                        )
                    }
                }
            }

            // --- D. Liquid Shockwave Expansion Ripple (at burst) ---
            if (burst > 0f && burst < 1f) {
                val rippleRadius = 80.dp.toPx() * burst
                val rippleAlpha = (1f - burst).coerceIn(0f, 1f)
                drawCircle(
                    color = SproutLeaf.copy(alpha = rippleAlpha * 0.45f),
                    center = Offset(cx, cy + 10.dp.toPx()),
                    radius = rippleRadius,
                    style = Stroke(width = 2.dp.toPx() * (1f - burst))
                )
                drawCircle(
                    color = SproutCitrus.copy(alpha = rippleAlpha * 0.35f),
                    center = Offset(cx, cy + 10.dp.toPx()),
                    radius = rippleRadius * 0.7f,
                    style = Stroke(width = 1.5.dp.toPx() * (1f - burst))
                )
            }

            // --- E. The Glowing Base Pedestal & Soil Roots ---
            val soilY = cy + 32.dp.toPx()
            val soilWidth = 110.dp.toPx() * bloomProg

            if (bloomProg > 0f) {
                // Soil mound curve
                val soilPath = Path().apply {
                    moveTo(cx - soilWidth, soilY)
                    quadraticTo(cx, soilY - 5.dp.toPx(), cx + soilWidth, soilY)
                }
                drawLine(
                    color = SproutInk.copy(alpha = 0.20f * bloomProg),
                    start = Offset(cx - soilWidth, soilY),
                    end = Offset(cx + soilWidth, soilY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawPath(
                    path = soilPath,
                    color = SproutInk.copy(alpha = 0.06f * bloomProg),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Root branchlets dipping down
                val rootDepth = 35.dp.toPx() * bloomProg
                val rootPath = Path().apply {
                    moveTo(cx, soilY)
                    cubicTo(
                        cx - 8.dp.toPx(), soilY + rootDepth * 0.4f,
                        cx + 8.dp.toPx(), soilY + rootDepth * 0.7f,
                        cx, soilY + rootDepth
                    )
                }
                drawPath(
                    path = rootPath,
                    color = SproutCitrus.copy(alpha = 0.65f * bloomProg),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Left & right micro roots
                val leftRoot = Path().apply {
                    moveTo(cx - 3.dp.toPx(), soilY + 8.dp.toPx())
                    quadraticTo(cx - 18.dp.toPx() * bloomProg, soilY + 16.dp.toPx(), cx - 28.dp.toPx() * bloomProg, soilY + 24.dp.toPx())
                }
                val rightRoot = Path().apply {
                    moveTo(cx + 3.dp.toPx(), soilY + 10.dp.toPx())
                    quadraticTo(cx + 18.dp.toPx() * bloomProg, soilY + 18.dp.toPx(), cx + 28.dp.toPx() * bloomProg, soilY + 26.dp.toPx())
                }
                drawPath(leftRoot, SproutCitrus.copy(alpha = 0.45f * bloomProg), style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightRoot, SproutCitrus.copy(alpha = 0.45f * bloomProg), style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
            }

            // --- F. The Golden Seed (Falls & Pops) ---
            if (seedDropProg < 1f || burst < 0.4f) {
                val startY = cy - 140.dp.toPx()
                val targetY = soilY
                val currentSeedY = startY + (targetY - startY) * (seedDropProg * seedDropProg)
                val seedScale = (1f - burst * 1.8f).coerceIn(0f, 1f)

                if (seedScale > 0f) {
                    scale(seedScale, pivot = Offset(cx, currentSeedY)) {
                        // Seed glow
                        drawCircle(
                            color = SproutCitrus.copy(alpha = 0.4f),
                            radius = 16.dp.toPx(),
                            center = Offset(cx, currentSeedY)
                        )
                        // Seed body
                        drawOval(
                            brush = Brush.verticalGradient(
                                listOf(SproutCitrus, SproutCautionBorder, SproutInkSoft)
                            ),
                            topLeft = Offset(cx - 9.dp.toPx(), currentSeedY - 12.dp.toPx()),
                            size = Size(18.dp.toPx(), 24.dp.toPx())
                        )
                    }
                }
            }

            // --- G. The Blooming Sprout (Lush Vector Cotyledons with Spring Scale) ---
            if (burst > 0f) {
                val stemHeight = 65.dp.toPx() * burst
                val stemTipY = soilY - stemHeight
                val sway = sin(ambientAngle * 0.05f) * 4.dp.toPx() * burst

                // 1. Organic Stem
                val stemPath = Path().apply {
                    moveTo(cx, soilY)
                    cubicTo(
                        cx + sway * 0.3f, soilY - stemHeight * 0.45f,
                        cx - sway * 0.5f, soilY - stemHeight * 0.8f,
                        cx + sway, stemTipY
                    )
                }

                // Stem outline
                drawPath(
                    path = stemPath,
                    color = SproutLeafDark,
                    style = Stroke(width = 6.dp.toPx() * burst, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Stem inner lush gradient
                drawPath(
                    path = stemPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(SproutLeafLight, SproutLeaf, SproutLeafDark),
                        startY = stemTipY,
                        endY = soilY
                    ),
                    style = Stroke(width = 3.8.dp.toPx() * burst, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 2. Unfurling Twin Leaves
                val leafSize = 44.dp.toPx() * burst
                val tipX = cx + sway

                // Left Leaf
                val leftAngle = -40f * burst + sin(ambientAngle * 0.04f).toFloat() * 3f
                rotate(degrees = leftAngle, pivot = Offset(tipX, stemTipY + 3.dp.toPx())) {
                    val leftPath = Path().apply {
                        moveTo(tipX, stemTipY + 3.dp.toPx())
                        cubicTo(
                            tipX - leafSize * 0.6f, stemTipY - leafSize * 0.35f,
                            tipX - leafSize * 1.1f, stemTipY - leafSize * 0.1f,
                            tipX - leafSize * 1.3f, stemTipY - leafSize * 0.65f
                        )
                        cubicTo(
                            tipX - leafSize * 0.8f, stemTipY - leafSize * 1.0f,
                            tipX - leafSize * 0.2f, stemTipY - leafSize * 0.7f,
                            tipX, stemTipY + 3.dp.toPx()
                        )
                        close()
                    }
                    // Leaf Fill
                    drawPath(
                        path = leftPath,
                        brush = Brush.linearGradient(
                            colors = listOf(SproutLeafLight, SproutLeaf, SproutLeafDark),
                            start = Offset(tipX, stemTipY),
                            end = Offset(tipX - leafSize * 1.3f, stemTipY - leafSize * 0.65f)
                        )
                    )
                    // Leaf Outline & Vein
                    drawPath(leftPath, SproutLeafDark, style = Stroke(width = 1.4.dp.toPx()))
                    drawLine(
                        color = SproutLeafLight.copy(alpha = 0.85f),
                        start = Offset(tipX, stemTipY + 3.dp.toPx()),
                        end = Offset(tipX - leafSize * 1.15f, stemTipY - leafSize * 0.55f),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Right Leaf
                val rightAngle = 40f * burst - sin(ambientAngle * 0.04f).toFloat() * 3f
                rotate(degrees = rightAngle, pivot = Offset(tipX, stemTipY + 3.dp.toPx())) {
                    val rightPath = Path().apply {
                        moveTo(tipX, stemTipY + 3.dp.toPx())
                        cubicTo(
                            tipX + leafSize * 0.6f, stemTipY - leafSize * 0.35f,
                            tipX + leafSize * 1.1f, stemTipY - leafSize * 0.1f,
                            tipX + leafSize * 1.3f, stemTipY - leafSize * 0.65f
                        )
                        cubicTo(
                            tipX + leafSize * 0.8f, stemTipY - leafSize * 1.0f,
                            tipX + leafSize * 0.2f, stemTipY - leafSize * 0.7f,
                            tipX, stemTipY + 3.dp.toPx()
                        )
                        close()
                    }
                    // Leaf Fill
                    drawPath(
                        path = rightPath,
                        brush = Brush.linearGradient(
                            colors = listOf(SproutLeafLight, SproutLeaf, SproutLeafDark),
                            start = Offset(tipX, stemTipY),
                            end = Offset(tipX + leafSize * 1.3f, stemTipY - leafSize * 0.65f)
                        )
                    )
                    // Leaf Outline & Vein
                    drawPath(rightPath, SproutLeafDark, style = Stroke(width = 1.4.dp.toPx()))
                    drawLine(
                        color = SproutLeafLight.copy(alpha = 0.85f),
                        start = Offset(tipX, stemTipY + 3.dp.toPx()),
                        end = Offset(tipX + leafSize * 1.15f, stemTipY - leafSize * 0.55f),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Morning Dewdrop on Right Leaf
                    val dewX = tipX + leafSize * 1.26f
                    val dewY = stemTipY - leafSize * 0.62f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, SproutLeafLight, SproutLeafDark),
                            center = Offset(dewX - 1.dp.toPx(), dewY - 1.dp.toPx()),
                            radius = 4.dp.toPx()
                        ),
                        radius = 3.8.dp.toPx(),
                        center = Offset(dewX, dewY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = dewGlint),
                        radius = 1.5.dp.toPx(),
                        center = Offset(dewX - 1.2.dp.toPx(), dewY - 1.2.dp.toPx())
                    )
                }

                // Center Golden Apex Bud
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SproutCitrus, SproutLeafLight, SproutLeaf),
                        center = Offset(tipX, stemTipY),
                        radius = 6.5.dp.toPx()
                    ),
                    radius = 5.dp.toPx() * burst,
                    center = Offset(tipX, stemTipY)
                )
            }

            // --- H. Ambient Floating Pollen Motes & Fireflies ---
            val motesCount = 14
            for (i in 0 until motesCount) {
                val seedOffset = i * 52.3f
                val moteProgress = (progress * 1.6f + seedOffset * 0.06f) % 1f
                val moteAngle = (seedOffset + ambientAngle * 0.6f) * (PI / 180f)
                val moteDist = 55.dp.toPx() + (i % 4) * 26.dp.toPx()
                val mx = cx + (moteDist * cos(moteAngle)).toFloat() + sin(progress * 7f + i).toFloat() * 10f
                val my = cy + (moteDist * sin(moteAngle)).toFloat() - moteProgress * 50.dp.toPx()

                val moteAlpha = (sin(moteProgress * PI.toFloat()) * (0.35f + (i % 3) * 0.2f)).coerceIn(0f, 1f)
                val moteColor = if (i % 2 == 0) SproutCitrus else SproutLeafLight

                drawCircle(
                    color = moteColor.copy(alpha = moteAlpha),
                    radius = (2.dp.toPx() + (i % 3) * 1.dp.toPx()),
                    center = Offset(mx, my)
                )
            }

            // --- I. Interactive Touch Particles ---
            touchParticles.forEach { p ->
                if (p.isLeaf) {
                    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        val leafP = Path().apply {
                            moveTo(p.x, p.y - p.size)
                            quadraticTo(p.x + p.size, p.y, p.x, p.y + p.size)
                            quadraticTo(p.x - p.size, p.y, p.x, p.y - p.size)
                        }
                        drawPath(path = leafP, color = p.color.copy(alpha = p.alpha))
                    }
                } else {
                    drawCircle(color = p.color.copy(alpha = p.alpha), radius = p.size, center = Offset(p.x, p.y))
                }
            }
        }

        // 2. Foreground Luxury Brand Typography & Progress Track
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 44.dp, start = 24.dp, end = 24.dp)
        ) {
            val brandAlpha = textRevealProg
            val brandScale = 0.92f + textRevealProg * 0.08f
            val brandOffset = (1f - textRevealProg) * 24f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = brandOffset.dp)
                    .alpha(brandAlpha)
                    .scale(brandScale)
            ) {
                // Crest Monogram Top Pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = SproutLeafLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SproutLeaf.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(100.dp), spotColor = SproutLeaf.copy(alpha = 0.15f))
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SproutCitrus)
                        )
                        Text(
                            text = "EST. 2026 · TAXONOMY & BIOCHEMISTRY",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = SproutLeafDark
                            )
                        )
                    }
                }

                // Main Title "Sprout Atlas" with luxury serif styling
                Text(
                    text = buildAnnotatedString {
                        append("Sprout ")
                        withStyle(
                            SpanStyle(
                                color = SproutLeaf,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append("Atlas")
                        }
                    },
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp,
                        color = SproutInk
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Field Guide Subtitle
                Text(
                    text = "A L I V E  ·  B O T A N I C A L  ·  E X P L O R E R",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.2.sp,
                        color = SproutInkMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Cinematic Micro Progress Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(textRevealProg)
            ) {
                // Sleek Gradient Progress Track
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(SproutInk.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SproutLeafDark, SproutLeaf, SproutCitrus)
                                )
                            )
                    )
                }

                Text(
                    text = "Tap anywhere to begin",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = SproutInkMuted.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.clickable { onFinish() }
                )
            }
        }
    }
}

