package com.example.ai

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
import java.util.concurrent.TimeUnit

class GeminiTutorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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

    suspend fun askTutor(query: String): String = withContext(Dispatchers.IO) {
        val keys = configuredApiKeys
        if (keys.isEmpty()) {
            return@withContext getOfflineResponse(query)
        }

        val systemInstruction = "You are the Sprout Atlas Nutrition Tutor, a warm, authoritative, and friendly produce specialist. Answer questions about fruits, vegetables, culinary preparation, washing/pesticide removal, storage, ripeness signs, and nutritional profiles in 2–4 concise, clear sentences. Keep it practical, jargon-free, and grounded in whole food science."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", query) })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 300)
            })
        }

        val bodyString = jsonBody.toString()

        for (key in keys) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"
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
                } else {
                    val code = response.code
                    if (code == 429 || code == 403 || code == 500 || code == 503) {
                        continue
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }

        getOfflineResponse(query)
    }

    private fun getOfflineResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("mango skin") || q.contains("mango peel") ->
                "Technically yes, mango skin is edible and rich in fiber and mangiferin, but it contains urushiol (the compound in poison ivy) which can trigger contact dermatitis or mouth irritation in sensitive individuals. Most people prefer to peel it."

            q.contains("season") || q.contains("in season") ->
                "Currently in season: Crisp pome fruits (apples, pears), hearty brassicas (broccoli, kale, Brussels sprouts), and sweet root vegetables (carrots, sweet potatoes, beets). Citrus fruits are also entering peak winter sweetness!"

            q.contains("raw") || q.contains("safe to eat raw") ->
                "Most fresh produce in the Atlas is safe and nutrient-dense eaten raw after thorough washing. Notable exceptions include wild mushrooms, elderberries, cassava (yuca), and raw kidney beans, which require proper cooking to eliminate natural toxins or inhibitors."

            q.contains("store") || q.contains("storage") ->
                "General produce rule: Keep stone fruits, avocados, tomatoes, and melons on the counter until fully ripe, then refrigerate to pause decay. Leafy greens, berries, and brassicas should go straight to the fridge crisper in breathable containers."

            q.contains("garlic") && q.contains("substitute") ->
                "Great substitutes for garlic include shallots (milder, sweeter onion-garlic blend), garlic chives, leek whites, asafoetida (hing powder in Indian cooking), or green ramps in springtime."

            q.contains("avocado") ->
                "Avocados ripen off the tree! To speed ripening, place firm avocados in a brown paper bag with a banana or apple — the trapped natural ethylene gas accelerates ripening within 24–48 hours."

            q.contains("wash") || q.contains("pesticide") ->
                "A 10–12 minute soak in a 1% baking soda solution (1 tsp baking soda per 2 cups cold water) followed by a running water rinse is clinically shown to remove significantly more surface pesticide residues than plain water alone."

            else -> {
                // Try matching a produce name
                val matched = ProduceCatalog.allItems.find { q.contains(it.name.lowercase()) }
                if (matched != null) {
                    "${matched.name} is a ${matched.type.lowercase()} in the ${matched.category} family. Peak nutrients include ${matched.nutrients.take(3).joinToString(", ")}. Storage tip: ${matched.storage}."
                } else {
                    "Sprout Atlas Guide: Whole fruits and vegetables provide optimal synergistic vitamins, enzymes, and prebiotic fibers. For best retention, wash just before eating and minimize high-heat boiling in excess water."
                }
            }
        }
    }
}
