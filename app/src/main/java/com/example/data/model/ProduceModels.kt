package com.example.data.model

data class ProduceItem(
    val id: Int,
    val name: String,
    val scientific: String = "",
    val category: String,
    val archetype: String,
    val type: String, // "Fruit" or "Vegetable"
    val blurb: String,
    val symbolId: String? = null,
    val nutrients: List<String> = emptyList(),
    val characteristics: String = blurb,
    val ripenessCues: List<String> = emptyList(),
    val cleaningSteps: List<String> = emptyList(),
    val treatment: String = "Standard field rinse recommended. Minimal post-harvest handling.",
    val treatments: String = treatment,
    val botanicalFacts: String = "",
    val season: String = "Peak in summer & autumn",
    val storage: String = "Cool dry pantry or refrigerator crisper drawer",
    val waterContent: String = "85%",
    val glycemicIndex: String = "Low (GI < 40)",
    val origin: String = "Tropical / Mediterranean",
    val trivia: String = "Prized across ancient and modern culinary history for dense botanical micronutrients."
)

data class ScanResult(
    val id: String,
    val produceName: String,
    val dateGroup: String, // "Today", "Yesterday", "Aug 12"
    val time: String,
    val confidence: ConfidenceLevel,
    val quality: String, // "Good", "Excellent", "Fair", "Spoiled"
    val ripenessStage: String, // "Breakfast-ripe", "Peak ripe", "Use soon", "Spoiled"
    val shelfLifeDays: String, // "3–4 days", "0 days (Spoiled)"
    val freshnessCues: String,
    val isEdible: Boolean = true,
    val edibilityVerdict: String = "Safe & Edible",
    val symbolId: String? = null
)

enum class ConfidenceLevel(val label: String) {
    HIGH("Likely"),
    MEDIUM("Possibly"),
    LOW("Low confidence")
}

data class DietaryCategory(
    val id: String,
    val name: String,
    val macro: String,
    val philosophy: String,
    val rule: String,
    val rotation: String,
    val token: String
)

data class MealItem(
    val id: String,
    val cat: String,
    val type: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val name: String,
    val portionDetails: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int,
    val highlights: String,
    val prepTimeMin: Int // minutes
)

enum class RainbowColor(
    val displayName: String,
    val hexColor: Long,
    val sampleProduce: String,
    val phytonutrient: String
) {
    RED("Red", 0xFFD6482F, "Strawberry", "Lycopene & Anthocyanins"),
    ORANGE("Orange", 0xFFE79B1F, "Carrot", "Alpha & Beta-Carotenes"),
    YELLOW("Yellow", 0xFFF0C419, "Banana", "Bioflavonoids & Lutein"),
    GREEN("Green", 0xFF3F7D4C, "Broccoli", "Chlorophyll & Sulforaphane"),
    BLUE_PURPLE("Blue & Purple", 0xFF6E3B6E, "Grape", "Resveratrol & Anthocyanins"),
    WHITE_BROWN("White & Brown", 0xFFC9A06A, "Potato", "Allicin & Quercetin")
}

data class RainbowLogEntry(
    val color: RainbowColor,
    val isLogged: Boolean,
    val produceName: String? = null,
    val loggedTime: String? = null
)

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CorkboardItem(
    val id: Int,
    val name: String,
    val archetype: String,
    val symbolId: String?,
    val rotation: Float,
    val tapeRotation: Float,
    val pinColor: Long = 0xFFD6482F,
    val pinStyle: String = "PUSHPIN", // "PUSHPIN", "WASHI_TAPE", "BRASS_TACK", "PAPERCLIP"
    val noteBadge: String? = null,
    val scientific: String = "",
    val funFactSnippet: String = ""
)

data class QuizQuestion(
    val specimenName: String,
    val archetype: String,
    val clues: List<String>,
    val options: List<String>,
    val correctIndex: Int,
    val funFact: String,
    val produceId: Int = 157,
    val symbolId: String? = null,
    val dateString: String = "Today",
    val dayNumber: Int = 1
)
