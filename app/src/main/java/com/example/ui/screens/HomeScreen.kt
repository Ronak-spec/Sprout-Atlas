package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Whatshot
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
import androidx.compose.ui.graphics.Shadow
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
import com.example.ui.theme.NunitoFontFamily
import kotlinx.coroutines.launch
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
        // HERO CAROUSEL SLIDES (Slide 1: Know Your Produce Atlas, Slide 2: Today's Quiz)
        item {
            val pagerState = rememberPagerState(pageCount = { 2 })
            val coroutineScope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Top Live Radar Tag & Slide Switcher Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slide Indicator Pills / Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, GenZElectricEmerald.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (pagerState.currentPage == 0) {
                                        PulsePillGradient
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = if (pagerState.currentPage == 0) Color.White else SproutInkSoft,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Atlas",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pagerState.currentPage == 0) Color.White else SproutInkSoft
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (pagerState.currentPage == 1) {
                                        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Psychology,
                                    contentDescription = null,
                                    tint = if (pagerState.currentPage == 1) Color.White else SproutInkSoft,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Daily Quiz",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pagerState.currentPage == 1) Color.White else SproutInkSoft
                                    )
                                )
                            }
                        }
                    }

                    // Daily Streak Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                )
                            )
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Whatshot,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$quizStreak Day Streak",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            )
                        }
                    }
                }

                // Horizontal Pager with 2 distinct interactive slide boards
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> {
                            // SLIDE 1: KNOW YOUR PRODUCE ATLAS BOARD
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = GenZCyberGreen.copy(alpha = 0.25f))
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(PulseSlideGradient)
                                    .border(
                                        width = 1.5.dp,
                                        brush = PulseBorderGradient,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Top Header & Specimen Avatars
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(100.dp),
                                                    color = Color.Transparent,
                                                    modifier = Modifier
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(GenZElectricEmerald.copy(alpha = 0.22f), GenZCyberGreen.copy(alpha = 0.15f))
                                                            ),
                                                            RoundedCornerShape(100.dp)
                                                        )
                                                        .border(1.dp, GenZElectricEmerald.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Eco,
                                                            contentDescription = null,
                                                            tint = GenZDarkForest,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "BOTANICAL FIELD ATLAS",
                                                            style = TextStyle(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = GenZDarkForest,
                                                                letterSpacing = 0.5.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = "Know Your Produce",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 25.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = (-0.4).sp,
                                                    color = SproutInk
                                                )
                                            )
                                        }

                                        // Specimen Cluster
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy((-8).dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE8F5E9))
                                                    .border(1.5.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ProduceDoodle("d-avocado", "stone", "Avocado", size = 26.dp)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFEBEE))
                                                    .border(1.5.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ProduceDoodle("d-strawberry", "berry", "Strawberry", size = 26.dp)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFF9C4))
                                                    .border(1.5.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ProduceDoodle("d-lemon", "citrus", "Lemon", size = 26.dp)
                                            }
                                        }
                                    }

                                    // Search Packet with Instant Navigation
                                    SearchPacket(
                                        query = searchInput,
                                        onQueryChange = {
                                            searchInput = it
                                            if (it.isNotEmpty()) {
                                                onNavigateToExplore(it)
                                            }
                                        },
                                        placeholder = "Search 500+ items (e.g. avocado, kale, mango)",
                                        showTypoBadge = true,
                                        isLarge = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Quick Jump Specimen Carousel
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "POPULAR BOTANICAL SPECIMENS",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SproutInkSoft,
                                                letterSpacing = 0.6.sp
                                            )
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(
                                                Triple("Avocado", "d-avocado", 157),
                                                Triple("Broccoli", "d-broccoli", 276),
                                                Triple("Strawberry", "d-strawberry", 91),
                                                Triple("Lemon", "d-lemon", 55),
                                                Triple("Carrot", "d-carrot", 331),
                                                Triple("Tomato", "d-tomato", 374),
                                                Triple("Grapes", "d-grapes", 95),
                                                Triple("Mango", "d-mango", 220),
                                                Triple("Blueberry", "d-blueberry", 92)
                                            ).forEach { (name, symbolId, id) ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(100.dp))
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(Color.White, GenZNeonMintLight)
                                                            )
                                                        )
                                                        .border(
                                                            1.dp,
                                                            GenZElectricEmerald.copy(alpha = 0.35f),
                                                            RoundedCornerShape(100.dp)
                                                        )
                                                        .clickable { onNavigateToGuide(id) }
                                                        .padding(horizontal = 11.dp, vertical = 6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        ProduceDoodle(
                                                            symbolId = symbolId,
                                                            archetype = "produce",
                                                            name = name,
                                                            size = 18.dp
                                                        )
                                                        Text(
                                                            text = name,
                                                            style = TextStyle(
                                                                fontFamily = FontFamily.Default,
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

                                    // Micro Value Stats Ribbon
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFFECFDF5).copy(alpha = 0.8f),
                                                        Color(0xFFEFF6FF).copy(alpha = 0.8f)
                                                    )
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                Color(0xFF10B981).copy(alpha = 0.25f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.MenuBook,
                                                    contentDescription = null,
                                                    tint = Color(0xFF047857),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    "500+ Guides",
                                                    style = TextStyle(
                                                        fontFamily = FontFamily.Default,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SproutInk
                                                    )
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981).copy(alpha = 0.4f))
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Biotech,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1D4ED8),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    "19 Families",
                                                    style = TextStyle(
                                                        fontFamily = FontFamily.Default,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SproutInk
                                                    )
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981).copy(alpha = 0.4f))
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Shield,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB45309),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    "Daily Quiz",
                                                    style = TextStyle(
                                                        fontFamily = FontFamily.Default,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // SLIDE 2: TODAY'S BOTANICAL QUIZ BOARD
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = GenZDarkForest.copy(alpha = 0.25f))
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFF4FBF6),
                                                Color(0xFFE3F7EB),
                                                Color(0xFFC7EED6)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                GenZElectricEmerald.copy(alpha = 0.6f),
                                                GenZCyberGreen.copy(alpha = 0.45f),
                                                GenZAcidLime.copy(alpha = 0.5f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(20.dp)
                                    .testTag("hero_daily_quiz_board")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(100.dp),
                                                    color = Color.Transparent,
                                                    modifier = Modifier
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(GenZElectricEmerald.copy(alpha = 0.22f), GenZCyberGreen.copy(alpha = 0.15f))
                                                            ),
                                                            RoundedCornerShape(100.dp)
                                                        )
                                                        .border(1.dp, GenZElectricEmerald.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                                                ) {
                                                    Text(
                                                        text = "TODAY'S BOTANICAL QUIZ",
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = GenZDarkForest,
                                                            letterSpacing = 0.5.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp)
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Whatshot,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "${quizStreak}-Day Streak",
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Default,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB45309)
                                                        )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = if (isQuizAnswered) "Solved for today!" else "Mystery Botanical Specimen",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 23.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = SproutInk
                                                ),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }

                                        // Quiz Doodle Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(if (isQuizAnswered) Color(0xFFDCFCE7) else Color(0xFFE8F5E9))
                                                .border(1.5.dp, if (isQuizAnswered) GenZElectricEmerald else GenZCyberGreen.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isQuizAnswered) {
                                                ProduceDoodle(
                                                    symbolId = todayQuiz.symbolId,
                                                    archetype = todayQuiz.archetype,
                                                    name = todayQuiz.specimenName,
                                                    size = 40.dp
                                                )
                                            } else {
                                                ProduceDoodle(
                                                    symbolId = "d-question",
                                                    archetype = "question",
                                                    name = "Daily Quiz",
                                                    size = 40.dp
                                                )
                                            }
                                        }
                                    }

                                    // Clue preview container
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isQuizAnswered) Color(0xFFF0FDF4) else Color.White.copy(alpha = 0.92f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isQuizAnswered) GenZElectricEmerald.copy(alpha = 0.4f) else GenZCyberGreen.copy(alpha = 0.3f),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isQuizAnswered) Icons.Outlined.CheckCircle else Icons.Outlined.Search,
                                                    contentDescription = null,
                                                    tint = if (isQuizAnswered) GenZDarkForest else Color(0xFF047857),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = if (isQuizAnswered) "Solved Specimen" else "Primary Clue",
                                                    style = TextStyle(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isQuizAnswered) GenZDarkForest else Color(0xFF047857)
                                                    )
                                                )
                                            }
                                            Text(
                                                text = if (isQuizAnswered)
                                                    "You identified ${todayQuiz.specimenName}! Tap below to review the full field guide or replay."
                                                else
                                                    "\"${todayQuiz.clues.firstOrNull() ?: "One mystery specimen, three clues, sixty seconds."}\"",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = SproutInk,
                                                    lineHeight = 19.sp
                                                )
                                            )
                                        }
                                    }

                                    // Action Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = onOpenQuiz,
                                            shape = RoundedCornerShape(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isQuizAnswered) Color(0xFF047857) else GenZDarkForest,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .shadow(3.dp, RoundedCornerShape(100.dp))
                                                .testTag("play_hero_quiz_btn")
                                        ) {
                                            Text(
                                                text = if (isQuizAnswered) "Review Today's Quiz →" else "Play Today's Quiz →",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                            },
                                            shape = RoundedCornerShape(100.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GenZElectricEmerald.copy(alpha = 0.4f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = SproutInk
                                            )
                                        ) {
                                            Text(
                                                "← Atlas",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Carousel Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 18.dp else 6.dp, 6.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                        )
                    }
                }
            }
        }

        // SEASONAL CORKBOARD SPECIMENS (Rotates weekly with shuffle & tactile pins)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Realistic wooden framed corkboard container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0x403E2723))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF8D6E63),
                                    Color(0xFF5D4037),
                                    Color(0xFF4E342E),
                                    Color(0xFF3E2723)
                                )
                            )
                        )
                        .padding(7.dp)
                ) {
                    // Inner cork canvas with procedural grain & texture
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFEAD9B8),
                                        Color(0xFFDECA9F),
                                        Color(0xFFD0B888),
                                        Color(0xFFBF9F6A)
                                    ),
                                    radius = 700f
                                )
                            )
                    ) {
                        // Procedural cork texture speckles
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val step = 32f
                            for (x in 0..(size.width / step).toInt()) {
                                for (y in 0..(size.height / step).toInt()) {
                                    val seed = (x * 73 + y * 137 + corkboardShuffleCount * 29) % 100
                                    if (seed > 65) {
                                        drawCircle(
                                            color = Color(0x286D4C41),
                                            radius = (seed % 3 + 1).toFloat(),
                                            center = Offset(x * step + (seed % 15), y * step + (seed % 17))
                                        )
                                    }
                                }
                            }
                        }

                        // 4 Brass Corner Screws/Brackets
                        listOf(
                            Alignment.TopStart to Offset(8f, 8f),
                            Alignment.TopEnd to Offset(-8f, 8f),
                            Alignment.BottomStart to Offset(8f, -8f),
                            Alignment.BottomEnd to Offset(-8f, -8f)
                        ).forEach { (align, _) ->
                            Box(
                                modifier = Modifier
                                    .align(align)
                                    .padding(6.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFFFECB3), Color(0xFFFFB300), Color(0xFF6D4C41))
                                        )
                                    )
                                    .border(0.6.dp, Color(0xFF3E2723), CircleShape)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top pinned header ribbon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 3D Red Pushpin Icon
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color.White, Color(0xFFE53935), Color(0xFFB71C1C))
                                                )
                                            )
                                            .shadow(1.dp, CircleShape)
                                    )
                                    Text(
                                        text = "SEASONAL CORKBOARD",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp,
                                            color = Color(0xFF3E2723),
                                            shadow = Shadow(color = Color(0x55FFFFFF), offset = Offset(1f, 1f), blurRadius = 1f)
                                        )
                                    )
                                }

                                // Quick Shuffle Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                                        .clickable { repository.shuffleCorkboard() }
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
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
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Shuffle",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Default,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4E342E)
                                            )
                                        )
                                    }
                                }
                            }

                            // First row of 3 weekly items with varied rotations & fasteners
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
                                        size = 90.dp
                                    )
                                }
                            }

                            // Second row of 3 weekly items
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
                                        size = 90.dp
                                    )
                                }
                            }

                            // Bottom bar: Translucent Week ribbon badge centered
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(Color(0xCC3E2723))
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "📍 $weekLabel · Rotates weekly with harvest",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFF8E1)
                                        )
                                    )
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
                    .padding(vertical = 10.dp)
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
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    homeCats.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.White, SproutPaperCard)
                                    )
                                )
                                .border(1.2.dp, SproutLine.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                                .clickable { onNavigateToExplore(cat.targetCat) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProduceDoodle(
                                symbolId = cat.symbolId,
                                archetype = cat.archetype,
                                name = cat.produceName,
                                size = 22.dp
                            )
                            Text(
                                text = cat.label,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
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

        // BROWSE GRID (8 Specimens)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                EyebrowHeader(text = "The atlas")
                Text(
                    text = "Open any guide, learn the whole story.",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = SproutInk
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
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
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToExplore(null) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = SproutPaperCard,
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.35f))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "470+",
                                                style = TextStyle(
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 24.sp,
                                                    color = SproutLeaf
                                                )
                                            )
                                            Text(
                                                text = "more guides in atlas →",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
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
        }

        // VISUAL SPECIMEN ANATOMY INFOGRAPHIC (Interactive & Graphic)
        item {
            var selectedAnatomyTab by remember { mutableStateOf(0) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                EyebrowHeader(text = "Visual botanical breakdown")
                Text(
                    text = "Inside Every Specimen Guide",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = SproutInk
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // 4 Interactive Visual Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(SproutPaperCard)
                        .border(1.2.dp, SproutLine.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf(
                        Pair(Icons.Outlined.Science, "Nutrition"),
                        Pair(Icons.Outlined.Kitchen, "Storage"),
                        Pair(Icons.Outlined.Public, "Terroir"),
                        Pair(Icons.Outlined.Sanitizer, "Detox")
                    ).forEachIndexed { index, (icon, title) ->
                        val isSelected = selectedAnatomyTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(listOf(SproutLeaf, SproutForest))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    }
                                )
                                .clickable { selectedAnatomyTab = index }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else SproutInkSoft,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = title,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else SproutInkSoft,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Visual Specimen Card (Avocado Focus)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x25047857))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White, Color(0xFFFAFCF9))
                            )
                        )
                        .border(1.2.dp, SproutLine.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToGuide(157) }
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header row with specimen doodle & tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ProduceDoodle("d-avocado", "stone", "Avocado", size = 38.dp)
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Avocado",
                                            fontFamily = FontFamily.Default,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = SproutInk
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0))
                                                    )
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        ) {
                                            Text(
                                                "IN SEASON",
                                                color = Color(0xFF047857),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Persea americana · Lauraceae",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        color = SproutInkSoft
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = SproutLeaf,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(color = SproutLine.copy(alpha = 0.25f), thickness = 1.dp)

                        // Dynamic Tab Content with Graphic Gauges & Micro-meters
                        when (selectedAnatomyTab) {
                            0 -> { // Nutrition
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    VisualMetricMeter("Healthy Monounsaturated Fats", "High (15g / 100g)", 0.88f, Color(0xFF10B981))
                                    VisualMetricMeter("Dietary Fiber", "High (7g / 100g)", 0.75f, Color(0xFF3B82F6))
                                    VisualMetricMeter("Potassium Density", "Exceeds Bananas (485mg)", 0.82f, Color(0xFFF59E0B))
                                    VisualMetricMeter("Glycemic Load", "Ultra Low (GL: 1)", 0.12f, Color(0xFF8B5CF6))
                                }
                            }
                            1 -> { // Storage
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    VisualStoragePill(
                                        iconVector = Icons.Outlined.Thermostat,
                                        iconTint = Color(0xFFD97706),
                                        phase = "Countertop (Until Yields)",
                                        rule = "Store at room temp (68°F/20°C) with stem on until gentle yield to thumb pressure."
                                    )
                                    VisualStoragePill(
                                        iconVector = Icons.Outlined.AcUnit,
                                        iconTint = Color(0xFF2563EB),
                                        phase = "Crisper Drawer (Ripe Preservation)",
                                        rule = "Transfer to fridge at 40°F (4°C) to halt ripening and preserve peak texture for 4-5 days."
                                    )
                                }
                            }
                            2 -> { // Terroir
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        VisualStatTile("Peak Harvest", "Feb — Sep", "Primary cycle", Icons.Outlined.Eco, Color(0xFF047857), Modifier.weight(1f))
                                        VisualStatTile("Optimum Climate", "Subtropical", "High drainage soil", Icons.Outlined.WbSunny, Color(0xFFD97706), Modifier.weight(1f))
                                    }
                                }
                            }
                            3 -> { // Detox Rinse
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    VisualStoragePill(
                                        iconVector = Icons.Outlined.Sanitizer,
                                        iconTint = Color(0xFF0D9488),
                                        phase = "Baking Soda & Brush Scrub",
                                        rule = "Even thick-skinned produce should be brushed under running water. Knife blades transfer peel bacteria directly into the flesh."
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            "Clean 15 Specimen: Extremely low pesticide residue.",
                                            fontFamily = FontFamily.Default,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }
                            }
                        }

                        // Tap to explore full guide banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View complete 6-section field guide →",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            )
                            Text(
                                text = "ID #157",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SproutInkSoft
                                )
                            )
                        }
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
        modifier = modifier.clickable { onClick() }.testTag("browse_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProduceDoodle(
                        symbolId = item.symbolId,
                        archetype = item.archetype,
                        name = item.name,
                        size = 50.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tag,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = item.name,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutInk,
                    lineHeight = 20.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = shortBlurb,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
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
        shape = RoundedCornerShape(16.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SproutInk
                )
                Text(
                    description,
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = SproutInkSoft,
                    lineHeight = 16.sp
                )
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
            fontFamily = FontFamily.Default,
            fontSize = 12.5.sp,
            color = SproutInkSoft,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontFamily = FontFamily.Default,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SproutInk
        )
    }
}

@Composable
private fun VisualMetricMeter(
    label: String,
    metric: String,
    fraction: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SproutInk
            )
            Text(
                metric,
                fontFamily = FontFamily.Default,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(SproutLine.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(100.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun VisualStoragePill(
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = SproutInk,
    phase: String,
    rule: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    phase,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SproutInk
                )
                Text(
                    rule,
                    fontFamily = FontFamily.Default,
                    fontSize = 11.5.sp,
                    color = SproutInkSoft,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun VisualStatTile(
    title: String,
    value: String,
    sub: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = Color(0xFF047857),
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SproutPaperCard,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                title,
                fontFamily = FontFamily.Default,
                fontSize = 11.sp,
                color = SproutInkSoft,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SproutInk
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    sub,
                    fontFamily = FontFamily.Default,
                    fontSize = 10.5.sp,
                    color = iconTint,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class HomeCategoryChip(
    val label: String,
    val targetCat: String,
    val symbolId: String,
    val archetype: String,
    val produceName: String
)

