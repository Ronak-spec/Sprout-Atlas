package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.ProduceCatalog
import com.example.data.model.ProduceItem
import com.example.data.repository.SproutAtlasRepository
import com.example.data.subscription.SubscriptionService
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun GuideDetailScreen(
    produceId: Int,
    repository: SproutAtlasRepository,
    subscriptionService: SubscriptionService? = null,
    onOpenPaywall: (() -> Unit)? = null,
    onBack: () -> Unit,
    onOpenQuiz: (() -> Unit)? = null,
    onNavigateToGuide: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val item = ProduceCatalog.getById(produceId) ?: ProduceCatalog.allItems.first()
    val savedItemIds by repository.savedItemIds.collectAsState()
    val isSaved = savedItemIds.contains(item.id)

    val isIdentifyLocked = subscriptionService?.isGuideTabLocked(1) ?: false
    val isChemicalsLocked = subscriptionService?.isGuideTabLocked(2) ?: false
    val isFullVisibility = subscriptionService?.isFullGuideVisibilityActive() ?: true
    val isDay1Preview = subscriptionService?.isFirstDayGracePeriodActive() ?: false
    val hoursRemaining = subscriptionService?.getFirstDayRemainingHours() ?: 0
    val isProUser = subscriptionService?.subscriptionState?.collectAsState()?.value?.isPro ?: false

    // 3 Distinct Guide Tabs requested by user:
    // 0 -> Botanical Information & Nutrients
    // 1 -> Freshness Identification & Washing Protocol
    // 2 -> Chemical Treatments & Safety
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(isFullVisibility) {
        if (!isFullVisibility && selectedTab != 0) {
            selectedTab = 0
        }
    }

    val relatedItems = remember(item) {
        ProduceCatalog.getRelated(item, count = 4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutPaper)
            .testTag("guide_detail_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. HERO PRESENTATION CARD (Clean, sticker-free, hooking & readable)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Doodle in illuminated circle
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(SproutPaperCard)
                        .border(2.dp, SproutLeaf.copy(alpha = 0.35f), CircleShape)
                        .shadow(elevation = 6.dp, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ProduceDoodle(
                        symbolId = item.symbolId,
                        archetype = item.archetype,
                        name = item.name,
                        size = 78.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scientific classification
                Text(
                    text = item.scientific.ifBlank { "Botanical Specimen · ${item.category}" },
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SproutLeafDark
                    ),
                    textAlign = TextAlign.Center
                )

                // Large, bold, readable Specimen Name
                Text(
                    text = item.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Clean botanical metadata line (Full text viewed fully, no sticker borders)
                Text(
                    text = "Category: ${item.category}  •  Type: ${item.type}  •  Specimen #${item.id}",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SproutInkSoft,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                // Hooking and readable introductory overview
                Text(
                    text = item.blurb,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = SproutInk,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(0.96f)
                )

                // Clean Save / Like Button with clear text and icon (no sticker noise beside it)
                Button(
                    onClick = { repository.toggleSave(item.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaved) SproutTomato else SproutLeaf.copy(alpha = 0.15f),
                        contentColor = if (isSaved) Color.White else SproutLeafDark
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("save_guide_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = if (isSaved) "Saved" else "Save to Journal",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) "Saved in Journal" else "Save Guide to Journal",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // 2. SEGMENTED THREE-TAB SELECTOR (High visibility, engaging, clear)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Free Plan vs Day-1 Grace Period notification badge
                if (!isProUser) {
                    if (isDay1Preview) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SproutLeaf.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = SproutLeafDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Day 1 Free Access: All 3 guide tabs unlocked today (${hoursRemaining}h remaining).",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.5.sp,
                                        color = SproutLeafDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SproutPaperCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SproutLine.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPaywall?.invoke() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = SproutCitrus,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Free Plan: Info tab is open. Identify & Chemical tabs require Pro.",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontSize = 11.5.sp,
                                            color = SproutInkSoft,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                Text(
                                    text = "Upgrade",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SproutLeafDark
                                    )
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SproutPaperCard)
                        .border(1.3.dp, SproutLine.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    if (isFullVisibility) {
                        // All 3 tabs visible during 24-hr preview or with Pro
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            GuideTabButton(
                                selected = selectedTab == 0,
                                title = "Information",
                                subtitle = "Nutrients & Profile",
                                icon = Icons.Outlined.Eco,
                                isLocked = false,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedTab = 0 }
                            )
                            GuideTabButton(
                                selected = selectedTab == 1,
                                title = "Identify & Wash",
                                subtitle = "Ripeness & Hygiene",
                                icon = Icons.Outlined.WaterDrop,
                                isLocked = false,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedTab = 1 }
                            )
                            GuideTabButton(
                                selected = selectedTab == 2,
                                title = "Chemicals",
                                subtitle = "Treatments & Safety",
                                icon = Icons.Outlined.Shield,
                                isLocked = false,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedTab = 2 }
                            )
                        }
                    } else {
                        // After 24 hrs for free users: strictly only 1 tab is visible
                        GuideTabButton(
                            selected = true,
                            title = "Information",
                            subtitle = "Nutrients & Botanical Profile • 1 of 1 Tab Visible",
                            icon = Icons.Outlined.Eco,
                            isLocked = false,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedTab = 0 }
                        )
                    }
                }

                // If only 1 tab visible, display locked tab upsell banner
                if (!isFullVisibility) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SproutPaperCard,
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutCitrus.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPaywall?.invoke() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = SproutCitrus,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "2 Tabs Locked (Identify & Wash, Chemicals)",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SproutInk
                                        )
                                    )
                                }
                                Text(
                                    text = "24-hr preview complete. Start 14-Day Free Trial to unlock all tabs.",
                                    style = TextStyle(
                                        fontSize = 11.5.sp,
                                        color = SproutInkSoft
                                    )
                                )
                            }
                            Button(
                                onClick = { onOpenPaywall?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = SproutLeafDark),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SproutPaper)
                            }
                        }
                    }
                }
            }
        }

        // 4. ACTIVE TAB CONTENT
        item {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { width -> (width * 0.35f).toInt() } + fadeIn(animationSpec = tween(220)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(180)
                                ) { width -> -(width * 0.35f).toInt() } + fadeOut(animationSpec = tween(180))
                            )
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { width -> -(width * 0.35f).toInt() } + fadeIn(animationSpec = tween(220)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(180)
                                ) { width -> (width * 0.35f).toInt() } + fadeOut(animationSpec = tween(180))
                            )
                    }
                },
                label = "GuideTabContent"
            ) { tab ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (tab) {
                        0 -> BotanicalInformationTab(item = item)
                        1 -> {
                            if (isIdentifyLocked) {
                                GuideProLockedTabCard(
                                    tabName = "Freshness Identification & Wash Protocols",
                                    description = "Free plan includes 1 day of full content visibility. Your 24-hour preview period has concluded. Unlock Sprout Atlas Pro or start your 14-day free trial to view in-depth ripeness markers, mold vision guides, and lab washing recipes.",
                                    onOpenPaywall = onOpenPaywall
                                )
                            } else {
                                IdentifyAndWashTab(item = item)
                            }
                        }
                        2 -> {
                            if (isChemicalsLocked) {
                                GuideProLockedTabCard(
                                    tabName = "Chemical Treatments & Food Safety",
                                    description = "Free plan includes 1 day of full content visibility. Your 24-hour preview period has concluded. Unlock Sprout Atlas Pro or start your 14-day free trial to view post-harvest fungicides, wax treatments, and scientific residue profiles.",
                                    onOpenPaywall = onOpenPaywall
                                )
                            } else {
                                ChemicalsAndTreatmentsTab(item = item)
                            }
                        }
                    }
                }
            }
        }

        // 5. RELATED SPECIMENS CAROUSEL
        if (relatedItems.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Related Guides in ${item.category}",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInk
                        )
                    )

                    val relatedScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(relatedScroll),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        relatedItems.forEach { rel ->
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SproutPaperCard)
                                    .border(1.2.dp, SproutLine.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToGuide?.invoke(rel.id) }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ProduceDoodle(
                                        symbolId = rel.symbolId,
                                        archetype = rel.archetype,
                                        name = rel.name,
                                        size = 42.dp
                                    )
                                    Text(
                                        text = rel.name,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SproutInk,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        ),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. DAILY QUIZ INTERACTIVE CTA
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SproutInk)
                    .clickable { onOpenQuiz?.invoke() }
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SproutCitrus.copy(alpha = 0.18f))
                            .border(1.2.dp, SproutCitrus, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Quiz",
                            tint = SproutCitrus,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test your knowledge on ${item.name}?",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutPaper
                            )
                        )
                        Text(
                            text = "Solve today's 3-clue daily botanical quiz",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFFCDD6C4)
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: BOTANICAL INFORMATION & NUTRIENTS
// -----------------------------------------------------------------------------------------
@Composable
private fun BotanicalInformationTab(item: ProduceItem) {
    // 1. Botanical Overview & Quick Metrics
    DetailedSectionCard(
        title = "Botanical Overview & Classification",
        icon = Icons.Outlined.Eco,
        iconTint = SproutLeaf
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = item.characteristics.ifBlank { item.blurb },
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = SproutInk
                )
            )

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPillBox(
                    label = "WATER CONTENT",
                    value = item.waterContent.ifBlank { "85–92%" },
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    label = "GLYCEMIC INDEX",
                    value = item.glycemicIndex.ifBlank { "Low (GI < 45)" },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPillBox(
                    label = "PEAK SEASON",
                    value = item.season.ifBlank { "Spring & Summer" },
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    label = "ORIGIN / FAMILY",
                    value = item.origin.ifBlank { item.category },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 2. Key Vitamins & Micronutrient Powerhouse
    DetailedSectionCard(
        title = "Key Vitamins & Micronutrients",
        icon = Icons.Outlined.Bolt,
        iconTint = SproutCitrus
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Essential vitamins, minerals, and bioactive antioxidants present in this specimen:",
                style = TextStyle(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = SproutInkSoft
                )
            )

            val displayNutrients = if (item.nutrients.isNotEmpty()) item.nutrients
            else listOf("Vitamin C (Ascorbic Acid)", "Potassium", "Dietary Fiber", "Folate (B9)", "Beta-Carotene", "Polyphenols")

            displayNutrients.forEach { nut ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SproutLeaf.copy(alpha = 0.08f))
                        .border(1.dp, SproutLeaf.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SproutLeaf)
                    )
                    Text(
                        text = nut,
                        style = TextStyle(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SproutInk
                        )
                    )
                }
            }
        }
    }

    // 3. Botanical Facts Spotlight (Hooking Dark Card)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SproutInk)
            .border(1.2.dp, Color(0x35FAF6E9), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = SproutCitrus,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Did You Know? · Botanical Facts",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutPaper
                    )
                )
            }

            Text(
                text = item.botanicalFacts.ifBlank { item.trivia },
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFFE2EAD8)
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: FRESHNESS IDENTIFICATION & WASHING PROTOCOL
// -----------------------------------------------------------------------------------------
@Composable
private fun IdentifyAndWashTab(item: ProduceItem) {
    // 1. Freshness & Ripeness Identification
    DetailedSectionCard(
        title = "How to Identify & Pick at Peak Freshness",
        icon = Icons.Outlined.CheckCircle,
        iconTint = SproutLeaf
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Look for these sensory benchmarks when selecting in the market:",
                style = TextStyle(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = SproutInkSoft
                )
            )

            val cues = if (item.ripenessCues.isNotEmpty()) item.ripenessCues
            else listOf(
                "Firm and turgid texture without softness or pitting",
                "Rich, characteristic aroma near the stem or floral juncture",
                "Deep, uniform coloration without pale green premature patches",
                "Heavy in hand indicating rich juice and moisture content"
            )

            cues.forEachIndexed { index, cue ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SproutPaper)
                        .border(1.dp, SproutLine.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(SproutLeaf.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutLeafDark
                            )
                        )
                    }

                    Text(
                        text = cue,
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium,
                            color = SproutInk
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 2. Step-by-Step Cleaning & Washing Protocol
    DetailedSectionCard(
        title = "Cleaning & Washing Protocol",
        icon = Icons.Outlined.WaterDrop,
        iconTint = SproutLeafDark
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Proper mechanical friction and safe sanitization degrades surface residues while preserving delicate cellular structures:",
                style = TextStyle(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = SproutInkSoft
                )
            )

            val steps = if (item.cleaningSteps.isNotEmpty()) item.cleaningSteps
            else listOf(
                "Cold Water Friction: Rinse thoroughly under cold running tap water (15–20°C) while rubbing gently by hand.",
                "Sodium Bicarbonate Soak: Soak for 10–12 minutes in a 1% baking soda solution (1 tsp per 2 cups water) to hydrolyze pesticide residues.",
                "Final Potable Rinse: Flush with fresh running potable water to remove any dislodged particulates or residual alkaline solution.",
                "Dry Thoroughly: Pat completely dry with a clean lint-free cloth before slicing or storing to prevent bacterial colonization."
            )

            steps.forEachIndexed { idx, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SproutPaperCard)
                        .border(1.2.dp, SproutLeafDark.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SproutLeafDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Text(
                        text = step,
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = SproutInk
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 3. Storage & Shelf Life Card
    DetailedSectionCard(
        title = "Optimal Storage & Shelf-Life Extension",
        icon = Icons.Outlined.Inventory2,
        iconTint = SproutTomato
    ) {
        Text(
            text = item.storage.ifBlank { "Store in high-humidity refrigerator crisper drawer (4°C / 40°F). Keep unwashed until ready to prepare." },
            style = TextStyle(
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                color = SproutInk
            )
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 3: POST-HARVEST CHEMICAL TREATMENTS & SAFETY
// -----------------------------------------------------------------------------------------
@Composable
private fun ChemicalsAndTreatmentsTab(item: ProduceItem) {
    // 1. Post-Harvest Treatments & Chemical Usage
    DetailedSectionCard(
        title = "Chemicals & Post-Harvest Treatments",
        icon = Icons.Outlined.Science,
        iconTint = SproutTomato
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Commercial produce is frequently treated during post-harvest sorting, transit, and storage:",
                style = TextStyle(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = SproutInkSoft
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SproutTomato.copy(alpha = 0.08f))
                    .border(1.2.dp, SproutTomato.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = item.treatment.ifBlank { item.treatments },
                    style = TextStyle(
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = SproutInk
                    )
                )
            }
        }
    }

    // 2. Residue Profile & Food Safety Breakdown
    DetailedSectionCard(
        title = "Residue Profile & Removal Efficiency",
        icon = Icons.Outlined.Shield,
        iconTint = SproutLeafDark
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SafetyGuidelineRow(
                title = "Wax Coatings",
                detail = "Food-grade carnauba, shellac, or morpholine wax applied to prevent moisture evaporation. Traps pesticides underneath."
            )
            SafetyGuidelineRow(
                title = "Fungicides & Sprout Inhibitors",
                detail = "Applied in packing houses (e.g. thiabendazole, imazalil, chlorpropham). Baking soda washes degrade up to 80% of surface residue."
            )
            SafetyGuidelineRow(
                title = "Peel vs. Wash Recommendation",
                detail = "If non-organic and consuming skin, perform an alkaline soak. For thick-skinned varieties, peeling removes >95% of hydrophobic residues."
            )
        }
    }

    // 3. Certified Botanical Caution Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SproutPaperCard)
            .border(1.3.dp, SproutTomato.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.HealthAndSafety,
                contentDescription = null,
                tint = SproutTomato,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Consumer Safety Best Practice",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutTomato
                    )
                )
                Text(
                    text = "Always clean cutting boards, knives, and hands after trimming commercial stems to avoid cross-contaminating interior flesh with surface residues.",
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = SproutInk
                    )
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// HELPER UI COMPONENTS
// -----------------------------------------------------------------------------------------
@Composable
private fun GuideTabButton(
    selected: Boolean,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SproutLeaf else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) Color.White else if (isLocked) SproutCitrus else SproutInkSoft,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else SproutInk,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = if (isLocked) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color.White.copy(alpha = 0.85f) else if (isLocked) SproutCitrus else SproutInkSoft,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GuideProLockedTabCard(
    tabName: String,
    description: String,
    onOpenPaywall: (() -> Unit)? = null
) {
    val lockPulseAnim = rememberInfiniteTransition(label = "LockPulse")
    val lockScale by lockPulseAnim.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LockScale"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFAFAF7),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4E6D2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .graphicsLayer {
                        scaleX = lockScale
                        scaleY = lockScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9).copy(alpha = 0.5f))
                        )
                    )
                    .border(2.dp, Color(0xFF81C784), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "PRO BOTANICAL GUIDE",
                            style = TextStyle(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF1B5E20)
                            )
                        )
                    }
                }

                Text(
                    text = tabName,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 19.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = SproutInkSoft,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Fresh benefits checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFFE0ECE0), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Peak ripeness visual indicators & spoilage marks",
                        fontSize = 12.5.sp,
                        color = SproutInk,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Scientific washing protocols & dilution ratios",
                        fontSize = 12.5.sp,
                        color = SproutInk,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Chemical coatings, fungicides & pesticide breakdown",
                        fontSize = 12.5.sp,
                        color = SproutInk,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = { onOpenPaywall?.invoke() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(100.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1B5E20),
                                Color(0xFF2E7D32),
                                Color(0xFF388E3C)
                            )
                        ),
                        RoundedCornerShape(100.dp)
                    )
                    .testTag("btn_unlock_guide_pro")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlock Pro • Start 14-Day Free Trial",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SproutPaperCard)
            .border(1.3.dp, SproutLine.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk
                    )
                )
            }
            content()
        }
    }
}

@Composable
private fun MetricPillBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SproutPaper)
            .border(1.dp, SproutLine.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutLeafDark
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SproutInk
                )
            )
        }
    }
}

@Composable
private fun SafetyGuidelineRow(
    title: String,
    detail: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SproutPaper)
            .border(1.dp, SproutLine.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SproutInk
            )
        )
        Text(
            text = detail,
            style = TextStyle(
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = SproutInkSoft
            )
        )
    }
}

@Composable
private fun PillBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(100.dp))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}
