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
                .background(SproutPaper.copy(alpha = 0.96f))
                .border(width = 1.3.dp, color = SproutLine.copy(alpha = 0.1f))
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 16.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SproutPaperCard)
                                    .border(1.3.dp, SproutLine.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .clickable { visibleLimit += 20 }
                                    .padding(vertical = 14.dp)
                                    .testTag("load_more_guides_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Load $toLoad more guides ↓",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Default,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SproutPaperCard)
            .border(1.3.dp, SproutLine.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("explore_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Enriched & Enlarged fruit/vegetable icon
                ProduceDoodle(
                    symbolId = item.symbolId,
                    archetype = item.archetype,
                    name = item.name,
                    size = 72.dp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SproutLeaf.copy(alpha = 0.12f))
                        .border(1.dp, SproutLeaf.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tagText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutLeafDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

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
                    fontSize = 12.5.sp,
                    color = SproutInkSoft,
                    lineHeight = 17.sp
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
                        fontWeight = FontWeight.Medium,
                        color = SproutLeafDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
