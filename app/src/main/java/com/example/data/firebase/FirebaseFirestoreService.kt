package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ConfidenceLevel
import com.example.data.model.RainbowColor
import com.example.data.model.RainbowLogEntry
import com.example.data.model.ScanResult
import com.example.data.repository.SproutAtlasRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreService(private val context: Context) {

    private val tag = "FirestoreService"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                Log.w(tag, "FirebaseApp not initialized. Firestore unavailable.")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize FirebaseFirestore", e)
            null
        }
    }

    private var userDocListener: ListenerRegistration? = null

    fun isAvailable(): Boolean = firestore != null

    /**
     * Listen in real-time to the current user's profile document in Firestore.
     * Updates SproutAtlasRepository whenever cloud data changes.
     */
    fun attachUserListener(uid: String, repository: SproutAtlasRepository) {
        val db = firestore ?: return
        userDocListener?.remove()

        try {
            val docRef = db.collection("users").document(uid)
            userDocListener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error listening to user document", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    applySnapshotToRepository(snapshot, repository)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach user snapshot listener", e)
        }
    }

    fun detachUserListener() {
        userDocListener?.remove()
        userDocListener = null
    }

    private fun applySnapshotToRepository(snapshot: DocumentSnapshot, repository: SproutAtlasRepository) {
        try {
            // 1. Saved item IDs
            val savedIds = (snapshot.get("savedItemIds") as? List<*>)
                ?.mapNotNull { (it as? Number)?.toInt() }
                ?.toSet()

            if (savedIds != null && savedIds.isNotEmpty()) {
                repository.setSavedItemIds(savedIds)
            }

            // 2. Streaks
            val quizStreak = (snapshot.get("quizStreak") as? Number)?.toInt()
            if (quizStreak != null) {
                repository.setQuizStreak(quizStreak)
            }

            val rainbowStreak = (snapshot.get("rainbowStreak") as? Number)?.toInt()
            if (rainbowStreak != null) {
                repository.setRainbowStreak(rainbowStreak)
            }

            // 3. Rainbow Logs
            val rainbowLogsMap = snapshot.get("rainbowLogs") as? Map<*, *>
            if (rainbowLogsMap != null) {
                val parsedLogs = mutableMapOf<RainbowColor, RainbowLogEntry>()
                RainbowColor.values().forEach { color ->
                    val colorData = rainbowLogsMap[color.name] as? Map<*, *>
                    if (colorData != null) {
                        val isLogged = colorData["isLogged"] as? Boolean ?: false
                        val produceName = colorData["produceName"] as? String
                        val loggedTime = colorData["loggedTime"] as? String
                        parsedLogs[color] = RainbowLogEntry(color, isLogged, produceName, loggedTime)
                    }
                }
                if (parsedLogs.isNotEmpty()) {
                    repository.setRainbowLogs(parsedLogs)
                }
            }

            // 4. Scan History
            val scansData = snapshot.get("scanHistory") as? List<*>
            if (scansData != null) {
                val parsedScans = scansData.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    try {
                        val id = map["id"] as? String ?: return@mapNotNull null
                        val produceName = map["produceName"] as? String ?: "Scanned Produce"
                        val dateGroup = map["dateGroup"] as? String ?: "Today"
                        val time = map["time"] as? String ?: "12:00 PM"
                        val confidenceStr = map["confidence"] as? String ?: "HIGH"
                        val confidence = try { ConfidenceLevel.valueOf(confidenceStr) } catch (_: Exception) { ConfidenceLevel.HIGH }
                        val quality = map["quality"] as? String ?: "Good"
                        val ripenessStage = map["ripenessStage"] as? String ?: "Peak ripe"
                        val shelfLifeDays = map["shelfLifeDays"] as? String ?: "3-5 days"
                        val freshnessCues = map["freshnessCues"] as? String ?: ""
                        val symbolId = map["symbolId"] as? String

                        ScanResult(
                            id = id,
                            produceName = produceName,
                            dateGroup = dateGroup,
                            time = time,
                            confidence = confidence,
                            quality = quality,
                            ripenessStage = ripenessStage,
                            shelfLifeDays = shelfLifeDays,
                            freshnessCues = freshnessCues,
                            symbolId = symbolId
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                if (parsedScans.isNotEmpty()) {
                    repository.setScanHistory(parsedScans)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing snapshot data", e)
        }
    }

    /**
     * Push all local repository state to Firestore for cloud persistence.
     */
    suspend fun syncRepositoryToCloud(uid: String, repository: SproutAtlasRepository): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val savedIds = repository.savedItemIds.value.toList()
            val quizStreak = repository.quizStreak.value
            val rainbowStreak = repository.rainbowStreak.value

            val rainbowLogs = repository.rainbowLogs.value.map { (color, entry) ->
                color.name to mapOf(
                    "isLogged" to entry.isLogged,
                    "produceName" to entry.produceName,
                    "loggedTime" to entry.loggedTime
                )
            }.toMap()

            val scanHistory = repository.scanHistory.value.map { scan ->
                mapOf(
                    "id" to scan.id,
                    "produceName" to scan.produceName,
                    "dateGroup" to scan.dateGroup,
                    "time" to scan.time,
                    "confidence" to scan.confidence.name,
                    "quality" to scan.quality,
                    "ripenessStage" to scan.ripenessStage,
                    "shelfLifeDays" to scan.shelfLifeDays,
                    "freshnessCues" to scan.freshnessCues,
                    "symbolId" to scan.symbolId
                )
            }

            val data = mapOf(
                "savedItemIds" to savedIds,
                "quizStreak" to quizStreak,
                "rainbowStreak" to rainbowStreak,
                "rainbowLogs" to rainbowLogs,
                "scanHistory" to scanHistory,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync repository to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Persist user's subscription and plan record in Firestore database:
     * 1. Under users/{uid} document (merged)
     * 2. In dedicated subscriptions/ audit collection for administrative tracking and cross-device lookups
     */
    suspend fun saveSubscriptionToCloud(
        uid: String,
        email: String?,
        planType: String,
        isPro: Boolean,
        expirationDate: String?,
        isTrial: Boolean,
        dailyAiLimit: Int = 200,
        durationSummary: String? = null
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val normalizedEmail = (email ?: "").trim().lowercase()
            val now = System.currentTimeMillis()
            val subscriptionData = mapOf(
                "isPro" to isPro,
                "planType" to planType,
                "activePlan" to planType,
                "email" to normalizedEmail,
                "uid" to uid,
                "expirationDate" to (expirationDate ?: ""),
                "isTrial" to isTrial,
                "hasUsedTrial" to (isTrial || isPro),
                "dailyAiLimit" to dailyAiLimit,
                "durationSummary" to (durationSummary ?: ""),
                "subscribedAt" to now,
                "updatedAt" to now
            )

            val userData = mapOf(
                "email" to normalizedEmail,
                "uid" to uid,
                "isPro" to isPro,
                "hasUsedTrial" to (isTrial || isPro),
                "subscription" to subscriptionData,
                "lastActiveAt" to now
            )

            // 1. Update user profile document in users collection
            db.collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .await()

            // 2. Also record in dedicated subscriptions collection indexed by email/uid
            val subDocId = if (normalizedEmail.isNotBlank()) {
                normalizedEmail.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
            } else {
                uid
            }
            db.collection("subscriptions").document(subDocId)
                .set(subscriptionData, SetOptions.merge())
                .await()

            Log.d(tag, "Successfully saved subscription record to Firestore database for $normalizedEmail ($planType)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to save subscription to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if this user (by UID or email) has previously consumed their one-time 14-day free trial.
     */
    suspend fun checkCloudTrialUsed(uid: String, email: String?): Boolean {
        val db = firestore ?: return false
        return try {
            // Check user doc
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val hasUsed = userDoc.getBoolean("hasUsedTrial")
                    ?: (userDoc.get("subscription.hasUsedTrial") as? Boolean)
                    ?: (userDoc.get("subscription.isTrial") as? Boolean)
                if (hasUsed == true) return true
            }

            // Check subscriptions doc by email
            val normalizedEmail = (email ?: "").trim().lowercase()
            if (normalizedEmail.isNotBlank()) {
                val subDocId = normalizedEmail.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
                val subDoc = db.collection("subscriptions").document(subDocId).get().await()
                if (subDoc.exists()) {
                    val hasUsed = subDoc.getBoolean("hasUsedTrial")
                        ?: subDoc.getBoolean("isTrial")
                    if (hasUsed == true) return true
                }
            }
            false
        } catch (e: Exception) {
            Log.w(tag, "Failed to check cloud trial history: ${e.message}")
            false
        }
    }
}
