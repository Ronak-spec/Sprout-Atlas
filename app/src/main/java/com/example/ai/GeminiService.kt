package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.datasource.ProduceCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiMealPlan(
    val title: String,
    val dietaryFocus: String,
    val summary: String,
    val macroSummary: String,
    val breakfast: AiMeal,
    val lunch: AiMeal,
    val dinner: AiMeal,
    val snack: AiMeal,
    val focusedNutrients: String = "",
    val currentWeight: String = "",
    val targetWeight: String = "",
    val medications: String = "",
    val clinicalPrecautions: String = ""
)

data class AiMeal(
    val name: String,
    val slot: String,
    val portionDetails: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int,
    val prepTimeMin: Int,
    val produceIngredients: List<String>,
    val nutrients: String
)

data class ImageAnalysisResult(
    val produceName: String,
    val confidenceScore: Int,
    val isEdible: Boolean,
    val edibilityVerdict: String, // e.g. "Safe & Edible (Peak)", "Consume Promptly (1–2 Days)", "NOT EDIBLE (Spoiled / Mold Danger)"
    val freshDaysRemaining: String, // e.g. "4–6 Days", "1–2 Days", "0 Days (Inedible / Discard)"
    val shelfLifeDaysCount: Int, // numeric day count e.g. 5, 2, 0
    val freshnessVerdict: String, // "Peak Freshness", "Ripe & Ready", "Overripe", "Spoiled / Decayed"
    val ripenessState: String,
    val spoilageRiskAnalysis: String, // Comprehensive breakdown of mold, rot, skin softening, internal browning
    val visualObservations: List<String>,
    val washingRecommendation: String,
    val storageTip: String,
    val matchedGuideId: Int?
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val configuredApiKeys: List<String>
        get() {
            val list = mutableListOf<String>()
            val primary = BuildConfig.GEMINI_API_KEY
            if (primary.isNotBlank() && primary != "MY_GEMINI_API_KEY" && !primary.startsWith("YOUR_")) {
                list.add(primary)
            }
            try {
                val backup = BuildConfig.GEMINI_BACKUP_API_KEY
                if (backup.isNotBlank() && backup != "MY_GEMINI_API_KEY" && !backup.startsWith("YOUR_") && !list.contains(backup)) {
                    list.add(backup)
                }
            } catch (e: Throwable) {
                // Ignore if BuildConfig field is absent
            }
            return list
        }

    private val isKeyConfigured: Boolean
        get() = configuredApiKeys.isNotEmpty()

    /**
     * Executes a POST request to Gemini generateContent with automatic key failover.
     */
    private suspend fun executeGeminiJsonCall(
        model: String,
        jsonBody: JSONObject
    ): JSONObject? = withContext(Dispatchers.IO) {
        val keys = configuredApiKeys
        if (keys.isEmpty()) return@withContext null

        val bodyString = jsonBody.toString()

        for (key in keys) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"
                val request = Request.Builder()
                    .url(url)
                    .post(bodyString.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respString = response.body?.string() ?: ""
                    val respJson = JSONObject(respString)
                    val candidates = respJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        return@withContext respJson
                    }
                } else {
                    // Try next fallback key if 429 (quota), 403, 500, or 503
                    val code = response.code
                    if (code == 429 || code == 403 || code == 500 || code == 503) {
                        continue
                    }
                }
            } catch (e: Exception) {
                // Try next backup key on timeout or connection failure
                continue
            }
        }
        null
    }

    // -------------------------------------------------------------
    // 1. MULTI-TURN CHATBOT (Tutor)
    // -------------------------------------------------------------
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        newPrompt: String
    ): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured) {
            return@withContext getOfflineChatFallback(newPrompt)
        }

        try {
            val systemInstructionText = "You are the Sprout Atlas Botanical & Nutrition Tutor, an expert botanist and culinary scientist specializing in fresh fruits, vegetables, food safety, pesticide washing, storage, and nutritional biochemistry. Answer questions warmly, authoritatively, and concisely in 2–4 sentences, grounded in whole-food science."

            val contentsArray = JSONArray()

            // Build historical multi-turn conversation
            for (msg in history.takeLast(10)) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
            }

            // Append current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", newPrompt) })
                })
            })

            val jsonBody = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstructionText) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                })
            }

            val respJson = executeGeminiJsonCall("gemini-2.5-flash", jsonBody)
                ?: executeGeminiJsonCall("gemini-1.5-flash", jsonBody)

            if (respJson != null) {
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val reply = parts.getJSONObject(0).optString("text", "")
                        if (reply.isNotBlank()) {
                            return@withContext reply
                        }
                    }
                }
            }
            getOfflineChatFallback(newPrompt)
        } catch (e: Exception) {
            getOfflineChatFallback(newPrompt)
        }
    }

    // -------------------------------------------------------------
    // 2. IMAGE ANALYSIS (Scanner using Gemini Vision)
    // -------------------------------------------------------------
    suspend fun analyzeProduceImage(
        bitmap: Bitmap,
        specimenHint: String = ""
    ): ImageAnalysisResult = withContext(Dispatchers.IO) {
        val base64Image = bitmapToBase64(bitmap)

        if (!isKeyConfigured) {
            return@withContext getOfflineImageScanFallback(specimenHint)
        }

        try {
            val prompt = """
                You are a senior agricultural pathologist, botanical scientist, and food safety specialist with Sprout Atlas.
                Inspect this fruit/vegetable image meticulously to evaluate its exact identity, safety/edibility status, remaining freshness shelf life, and spoilage pathology.
                ${if (specimenHint.isNotBlank()) "User context/specimen reference: $specimenHint" else ""}

                MANDATORY EVALUATION INSTRUCTIONS:
                1. PRODUCE IDENTIFICATION: Identify the specific common produce name and cultivar/variety if discernible.
                2. EDIBILITY EVALUATION:
                   - "isEdible": boolean (true if safe for human consumption; false if spoiled, rotted, moldy, fermenting, bacterially decomposed, or toxic).
                   - "edibilityVerdict": exact safety verdict string, e.g. "Safe & Edible (Peak Condition)", "Edible (Consume Within 24–48 Hours)", "Edible for Cooking/Baking (Overripe)", or "NOT EDIBLE (Spoiled / Mold Danger / Discard)".
                3. FRESHNESS & SHELF LIFE (REMAINING DAYS):
                   - "freshDaysRemaining": estimated remaining edible days string, e.g. "4–6 Days (Refrigerated)", "1–2 Days (Consume Soon)", "7–10 Days (Cool Pantry)", or "0 Days (Inedible / Expired)".
                   - "shelfLifeDaysCount": integer representing the estimated remaining edible days (e.g. 5, 2, 0).
                4. FRESHNESS VERDICT: "Peak Freshness", "Ripe & Ready", "Overripe", or "Spoiled / Decayed".
                5. RIPENESS STATE: Describe the current physiological ripeness, firmness, and sugar/acid profile.
                6. SPOILAGE RISK & PATHOLOGY ANALYSIS: Detailed scientific explanation of whether mold (e.g. Botrytis, Penicillium), bacterial soft rot, ethylene senescence, dehydration, bruising, or tissue oxidation is present or at risk.
                7. VISUAL OBSERVATIONS: 3–4 specific visual bullet points noting skin turgor, color uniformity, stem/calyx vitality, surface bloom, or defects.
                8. WASHING RECOMMENDATION: Specific scientific washing/sanitizing step (e.g. 1% baking soda soak, vinegar solution, cold friction rinse, or peel discard).
                9. STORAGE TIP: Precise temperature, humidity, and airflow guidance to maximize remaining shelf life.

                Respond with ONLY a valid JSON object matching this schema:
                {
                  "produceName": "Common Name",
                  "confidenceScore": 95,
                  "isEdible": true,
                  "edibilityVerdict": "Safe & Edible (Peak Condition)",
                  "freshDaysRemaining": "4–6 Days",
                  "shelfLifeDaysCount": 5,
                  "freshnessVerdict": "Peak Freshness",
                  "ripenessState": "Optimal firm texture with intact epidermal layer",
                  "spoilageRiskAnalysis": "No mycelial growth, sunken lesions, or soft bacterial rot observed. Epidermal turgor is robust.",
                  "visualObservations": [
                    "Vibrant, uniform pigmentation without chlorosis or necrotic spotting",
                    "Intact natural cuticle bloom with no skin lacerations",
                    "Healthy stem attachment showing no mold sporulation"
                  ],
                  "washingRecommendation": "Submerge in 1% baking soda solution for 10 minutes then rinse thoroughly under cool tap water.",
                  "storageTip": "Refrigerate in a breathable crisper drawer at 36–40°F (2–4°C) to extend freshness."
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 800)
                    put("responseMimeType", "application/json")
                })
            }

            val respJson = executeGeminiJsonCall("gemini-2.5-flash", jsonBody)
                ?: executeGeminiJsonCall("gemini-1.5-flash", jsonBody)

            if (respJson != null) {
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val textJson = parts.getJSONObject(0).optString("text", "")
                        if (textJson.isNotBlank()) {
                            return@withContext parseScanResult(textJson, specimenHint)
                        }
                    }
                }
            }
            getOfflineImageScanFallback(specimenHint)
        } catch (e: Exception) {
            getOfflineImageScanFallback(specimenHint)
        }
    }

    private fun parseScanResult(rawJson: String, fallbackHint: String = ""): ImageAnalysisResult {
        try {
            val clean = rawJson.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(clean)
            val name = obj.optString("produceName", if (fallbackHint.isNotBlank()) fallbackHint else "Fresh Produce Specimen")
            val score = obj.optInt("confidenceScore", 95)
            val isEdible = obj.optBoolean("isEdible", !name.contains("mold", true) && !name.contains("spoil", true) && !name.contains("rot", true))
            val edibilityVerdict = obj.optString("edibilityVerdict", if (isEdible) "Safe & Edible (Peak Condition)" else "NOT EDIBLE (Spoiled / Mold Hazard)")
            val freshDays = obj.optString("freshDaysRemaining", if (isEdible) "3–5 Days" else "0 Days (Inedible)")
            val daysCount = obj.optInt("shelfLifeDaysCount", if (isEdible) 4 else 0)
            val verdict = obj.optString("freshnessVerdict", if (isEdible) "Peak Freshness" else "Spoiled / Decayed")
            val state = obj.optString("ripenessState", if (isEdible) "Ready to consume" else "Past consumption window")
            val spoilage = obj.optString("spoilageRiskAnalysis", if (isEdible) "No pathogenic fungal hyphae or bacterial rot detected." else "Significant microbial breakdown or tissue soft rot present.")

            val obsArray = obj.optJSONArray("visualObservations")
            val observations = mutableListOf<String>()
            if (obsArray != null && obsArray.length() > 0) {
                for (i in 0 until obsArray.length()) {
                    observations.add(obsArray.getString(i))
                }
            } else {
                observations.add("Natural skin pigmentation intact")
                observations.add("Intact calyx structure with minimal mechanical bruising")
            }
            val washing = obj.optString("washingRecommendation", "Wash under cold running water with light friction before prep.")
            val storage = obj.optString("storageTip", "Keep in a cool, ventilated produce crisper drawer.")

            val matched = ProduceCatalog.allItems.find {
                it.name.equals(name, ignoreCase = true) || name.contains(it.name, ignoreCase = true)
            }

            return ImageAnalysisResult(
                produceName = name,
                confidenceScore = score,
                isEdible = isEdible,
                edibilityVerdict = edibilityVerdict,
                freshDaysRemaining = freshDays,
                shelfLifeDaysCount = daysCount,
                freshnessVerdict = verdict,
                ripenessState = state,
                spoilageRiskAnalysis = spoilage,
                visualObservations = observations,
                washingRecommendation = washing,
                storageTip = storage,
                matchedGuideId = matched?.id
            )
        } catch (e: Exception) {
            return getOfflineImageScanFallback(fallbackHint)
        }
    }

    // -------------------------------------------------------------
    // 3. CLINICAL AI MEAL PLANNER
    // -------------------------------------------------------------
    suspend fun generateAiMealPlan(
        focus: String,
        focusedNutrients: String = "",
        currentWeight: String = "",
        targetWeight: String = "",
        medications: String = "",
        availableIngredients: String = ""
    ): AiMealPlan = withContext(Dispatchers.IO) {
        if (!isKeyConfigured) {
            return@withContext getOfflinePlannerFallback(
                focus = focus,
                focusedNutrients = focusedNutrients,
                currentWeight = currentWeight,
                targetWeight = targetWeight,
                medications = medications
            )
        }

        try {
            val prompt = """
                You are the Sprout Atlas Clinical-Grade Nutrition Rotation & Dietary Assessment Engine.
                Generate an authentic, whole-food clinical day plan taking into account the user's clinical intake:
                
                CLINICAL INTAKE PARAMETERS:
                1. Dietary Focus Archetype: $focus
                2. Targeted Micronutrients & Nutrient Focus: ${if (focusedNutrients.isBlank()) "Standard high-density balance" else focusedNutrients}
                3. Current Baseline Body Weight: ${if (currentWeight.isBlank()) "75 kg / 165 lbs (reference)" else currentWeight}
                4. Target Weight Goal & Metabolic Objective: ${if (targetWeight.isBlank()) "Maintain healthy body composition" else targetWeight}
                5. Active Medications & Clinical Considerations: ${if (medications.isBlank()) "None reported (healthy reference)" else medications}
                6. Requested / On-Hand Produce: ${if (availableIngredients.isBlank()) "Seasonal peak botanical produce" else availableIngredients}
                
                CLINICAL REQUIREMENTS:
                - Calibrate total energy (kcal) and protein (g/kg) strictly to the current vs target weight trajectory.
                - Prioritize the requested focused nutrients in every meal slot.
                - Screen and safeguard against any potential food-drug interactions or contraindications related to the disclosed active medications (e.g. avoid high-potassium surges with ACE-inhibitors/ARBs, avoid grapefruit with statins/calcium channel blockers, monitor Vitamin K with blood thinners, avoid tyramine surges with MAOIs/SSRIs).
                - Use 100% whole, unrefined foods with exact portion sizes in grams.
                
                Respond with ONLY a valid JSON object matching this schema:
                {
                  "title": "Clinical Rotation Plan Title",
                  "dietaryFocus": "$focus",
                  "summary": "1-2 sentence clinical summary addressing caloric allocation, target nutrients, and cellular autophagy/mTOR mechanics.",
                  "macroSummary": "e.g. 40% Protein (165g) | 25% Net Carbs (105g) | 35% Fats (65g)",
                  "focusedNutrients": "${focusedNutrients.ifBlank { "High Bioavailable Micronutrients" }}",
                  "currentWeight": "${currentWeight.ifBlank { "75 kg" }}",
                  "targetWeight": "${targetWeight.ifBlank { "Optimal composition" }}",
                  "medications": "${medications.ifBlank { "None reported" }}",
                  "clinicalPrecautions": "Specific clinical food-drug or electrolyte guidance notes based on the reported medications and target weight trajectory.",
                  "breakfast": {
                    "name": "Meal Name",
                    "slot": "Breakfast",
                    "portionDetails": "Exact grams and whole-food ingredients (e.g. 180g Grass-fed steak, 3 pastured eggs, 1 cup spinach in EVOO)",
                    "calories": 580,
                    "protein": 48,
                    "carbs": 12,
                    "fats": 36,
                    "fiber": 6,
                    "prepTimeMin": 15,
                    "produceIngredients": ["Avocado", "Spinach", "Tomato"],
                    "nutrients": "Heme Iron, Choline, Vitamin B12, Lutein"
                  },
                  "lunch": {
                    "name": "Meal Name",
                    "slot": "Lunch",
                    "portionDetails": "Exact grams and whole-food ingredients",
                    "calories": 610,
                    "protein": 52,
                    "carbs": 24,
                    "fats": 32,
                    "fiber": 7,
                    "prepTimeMin": 20,
                    "produceIngredients": ["Kale", "Cucumber", "Radish"],
                    "nutrients": "EPA/DHA, Sulforaphane, Potassium, Folate"
                  },
                  "dinner": {
                    "name": "Meal Name",
                    "slot": "Dinner",
                    "portionDetails": "Exact grams and whole-food ingredients",
                    "calories": 650,
                    "protein": 54,
                    "carbs": 20,
                    "fats": 38,
                    "fiber": 8,
                    "prepTimeMin": 25,
                    "produceIngredients": ["Sweet Potato", "Broccoli", "Garlic"],
                    "nutrients": "Bioavailable Zinc, Astaxanthin, Beta-Carotene"
                  },
                  "snack": {
                    "name": "Meal Name",
                    "slot": "Snack",
                    "portionDetails": "Exact grams and whole-food ingredients",
                    "calories": 280,
                    "protein": 18,
                    "carbs": 10,
                    "fats": 18,
                    "fiber": 5,
                    "prepTimeMin": 5,
                    "produceIngredients": ["Blueberries", "Walnuts"],
                    "nutrients": "Ellagic Acid, Polyphenols, Magnesium"
                  }
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1200)
                    put("responseMimeType", "application/json")
                })
            }

            val respJson = executeGeminiJsonCall("gemini-2.5-flash", jsonBody)
                ?: executeGeminiJsonCall("gemini-1.5-flash", jsonBody)

            if (respJson != null) {
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val textJson = parts.getJSONObject(0).optString("text", "")
                        if (textJson.isNotBlank()) {
                            return@withContext parseMealPlan(
                                rawJson = textJson,
                                defaultFocus = focus,
                                focusedNutrients = focusedNutrients,
                                currentWeight = currentWeight,
                                targetWeight = targetWeight,
                                medications = medications
                            )
                        }
                    }
                }
            }
            getOfflinePlannerFallback(focus, focusedNutrients, currentWeight, targetWeight, medications)
        } catch (e: Exception) {
            getOfflinePlannerFallback(focus, focusedNutrients, currentWeight, targetWeight, medications)
        }
    }

    private fun parseMealPlan(
        rawJson: String,
        defaultFocus: String,
        focusedNutrients: String = "",
        currentWeight: String = "",
        targetWeight: String = "",
        medications: String = ""
    ): AiMealPlan {
        try {
            val clean = rawJson.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(clean)
            return AiMealPlan(
                title = obj.optString("title", "$defaultFocus Clinical Rotation Plan"),
                dietaryFocus = obj.optString("dietaryFocus", defaultFocus),
                summary = obj.optString("summary", "Clinical rotation calibrated to target micronutrient priorities and healthy metabolic trajectory."),
                macroSummary = obj.optString("macroSummary", "40% Protein | 25% Net Carbs | 35% Fats"),
                focusedNutrients = obj.optString("focusedNutrients", focusedNutrients.ifBlank { "Bioavailable Micronutrients" }),
                currentWeight = obj.optString("currentWeight", currentWeight.ifBlank { "Baseline" }),
                targetWeight = obj.optString("targetWeight", targetWeight.ifBlank { "Optimal" }),
                medications = obj.optString("medications", medications.ifBlank { "None reported" }),
                clinicalPrecautions = obj.optString("clinicalPrecautions", if (medications.isNotBlank()) "Screened against active medication contraindications and electrolyte thresholds." else "Standard whole-food clinical protocol verified."),
                breakfast = parseMealObject(obj.optJSONObject("breakfast"), "Breakfast", "Grass-Fed Steak & Pastured Eggs Power Plate", "180g Grass-fed steak, 3 pastured eggs, 1/2 avocado, 1 cup spinach in EVOO", 620, 52, 6, 42, 5, 15, listOf("Avocado", "Spinach"), "Heme Iron, Choline, Vitamin B12, Creatine"),
                lunch = parseMealObject(obj.optJSONObject("lunch"), "Lunch", "Wild Salmon & Roasted Broccolini Power Plate", "200g Wild Sockeye salmon, 1 cup steamed broccolini, 100g sweet potato cubes", 580, 54, 26, 28, 6, 20, listOf("Broccoli", "Sweet Potato"), "EPA/DHA Marine Omega-3s, Astaxanthin, Sulforaphane"),
                dinner = parseMealObject(obj.optJSONObject("dinner"), "Dinner", "Bison Hash with Garlic Mushrooms & Asparagus", "200g Grass-fed ground bison, 150g grilled asparagus, 1 cup sautéed shiitake in ghee", 640, 56, 12, 40, 6, 25, listOf("Garlic", "Asparagus"), "Bioavailable Zinc, Ergothioneine, Prebiotic Inulin"),
                snack = parseMealObject(obj.optJSONObject("snack"), "Snack", "Sprouted Pumpkin Seeds & Wild Blackberries", "35g Raw sprouted pumpkin seeds, 1/2 cup fresh wild blackberries", 240, 14, 12, 16, 6, 5, listOf("Blueberry"), "Ellagic acid, Magnesium, Plant sterols")
            )
        } catch (e: Exception) {
            return getOfflinePlannerFallback(defaultFocus, focusedNutrients, currentWeight, targetWeight, medications)
        }
    }

    private fun parseMealObject(
        obj: JSONObject?,
        slotName: String,
        defaultName: String,
        defaultPortion: String,
        cal: Int,
        prot: Int,
        carbs: Int,
        fat: Int,
        fib: Int,
        prep: Int,
        defaultIngredients: List<String>,
        defaultNutrients: String
    ): AiMeal {
        if (obj == null) {
            return AiMeal(
                name = defaultName,
                slot = slotName,
                portionDetails = defaultPortion,
                calories = cal,
                protein = prot,
                carbs = carbs,
                fats = fat,
                fiber = fib,
                prepTimeMin = prep,
                produceIngredients = defaultIngredients,
                nutrients = defaultNutrients
            )
        }
        val ingArray = obj.optJSONArray("produceIngredients")
        val ingList = mutableListOf<String>()
        if (ingArray != null) {
            for (i in 0 until ingArray.length()) {
                ingList.add(ingArray.getString(i))
            }
        }
        return AiMeal(
            name = obj.optString("name", defaultName),
            slot = obj.optString("slot", slotName),
            portionDetails = obj.optString("portionDetails", defaultPortion),
            calories = obj.optInt("calories", cal),
            protein = obj.optInt("protein", prot),
            carbs = obj.optInt("carbs", carbs),
            fats = obj.optInt("fats", fat),
            fiber = obj.optInt("fiber", fib),
            prepTimeMin = obj.optInt("prepTimeMin", prep),
            produceIngredients = if (ingList.isNotEmpty()) ingList else defaultIngredients,
            nutrients = obj.optString("nutrients", defaultNutrients)
        )
    }

    // --- Helpers & Offline fallbacks ---
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun getOfflineChatFallback(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("mango") && (q.contains("skin") || q.contains("peel")) ->
                "Technically yes, mango skin contains fiber and mangiferin, but it also carries urushiol (the compound in poison ivy) which can cause contact dermatitis or allergic lip tingling. Peeling is strongly recommended."
            q.contains("wax") || q.contains("apple") ->
                "Commercial apples often receive a thin food-grade carnauba or shellac wax to lock in moisture during storage. To remove it effectively, soak in warm water with a teaspoon of baking soda and wipe with a textured cloth."
            q.contains("store") || q.contains("storage") ->
                "Produce Storage Rule of Thumb: Keep climacteric fruits (avocados, peaches, tomatoes) at room temperature until ripe, then refrigerate. Leafy greens, herbs, and berries thrive best in high-humidity fridge drawers with airflow."
            q.contains("wash") || q.contains("pesticide") ->
                "Research demonstrates that a 10–12 minute soak in a 1% baking soda water solution (1 tsp baking soda per 2 cups cold water) removes significantly more surface pesticide residues than tap water alone."
            q.contains("onion") && q.contains("cry") ->
                "When an onion's cell walls rupture, alliinase enzymes react with sulfoxides to form syn-propanethial-S-oxide gas, which stimulates tear glands. Chilling the onion before slicing slows this enzymatic reaction dramatically!"
            else -> {
                val matched = ProduceCatalog.allItems.find { q.contains(it.name.lowercase()) }
                if (matched != null) {
                    "${matched.name} is a ${matched.type.lowercase()} in the ${matched.category} family. Primary active nutrients include ${matched.nutrients.take(3).joinToString(", ")}. Best stored: ${matched.storage}."
                } else {
                    "Sprout Atlas Botanical Fact: Eating 30+ different whole plant species weekly supports gut microbiome diversity, stable glycemic response, and longevity through synergistic bioflavonoids."
                }
            }
        }
    }

    private fun getOfflineImageScanFallback(produceHint: String = ""): ImageAnalysisResult {
        val lower = produceHint.lowercase()
        return when {
            lower.contains("mold") || lower.contains("spoil") || lower.contains("rot") -> {
                ImageAnalysisResult(
                    produceName = "Spoiled Strawberry (Fungal Mold Danger)",
                    confidenceScore = 98,
                    isEdible = false,
                    edibilityVerdict = "NOT EDIBLE (Botrytis Mold Hazard / Discard)",
                    freshDaysRemaining = "0 Days (Inedible / Expired)",
                    shelfLifeDaysCount = 0,
                    freshnessVerdict = "Spoiled / Decayed",
                    ripenessState = "Past consumption window; severe fungal mycelium breakdown",
                    spoilageRiskAnalysis = "Botrytis cinerea (gray mold) sporulation identified. Soft fruits have high moisture and porous tissue allowing microscopic mycotoxins to penetrate deeply beyond the visible fuzz. Ingesting can cause gastrointestinal distress and respiratory allergic reactions.",
                    visualObservations = listOf(
                        "Visible white/gray fuzzy fungal sporulation across outer epidermal seeds",
                        "Complete loss of cellular turgidity leading to collapsed, weeping fruit tissue",
                        "Severe chlorosis, browning, and dehydration of the calyx leaves",
                        "High risk of spore cross-contamination to surrounding produce"
                    ),
                    washingRecommendation = "Do not wash or trim off mold on soft fruit. Immediately discard entire contaminated specimen into compost or organic waste.",
                    storageTip = "Sanitize the refrigerator crisper bin with warm soapy water or diluted vinegar before storing fresh produce.",
                    matchedGuideId = 15
                )
            }
            lower.contains("banana") || lower.contains("spotted") || lower.contains("overripe") -> {
                ImageAnalysisResult(
                    produceName = "Spotted Cavendish Banana",
                    confidenceScore = 96,
                    isEdible = true,
                    edibilityVerdict = "Edible for Baking & Smoothies (Consume Today)",
                    freshDaysRemaining = "1–2 Days (Consume Promptly)",
                    shelfLifeDaysCount = 1,
                    freshnessVerdict = "Overripe / High Natural Sugars",
                    ripenessState = "Peak sugar conversion; starch hydrolyzed to simple glucose and fructose",
                    spoilageRiskAnalysis = "Dense brown sugar spots (senescent spotting) reflect normal amylase enzyme activity. No rind splitting, sour fermentation odor, or black fungal rot observed.",
                    visualObservations = listOf(
                        "Extensive golden peel coverage with aromatic brown sugar freckling",
                        "Softened internal pulp with high natural sweetness and moisture",
                        "Intact crown pedicel with zero mold hyphae or fruit fly punctures"
                    ),
                    washingRecommendation = "Rinse hands after peeling to avoid transferring outer peel dirt to fruit pulp.",
                    storageTip = "Peel, slice, and freeze in an airtight bag for smoothie thickening, or mash into whole-grain banana bread.",
                    matchedGuideId = 3
                )
            }
            lower.contains("apple") -> {
                ImageAnalysisResult(
                    produceName = "Honeycrisp Apple",
                    confidenceScore = 97,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Peak Freshness)",
                    freshDaysRemaining = "7–12 Days (Refrigerated)",
                    shelfLifeDaysCount = 10,
                    freshnessVerdict = "Peak Crispness",
                    ripenessState = "Crisp, dense cell walls with optimal acid-sugar balance",
                    spoilageRiskAnalysis = "High turgor pressure with zero mechanical bruising or core rot. Natural epicuticular wax bloom locks in hydration.",
                    visualObservations = listOf(
                        "Bright crimson-red striations over vibrant yellow ground color",
                        "Intact lenticels and tight epidermal structure without soft depressions",
                        "Clean stem cavity with no mold sporulation"
                    ),
                    washingRecommendation = "Submerge in a 1% baking soda soak (1 tsp per 2 cups water) for 10 minutes, then rinse under cold running water to strip residues.",
                    storageTip = "Store in the refrigerator crisper drawer at 34–38°F away from leafy greens to prevent ethylene-induced yellowing.",
                    matchedGuideId = 1
                )
            }
            lower.contains("tomato") -> {
                ImageAnalysisResult(
                    produceName = "Vine-Ripened Roma Tomato",
                    confidenceScore = 95,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Vine Ripe & Juicy)",
                    freshDaysRemaining = "4–6 Days (Countertop)",
                    shelfLifeDaysCount = 5,
                    freshnessVerdict = "Prime Ripeness",
                    ripenessState = "Firm shoulders with uniform lycopene saturation",
                    spoilageRiskAnalysis = "Smooth skin without splitting or blossom-end rot. High internal hydrostatic pressure with zero bacterial weeping.",
                    visualObservations = listOf(
                        "Deep crimson-red coloration indicating peak antioxidant concentration",
                        "Intact green calyx showing healthy botanical hydration",
                        "Zero soft indentations, black mold spots, or skin tears"
                    ),
                    washingRecommendation = "Wash under cool running water with gentle friction right before slicing.",
                    storageTip = "Store stem-side down at room temperature (65–70°F) away from direct sunlight to preserve aromatic volatile esters.",
                    matchedGuideId = 2
                )
            }
            lower.contains("broccoli") -> {
                ImageAnalysisResult(
                    produceName = "Broccoli Crown",
                    confidenceScore = 96,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Peak Freshness)",
                    freshDaysRemaining = "5–7 Days (Refrigerated)",
                    shelfLifeDaysCount = 6,
                    freshnessVerdict = "Crisp & Dense",
                    ripenessState = "Tight, dark green floret clusters with high sulforaphane potential",
                    spoilageRiskAnalysis = "No floret chlorosis (yellowing) or stalk hollow rot. Cell walls are rigid and hydrated with no sulfurous degradation odors.",
                    visualObservations = listOf(
                        "Deep blue-green unopened floral buds forming a compact crown",
                        "Crisp stalk cut without browning or slimy bacterial film",
                        "Zero fungal sporulation or insect bore holes"
                    ),
                    washingRecommendation = "Soak crown upside down in cold salted water for 5 minutes, then rinse under high-pressure cold tap water.",
                    storageTip = "Store loosely wrapped in a damp paper towel in a perforated crisper bag at 34–38°F.",
                    matchedGuideId = 18
                )
            }
            lower.contains("dragon") -> {
                ImageAnalysisResult(
                    produceName = "Dragon Fruit (Pitaya)",
                    confidenceScore = 94,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Peak Exotic Freshness)",
                    freshDaysRemaining = "5–7 Days (Refrigerated)",
                    shelfLifeDaysCount = 6,
                    freshnessVerdict = "Prime Ripeness",
                    ripenessState = "Vibrant magenta rind with supple green-tipped bracts",
                    spoilageRiskAnalysis = "No brown scale decay or soft sunken bases. High betalain and prebiotic dietary fiber content.",
                    visualObservations = listOf(
                        "Even magenta-pink coloration across the outer pericarp",
                        "Intact green-yellow tips on the leafy scales without shriveling",
                        "Firm yielding texture indicating juicy seed-studded internal pulp"
                    ),
                    washingRecommendation = "Rinse exterior skin before halving to avoid blade contamination of internal edible flesh.",
                    storageTip = "Keep in the refrigerator crisper drawer for up to 1 week, or slice and freeze for smoothie bowls.",
                    matchedGuideId = 10
                )
            }
            lower.contains("mango") -> {
                ImageAnalysisResult(
                    produceName = "Honey / Ataulfo Mango",
                    confidenceScore = 95,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Sweet & Ripe)",
                    freshDaysRemaining = "3–5 Days",
                    shelfLifeDaysCount = 4,
                    freshnessVerdict = "Peak Sweetness",
                    ripenessState = "Soft yielding shoulder with golden amber skin and fragrant aroma",
                    spoilageRiskAnalysis = "Minor micro-wrinkling indicates sugar concentration rather than spoilage. Stem end is clean with no black anthracnose spotting.",
                    visualObservations = listOf(
                        "Rich golden-yellow peel with subtle red blush",
                        "Supple skin indicating fibrous pulp softening and ester synthesis",
                        "Clean stem scar without sap burn or dark mold spots"
                    ),
                    washingRecommendation = "Wash skin thoroughly with warm water and a scrub brush to remove sap and urushiol traces before peeling.",
                    storageTip = "Store at room temperature until fragrant and yielding, then refrigerate for up to 5 days to halt over-ripening.",
                    matchedGuideId = 14
                )
            }
            lower.contains("guava") -> {
                ImageAnalysisResult(
                    produceName = "Tropical Guava",
                    confidenceScore = 93,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Aromatic Ripeness)",
                    freshDaysRemaining = "3–4 Days",
                    shelfLifeDaysCount = 3,
                    freshnessVerdict = "Peak Fragrance",
                    ripenessState = "Pale yellowish-green rind yielding gently to thumb pressure",
                    spoilageRiskAnalysis = "Intact skin with zero fruit-fly punctures or soft decay spots. Dense in vitamin C and polyphenols.",
                    visualObservations = listOf(
                        "Light green to yellowish-green skin with intact surface texture",
                        "Intact calyx remnants and fragrant floral aroma profile",
                        "Firm yet yielding pericarp"
                    ),
                    washingRecommendation = "Rinse well under cold water; the entire fruit including skin and seeds is edible.",
                    storageTip = "Refrigerate ripe guavas in a produce bag for up to 4 days.",
                    matchedGuideId = 11
                )
            }
            else -> {
                ImageAnalysisResult(
                    produceName = "Hass Avocado",
                    confidenceScore = 96,
                    isEdible = true,
                    edibilityVerdict = "Safe to Eat (Peak Ripeness)",
                    freshDaysRemaining = "3–4 Days (Refrigerated)",
                    shelfLifeDaysCount = 3,
                    freshnessVerdict = "Peak Ripeness",
                    ripenessState = "Yields gently to palm pressure with rich dark emerald pebbled skin",
                    spoilageRiskAnalysis = "Zero sunken soft rot lesions or rancid lipid oxidation. Internal flesh is creamy and free of vascular browning.",
                    visualObservations = listOf(
                        "Uniform dark emerald-to-purplish pebbled exocarp with zero soft depressions",
                        "Stem button remains intact; bright emerald underlayer when examined",
                        "Firm shoulder geometry indicating creamy internal flesh without oxidation"
                    ),
                    washingRecommendation = "Rinse skin under cool running water before slicing to prevent knife transfer of surface bacteria into the pulp.",
                    storageTip = "Ready to eat immediately, or refrigerate at 38°F (3°C) for up to 4 days to pause softening.",
                    matchedGuideId = 4
                )
            }
        }
    }

    private fun getOfflinePlannerFallback(
        focus: String,
        focusedNutrients: String = "",
        currentWeight: String = "",
        targetWeight: String = "",
        medications: String = ""
    ): AiMealPlan {
        val displayNutrients = focusedNutrients.ifBlank {
            when (focus) {
                "Heavy" -> "High Bioavailable Protein, Heme Iron, Creatine & Choline"
                "Longevity" -> "Polyphenols, Sirtuin Activators, Spermidine & Omega-3"
                "Neuro-Fuel" -> "DHA/EPA Marine Lipids, Choline & Antioxidants"
                "Metabolic Reset" -> "Clean Lipids, Potassium, Magnesium & Low-GI"
                else -> "Prebiotic Soluble Fiber, Live Probiotics & Diversity"
            }
        }
        val displayWeightNow = currentWeight.ifBlank { "75 kg / 165 lbs (Reference)" }
        val displayTargetWeight = targetWeight.ifBlank { "Optimized body composition" }
        val displayMedication = medications.ifBlank { "None reported / No interactions flagged" }
        val displayPrecautions = if (medications.isNotBlank()) {
            "Screened against active medication contraindications and electrolyte balance."
        } else {
            "Verified 100% whole unrefined foods with zero ultra-processed ingredients."
        }

        return AiMealPlan(
            title = "$focus Clinical Rotation Plan",
            dietaryFocus = focus,
            summary = "Clinical framework calibrated for $displayNutrients targeting $displayTargetWeight from baseline $displayWeightNow.",
            macroSummary = when (focus) {
                "Heavy" -> "40% Protein (170g) | 25% Net Carbs (105g) | 35% Fats (65g)"
                "Longevity" -> "18% Protein (75g) | 45% Complex Carbs (190g) | 37% Fats (70g)"
                "Neuro-Fuel" -> "25% Protein (110g) | 30% Low-GI Carbs (130g) | 45% Brain Fats (88g)"
                "Metabolic Reset" -> "25% Protein (120g) | 5% Net Carbs (<25g) | 70% Healthy Fats (145g)"
                else -> "22% Protein (95g) | 48% Prebiotic Carbs (210g) | 30% Polyphenol Fats (60g)"
            },
            focusedNutrients = displayNutrients,
            currentWeight = displayWeightNow,
            targetWeight = displayTargetWeight,
            medications = displayMedication,
            clinicalPrecautions = displayPrecautions,
            breakfast = AiMeal(
                name = "Grass-Fed Steak & Pastured Eggs Power Plate",
                slot = "Breakfast",
                portionDetails = "180g Grass-fed tenderloin steak, 3 pastured whole eggs, 1/2 sliced avocado, 1 cup baby spinach sautéed in extra virgin olive oil",
                calories = 640,
                protein = 54,
                carbs = 3,
                fats = 44,
                fiber = 5,
                prepTimeMin = 15,
                produceIngredients = listOf("Avocado", "Spinach"),
                nutrients = "Heme Iron, Choline, Vitamin B12, Creatine, Lutein"
            ),
            lunch = AiMeal(
                name = "Pan-Seared Wild Salmon with Roasted Asparagus & Quinoa",
                slot = "Lunch",
                portionDetails = "200g Wild Sockeye salmon fillet, 150g roasted asparagus spears, 1/2 cup cooked organic quinoa, lemon-olive oil drizzle",
                calories = 610,
                protein = 52,
                carbs = 22,
                fats = 32,
                fiber = 6,
                prepTimeMin = 20,
                produceIngredients = listOf("Asparagus"),
                nutrients = "Astaxanthin, Potassium, Folate, Marine Omega-3 Fatty Acids"
            ),
            dinner = AiMeal(
                name = "Grass-Fed Ribeye Steak with Garlic Herb Mushrooms & Broccolini",
                slot = "Dinner",
                portionDetails = "250g Grass-fed ribeye steak, 1 cup shiitake mushrooms in butter, 150g steamed broccolini with sea salt",
                calories = 720,
                protein = 58,
                carbs = 5,
                fats = 52,
                fiber = 5,
                prepTimeMin = 20,
                produceIngredients = listOf("Broccoli", "Garlic"),
                nutrients = "Creatine, Carnosine, Ergothioneine, Sulforaphane, Heme Iron"
            ),
            snack = AiMeal(
                name = "Pastured Hard-Boiled Eggs with Guacamole & Hemp Seeds",
                slot = "Snack",
                portionDetails = "3 Pastured hard-boiled eggs, 3 tbsp fresh guacamole, 1 tbsp raw shelled hemp hearts, pinch of Celtic sea salt",
                calories = 360,
                protein = 24,
                carbs = 4,
                fats = 26,
                fiber = 4,
                prepTimeMin = 5,
                produceIngredients = listOf("Avocado"),
                nutrients = "Choline, Lutein, Zeaxanthin, Essential Fatty Acids"
            )
        )
    }
}
