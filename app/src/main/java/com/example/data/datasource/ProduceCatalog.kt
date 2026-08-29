package com.example.data.datasource

import com.example.data.model.ProduceItem

object ProduceCatalog {

    val categoryColors = mapOf(
        "All" to 0xFF233022,
        "Tropical Fruits" to 0xFFE79B1F,
        "Citrus Fruits" to 0xFFE79B1F,
        "Melons" to 0xFF5A9E4A,
        "Berries & Small Fruits" to 0xFF6E3B6E,
        "Stone Fruits (Drupes)" to 0xFFF3A35C,
        "Pome Fruits" to 0xFFC3D24E,
        "Grapes & Vine Fruits" to 0xFF6E3B6E,
        "Exotic & Specialty Fruits" to 0xFFD6428F,
        "Leafy Greens" to 0xFF3F7D4C,
        "Cruciferous Vegetables" to 0xFFA8C85A,
        "Alliums" to 0xFFDCD3AE,
        "Root & Tuber Vegetables" to 0xFFB1552F,
        "Nightshades" to 0xFFD6482F,
        "Gourds & Squashes" to 0xFFE0A95E,
        "Podded Vegetables & Legumes" to 0xFF6A9E42,
        "Stems & Shoots" to 0xFF7FAE4C,
        "Edible Culinary Herbs" to 0xFF4C7A3E,
        "Culinary Mushrooms" to 0xFFC9A06A,
        "Sea Vegetables" to 0xFF2C4A34
    )

    val allCategories = listOf("All") + categoryColors.keys.filter { it != "All" }
    val categories: List<String> = allCategories

    val allItems: List<ProduceItem> by lazy {
        CatalogChunk1.items +
        CatalogChunk2.items +
        CatalogChunk3.items +
        CatalogChunk4.items +
        CatalogChunk5.items
    }

    /**
     * Jumbled & balanced view across all 19 botanical categories.
     * Round-robins across categories so user sees an inspiring diverse assortment
     * rather than a single category block first.
     */
    val jumbledAllItems: List<ProduceItem> by lazy {
        val categoryOrder = categoryColors.keys.filter { it != "All" }
        val categoryBuckets = categoryOrder.associateWith { cat ->
            allItems.filter { it.category == cat }.toMutableList()
        }

        val result = mutableListOf<ProduceItem>()
        var hasMore = true
        while (hasMore) {
            hasMore = false
            for (cat in categoryOrder) {
                val bucket = categoryBuckets[cat]
                if (bucket != null && bucket.isNotEmpty()) {
                    result.add(bucket.removeAt(0))
                    hasMore = true
                }
            }
        }
        // Fallback in case any item was in an unlisted category
        val accountedIds = result.map { it.id }.toSet()
        val remaining = allItems.filter { !accountedIds.contains(it.id) }
        result + remaining
    }

    fun getById(id: Int): ProduceItem? {
        return allItems.find { it.id == id }
    }

    fun getByCategory(category: String): List<ProduceItem> {
        return if (category == "All") allItems else allItems.filter { it.category == category }
    }

    fun getRelated(item: ProduceItem, count: Int = 3): List<ProduceItem> {
        return allItems.filter { it.category == item.category && it.id != item.id }.take(count)
    }

    // Levenshtein fuzzy matching matching the exact algorithm in the design
    fun searchFuzzy(query: String, category: String = "All"): List<ProduceItem> {
        val q = query.lowercase().filter { it.isLetterOrDigit() || it == ' ' }.trim()
        val filteredByCat = if (category == "All") allItems else allItems.filter { it.category == category }
        if (q.isEmpty()) return filteredByCat

        return filteredByCat.filter { item ->
            val n = item.name.lowercase().filter { it.isLetterOrDigit() || it == ' ' }.trim()
            if (n.contains(q)) return@filter true
            val words = n.split(" ")
            val threshold = when {
                q.length <= 4 -> 1
                q.length <= 7 -> 2
                else -> 3
            }
            words.any { w -> levenshtein(q, w) <= threshold }
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        if (m == 0) return n
        if (n == 0) return m
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[m][n]
    }
}
