package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.ProduceCatalog
import com.example.ui.theme.*

@Composable
fun EyebrowHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SproutLeafDark
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(1.5.dp)
                .background(color)
        )
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = color
            )
        )
    }
}

@Composable
fun SearchPacket(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search — typos okay",
    modifier: Modifier = Modifier,
    showTypoBadge: Boolean = false,
    isLarge: Boolean = false
) {
    val horizontalPad = if (isLarge) 16.dp else 14.dp
    val verticalPad = if (isLarge) 14.dp else 10.dp
    val cornerRadius = if (isLarge) 16.dp else 14.dp
    val iconSize = if (isLarge) 22.dp else 18.dp
    val textSize = if (isLarge) 16.sp else 14.sp
    val minHeight = if (isLarge) 54.dp else 44.dp

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(cornerRadius))
                .background(SproutPaperCard)
                .border(if (isLarge) 1.6.dp else 1.4.dp, SproutInk, RoundedCornerShape(cornerRadius))
                .padding(horizontal = horizontalPad, vertical = verticalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isLarge) 12.dp else 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SproutInkSoft,
                modifier = Modifier.size(iconSize)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input"),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = textSize,
                    color = SproutInk,
                    fontFamily = FontFamily.Default
                ),
                cursorBrush = SolidColor(SproutInk),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = SproutInkMuted,
                            fontSize = textSize
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(SproutPaper)
                        .border(1.dp, SproutInk.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                        .clickable { onQueryChange("") }
                        .padding(horizontal = if (isLarge) 10.dp else 8.dp, vertical = if (isLarge) 5.dp else 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "clear",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = if (isLarge) 11.sp else 10.sp,
                            color = SproutInkSoft
                        )
                    )
                }
            }
        }

        if (showTypoBadge) {
            Box(
                modifier = Modifier
                    .offset(x = 18.dp, y = if (isLarge) (-12).dp else (-11).dp)
                    .rotate(-3f)
                    .clip(RoundedCornerShape(100.dp))
                    .background(SproutCitrus)
                    .border(1.2.dp, SproutInk, RoundedCornerShape(100.dp))
                    .padding(horizontal = 9.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = "even with typos ✓",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isLarge) 10.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutInk
                    )
                )
            }
        }
    }
}

@Composable
fun FilterChipScroll(
    categories: List<String>,
    activeCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { cat ->
            val isActive = cat == activeCategory
            val dotColor = Color(ProduceCatalog.categoryColors[cat] ?: 0xFF233022)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isActive) SproutInk else SproutPaperCard)
                    .border(
                        1.2.dp,
                        if (isActive) SproutInk else SproutInk.copy(alpha = 0.22f),
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { onCategorySelected(cat) }
                    .padding(horizontal = 13.dp, vertical = 7.dp)
                    .testTag("filter_chip_$cat"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (cat == "All" && isActive) SproutCitrus else dotColor)
                )
                Text(
                    text = cat,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) SproutPaper else SproutInk
                    )
                )
            }
        }
    }
}

@Composable
fun NutrientChip(
    nutrient: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(SproutLeafLight)
            .border(1.dp, SproutLeaf.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = nutrient,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SproutLeafDark
            )
        )
    }
}

@Composable
fun CheckListItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SproutLeaf,
            modifier = Modifier
                .size(16.dp)
                .offset(y = 2.dp)
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = SproutInk
            )
        )
    }
}

@Composable
fun StepListItem(
    number: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(SproutInk)
                .offset(y = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutPaper
                )
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = SproutInk
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CorkboardPin(
    symbolId: String?,
    name: String,
    archetype: String,
    rotation: Float,
    tapeRotation: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 86.dp,
    pinColor: Long = 0xFFD6482F,
    pinStyle: String = "PUSHPIN",
    noteBadge: String? = null
) {
    Box(
        modifier = modifier
            .rotate(rotation)
            .clickable { onClick() }
            .padding(top = 11.dp, bottom = 6.dp, start = 3.dp, end = 3.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Specimen Polaroid / Botanical Slide Card with authentic photo paper feel & drop shadow
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 3.5.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E0D2)),
            modifier = Modifier
                .widthIn(min = size + 16.dp, max = size + 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp, start = 6.dp, end = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Photo display frame
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(0xFFF9FAF8), Color(0xFFF0F5EE))
                            )
                        )
                        .border(0.8.dp, Color(0xFFE0E8DC), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ProduceDoodle(
                        symbolId = symbolId,
                        archetype = archetype,
                        name = name,
                        size = size - 2.dp
                    )
                }

                // Specimen Title Label (clean, elegant, bold)
                Text(
                    text = name,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2523),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        // Fastening: 3D Glossy Pushpin, Brass Tack, or Translucent Washi Tape with Drop Shadows
        when (pinStyle) {
            "WASHI_TAPE" -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-7).dp)
                        .width(46.dp)
                        .height(15.dp)
                        .rotate(tapeRotation)
                        .shadow(1.5.dp, RoundedCornerShape(2.dp))
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xE6E8D68A))
                        .border(0.6.dp, Color(0xFFD4C175), RoundedCornerShape(2.dp))
                )
            }
            "BRASS_TACK" -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-6).dp)
                        .shadow(2.dp, CircleShape)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF7C2), Color(0xFFE6C158), Color(0xFF997A24))
                            )
                        )
                        .border(1.dp, Color(0xFF6B5416), CircleShape)
                )
            }
            else -> {
                // Realistic 3D Round Pushpin with shine and shadow
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-7).dp)
                        .shadow(3.dp, CircleShape)
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    Color(pinColor),
                                    Color(pinColor).copy(alpha = 0.9f),
                                    Color(0xFF1F1F1F).copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}

@Composable
fun SproutBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = SproutPaper.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.3.dp, SproutLine.copy(alpha = 0.25f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                title = "Pulse",
                selected = currentTab == "home",
                icon = { Icon(Icons.Outlined.GraphicEq, contentDescription = "Pulse", modifier = Modifier.size(28.dp)) },
                onClick = { onTabSelected("home") }
            )
            BottomTabItem(
                title = "Explore",
                selected = currentTab == "explore",
                icon = { Icon(Icons.Outlined.TravelExplore, contentDescription = "Explore", modifier = Modifier.size(28.dp)) },
                onClick = { onTabSelected("explore") }
            )
            BottomTabItem(
                title = "Labs",
                selected = currentTab == "labs",
                icon = { Icon(Icons.Outlined.Science, contentDescription = "Labs", modifier = Modifier.size(28.dp)) },
                onClick = { onTabSelected("labs") }
            )
            BottomTabItem(
                title = "View",
                selected = currentTab == "view",
                icon = { Icon(Icons.Outlined.Visibility, contentDescription = "View", modifier = Modifier.size(28.dp)) },
                onClick = { onTabSelected("view") }
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    title: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("tab_$title"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides if (selected) SproutLeaf else SproutInkSoft) {
            icon()
        }
        Text(
            text = title,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) SproutLeafDark else SproutInkSoft
            )
        )
    }
}
