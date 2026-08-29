package com.example.data.repository

import com.example.data.datasource.ProduceCatalog
import com.example.data.model.ConfidenceLevel
import com.example.data.model.ProduceItem
import com.example.data.model.QuizQuestion
import com.example.data.model.RainbowColor
import com.example.data.model.RainbowLogEntry
import com.example.data.model.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SproutAtlasRepository(private val context: android.content.Context? = null) {

    private val prefs = context?.getSharedPreferences("sprout_atlas_prefs", android.content.Context.MODE_PRIVATE)

    // Quick Tour
    private val _hasSeenQuickTour = MutableStateFlow(prefs?.getBoolean("has_seen_quick_tour", false) ?: false)
    val hasSeenQuickTour: StateFlow<Boolean> = _hasSeenQuickTour.asStateFlow()

    fun setHasSeenQuickTour(seen: Boolean) {
        _hasSeenQuickTour.value = seen
        prefs?.edit()?.putBoolean("has_seen_quick_tour", seen)?.apply()
    }

    // User Profile
    private val _userName = MutableStateFlow(prefs?.getString("user_name", "Alex Vance") ?: "Alex Vance")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userTitle = MutableStateFlow(prefs?.getString("user_title", "Field Botanist & Forager") ?: "Field Botanist & Forager")
    val userTitle: StateFlow<String> = _userTitle.asStateFlow()

    private val _userAvatarSymbol = MutableStateFlow(prefs?.getString("user_avatar_symbol", "🌿") ?: "🌿")
    val userAvatarSymbol: StateFlow<String> = _userAvatarSymbol.asStateFlow()

    fun updateProfile(name: String, title: String, avatarSymbol: String = "🌿") {
        val trimmedName = name.trim().ifEmpty { "Field Botanist" }
        val trimmedTitle = title.trim().ifEmpty { "Botanist & Forager" }
        _userName.value = trimmedName
        _userTitle.value = trimmedTitle
        _userAvatarSymbol.value = avatarSymbol
        prefs?.edit()
            ?.putString("user_name", trimmedName)
            ?.putString("user_title", trimmedTitle)
            ?.putString("user_avatar_symbol", avatarSymbol)
            ?.apply()
    }

    fun getUserMonogram(name: String = _userName.value): String {
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.size == 1 -> "${parts[0].first().uppercaseChar()}"
            else -> "AV"
        }
    }

    // Corkboard shuffle seed
    private val _corkboardShuffleCount = MutableStateFlow(0)
    val corkboardShuffleCount: StateFlow<Int> = _corkboardShuffleCount.asStateFlow()

    fun shuffleCorkboard() {
        _corkboardShuffleCount.update { it + 1 }
    }

    // Saved guides IDs
    private val _savedItemIds = MutableStateFlow<Set<Int>>(
        prefs?.getStringSet("saved_item_ids", emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()
    )
    val savedItemIds: StateFlow<Set<Int>> = _savedItemIds.asStateFlow()

    // Scan history
    private val _scanHistory = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanHistory: StateFlow<List<ScanResult>> = _scanHistory.asStateFlow()

    // Rainbow challenge log
    private val _rainbowLogs = MutableStateFlow<Map<RainbowColor, RainbowLogEntry>>(
        mapOf(
            RainbowColor.RED to RainbowLogEntry(RainbowColor.RED, false, null, null),
            RainbowColor.ORANGE to RainbowLogEntry(RainbowColor.ORANGE, false, null, null),
            RainbowColor.YELLOW to RainbowLogEntry(RainbowColor.YELLOW, false, null, null),
            RainbowColor.GREEN to RainbowLogEntry(RainbowColor.GREEN, false, null, null),
            RainbowColor.BLUE_PURPLE to RainbowLogEntry(RainbowColor.BLUE_PURPLE, false, null, null),
            RainbowColor.WHITE_BROWN to RainbowLogEntry(RainbowColor.WHITE_BROWN, false, null, null)
        )
    )
    val rainbowLogs: StateFlow<Map<RainbowColor, RainbowLogEntry>> = _rainbowLogs.asStateFlow()

    // Streaks
    private val _quizStreak = MutableStateFlow(prefs?.getInt("quiz_streak", 0) ?: 0)
    val quizStreak: StateFlow<Int> = _quizStreak.asStateFlow()

    private val _rainbowStreak = MutableStateFlow(prefs?.getInt("rainbow_streak", 0) ?: 0)
    val rainbowStreak: StateFlow<Int> = _rainbowStreak.asStateFlow()

    private val todayDateKey: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    // Daily Quiz answered status
    private val _isTodayQuizAnswered = MutableStateFlow(
        prefs?.getString("last_quiz_completed_date", "") == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    )
    val isTodayQuizAnswered: StateFlow<Boolean> = _isTodayQuizAnswered.asStateFlow()

    companion object {
        const val FREE_DAILY_LABS_LIMIT = 3
        const val PRO_DAILY_LABS_LIMIT = 500
    }

    // Daily Labs Usage (3 runs/day for Free users; 500 runs/day for Pro users)
    private val _dailyLabsUsage = MutableStateFlow(
        prefs?.getInt("labs_usage_${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}", 0) ?: 0
    )
    val dailyLabsUsage: StateFlow<Int> = _dailyLabsUsage.asStateFlow()

    fun canUseLabFeature(isPro: Boolean): Boolean {
        val limit = if (isPro) PRO_DAILY_LABS_LIMIT else FREE_DAILY_LABS_LIMIT
        val todayKey = "labs_usage_$todayDateKey"
        val current = prefs?.getInt(todayKey, 0) ?: _dailyLabsUsage.value
        return current < limit
    }

    fun recordLabUsage(isPro: Boolean): Boolean {
        val limit = if (isPro) PRO_DAILY_LABS_LIMIT else FREE_DAILY_LABS_LIMIT
        val todayKey = "labs_usage_$todayDateKey"
        val current = prefs?.getInt(todayKey, 0) ?: _dailyLabsUsage.value
        if (current >= limit) {
            return false // Limit reached!
        }
        val next = current + 1
        prefs?.edit()?.putInt(todayKey, next)?.apply()
        _dailyLabsUsage.value = next
        return true
    }

    fun getRemainingLabsCredits(isPro: Boolean): Int {
        val limit = if (isPro) PRO_DAILY_LABS_LIMIT else FREE_DAILY_LABS_LIMIT
        val current = _dailyLabsUsage.value
        return maxOf(0, limit - current)
    }

    fun toggleSave(produceId: Int) {
        _savedItemIds.update { set ->
            val updated = if (set.contains(produceId)) set - produceId else set + produceId
            prefs?.edit()?.putStringSet("saved_item_ids", updated.map { it.toString() }.toSet())?.apply()
            updated
        }
    }

    fun isSaved(produceId: Int): Boolean {
        return _savedItemIds.value.contains(produceId)
    }

    fun addScan(result: ScanResult) {
        _scanHistory.update { listOf(result) + it }
    }

    fun toggleRainbowColor(color: RainbowColor) {
        _rainbowLogs.update { current ->
            val existing = current[color] ?: RainbowLogEntry(color, false)
            val newLogged = !existing.isLogged
            val newEntry = RainbowLogEntry(
                color = color,
                isLogged = newLogged,
                produceName = if (newLogged) color.sampleProduce else null,
                loggedTime = if (newLogged) "Just now" else null
            )
            current + (color to newEntry)
        }
    }

    fun incrementQuizStreak() {
        _quizStreak.update { current ->
            val next = current + 1
            prefs?.edit()?.putInt("quiz_streak", next)?.apply()
            next
        }
    }

    fun setSavedItemIds(ids: Set<Int>) {
        _savedItemIds.value = ids
        prefs?.edit()?.putStringSet("saved_item_ids", ids.map { it.toString() }.toSet())?.apply()
    }

    fun markTodayQuizCompleted() {
        _isTodayQuizAnswered.value = true
        prefs?.edit()?.putString("last_quiz_completed_date", todayDateKey)?.apply()
        incrementQuizStreak()
    }

    fun setQuizStreak(streak: Int) {
        _quizStreak.value = streak
        prefs?.edit()?.putInt("quiz_streak", streak)?.apply()
    }

    fun setRainbowStreak(streak: Int) {
        _rainbowStreak.value = streak
        prefs?.edit()?.putInt("rainbow_streak", streak)?.apply()
    }

    fun setRainbowLogs(logs: Map<RainbowColor, RainbowLogEntry>) {
        _rainbowLogs.value = logs
    }

    fun resetAllData() {
        prefs?.edit()?.clear()?.apply()
        _hasSeenQuickTour.value = false
        _savedItemIds.value = emptySet()
        _scanHistory.value = emptyList()
        _rainbowLogs.value = mapOf(
            RainbowColor.RED to RainbowLogEntry(RainbowColor.RED, false, null, null),
            RainbowColor.ORANGE to RainbowLogEntry(RainbowColor.ORANGE, false, null, null),
            RainbowColor.YELLOW to RainbowLogEntry(RainbowColor.YELLOW, false, null, null),
            RainbowColor.GREEN to RainbowLogEntry(RainbowColor.GREEN, false, null, null),
            RainbowColor.BLUE_PURPLE to RainbowLogEntry(RainbowColor.BLUE_PURPLE, false, null, null),
            RainbowColor.WHITE_BROWN to RainbowLogEntry(RainbowColor.WHITE_BROWN, false, null, null)
        )
        _quizStreak.value = 0
        _rainbowStreak.value = 0
        _isTodayQuizAnswered.value = false
    }

    fun setScanHistory(scans: List<ScanResult>) {
        _scanHistory.value = scans
    }

    /**
     * Dynamic Weekly Corkboard:
     * Generates 8 distinct, seasonal specimens pinned to the corkboard based on the current calendar week.
     * Rotates smoothly week to week with shuffle support and creative curation tags.
     */
    fun getWeeklyCorkboardInfo(
        cal: java.util.Calendar = java.util.Calendar.getInstance(),
        shuffleSeed: Int = _corkboardShuffleCount.value
    ): Pair<String, List<com.example.data.model.CorkboardItem>> {
        val week = cal.get(java.util.Calendar.WEEK_OF_YEAR)
        val year = cal.get(java.util.Calendar.YEAR)
        val weekSeed = (year * 100L + week + shuffleSeed * 10007L)

        val all = ProduceCatalog.allItems
        if (all.isEmpty()) {
            return Pair("Week $week specimens pinned", emptyList())
        }

        // Deterministic pseudo-random sequence for this week
        var a = weekSeed
        fun nextFloat(): Float {
            a = (a + 0x6D2B79F5L) and 0xFFFFFFFFL
            var t = (a xor (a ushr 15)) * (1L or a) and 0xFFFFFFFFL
            t = (t + ((t xor (t ushr 7)) * (61L or t) and 0xFFFFFFFFL)) xor t and 0xFFFFFFFFL
            return (((t xor (t ushr 14)) and 0xFFFFFFFFL).toFloat()) / 4294967296f
        }

        // Pick 6 diverse archetypes across the catalog
        val selected = mutableListOf<com.example.data.model.CorkboardItem>()
        val usedIds = mutableSetOf<Int>()
        val offset = (nextFloat() * all.size).toInt()

        val rotations = listOf(-4.5f, 3.8f, -3.5f, 4.2f, -3.8f, 4.0f)
        val tapeRotations = listOf(3.5f, -3.5f, 4.0f, -3.8f, 4.2f, -4.0f)
        val pinColors = listOf(0xFFD6482F, 0xFF2E7D32, 0xFFD4AF37, 0xFF00897B, 0xFFE65100, 0xFF6A1B9A)
        val pinStyles = listOf("PUSHPIN", "WASHI_TAPE", "BRASS_TACK", "WASHI_TAPE", "PUSHPIN", "BRASS_TACK")

        for (i in 0 until 6) {
            val idx = (offset + i * 37) % all.size
            var item = all[idx]
            var searchOffset = 1
            while (usedIds.contains(item.id) && searchOffset < all.size) {
                item = all[(idx + searchOffset) % all.size]
                searchOffset++
            }
            usedIds.add(item.id)

            val rot = rotations.getOrElse(i) { -4f + nextFloat() * 8f }
            val tapeRot = tapeRotations.getOrElse(i) { -3.5f + nextFloat() * 7f }
            val pColor = pinColors.getOrElse(i) { 0xFFD6482F }
            val pStyle = pinStyles.getOrElse(i) { "PUSHPIN" }

            selected.add(
                com.example.data.model.CorkboardItem(
                    id = item.id,
                    name = item.name,
                    archetype = item.archetype,
                    symbolId = item.symbolId,
                    rotation = rot,
                    tapeRotation = tapeRot,
                    pinColor = pColor,
                    pinStyle = pStyle,
                    noteBadge = null,
                    scientific = item.scientific.ifEmpty { item.name },
                    funFactSnippet = item.nutrients.firstOrNull() ?: item.origin
                )
            )
        }

        val label = if (shuffleSeed == 0) "Week $week Specimens" else "Curated Board #$shuffleSeed"
        return Pair(label, selected)
    }

    /**
     * Dynamic Daily Quiz:
     * Generates a unique, educational quiz question every day based on the calendar day.
     * Generates 3 accurate botanical clues, 4 multiple choice options, and a fun fact.
     */
    fun getTodayQuiz(cal: java.util.Calendar = java.util.Calendar.getInstance()): QuizQuestion {
        val dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
        val year = cal.get(java.util.Calendar.YEAR)
        val daySeed = (year * 1000L + dayOfYear)

        val monthFormat = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
        val dateStr = monthFormat.format(cal.time)

        val all = ProduceCatalog.allItems
        if (all.isEmpty()) {
            return QuizQuestion(
                specimenName = "Hass Avocado",
                archetype = "stone",
                clues = listOf(
                    "I am a single-seeded stone fruit with pebbly skin that darkens as I ripen.",
                    "I have more potassium than a dessert banana and over 75% heart-healthy monounsaturated oleic fats.",
                    "Every commercial tree descends from a single backyard mother tree planted in 1926."
                ),
                options = listOf("Mangosteen", "Hass Avocado", "Papaya", "Passion Fruit"),
                correctIndex = 1,
                funFact = "Every commercial Hass avocado tree descends from Rudolph Hass's 1926 backyard tree in California!",
                produceId = 157,
                symbolId = "d-avocado",
                dateString = dateStr,
                dayNumber = dayOfYear
            )
        }

        var a = daySeed
        fun nextFloat(): Float {
            a = (a + 0x6D2B79F5L) and 0xFFFFFFFFL
            var t = (a xor (a ushr 15)) * (1L or a) and 0xFFFFFFFFL
            t = (t + ((t xor (t ushr 7)) * (61L or t) and 0xFFFFFFFFL)) xor t and 0xFFFFFFFFL
            return (((t xor (t ushr 14)) and 0xFFFFFFFFL).toFloat()) / 4294967296f
        }

        // Pick today's target specimen
        val targetIdx = (nextFloat() * all.size).toInt().coerceIn(0, all.size - 1)
        val target = all[targetIdx]

        // Generate 3 authentic clues
        val clue1 = if (target.scientific.isNotEmpty()) {
            "Botanically classified as ${target.scientific}, belonging to the ${target.category.lowercase().removeSuffix("s")} family."
        } else {
            "A prized ${target.type.lowercase()} with a distinctive ${target.archetype} botanical structure."
        }

        val clue2 = if (target.nutrients.isNotEmpty()) {
            "Dense natural source of ${target.nutrients.take(3).joinToString(", ")}."
        } else if (target.season.isNotEmpty()) {
            "Grown primarily in ${target.origin} with peak harvest around ${target.season}."
        } else {
            "Known for: ${target.blurb.split(".").firstOrNull() ?: target.blurb}."
        }

        val clue3 = if (target.trivia.isNotEmpty() && target.trivia != "Prized across ancient and modern culinary history for dense botanical micronutrients.") {
            target.trivia
        } else if (target.characteristics.isNotEmpty()) {
            target.characteristics.split(".").firstOrNull()?.trim()?.plus(".") ?: "Thrives best when stored in ${target.storage.lowercase()}."
        } else {
            "Key storage guideline: ${target.storage}."
        }

        // Generate 3 distractors
        val distractors = mutableListOf<String>()
        val usedDistractorIds = mutableSetOf(target.id)

        // Try to pick from same category first for realistic challenge
        val sameCat = all.filter { it.category == target.category && it.id != target.id }
        for (item in sameCat) {
            if (distractors.size < 3 && !usedDistractorIds.contains(item.id)) {
                distractors.add(item.name)
                usedDistractorIds.add(item.id)
            }
        }

        // If needed, fill from whole catalog
        var safetyCount = 0
        while (distractors.size < 3 && safetyCount < 30) {
            val randomItem = all[(nextFloat() * all.size).toInt().coerceIn(0, all.size - 1)]
            if (!usedDistractorIds.contains(randomItem.id)) {
                distractors.add(randomItem.name)
                usedDistractorIds.add(randomItem.id)
            }
            safetyCount++
        }

        // Place correct answer at deterministic index (0..3)
        val correctIndex = (nextFloat() * 4).toInt().coerceIn(0, 3)
        val options = mutableListOf<String>()
        var dIndex = 0
        for (i in 0 until 4) {
            if (i == correctIndex) {
                options.add(target.name)
            } else {
                options.add(distractors.getOrElse(dIndex++) { "Botanical Sample" })
            }
        }

        val funFact = if (target.trivia.isNotEmpty()) target.trivia else target.blurb

        return QuizQuestion(
            specimenName = target.name,
            archetype = target.archetype,
            clues = listOf(clue1, clue2, clue3),
            options = options,
            correctIndex = correctIndex,
            funFact = funFact,
            produceId = target.id,
            symbolId = target.symbolId,
            dateString = dateStr,
            dayNumber = dayOfYear
        )
    }
}
