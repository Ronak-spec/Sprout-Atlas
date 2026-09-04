package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.ProduceCatalog
import com.example.data.model.ProduceItem
import com.example.data.repository.SproutAtlasRepository
import com.example.ui.components.EyebrowHeader
import com.example.ui.components.FilterChipScroll
import com.example.ui.components.ProduceDoodle
import com.example.ui.components.SearchPacket
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    repository: SproutAtlasRepository,
    initialCategory: String? = null,
    initialQuery: String? = null,
    onNavigateToGuide: (Int) -> Unit
) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery ?: "") }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory ?: "All") }
    var visibleLimit by remember { mutableStateOf(40) }

    LaunchedEffect(initialQuery) {
        if (initialQuery != null) {
            query = initialQuery
        }
    }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            selectedCategory = initialCategory
        }
    }

    // Reset pagination when filter or query changes
    LaunchedEffect(query, selectedCategory) {
        visibleLimit = 40
    }

    val allMatchedItems = remember(query, selectedCategory) {
        val baseList = if (query.isBlank()) {
            if (selectedCategory == "All") ProduceCatalog.jumbledAllItems
            else ProduceCatalog.getByCategory(selectedCategory)
        } else {
            val searched = ProduceCatalog.searchFuzzy(query)
            if (selectedCategory == "All") searched
            else searched.filter { it.category == selectedCategory }
        }
        baseList
    }

    val visibleItems = remember(allMatchedItems, visibleLimit) {
        allMatchedItems.take(visibleLimit)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutPaper)
            .testTag("explore_screen")
    ) {
        // TOP STICKY BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SproutPaperCard)
                .border(
                    width = 1.2.dp,
                    color = SproutLine.copy(alpha = 0.35f),
                    shape = androidx.compose.ui.graphics.RectangleShape
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    EyebrowHeader(text = "${ProduceCatalog.allItems.size} guides")
                    Text(
                        text = "Explore",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = SproutInk
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // SEARCH PACKET
            SearchPacket(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search — typos okay",
                showTypoBadge = false
            )

            // CATEGORY FILTER CHIPS
            FilterChipScroll(
                categories = ProduceCatalog.categories,
                activeCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
        }

        // CONTENT PAD
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // RESULT COUNT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${allMatchedItems.size} guides found",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SproutInkSoft
                    )
                )
            }

            if (allMatchedItems.isEmpty()) {
                // EMPTY STATE
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProduceDoodle(
                            symbolId = "d-sprout",
                            archetype = "leafy",
                            name = "Empty",
                            size = 64.dp
                        )
                        Text(
                            text = "No guides match that search.\nTry a different spelling — typos are okay.",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center,
                                color = SproutInkSoft
                            )
                        )
                    }
                }
            } else {
                // 2-COLUMN GRID
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleItems, key = { it.id }) { item ->
                        ExploreGridCard(
                            item = item,
                            onClick = { onNavigateToGuide(item.id) }
                        )
                    }

                    // LOAD MORE BUTTON
                    if (visibleLimit < allMatchedItems.size) {
                        val remaining = allMatchedItems.size - visibleLimit
                        val toLoad = minOf(20, remaining)
                        item(span = { GridItemSpan(2) }) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 16.dp)
                                    .clickable { visibleLimit += 20 }
                                    .testTag("load_more_guides_btn"),
                                shape = RoundedCornerShape(100.dp),
                                color = SproutPaperCard,
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine.copy(alpha = 0.35f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Load $toLoad more guides ↓",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SproutLeaf
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

@Composable
private fun ExploreGridCard(
    item: ProduceItem,
    onClick: () -> Unit
) {
    val tagText = item.category.split(" ").firstOrNull() ?: item.category
    val shortBlurb = remember(item.blurb) {
        val firstSentence = item.blurb.split(".").firstOrNull()?.trim() ?: item.blurb
        firstSentence.split(";").firstOrNull()?.trim() ?: firstSentence
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("explore_card_${item.id}"),
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
                        .size(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProduceDoodle(
                        symbolId = item.symbolId,
                        archetype = item.archetype,
                        name = item.name,
                        size = 56.dp
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
                        text = tagText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 10.sp,
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
                    fontSize = 16.sp,
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

            if (item.nutrients.isNotEmpty()) {
                Text(
                    text = "🌱 " + item.nutrients.take(2).joinToString(", "),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SproutLeaf
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
