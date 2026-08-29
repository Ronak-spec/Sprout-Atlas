package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datasource.ProduceCatalog
import com.example.data.model.ConfidenceLevel
import com.example.data.model.ProduceItem
import com.example.data.model.ScanResult
import com.example.data.repository.SproutAtlasRepository
import com.example.ui.components.EyebrowHeader
import com.example.ui.components.ProduceDoodle
import com.example.ui.theme.*

@Composable
fun ViewScreen(
    repository: SproutAtlasRepository,
    onNavigateToGuide: (Int) -> Unit,
    onNavigateToExplore: () -> Unit
) {
    var activeTab by remember { mutableStateOf("saved") } // "saved", "scans", "progress"

    val savedItemIds by repository.savedItemIds.collectAsState()
    val scanHistory by repository.scanHistory.collectAsState()
    val quizStreak by repository.quizStreak.collectAsState()
    val rainbowStreak by repository.rainbowStreak.collectAsState()
    val userName by repository.userName.collectAsState()
    val userTitle by repository.userTitle.collectAsState()
    val userAvatarSymbol by repository.userAvatarSymbol.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }

    val savedItems = remember(savedItemIds) {
        ProduceCatalog.allItems.filter { savedItemIds.contains(it.id) }
    }

    if (showProfileDialog) {
        BotanistProfileDialog(
            currentName = userName,
            currentTitle = userTitle,
            currentAvatar = userAvatarSymbol,
            savedCount = savedItems.size,
            scansCount = scanHistory.size,
            quizStreak = quizStreak,
            onDismiss = { showProfileDialog = false },
            onSaveProfile = { newName, newTitle, newAvatar ->
                repository.updateProfile(newName, newTitle, newAvatar)
                showProfileDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutPaper)
            .testTag("view_screen")
    ) {
        // TOP BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SproutPaper.copy(alpha = 0.96f))
                .border(width = 1.3.dp, color = SproutLine.copy(alpha = 0.1f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    EyebrowHeader(text = "Your atlas")
                    Text(
                        text = "View",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInk
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Dynamic Profile Chip / Botanist ID
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(SproutPaperCard)
                        .border(1.4.dp, SproutInk, RoundedCornerShape(100.dp))
                        .clickable { showProfileDialog = true }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("user_profile_chip"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SproutLeafLight)
                            .border(1.dp, SproutLeaf, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = repository.getUserMonogram(userName),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutLeafDark
                            )
                        )
                    }
                    Text(
                        text = userName.split(" ").firstOrNull()?.take(10) ?: "Profile",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInk
                        ),
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Profile",
                        tint = SproutInkSoft,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // SEGMENTED TAB STRIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SproutPaperCard)
                    .border(1.3.dp, SproutLine.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ViewSegmentTab(
                    title = "Saved",
                    icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selected = activeTab == "saved",
                    onClick = { activeTab = "saved" },
                    modifier = Modifier.weight(1f)
                )
                ViewSegmentTab(
                    title = "Scan History",
                    icon = { Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selected = activeTab == "scans",
                    onClick = { activeTab = "scans" },
                    modifier = Modifier.weight(1f)
                )
                ViewSegmentTab(
                    title = "Progress",
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selected = activeTab == "progress",
                    onClick = { activeTab = "progress" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CONTENT PANELS
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            when (activeTab) {
                "saved" -> {
                    SavedGuidesPanel(
                        savedItems = savedItems,
                        onUnsave = { repository.toggleSave(it) },
                        onNavigateToGuide = onNavigateToGuide
                    )
                }
                "scans" -> {
                    ScanHistoryPanel(
                        scanHistory = scanHistory,
                        onNavigateToGuide = onNavigateToGuide
                    )
                }
                "progress" -> {
                    ProgressPanel(
                        quizStreak = quizStreak,
                        rainbowStreak = rainbowStreak
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewSegmentTab(
    title: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SproutInk else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) SproutCitrus else SproutInkSoft
            ) {
                icon()
            }
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) SproutPaper else SproutInkSoft,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SavedGuidesPanel(
    savedItems: List<ProduceItem>,
    onUnsave: (Int) -> Unit,
    onNavigateToGuide: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp, bottom = 24.dp)
    ) {
        // Result row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${savedItems.size} saved",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutInk
                )
            )

            Text(
                text = "Recently added ▾",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    color = SproutInkSoft
                )
            )
        }

        if (savedItems.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProduceDoodle(
                        symbolId = "d-sprout",
                        archetype = "leafy",
                        name = "Sprout",
                        size = 52.dp
                    )
                    Text(
                        text = "Nothing saved yet — tap the star on any guide.",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = SproutInkSoft,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        } else {
            // 2-column saved grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(savedItems, key = { it.id }) { item ->
                    val tagText = item.category.split(" ").firstOrNull() ?: item.category
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SproutPaperCard)
                            .border(1.3.dp, SproutLine.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                            .clickable { onNavigateToGuide(item.id) }
                            .padding(13.dp)
                            .testTag("saved_card_${item.id}")
                    ) {
                        // Star button in top-left
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(SproutPaper)
                                .border(1.2.dp, SproutLine.copy(alpha = 0.2f), CircleShape)
                                .clickable { onUnsave(item.id) }
                                .testTag("unsave_btn_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Unsave",
                                tint = SproutCitrus,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Category tag in top-right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(100.dp))
                                .background(SproutPaper)
                                .border(1.dp, SproutLine.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tagText,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = SproutInkSoft
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            ProduceDoodle(
                                symbolId = item.symbolId,
                                archetype = item.archetype,
                                name = item.name,
                                size = 34.dp
                            )

                            Text(
                                text = item.name,
                                style = TextStyle(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SproutInk
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = item.blurb,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = SproutInkSoft,
                                    lineHeight = 14.sp
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanHistoryPanel(
    scanHistory: List<ScanResult>,
    onNavigateToGuide: (Int) -> Unit
) {
    if (scanHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProduceDoodle(
                    symbolId = "d-sprout",
                    archetype = "leafy",
                    name = "Sprout",
                    size = 52.dp
                )
                Text(
                    text = "No scan history yet — use the Camera scanner in Labs to identify fresh produce.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SproutInkSoft,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        val groupedScans = remember(scanHistory) {
            scanHistory.groupBy { it.dateGroup }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            groupedScans.forEach { (dateLabel, items) ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = dateLabel.uppercase(),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInkSoft
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEach { scan ->
                                val matchedItem = ProduceCatalog.allItems.find {
                                    it.name.contains(scan.produceName, ignoreCase = true) || scan.produceName.contains(it.name, ignoreCase = true)
                                }

                                val confBg = when (scan.confidence) {
                                    ConfidenceLevel.HIGH -> Color(0xFFE4F0E0)
                                    ConfidenceLevel.MEDIUM -> Color(0xFFF7E9CF)
                                    ConfidenceLevel.LOW -> Color(0xFFF5E6DA)
                                }

                                val confTextColor = when (scan.confidence) {
                                    ConfidenceLevel.HIGH -> SproutLeafDark
                                    ConfidenceLevel.MEDIUM -> Color(0xFF8A5A10)
                                    ConfidenceLevel.LOW -> Color(0xFF8A4A1E)
                                }

                                val confLabel = when (scan.confidence) {
                                    ConfidenceLevel.HIGH -> "Likely: ${scan.produceName}"
                                    ConfidenceLevel.MEDIUM -> "Possibly: ${scan.produceName}"
                                    ConfidenceLevel.LOW -> "Low confidence"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SproutPaperCard)
                                        .border(1.3.dp, SproutLine.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                        .clickable(enabled = matchedItem != null) {
                                            matchedItem?.let { onNavigateToGuide(it.id) }
                                        }
                                        .padding(10.dp, 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                                ) {
                                    // Scan icon
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(SproutPaper)
                                            .border(1.dp, SproutLine.copy(alpha = 0.15f), RoundedCornerShape(11.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (matchedItem != null) {
                                            ProduceDoodle(
                                                symbolId = matchedItem.symbolId,
                                                archetype = matchedItem.archetype,
                                                name = matchedItem.name,
                                                size = 24.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.PhotoCamera,
                                                contentDescription = null,
                                                tint = SproutInkSoft,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Scan info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = scan.produceName,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SproutInk
                                            )
                                        )
                                        Text(
                                            text = scan.time,
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = SproutInkSoft
                                            ),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Confidence badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(confBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = confLabel,
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = confTextColor
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
}

@Composable
private fun ProgressPanel(
    quizStreak: Int,
    rainbowStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // STAT ROW (QUIZ & RAINBOW STREAK CARDS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quiz Streak Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SproutInk)
                    .border(1.4.dp, Color(0x33FAF6E9), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.Whatshot,
                        contentDescription = "Quiz Streak",
                        tint = SproutTomato,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$quizStreak",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutTomato
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "day quiz streak",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFCDD6C4)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Rainbow Streak Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SproutInk)
                    .border(1.4.dp, Color(0x33FAF6E9), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Rainbow Plate Streak",
                        tint = SproutCitrus,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$rainbowStreak",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutCitrus
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "day rainbow streak",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFCDD6C4)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // 7-DAY ACTIVITY HEATMAP CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SproutPaperCard)
                .border(1.3.dp, SproutLine.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "Activity tracking",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SproutInk
                    )
                )

                val hasAnyActivity = quizStreak > 0 || rainbowStreak > 0
                val heatData = if (hasAnyActivity) {
                    listOf(
                        "Sun" to 0.1f,
                        "Mon" to 0.2f,
                        "Tue" to 0.3f,
                        "Wed" to 0.4f,
                        "Thu" to 0.5f,
                        "Fri" to 0.6f,
                        "Today" to 0.8f
                    )
                } else {
                    listOf(
                        "Sun" to 0.0f,
                        "Mon" to 0.0f,
                        "Tue" to 0.0f,
                        "Wed" to 0.0f,
                        "Thu" to 0.0f,
                        "Fri" to 0.0f,
                        "Sat" to 0.0f
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    heatData.forEach { (day, fraction) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(SproutInk.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (fraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fraction)
                                            .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                            .background(SproutLeaf.copy(alpha = 0.35f + fraction * 0.65f))
                                    )
                                }
                            }

                            Text(
                                text = day.uppercase(),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = SproutInkSoft
                                )
                            )
                        }
                    }
                }
            }
        }

        // STREAK FOOTER
        Text(
            text = "Best quiz streak: $quizStreak days · Best rainbow streak: $rainbowStreak days",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                color = SproutInkSoft,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun BotanistProfileDialog(
    currentName: String,
    currentTitle: String,
    currentAvatar: String,
    savedCount: Int,
    scansCount: Int,
    quizStreak: Int,
    onDismiss: () -> Unit,
    onSaveProfile: (name: String, title: String, avatar: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var titleInput by remember { mutableStateOf(currentTitle) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }

    val liveMonogram = remember(nameInput) {
        val parts = nameInput.trim().split(" ").filter { it.isNotEmpty() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.size == 1 -> "${parts[0].first().uppercaseChar()}"
            else -> "AV"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SproutPaperCard),
            border = androidx.compose.foundation.BorderStroke(1.4.dp, SproutLine.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("botanist_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SproutLeafLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedAvatar, fontSize = 15.sp)
                        }
                        Column {
                            Text(
                                text = "Field Botanist Profile",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SproutInk
                                )
                            )
                            Text(
                                text = "Your atlas explorer identification",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = SproutInkSoft
                                )
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SproutInkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Monogram & Avatar Preview Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SproutPaper)
                        .border(1.dp, SproutLine.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SproutLeafLight)
                            .border(1.6.dp, SproutLeaf, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = liveMonogram,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutLeafDark
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nameInput.isNotBlank()) nameInput else "Field Botanist",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInk
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = if (titleInput.isNotBlank()) titleInput else "Botanist & Forager",
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                color = SproutInkSoft
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Monogram: “$liveMonogram” (Shown in View header)",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                color = SproutLeafDark
                            )
                        )
                    }
                }

                // Name Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "YOUR NAME / INITIALS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInkSoft
                        )
                    )
                    BasicTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SproutPaper)
                            .border(1.2.dp, SproutInk.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("profile_name_input"),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SproutInk
                        ),
                        cursorBrush = SolidColor(SproutInk),
                        decorationBox = { inner ->
                            if (nameInput.isEmpty()) {
                                Text("Enter your name...", color = SproutInkMuted, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }

                // Title Input + Presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "FIELD TITLE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInkSoft
                        )
                    )
                    BasicTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SproutPaper)
                            .border(1.2.dp, SproutInk.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("profile_title_input"),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            color = SproutInk
                        ),
                        cursorBrush = SolidColor(SproutInk)
                    )

                    // Quick Title preset chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Botanist", "Forager", "Nutritionist", "Agronomist").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (titleInput.contains(preset)) SproutLeafLight else SproutPaper)
                                    .border(1.dp, SproutLine.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                    .clickable { titleInput = "Field $preset" }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = preset,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SproutInk
                                    )
                                )
                            }
                        }
                    }
                }

                // Avatar Emblem Choice
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "BOTANICAL EMBLEM",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInkSoft
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("🌿", "🔬", "🌻", "🥑", "🍎", "🍓", "🍄").forEach { sym ->
                            val isSelected = selectedAvatar == sym
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SproutLeafLight else SproutPaper)
                                    .border(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) SproutLeaf else SproutLine.copy(alpha = 0.25f),
                                        CircleShape
                                    )
                                    .clickable { selectedAvatar = sym },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = sym, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Stats overview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SproutPaper)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$savedCount",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SproutInk)
                        )
                        Text(
                            text = "Saved",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = SproutInkSoft)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$scansCount",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SproutInk)
                        )
                        Text(
                            text = "Scanned",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = SproutInkSoft)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$quizStreak d",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SproutTomato)
                        )
                        Text(
                            text = "Quiz Streak",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = SproutInkSoft)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SproutInk)
                    ) {
                        Text("Cancel", fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            onSaveProfile(nameInput, titleInput, selectedAvatar)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_profile_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SproutLeaf,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
