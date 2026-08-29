package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.ProduceCatalog
import com.example.data.model.ProduceItem
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToExplore: (String?) -> Unit,
    onNavigateToGuide: (Int) -> Unit,
    onOpenQuiz: () -> Unit,
    repository: com.example.data.repository.SproutAtlasRepository = remember { com.example.data.repository.SproutAtlasRepository() }
) {
    var searchInput by remember { mutableStateOf("") }
    val corkboardShuffleCount by repository.corkboardShuffleCount.collectAsState()
    val (weekLabel, weeklyItems) = remember(corkboardShuffleCount) {
        repository.getWeeklyCorkboardInfo(shuffleSeed = corkboardShuffleCount)
    }
    val quizStreak by repository.quizStreak.collectAsState()
    val isQuizAnswered by repository.isTodayQuizAnswered.collectAsState()
    val todayQuiz = remember { repository.getTodayQuiz() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutPaper)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // HERO COPY
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                EyebrowHeader(text = "A field guide for the produce aisle")

                Text(
                    text = buildAnnotatedString {
                        append("Know what you're ")
                        withStyle(SpanStyle(color = SproutTomato, fontStyle = FontStyle.Italic)) {
                            append("actually")
                        }
                        append(" eating.")
                    },
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                        color = SproutInk
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                )

                Text(
                    text = "A field guide for 500+ fruits and vegetables — nutrition, storage, and how to keep it safe to eat.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = SproutInkSoft
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SearchPacket(
                    query = searchInput,
                    onQueryChange = {
                        searchInput = it
                        if (it.isNotEmpty()) {
                            onNavigateToExplore(it)
                        }
                    },
                    placeholder = "Try “brocoli” — typos are okay",
                    showTypoBadge = true,
                    isLarge = true,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroStat(number = "500+", label = "guides & growing", modifier = Modifier.weight(1f))
                    HeroStat(number = "6", label = "things per item", modifier = Modifier.weight(1f))
                    HeroStat(number = "1", label = "daily quiz", modifier = Modifier.weight(1f))
                }
            }
        }

        // CORKBOARD SPECIMENS (Rotates weekly with creative tactile styling)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Realistic wooden framed corkboard container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x334E342E))
                        .clip(RoundedCornerShape(22.dp))
                        // Outer wooden frame
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8D6E63), Color(0xFF6D4C41), Color(0xFF4E342E))
                            )
                        )
                        .border(2.5.dp, Color(0xFF3E2723), RoundedCornerShape(22.dp))
                        .padding(7.dp) // Wooden frame thickness
                ) {
                    // Inner Cork Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFF3E5CA), Color(0xFFE6D3B1), Color(0xFFD8C09A)),
                                    radius = 1200f
                                )
                            )
                            .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.45f), RoundedCornerShape(15.dp))
                            .padding(12.dp)
                    ) {
                        // Background Cork Texture Specks
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val dotColor1 = Color(0xFF8D6E63).copy(alpha = 0.12f)
                            val dotColor2 = Color(0xFF4E342E).copy(alpha = 0.08f)
                            val w = size.width
                            val h = size.height
                            if (w > 0 && h > 0) {
                                // Deterministic procedural cork grain flecks
                                for (i in 0 until 100) {
                                    val x = ((i * 7919) % 1000) / 1000f * w
                                    val y = ((i * 6271) % 1000) / 1000f * h
                                    val radius = ((i % 4) + 1.1f)
                                    drawCircle(
                                        color = if (i % 2 == 0) dotColor1 else dotColor2,
                                        radius = radius,
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }

                        // Four Brass Corner Screws/Brackets on the corkboard
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFEEA8), Color(0xFFD4AF37), Color(0xFF7A5C16))
                                    )
                                )
                                .border(0.8.dp, Color(0xFF4E342E), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFEEA8), Color(0xFFD4AF37), Color(0xFF7A5C16))
                                    )
                                )
                                .border(0.8.dp, Color(0xFF4E342E), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFEEA8), Color(0xFFD4AF37), Color(0xFF7A5C16))
                                    )
                                )
                                .border(0.8.dp, Color(0xFF4E342E), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFEEA8), Color(0xFFD4AF37), Color(0xFF7A5C16))
                                    )
                                )
                                .border(0.8.dp, Color(0xFF4E342E), CircleShape)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Top pinned header bar (clean & minimal without extra text clutter)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.PushPin,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "SEASONAL PINBOARD",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.2.sp,
                                            color = Color(0xFF4E342E)
                                        )
                                    )
                                }

                                // Quick Shuffle Button
                                Box(
                                    modifier = Modifier
                                        .shadow(1.dp, RoundedCornerShape(100.dp))
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                                        .clickable { repository.shuffleCorkboard() }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("shuffle_corkboard_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Shuffle,
                                            contentDescription = "Shuffle",
                                            tint = Color(0xFF4E342E),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Shuffle",
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4E342E)
                                            )
                                        )
                                    }
                                }
                            }

                            // First row of 3 weekly items (Bigger & spacious)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                weeklyItems.take(3).forEach { item ->
                                    CorkboardPin(
                                        symbolId = item.symbolId,
                                        name = item.name,
                                        archetype = item.archetype,
                                        rotation = item.rotation,
                                        tapeRotation = item.tapeRotation,
                                        pinColor = item.pinColor,
                                        pinStyle = item.pinStyle,
                                        onClick = { onNavigateToGuide(item.id) },
                                        size = 88.dp
                                    )
                                }
                            }

                            // Second row of 3 weekly items (Bigger & spacious)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                weeklyItems.drop(3).take(3).forEach { item ->
                                    CorkboardPin(
                                        symbolId = item.symbolId,
                                        name = item.name,
                                        archetype = item.archetype,
                                        rotation = item.rotation,
                                        tapeRotation = item.tapeRotation,
                                        pinColor = item.pinColor,
                                        pinStyle = item.pinStyle,
                                        onClick = { onNavigateToGuide(item.id) },
                                        size = 88.dp
                                    )
                                }
                            }

                            // Bottom bar of corkboard: Clean Week badge centered
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(SproutPaperCard.copy(alpha = 0.95f))
                                        .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 14.dp, vertical = 4.5.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SproutLeaf)
                                        )
                                        Text(
                                            text = weekLabel,
                                            style = TextStyle(
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SproutInk
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CATEGORY CHIP STRIP
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val homeCats = listOf(
                    HomeCategoryChip("Fruits", "Pome Fruits", "d-apple", "pome", "Apple"),
                    HomeCategoryChip("Leafy & green", "Leafy Greens", "d-broccoli", "crucifer", "Broccoli"),
                    HomeCategoryChip("Roots & tubers", "Root & Tuber Vegetables", "d-carrot", "root", "Carrot"),
                    HomeCategoryChip("Nightshades", "Nightshades", "d-tomato", "nightshade", "Tomato"),
                    HomeCategoryChip("Citrus", "Citrus Fruits", "d-lemon", "citrus", "Lemon"),
                    HomeCategoryChip("Stone & pit", "Stone Fruits (Drupes)", "d-peach", "stone", "Peach"),
                    HomeCategoryChip("Berries", "Berries & Small Fruits", "d-strawberry", "berry", "Strawberry"),
                    HomeCategoryChip("Tropical", "Tropical Fruits", "d-banana", "tropical", "Banana")
                )
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    homeCats.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(SproutPaperCard)
                                .border(1.2.dp, SproutInk.copy(alpha = 0.22f), RoundedCornerShape(100.dp))
                                .clickable { onNavigateToExplore(cat.targetCat) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProduceDoodle(
                                symbolId = cat.symbolId,
                                archetype = cat.archetype,
                                name = cat.produceName,
                                size = 24.dp
                            )
                            Text(
                                text = cat.label,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SproutInk
                                )
                            )
                        }
                    }
                }
            }
        }

        // DAILY QUIZ CARD (Rich Visual Split)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .shadow(4.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1F2B1E), Color(0xFF141F14), Color(0xFF0F180F))
                        )
                    )
                    .border(1.5.dp, Color(0x45FAF6E9), RoundedCornerShape(22.dp))
                    .clickable { onOpenQuiz() }
                    .padding(18.dp)
                    .testTag("daily_quiz_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EyebrowHeader(
                                text = "Daily Specimen",
                                color = SproutCitrus
                            )
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0x28FAF6E9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SproutCitrus.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = SproutCitrus,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "${quizStreak}-day streak",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SproutCitrus,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isQuizAnswered) "Solved for today!" else "Guess it before we name it.",
                            style = TextStyle(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutPaper
                            )
                        )

                        Text(
                            text = if (isQuizAnswered) "You identified today's specimen! Tap to review clues or open full guide." else "One mystery specimen, three clues, sixty seconds.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFFD4DEC8)
                            )
                        )

                        Button(
                            onClick = onOpenQuiz,
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isQuizAnswered) SproutLeaf else SproutCitrus,
                                contentColor = if (isQuizAnswered) Color.White else SproutInk
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("play_quiz_btn")
                        ) {
                            Text(
                                text = if (isQuizAnswered) "Review today's quiz →" else "Play today's quiz →",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Visual Quiz Emblem
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0x18FFFFFF))
                            .border(1.2.dp, Color(0x30FAF6E9), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isQuizAnswered) {
                            ProduceDoodle(
                                symbolId = todayQuiz.symbolId,
                                archetype = todayQuiz.archetype,
                                name = todayQuiz.specimenName,
                                size = 64.dp
                            )
                        } else {
                            ProduceDoodle(
                                symbolId = "d-question",
                                archetype = "question",
                                name = "Daily Quiz",
                                size = 64.dp
                            )
                        }
                    }
                }
            }
        }

        // BROWSE GRID (8 Specimens)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                EyebrowHeader(text = "The atlas")
                Text(
                    text = "Open any guide, learn the whole story.",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                val sampleGuides = listOf(
                    Pair(ProduceCatalog.getById(166)!!, "In season"),
                    Pair(ProduceCatalog.getById(331)!!, "Root veg"),
                    Pair(ProduceCatalog.getById(91)!!, "Berry"),
                    Pair(ProduceCatalog.getById(157)!!, "Stone fruit"),
                    Pair(ProduceCatalog.getById(276)!!, "Cruciferous"),
                    Pair(ProduceCatalog.getById(55)!!, "Citrus"),
                    Pair(ProduceCatalog.getById(374)!!, "Nightshade")
                )

                // 2-Column Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0 until sampleGuides.size step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val (item1, tag1) = sampleGuides[i]
                            BrowseCard(
                                item = item1,
                                tag = tag1,
                                onClick = { onNavigateToGuide(item1.id) },
                                modifier = Modifier.weight(1f)
                            )
                            if (i + 1 < sampleGuides.size) {
                                val (item2, tag2) = sampleGuides[i + 1]
                                BrowseCard(
                                    item = item2,
                                    tag = tag2,
                                    onClick = { onNavigateToGuide(item2.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // 8th element: "470+ more guides"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SproutPaperCard)
                                        .border(1.3.dp, SproutLine.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .clickable { onNavigateToExplore(null) }
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "470+",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Default,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                color = SproutInk
                                            )
                                        )
                                        Text(
                                            text = "more guides in atlas →",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center,
                                                color = SproutInkSoft
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // INSIDE A GUIDE SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                EyebrowHeader(text = "What's inside every guide")
                Text(
                    text = "Four essentials before you take a bite.",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk
                    ),
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureRow(
                        icon = { Icon(Icons.Outlined.Eco, contentDescription = null, tint = SproutTomato) },
                        title = "Nutrition, in plain terms",
                        description = "Key vitamins & benefits without a wall of numbers."
                    )
                    FeatureRow(
                        icon = { Icon(Icons.Outlined.Kitchen, contentDescription = null, tint = SproutCitrus) },
                        title = "Storage that actually works",
                        description = "Fridge, counter, or freezer — and shelf life."
                    )
                    FeatureRow(
                        icon = { Icon(Icons.Outlined.LocalFlorist, contentDescription = null, tint = SproutLeaf) },
                        title = "Where and how it grows",
                        description = "Season, climate, and the plant itself."
                    )
                    FeatureRow(
                        icon = { Icon(Icons.Outlined.Sanitizer, contentDescription = null, tint = SproutLeafDark) },
                        title = "Washing & treatments",
                        description = "What's sprayed or waxed, and how to rinse."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guide Preview Card (Avocado)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SproutPaperCard)
                        .border(1.3.dp, SproutLine.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                        .clickable { onNavigateToGuide(157) }
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProduceDoodle("d-avocado", "stone", "Avocado", size = 48.dp)
                            Column {
                                Text("Avocado", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SproutInk)
                                Text("Stone fruit · Guide preview", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SproutInkSoft)
                            }
                        }

                        HorizontalDivider(color = SproutLine.copy(alpha = 0.2f), thickness = 1.dp)

                        PreviewRow("Peak season", "Feb – Sep")
                        PreviewRow("Best storage", "Counter, then fridge")
                        PreviewRow("Common treatment", "Wash outer skin with brush")
                        PreviewRow("Good source of", "Healthy fats, fiber, potassium")
                    }
                }
            }
        }

        // ABOUT / CTA BAND
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EyebrowHeader(text = "Field-sketched guides")
                Text(
                    text = "Growing toward 500+ specimens.",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = SproutInk
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "Botanical field guides, camera identifier, and meal science assistant.",
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        color = SproutInkSoft
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigateToExplore(null) },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SproutTomato,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Browse the atlas", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenQuiz,
                        shape = RoundedCornerShape(100.dp),
                        border = androidx.compose.foundation.BorderStroke(1.4.dp, SproutInk),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SproutInk)
                    ) {
                        Text("Take today's quiz", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        // FOOTER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = SproutLine.copy(alpha = 0.2f), thickness = 1.dp)
                Text(
                    text = "© 2026 Sprout Atlas — a small project, growing on purpose.\nField-sketched, not stock-photographed.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = SproutInkSoft,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = number,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SproutInk
                )
            )
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = SproutInkSoft
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BrowseCard(
    item: ProduceItem,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shortBlurb = remember(item.blurb) {
        val firstSentence = item.blurb.split(".").firstOrNull()?.trim() ?: item.blurb
        firstSentence.split(";").firstOrNull()?.trim() ?: firstSentence
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.3f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .clickable { onClick() }
            .testTag("browse_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SproutPaper)
                        .border(1.dp, SproutLine.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProduceDoodle(
                        symbolId = item.symbolId,
                        archetype = item.archetype,
                        name = item.name,
                        size = 58.dp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = SproutLeaf.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SproutLeaf.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = tag,
                        style = TextStyle(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutLeafDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.name,
                style = TextStyle(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutInk,
                    lineHeight = 19.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = shortBlurb,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = SproutInkSoft,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SproutLine.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SproutPaper)
                    .border(1.2.dp, SproutLine.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SproutInk)
                Text(description, fontSize = 12.sp, color = SproutInkSoft, lineHeight = 16.5.sp)
            }
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = SproutInkSoft,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SproutInk
        )
    }
}

private data class HomeCategoryChip(
    val label: String,
    val targetCat: String,
    val symbolId: String,
    val archetype: String,
    val produceName: String
)

