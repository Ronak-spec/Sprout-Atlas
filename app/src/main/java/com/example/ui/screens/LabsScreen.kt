package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiMeal
import com.example.ai.AiMealPlan
import com.example.ai.ChatMessage
import com.example.ai.GeminiService
import com.example.ai.ImageAnalysisResult
import com.example.data.datasource.PlannerData
import com.example.data.datasource.ProduceCatalog
import com.example.data.model.ConfidenceLevel
import com.example.data.model.RainbowColor
import com.example.data.model.ScanResult
import com.example.data.repository.SproutAtlasRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.subscription.SubscriptionService
import com.example.data.subscription.SproutSubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LabsScreen(
    repository: SproutAtlasRepository,
    geminiService: GeminiService,
    subscriptionService: SubscriptionService? = null,
    onOpenPaywall: () -> Unit = {},
    onNavigateToGuideByName: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedLabTab by remember { mutableStateOf("planner") } // "planner", "tutor", "scanner", "rainbow"

    val subState by (subscriptionService?.subscriptionState ?: MutableStateFlow(SproutSubscriptionState())).collectAsState()
    val isPro = subState.isPro
    val dailyLabsUsage by repository.dailyLabsUsage.collectAsState()
    val remainingCredits = repository.getRemainingLabsCredits(isPro)

    // ----------------------------------------------------------------
    // 1. SCANNER STATE (Analyze Images using Multimodal Vision AI)
    // ----------------------------------------------------------------
    var isScanningImage by remember { mutableStateOf(false) }
    var scannedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageAnalysisResult by remember { mutableStateOf<ImageAnalysisResult?>(null) }
    val scanHistory by repository.scanHistory.collectAsState()

    fun handleScannedBitmap(bitmap: Bitmap, specimenHint: String = "") {
        if (!repository.canUseLabFeature(isPro)) {
            onOpenPaywall()
            return
        }
        repository.recordLabUsage(isPro)
        scannedImageBitmap = bitmap
        isScanningImage = true
        scope.launch {
            val result = geminiService.analyzeProduceImage(bitmap, specimenHint)
            imageAnalysisResult = result
            repository.addScan(
                ScanResult(
                    id = "scan-${System.currentTimeMillis()}",
                    produceName = result.produceName,
                    dateGroup = "Today",
                    time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                    confidence = if (result.confidenceScore >= 90) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM,
                    quality = result.freshnessVerdict,
                    ripenessStage = result.ripenessState,
                    shelfLifeDays = result.freshDaysRemaining,
                    freshnessCues = result.visualObservations.joinToString(" • "),
                    isEdible = result.isEdible,
                    edibilityVerdict = result.edibilityVerdict,
                    symbolId = "d-${result.produceName.lowercase().replace(" ", "").replace("-", "")}"
                )
            )
            isScanningImage = false
            if (!isPro && repository.getRemainingLabsCredits(isPro) <= 0) {
                kotlinx.coroutines.delay(1200L)
                onOpenPaywall()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = decodeSampledBitmapFromUri(context, uri, 600, 600)
            if (bitmap != null) {
                handleScannedBitmap(bitmap)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            handleScannedBitmap(bitmap)
        }
    }

    // ----------------------------------------------------------------
    // 2. TUTOR CHATBOT STATE (Botanical Science Tutor)
    // ----------------------------------------------------------------
    var chatInput by remember { mutableStateOf("") }
    var isTutorThinking by remember { mutableStateOf(false) }
    val chatHistory = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hello! I am your Sprout Atlas Botanical Science & Nutrition Tutor. Ask me anything about whole-food biology, pesticide removal, peel edibility, enzyme retention, or seasonal alternatives!",
                isUser = false
            )
        )
    }
    val chatListState = rememberLazyListState()

    // ----------------------------------------------------------------
    // 3. CLINICAL AI MEAL PLANNER STATE (Clinical Consultation Intake)
    // ----------------------------------------------------------------
    var selectedArchetypeToken by remember { mutableStateOf("Heavy") }
    var isIntakeExpanded by remember { mutableStateOf(false) }
    var focusedNutrientsInput by remember { mutableStateOf("") }
    val selectedNutrientChips = remember {
        mutableStateListOf(
            "High Bioavailable Protein",
            "Potassium & Magnesium"
        )
    }

    var currentWeightInput by remember { mutableStateOf("74") }
    var targetWeightInput by remember { mutableStateOf("68") }
    var weightUnit by remember { mutableStateOf("kg") } // "kg" or "lbs"
    var selectedGoalDirection by remember { mutableStateOf("Lean Muscle Hypertrophy") }

    var activeMedicationsInput by remember { mutableStateOf("") }
    val selectedMedicationChips = remember {
        mutableStateListOf("None / No Active Prescriptions")
    }

    var customIngredientsInput by remember { mutableStateOf("") }
    var isGeneratingPlan by remember { mutableStateOf(false) }
    var aiMealPlan by remember { mutableStateOf<AiMealPlan?>(null) }

    // Initialize or get default plan
    val activeCategory = PlannerData.categories.find { it.token == selectedArchetypeToken } ?: PlannerData.categories.first()
    val staticDefaultMeals = remember(selectedArchetypeToken) {
        PlannerData.getDayPlan(selectedArchetypeToken)
    }

    // ----------------------------------------------------------------
    // 4. RAINBOW CHALLENGE STATE
    // ----------------------------------------------------------------
    val rainbowLogs by repository.rainbowLogs.collectAsState()
    val rainbowStreak by repository.rainbowStreak.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutPaper)
            .testTag("labs_screen")
    ) {
        // TOP HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SproutPaperCard)
                .border(
                    width = 1.2.dp,
                    color = SproutLine.copy(alpha = 0.35f),
                    shape = androidx.compose.ui.graphics.RectangleShape
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            EyebrowHeader(text = "Experimental features")
            Text(
                text = "Labs",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutInk
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Lab segmented tabs matching HTML design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SproutPaperCard)
                    .border(1.3.dp, SproutLine.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LabSegmentTab(
                    title = "Nutrition\nTutor",
                    icon = { Icon(Icons.Outlined.Forum, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selected = selectedLabTab == "tutor",
                    accentColor = SproutLeaf,
                    onClick = { selectedLabTab = "tutor" },
                    modifier = Modifier.weight(1f)
                )
                LabSegmentTab(
                    title = "Meal\nPlanner",
                    icon = { Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selected = selectedLabTab == "planner",
                    accentColor = SproutTomato,
                    onClick = { selectedLabTab = "planner" },
                    modifier = Modifier.weight(1f)
                )
                LabSegmentTab(
                    title = "Rainbow\nChallenge",
                    icon = { Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selected = selectedLabTab == "rainbow",
                    accentColor = SproutCitrus,
                    onClick = { selectedLabTab = "rainbow" },
                    modifier = Modifier.weight(1f)
                )
                LabSegmentTab(
                    title = "Scanner\nVision",
                    icon = { Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selected = selectedLabTab == "scanner",
                    accentColor = Color(0xFF64B5F6),
                    onClick = { selectedLabTab = "scanner" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Material 3 Botanical Quota & Pro Access Banner (Compact & Scrollable)
            Spacer(modifier = Modifier.height(6.dp))

            if (isPro) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF4F9F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "Sprout Pro Active",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            )
                            Text(
                                text = "• 200 AI runs/day",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = SproutInkSoft
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFF2E7D32))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$remainingCredits / ${SproutAtlasRepository.PRO_DAILY_LABS_LIMIT} LEFT",
                                style = TextStyle(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.4.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            } else {
                val isLimitReached = remainingCredits <= 0
                val usedCredits = (SproutAtlasRepository.FREE_DAILY_LABS_LIMIT - remainingCredits).coerceAtLeast(0)
                val progressFraction = (usedCredits.toFloat() / SproutAtlasRepository.FREE_DAILY_LABS_LIMIT).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (!isLimitReached) Color(0xFFD3E7D0) else Color(0xFFFFCCBC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Icon + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (!isLimitReached) Color(0xFFE8F5E9) else Color(0xFFFFECE0)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (!isLimitReached) Icons.Filled.AutoAwesome else Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = if (!isLimitReached) Color(0xFF2E7D32) else Color(0xFFD84315),
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            Text(
                                text = if (!isLimitReached) "Free Daily AI Runs" else "Free Limit Reached",
                                style = TextStyle(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isLimitReached) SproutInk else Color(0xFFBF360C)
                                )
                            )
                        }

                        // Counter Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (!isLimitReached) Color(0xFFE8F5E9) else Color(0xFFFFECE0)
                                )
                                .border(
                                    1.dp,
                                    if (!isLimitReached) Color(0xFFA5D6A7) else Color(0xFFFFAB91),
                                    RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = if (!isLimitReached) "$remainingCredits / ${SproutAtlasRepository.FREE_DAILY_LABS_LIMIT} LEFT" else "0 LEFT",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp,
                                    color = if (!isLimitReached) Color(0xFF2E7D32) else Color(0xFFD84315)
                                )
                            )
                        }

                        // Mini Slim Progress Bar
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFFF0F4EF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = progressFraction)
                                    .background(if (!isLimitReached) Color(0xFF2E7D32) else Color(0xFFD84315))
                            )
                        }

                        // Small Upgrade Action Button
                        Button(
                            onClick = { onOpenPaywall() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Unlock Pro",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SproutLine, thickness = 1.dp)

        // LAB TAB CONTENT
        when (selectedLabTab) {
            "planner" -> {
                // ----------------------------------------------------------------
                // 1. CLINICAL AI MEAL PLANNER TAB (Clinical Whole-Food Engine)
                // ----------------------------------------------------------------
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // -------------------------------------------------------------
                    // 1. CLINICAL INTAKE & FORMULATION STUDIO
                    // -------------------------------------------------------------
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(SproutPaperCard)
                                .border(1.3.dp, SproutLine, RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Header & Reset
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        EyebrowHeader(text = "Clinical Dietetic Consultation")
                                        Text(
                                            text = "Personalized Meal Planner",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SproutInk
                                            )
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedNutrientChips.clear()
                                            selectedNutrientChips.addAll(listOf("High Bioavailable Protein", "Potassium & Magnesium"))
                                            selectedMedicationChips.clear()
                                            selectedMedicationChips.add("None / No Active Prescriptions")
                                            focusedNutrientsInput = ""
                                            currentWeightInput = "74"
                                            targetWeightInput = "68"
                                            activeMedicationsInput = ""
                                            customIngredientsInput = ""
                                            aiMealPlan = null
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reset intake fields", tint = SproutLeafDark)
                                    }
                                }

                                Text(
                                    text = "Prescriptive whole-food nutrition calibrated to metabolic targets, weight trajectory, and botanical pharmacology.",
                                    fontSize = 14.5.sp,
                                    lineHeight = 21.sp,
                                    color = SproutInkSoft
                                )

                                // -------------------------------------------------------------
                                // ARCHETYPE CAROUSEL
                                // -------------------------------------------------------------
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Dietary Focus Archetype",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.5.sp,
                                        color = SproutInk
                                    )

                                    val catScroll = rememberScrollState()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(catScroll),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PlannerData.categories.forEach { cat ->
                                            val isSelected = cat.token == selectedArchetypeToken
                                            val emoji = when (cat.token) {
                                                "Heavy" -> "🥩"
                                                "Longevity" -> "🍇"
                                                "Neuro-Fuel" -> "🧠"
                                                "Metabolic Reset" -> "🥑"
                                                else -> "🌿"
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(if (isSelected) SproutInk else SproutPaper)
                                                    .border(
                                                        1.2.dp,
                                                        if (isSelected) SproutInk else SproutLine,
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { selectedArchetypeToken = cat.token }
                                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(emoji, fontSize = 15.sp)
                                                    Text(
                                                        text = cat.token,
                                                        style = TextStyle(
                                                            fontSize = 14.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) SproutPaper else SproutInk
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Active Archetype Details Banner
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SproutPaper)
                                            .border(1.dp, SproutLine.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = activeCategory.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.5.sp,
                                                    color = SproutInk
                                                )
                                            }
                                            Text(
                                                text = activeCategory.macro,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SproutTomato
                                            )
                                            Text(
                                                text = activeCategory.philosophy,
                                                fontSize = 13.5.sp,
                                                color = SproutInkSoft,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = SproutLine.copy(alpha = 0.6f), thickness = 1.dp)

                                // -------------------------------------------------------------
                                // COLLAPSIBLE INTAKE & BIOMETRICS STUDIO
                                // -------------------------------------------------------------
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // Expand / Collapse Header Strip
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isIntakeExpanded) SproutLeafLight.copy(alpha = 0.5f) else SproutPaper)
                                            .border(1.dp, if (isIntakeExpanded) SproutLeaf else SproutLine, RoundedCornerShape(12.dp))
                                            .clickable { isIntakeExpanded = !isIntakeExpanded }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("⚙️", fontSize = 16.sp)
                                            Column {
                                                Text(
                                                    text = "Intake Biometrics & Prescriptions",
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SproutInk
                                                )
                                                Text(
                                                    text = if (isIntakeExpanded) "Tap to collapse consultation" else "$currentWeightInput $weightUnit ➔ $targetWeightInput $weightUnit · ${selectedNutrientChips.firstOrNull()?.take(20) ?: "Custom"}",
                                                    fontSize = 12.5.sp,
                                                    color = SproutInkSoft
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (isIntakeExpanded) "▲ Hide" else "▼ Edit",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SproutLeafDark
                                        )
                                    }

                                    // Expanded Form Fields with Smooth Visibility
                                    AnimatedVisibility(visible = isIntakeExpanded) {
                                        Column(
                                            modifier = Modifier.padding(top = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            // STEP 1: FOCUSED NUTRIENTS
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(SproutLeafLight),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("1", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SproutLeafDark)
                                                    }
                                                    Text(
                                                        text = "Targeted Micronutrients",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = SproutInk
                                                    )
                                                }

                                                // Nutrient Quick Chips
                                                val nutrientOptions = listOf(
                                                    "High Bioavailable Protein",
                                                    "Potassium & Magnesium",
                                                    "Prebiotic Soluble Fiber",
                                                    "Polyphenols & Omega-3",
                                                    "Bioavailable Iron & B12",
                                                    "Low Glycemic Index",
                                                    "Calcium & Vitamin D3",
                                                    "Sulforaphane & Antioxidants"
                                                )

                                                val nutrientScroll = rememberScrollState()
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(nutrientScroll),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    nutrientOptions.forEach { nutrient ->
                                                        val isChecked = selectedNutrientChips.contains(nutrient)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(100.dp))
                                                                .background(if (isChecked) SproutLeafLight else SproutPaper)
                                                                .border(
                                                                    1.dp,
                                                                    if (isChecked) SproutLeaf else SproutLine,
                                                                    RoundedCornerShape(100.dp)
                                                                )
                                                                .clickable {
                                                                    if (isChecked) {
                                                                        selectedNutrientChips.remove(nutrient)
                                                                    } else {
                                                                        selectedNutrientChips.add(nutrient)
                                                                    }
                                                                }
                                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                if (isChecked) {
                                                                    Icon(
                                                                        Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = SproutLeafDark,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                                Text(
                                                                    text = nutrient,
                                                                    fontSize = 13.5.sp,
                                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                                                    color = if (isChecked) SproutLeafDark else SproutInk
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Custom nutrient text field
                                                TextField(
                                                    value = focusedNutrientsInput,
                                                    onValueChange = { focusedNutrientsInput = it },
                                                    placeholder = {
                                                        Text(
                                                            "Additional nutrient targets (e.g. 170g protein, zinc, creatine)...",
                                                            fontSize = 13.5.sp,
                                                            color = SproutInkSoft
                                                        )
                                                    },
                                                    textStyle = TextStyle(fontSize = 14.5.sp, color = SproutInk),
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = SproutPaper,
                                                        unfocusedContainerColor = SproutPaper,
                                                        focusedIndicatorColor = SproutLeaf,
                                                        unfocusedIndicatorColor = SproutLine
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )
                                            }

                                            // STEP 2: WEIGHT BIOMETRICS
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .clip(CircleShape)
                                                                .background(SproutLeafLight),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SproutLeafDark)
                                                        }
                                                        Text(
                                                            text = "Baseline & Target Weight",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = SproutInk
                                                        )
                                                    }

                                                    // Unit Selector
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(100.dp))
                                                            .background(SproutPaper)
                                                            .border(1.dp, SproutLine, RoundedCornerShape(100.dp))
                                                            .padding(2.dp)
                                                    ) {
                                                        listOf("kg", "lbs").forEach { unit ->
                                                            val isUnitSelected = weightUnit == unit
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(100.dp))
                                                                    .background(if (isUnitSelected) SproutInk else Color.Transparent)
                                                                    .clickable {
                                                                        if (weightUnit != unit) {
                                                                            weightUnit = unit
                                                                            val cur = currentWeightInput.toDoubleOrNull()
                                                                            val tgt = targetWeightInput.toDoubleOrNull()
                                                                            if (cur != null) {
                                                                                currentWeightInput = if (unit == "lbs") "%.0f".format(cur * 2.20462) else "%.0f".format(cur / 2.20462)
                                                                            }
                                                                            if (tgt != null) {
                                                                                targetWeightInput = if (unit == "lbs") "%.0f".format(tgt * 2.20462) else "%.0f".format(tgt / 2.20462)
                                                                            }
                                                                        }
                                                                    }
                                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    text = unit,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = if (isUnitSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    color = if (isUnitSelected) SproutPaper else SproutInk
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Weight Inputs
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("Current Weight", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SproutInkSoft)
                                                        TextField(
                                                            value = currentWeightInput,
                                                            onValueChange = { currentWeightInput = it },
                                                            trailingIcon = { Text(weightUnit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SproutInkSoft, modifier = Modifier.padding(end = 8.dp)) },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SproutInk),
                                                            colors = TextFieldDefaults.colors(
                                                                focusedContainerColor = SproutPaper,
                                                                unfocusedContainerColor = SproutPaper,
                                                                focusedIndicatorColor = SproutLeaf,
                                                                unfocusedIndicatorColor = SproutLine
                                                            ),
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier.fillMaxWidth(),
                                                            singleLine = true
                                                        )
                                                    }

                                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("Target Weight", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SproutTomato)
                                                        TextField(
                                                            value = targetWeightInput,
                                                            onValueChange = { targetWeightInput = it },
                                                            trailingIcon = { Text(weightUnit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SproutTomato, modifier = Modifier.padding(end = 8.dp)) },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SproutInk),
                                                            colors = TextFieldDefaults.colors(
                                                                focusedContainerColor = SproutPaper,
                                                                unfocusedContainerColor = SproutPaper,
                                                                focusedIndicatorColor = SproutTomato,
                                                                unfocusedIndicatorColor = SproutLine
                                                            ),
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier.fillMaxWidth(),
                                                            singleLine = true
                                                        )
                                                    }
                                                }

                                                // Goal Direction Selector
                                                val goalOptions = listOf(
                                                    "Lean Muscle Hypertrophy",
                                                    "Caloric Deficit (Fat Loss)",
                                                    "Metabolic Reset",
                                                    "Longevity Cycling"
                                                )

                                                val goalScroll = rememberScrollState()
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(goalScroll),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    goalOptions.forEach { goal ->
                                                        val isGoalSelected = selectedGoalDirection == goal
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(100.dp))
                                                                .background(if (isGoalSelected) SproutCitrus.copy(alpha = 0.2f) else SproutPaper)
                                                                .border(1.dp, if (isGoalSelected) SproutCitrus else SproutLine, RoundedCornerShape(100.dp))
                                                                .clickable { selectedGoalDirection = goal }
                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = goal,
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isGoalSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isGoalSelected) Color(0xFF9A5B00) else SproutInk
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // STEP 3: PHARMACOLOGY & MEDICATIONS
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(SproutTomatoLight),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("3", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SproutTomato)
                                                    }
                                                    Text(
                                                        text = "Active Medications & Pharmacology Screen",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = SproutInk
                                                    )
                                                }

                                                // Medication Presets
                                                val medOptions = listOf(
                                                    "None / No Active Prescriptions",
                                                    "Blood Pressure (ACE/ARBs)",
                                                    "Metformin / Insulin",
                                                    "Statins (Lipid-Lowering)",
                                                    "Blood Thinners (Warfarin)",
                                                    "Thyroid (Levothyroxine)",
                                                    "SSRIs / Antidepressants"
                                                )

                                                val medScroll = rememberScrollState()
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(medScroll),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    medOptions.forEach { med ->
                                                        val isMedChecked = selectedMedicationChips.contains(med)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(100.dp))
                                                                .background(if (isMedChecked) SproutTomatoLight else SproutPaper)
                                                                .border(
                                                                    1.dp,
                                                                    if (isMedChecked) SproutTomato else SproutLine,
                                                                    RoundedCornerShape(100.dp)
                                                                )
                                                                .clickable {
                                                                    if (med == "None / No Active Prescriptions") {
                                                                        selectedMedicationChips.clear()
                                                                        selectedMedicationChips.add(med)
                                                                    } else {
                                                                        selectedMedicationChips.remove("None / No Active Prescriptions")
                                                                        if (isMedChecked) {
                                                                            selectedMedicationChips.remove(med)
                                                                            if (selectedMedicationChips.isEmpty()) {
                                                                                selectedMedicationChips.add("None / No Active Prescriptions")
                                                                            }
                                                                        } else {
                                                                            selectedMedicationChips.add(med)
                                                                        }
                                                                    }
                                                                }
                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                if (isMedChecked) {
                                                                    Icon(
                                                                        Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = SproutTomato,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                                Text(
                                                                    text = med,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = if (isMedChecked) FontWeight.Bold else FontWeight.Normal,
                                                                    color = if (isMedChecked) SproutTomato else SproutInk
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Custom medication input
                                                TextField(
                                                    value = activeMedicationsInput,
                                                    onValueChange = { activeMedicationsInput = it },
                                                    placeholder = {
                                                        Text(
                                                            "Specific prescription details (e.g. Lisinopril 10mg, Atorvastatin)...",
                                                            fontSize = 13.5.sp,
                                                            color = SproutInkSoft
                                                        )
                                                    },
                                                    textStyle = TextStyle(fontSize = 14.5.sp, color = SproutInk),
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = SproutPaper,
                                                        unfocusedContainerColor = SproutPaper,
                                                        focusedIndicatorColor = SproutTomato,
                                                        unfocusedIndicatorColor = SproutLine
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )
                                            }

                                            // STEP 4: PANTRY PRODUCE ON HAND
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "🥕 Produce on Hand & Kitchen Ingredients (Optional)",
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SproutInk
                                                )
                                                TextField(
                                                    value = customIngredientsInput,
                                                    onValueChange = { customIngredientsInput = it },
                                                    placeholder = {
                                                        Text(
                                                            "e.g. Sockeye salmon, avocados, sweet potatoes, broccoli, berries...",
                                                            fontSize = 13.5.sp,
                                                            color = SproutInkSoft
                                                        )
                                                    },
                                                    textStyle = TextStyle(fontSize = 14.5.sp, color = SproutInk),
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = SproutPaper,
                                                        unfocusedContainerColor = SproutPaper,
                                                        focusedIndicatorColor = SproutLeaf,
                                                        unfocusedIndicatorColor = SproutLine
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )
                                            }
                                        }
                                    }
                                }

                                // -------------------------------------------------------------
                                // GENERATE ACTION BUTTON
                                // -------------------------------------------------------------
                                Button(
                                    onClick = {
                                        if (!repository.canUseLabFeature(isPro)) {
                                            onOpenPaywall()
                                            return@Button
                                        }
                                        repository.recordLabUsage(isPro)
                                        isGeneratingPlan = true
                                        val combinedNutrients = (selectedNutrientChips + listOf(focusedNutrientsInput))
                                            .filter { it.isNotBlank() }
                                            .joinToString(", ")
                                        val currentWeightStr = "$currentWeightInput $weightUnit"
                                        val targetWeightStr = "$targetWeightInput $weightUnit ($selectedGoalDirection)"
                                        val combinedMeds = (selectedMedicationChips + listOf(activeMedicationsInput))
                                            .filter { it.isNotBlank() }
                                            .joinToString(", ")

                                        scope.launch {
                                            aiMealPlan = geminiService.generateAiMealPlan(
                                                focus = selectedArchetypeToken,
                                                focusedNutrients = combinedNutrients,
                                                currentWeight = currentWeightStr,
                                                targetWeight = targetWeightStr,
                                                medications = combinedMeds,
                                                availableIngredients = customIngredientsInput
                                            )
                                            isGeneratingPlan = false
                                            if (!isPro && repository.getRemainingLabsCredits(isPro) <= 0) {
                                                kotlinx.coroutines.delay(1200L)
                                                onOpenPaywall()
                                            }
                                        }
                                    },
                                    enabled = !isGeneratingPlan,
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SproutTomato,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("generate_plan_btn")
                                ) {
                                    if (isGeneratingPlan) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Formulating Clinical Day Plan...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.RestaurantMenu,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Generate Personalized Clinical Plan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Menu Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EyebrowHeader(
                                text = if (aiMealPlan != null) "AI CLINICAL MEAL PLAN & INTAKE PROFILE" else "PRESCRIBED WHOLE-FOOD DAILY ROTATION"
                            )
                            if (aiMealPlan != null) {
                                TextButton(onClick = { aiMealPlan = null }) {
                                    Text("Reset to Preset", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SproutLeafDark)
                                }
                            }
                        }
                    }

                    // Display AI Generated Meals with Clinical Intake Summary or Catalog Presets
                    if (aiMealPlan != null) {
                        val plan = aiMealPlan!!

                        // Clinical Patient Trajectory & Safety Clearance Banner Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(SproutPaperCard)
                                    .border(1.3.dp, SproutLeaf, RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = plan.title,
                                            style = TextStyle(
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SproutInk
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(SproutLeafLight)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "CLINICAL MATCH",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SproutLeafDark
                                            )
                                        }
                                    }

                                    // Weight Trajectory & Target Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SproutPaper)
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text("Baseline Weight", fontSize = 12.sp, color = SproutInkSoft)
                                                Text(plan.currentWeight, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SproutInk)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SproutPaper)
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text("Target Goal Weight", fontSize = 12.sp, color = SproutTomato)
                                                Text(plan.targetWeight, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SproutTomato)
                                            }
                                        }
                                    }

                                    // Total Day Macro Strip
                                    val dayProtein = plan.breakfast.protein + plan.lunch.protein + plan.dinner.protein + plan.snack.protein
                                    val dayCarbs = plan.breakfast.carbs + plan.lunch.carbs + plan.dinner.carbs + plan.snack.carbs
                                    val dayFats = plan.breakfast.fats + plan.lunch.fats + plan.dinner.fats + plan.snack.fats
                                    val dayFiber = plan.breakfast.fiber + plan.lunch.fiber + plan.dinner.fiber + plan.snack.fiber

                                    MacroVisualStrip(
                                        protein = dayProtein,
                                        carbs = dayCarbs,
                                        fats = dayFats,
                                        fiber = dayFiber
                                    )

                                    if (plan.macroSummary.isNotBlank()) {
                                        Text(
                                            text = "Clinical Focus: ${plan.macroSummary}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SproutTomato
                                        )
                                    }

                                    // Summary text
                                    Text(
                                        text = plan.summary,
                                        fontSize = 14.5.sp,
                                        lineHeight = 21.sp,
                                        color = SproutInkSoft
                                    )

                                    // Clinical Safety Screen Note
                                    if (plan.clinicalPrecautions.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFBF4E8))
                                                .border(1.dp, Color(0xFFE8D4B8), RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("🛡️", fontSize = 15.sp)
                                                Text(
                                                    text = "Pharmacology Safety Screen: ${plan.clinicalPrecautions}",
                                                    fontSize = 13.5.sp,
                                                    lineHeight = 19.sp,
                                                    color = Color(0xFF6E4515)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            ClinicalMealCard(
                                slot = "Breakfast",
                                meal = plan.breakfast,
                                onNavigate = onNavigateToGuideByName
                            )
                        }
                        item {
                            ClinicalMealCard(
                                slot = "Lunch",
                                meal = plan.lunch,
                                onNavigate = onNavigateToGuideByName
                            )
                        }
                        item {
                            ClinicalMealCard(
                                slot = "Dinner",
                                meal = plan.dinner,
                                onNavigate = onNavigateToGuideByName
                            )
                        }
                        item {
                            ClinicalMealCard(
                                slot = "Snack",
                                meal = plan.snack,
                                onNavigate = onNavigateToGuideByName
                            )
                        }
                    } else {
                        items(staticDefaultMeals, key = { it.id }) { meal ->
                            ClinicalMealCard(
                                slot = meal.type,
                                meal = AiMeal(
                                    name = meal.name,
                                    slot = meal.type,
                                    calories = meal.calories,
                                    protein = meal.protein,
                                    carbs = meal.carbs,
                                    fats = meal.fats,
                                    fiber = meal.fiber,
                                    prepTimeMin = meal.prepTimeMin,
                                    portionDetails = meal.portionDetails,
                                    produceIngredients = emptyList(),
                                    nutrients = meal.highlights
                                ),
                                onNavigate = onNavigateToGuideByName
                            )
                        }
                    }
                }
            }

            "tutor" -> {
                // ----------------------------------------------------------------
                // 2. BOTANICAL SCIENCE TUTOR CHAT (Conversational Chatbot)
                // ----------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Chat header and prompt chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProduceDoodle(
                                symbolId = "d-sprout",
                                archetype = "leafy",
                                name = "Sprout",
                                size = 28.dp
                            )
                            Column {
                                Text("Botanical Science Tutor", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SproutInk)
                                Text("Field Science & Produce Nutrition", fontFamily = FontFamily.Default, fontSize = 13.5.sp, color = SproutInkSoft)
                            }
                        }

                        IconButton(
                            onClick = {
                                chatHistory.clear()
                                chatHistory.add(
                                    ChatMessage(
                                        text = "Session refreshed. Ask me anything about produce biology, nutrients, storage, or food safety!",
                                        isUser = false
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Clear chat", tint = SproutInkSoft)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Suggested quick chips
                    val quickPrompts = listOf(
                        "Is mango skin edible?",
                        "How to wash off apple wax?",
                        "Why do onions make you cry?",
                        "How to store avocados?"
                    )
                    val chipScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipScroll)
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPrompts.forEach { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(SproutPaperCard)
                                    .border(1.dp, SproutLine, RoundedCornerShape(100.dp))
                                    .clickable {
                                        if (!repository.canUseLabFeature(isPro)) {
                                            onOpenPaywall()
                                            return@clickable
                                        }
                                        repository.recordLabUsage(isPro)
                                        chatHistory.add(ChatMessage(text = q, isUser = true))
                                        isTutorThinking = true
                                        scope.launch {
                                            val reply = geminiService.sendChatMessage(chatHistory, q)
                                            chatHistory.add(ChatMessage(text = reply, isUser = false))
                                            isTutorThinking = false
                                            chatListState.animateScrollToItem(chatHistory.size - 1)
                                            if (!isPro && repository.getRemainingLabsCredits(isPro) <= 0) {
                                                kotlinx.coroutines.delay(1200L)
                                                onOpenPaywall()
                                            }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(q, fontSize = 13.sp, color = SproutInk)
                            }
                        }
                    }

                    // Scrollable Message Thread
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chatHistory, key = { it.id }) { msg ->
                            if (msg.isUser) {
                                // User message (Right)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 290.dp)
                                            .clip(RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp))
                                            .background(SproutInk)
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                color = SproutPaper,
                                                lineHeight = 21.sp
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Tutor message (Left)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        ProduceDoodle(
                                            symbolId = "d-sprout",
                                            archetype = "leafy",
                                            name = "Sprout",
                                            size = 28.dp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .widthIn(max = 300.dp)
                                                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp))
                                                .background(SproutPaperCard)
                                                .border(1.3.dp, SproutLine, RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp))
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = msg.text,
                                                style = TextStyle(
                                                    fontSize = 15.sp,
                                                    color = SproutInk,
                                                    lineHeight = 21.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isTutorThinking) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(SproutPaperCard)
                                            .border(1.dp, SproutLeaf, RoundedCornerShape(100.dp))
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = SproutLeaf,
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Text("Consulting botanical research & food science...", fontSize = 13.sp, color = SproutLeafDark)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Chat Input Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100.dp))
                            .background(SproutPaperCard)
                            .border(1.3.dp, SproutInk, RoundedCornerShape(100.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Ask the botanical produce tutor...", fontSize = 14.5.sp, color = SproutInkSoft) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 15.sp, color = SproutInk)
                        )

                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank() && !isTutorThinking) {
                                    if (!repository.canUseLabFeature(isPro)) {
                                        onOpenPaywall()
                                        return@IconButton
                                    }
                                    repository.recordLabUsage(isPro)
                                    val q = chatInput.trim()
                                    chatInput = ""
                                    chatHistory.add(ChatMessage(text = q, isUser = true))
                                    isTutorThinking = true
                                    scope.launch {
                                        val reply = geminiService.sendChatMessage(chatHistory, q)
                                        chatHistory.add(ChatMessage(text = reply, isUser = false))
                                        isTutorThinking = false
                                        chatListState.animateScrollToItem(chatHistory.size - 1)
                                        if (!isPro && repository.getRemainingLabsCredits(isPro) <= 0) {
                                            kotlinx.coroutines.delay(1200L)
                                            onOpenPaywall()
                                        }
                                    }
                                }
                            },
                            enabled = chatInput.isNotBlank() && !isTutorThinking
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = if (chatInput.isNotBlank()) SproutLeafDark else SproutInkSoft
                            )
                        }
                    }
                }
            }

            "scanner" -> {
                // ----------------------------------------------------------------
                // 3. BOTANICAL VISION SCANNER TAB (Multimodal Image Understanding)
                // ----------------------------------------------------------------
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SproutInk)
                                .border(1.5.dp, Color(0x35FAF6E9), RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SproutCitrus, modifier = Modifier.size(18.dp))
                                    EyebrowHeader(text = "Botanical Vision & Spoilage AI", color = SproutCitrus)
                                }

                                if (scannedImageBitmap != null) {
                                    Image(
                                        bitmap = scannedImageBitmap!!.asImageBitmap(),
                                        contentDescription = "Scanned Specimen",
                                        modifier = Modifier
                                            .size(130.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(2.dp, SproutCitrus, RoundedCornerShape(16.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x1FFAF6E9))
                                            .border(2.dp, SproutCitrus, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isScanningImage) {
                                            CircularProgressIndicator(
                                                color = SproutCitrus,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Scan produce",
                                                tint = SproutCitrus,
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = if (isScanningImage) "Analyzing Freshness & Edibility with Gemini..." else "Produce Freshness & Edibility Inspector",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SproutPaper,
                                        textAlign = TextAlign.Center
                                    )
                                )

                                Text(
                                    text = "Upload a photo or capture live produce to inspect botanical identity, calculate exact days of freshness remaining, evaluate mold/spoilage risks, and verify if it's safe to eat.",
                                    style = TextStyle(
                                        fontSize = 14.5.sp,
                                        color = Color(0xFFCDD6C4),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (!repository.canUseLabFeature(isPro)) {
                                                onOpenPaywall()
                                            } else {
                                                cameraLauncher.launch(null)
                                            }
                                        },
                                        enabled = !isScanningImage,
                                        shape = RoundedCornerShape(100.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SproutCitrus,
                                            contentColor = SproutInk
                                        ),
                                        modifier = Modifier.testTag("camera_scan_btn")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Take Photo", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (!repository.canUseLabFeature(isPro)) {
                                                onOpenPaywall()
                                            } else {
                                                galleryLauncher.launch("image/*")
                                            }
                                        },
                                        enabled = !isScanningImage,
                                        shape = RoundedCornerShape(100.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SproutCitrus),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = SproutPaper
                                        ),
                                        modifier = Modifier.testTag("upload_image_btn")
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = SproutCitrus, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upload Image", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = SproutPaper)
                                    }
                                }

                                // Specimen Presets Quick Tray
                                Text(
                                    text = "Or test preset produce states:",
                                    fontSize = 13.5.sp,
                                    fontFamily = FontFamily.Default,
                                    color = Color(0xFFA5B29B)
                                )

                                val sampleSpecimens = listOf(
                                    "Avocado" to "Avocado (Fresh)",
                                    "Apple" to "Crisp Apple (10d)",
                                    "Tomato" to "Vine Tomato (5d)",
                                    "Banana" to "Spotted Banana (1d)",
                                    "Broccoli" to "Broccoli Crown",
                                    "Strawberry" to "Strawberry (Fresh)",
                                    "Spoiled Strawberry" to "Spoiled Strawberry (Mold)",
                                    "Dragon Fruit" to "Dragon Fruit"
                                )
                                val sampleScroll = rememberScrollState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(sampleScroll),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    sampleSpecimens.forEach { (queryName, label) ->
                                        val isSpoiledSample = queryName.contains("Spoiled", true)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(if (isSpoiledSample) Color(0x35E04F4F) else Color(0x2EFAF6E9))
                                                .border(1.dp, if (isSpoiledSample) SproutTomato else Color(0x40FAF6E9), RoundedCornerShape(100.dp))
                                                .clickable {
                                                    val sampleBmp = createSampleProduceBitmap(queryName)
                                                    handleScannedBitmap(sampleBmp, queryName)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                label,
                                                fontSize = 13.sp,
                                                color = if (isSpoiledSample) Color(0xFFFFB4A8) else SproutPaper,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Scanned Image Analysis Result Card
                    if (imageAnalysisResult != null) {
                        val res = imageAnalysisResult!!
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(SproutPaperCard)
                                    .border(
                                        1.5.dp,
                                        if (!res.isEdible) SproutTomato else if (res.shelfLifeDaysCount <= 2) Color(0xFFE5A024) else SproutLeaf,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Header: Produce name and Confidence
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = if (!res.isEdible) SproutTomato else SproutLeaf,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(res.produceName, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = SproutInk)
                                            }
                                            Text(
                                                "Vision Identity Match: ${res.confidenceScore}%",
                                                fontFamily = FontFamily.Default,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SproutLeafDark
                                            )
                                        }

                                        Button(
                                            onClick = { onNavigateToGuideByName(res.produceName) },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (!res.isEdible) SproutTomato else SproutLeaf,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("Open Guide", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    HorizontalDivider(color = SproutLine, thickness = 1.dp)

                                    // HERO EDIBILITY & FRESHNESS DAYS BANNER
                                    val edibilityBgColor = when {
                                        !res.isEdible -> Color(0xFFFDE8E8)
                                        res.shelfLifeDaysCount <= 2 -> Color(0xFFFFF6E6)
                                        else -> Color(0xFFEBF5EB)
                                    }
                                    val edibilityBorderColor = when {
                                        !res.isEdible -> Color(0xFFE04F4F)
                                        res.shelfLifeDaysCount <= 2 -> Color(0xFFE5A024)
                                        else -> Color(0xFF4C7B4C)
                                    }
                                    val edibilityTextColor = when {
                                        !res.isEdible -> Color(0xFF9E1B1B)
                                        res.shelfLifeDaysCount <= 2 -> Color(0xFF8A5300)
                                        else -> Color(0xFF1B5E20)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(edibilityBgColor)
                                            .border(1.5.dp, edibilityBorderColor, RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            !res.isEdible -> Icons.Outlined.Cancel
                                                            res.shelfLifeDaysCount <= 2 -> Icons.Outlined.WarningAmber
                                                            else -> Icons.Outlined.CheckCircle
                                                        },
                                                        contentDescription = null,
                                                        tint = edibilityTextColor,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = if (res.isEdible) "EDIBLE & SAFE TO CONSUME" else "DO NOT EAT · INEDIBLE",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            letterSpacing = 1.sp,
                                                            color = edibilityTextColor
                                                        )
                                                        Text(
                                                            text = res.edibilityVerdict,
                                                            fontSize = 15.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = edibilityTextColor
                                                        )
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = edibilityBorderColor.copy(alpha = 0.3f), thickness = 1.dp)

                                            // Fresh Days Remaining Indicator
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.HourglassTop,
                                                        contentDescription = null,
                                                        tint = edibilityTextColor,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Estimated Freshness:",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = edibilityTextColor
                                                    )
                                                }
                                                Text(
                                                    text = res.freshDaysRemaining,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = edibilityTextColor
                                                )
                                            }
                                        }
                                    }

                                    // Spoilage & Safety Pathology Breakdown
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SproutPaper)
                                            .border(1.dp, SproutLine, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Shield,
                                                    contentDescription = null,
                                                    tint = if (!res.isEdible) SproutTomato else SproutLeafDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    "Spoilage Risk & Pathology:",
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SproutInk
                                                )
                                            }
                                            Text(
                                                text = res.spoilageRiskAnalysis,
                                                fontSize = 14.sp,
                                                color = SproutInkSoft,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }

                                    // Ripeness State & Quality
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Quality State", fontFamily = FontFamily.Default, fontSize = 13.sp, color = SproutInkSoft)
                                            Text(res.freshnessVerdict, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (!res.isEdible) SproutTomato else SproutLeafDark)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text("Ripeness Stage", fontFamily = FontFamily.Default, fontSize = 13.sp, color = SproutInkSoft)
                                            Text(res.ripenessState, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SproutInk)
                                        }
                                    }

                                    // Visual Botanical Cues
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Botanical Visual Cues:", fontFamily = FontFamily.Default, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SproutInk)
                                        res.visualObservations.forEach { obs ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            ) {
                                                Text("•", fontWeight = FontWeight.Bold, color = SproutLeafDark, fontSize = 15.sp)
                                                Text(obs, fontSize = 14.sp, color = SproutInkSoft, lineHeight = 19.sp)
                                            }
                                        }
                                    }

                                    // Washing & Storage Tips Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SproutPaper)
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Outlined.CleanHands, contentDescription = null, tint = SproutLeafDark, modifier = Modifier.size(17.dp))
                                                Text("Sanitization & Washing:", fontFamily = FontFamily.Default, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SproutInk)
                                            }
                                            Text(res.washingRecommendation, fontSize = 14.sp, color = SproutInkSoft, lineHeight = 19.sp)

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = SproutLeafDark, modifier = Modifier.size(17.dp))
                                                Text("Optimal Shelf Life Storage:", fontFamily = FontFamily.Default, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SproutInk)
                                            }
                                            Text(res.storageTip, fontSize = 14.sp, color = SproutInkSoft, lineHeight = 19.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SCAN HISTORY LIST
                    item {
                        EyebrowHeader(text = "Recent Freshness & Scan Logs")
                    }

                    items(scanHistory, key = { it.id }) { scan ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SproutPaperCard)
                                .border(1.2.dp, if (!scan.isEdible) SproutTomato.copy(alpha = 0.5f) else SproutLine, RoundedCornerShape(14.dp))
                                .clickable { onNavigateToGuideByName(scan.produceName) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ProduceDoodle(
                                        symbolId = scan.symbolId,
                                        archetype = "leafy",
                                        name = scan.produceName,
                                        size = 32.dp
                                    )
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(scan.produceName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = SproutInk)
                                            if (!scan.isEdible) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFFFEBEE))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("SPOILED", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = SproutTomato)
                                                }
                                            }
                                        }
                                        Text("${scan.dateGroup} · ${scan.time} · ${scan.ripenessStage}", fontFamily = FontFamily.Default, fontSize = 13.sp, color = SproutInkSoft)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = scan.shelfLifeDays,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!scan.isEdible) SproutTomato else SproutLeafDark
                                        )
                                    )
                                    Text(
                                        text = if (scan.isEdible) "Edible" else "Discard",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (!scan.isEdible) SproutTomato else SproutLeaf
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "rainbow" -> {
                // ----------------------------------------------------------------
                // 4. RAINBOW CHALLENGE TAB
                // ----------------------------------------------------------------
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SproutPaperCard)
                                    .border(1.4.dp, SproutLine, RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            EyebrowHeader(text = "Phytonutrient Diversity")
                                            Text(
                                                text = "Eat the Rainbow",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Serif,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SproutInk
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(Color(0x1F233022))
                                                .border(1.dp, SproutLine, RoundedCornerShape(100.dp))
                                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "${rainbowStreak}-day streak",
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SproutTomato
                                                )
                                            )
                                        }
                                    }

                                    val loggedCount = rainbowLogs.values.count { it.isLogged }
                                    Text(
                                        text = "$loggedCount of 6 color groups logged today. Aim for all six colors each day to saturate full-spectrum antioxidant cellular defense.",
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = SproutInkSoft
                                    )

                                    LinearProgressIndicator(
                                        progress = { loggedCount / 6f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(100.dp)),
                                        color = SproutLeaf,
                                        trackColor = SproutPaper
                                    )
                                }
                            }

                            RainbowColor.values().forEach { color ->
                                val entry = rainbowLogs[color]
                                val isLogged = entry?.isLogged == true

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SproutPaperCard)
                                        .border(1.2.dp, if (isLogged) SproutLeaf else SproutLine, RoundedCornerShape(14.dp))
                                        .clickable { repository.toggleRainbowColor(color) }
                                        .padding(14.dp)
                                        .testTag("rainbow_card_${color.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(color.hexColor))
                                                    .border(1.dp, SproutInk.copy(alpha = 0.2f), CircleShape)
                                            )
                                            Column {
                                                Text(
                                                    text = color.displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.5.sp,
                                                    color = SproutInk
                                                )
                                                Text(
                                                    text = color.phytonutrient,
                                                    fontFamily = FontFamily.Default,
                                                    fontSize = 14.sp,
                                                    color = SproutInkSoft
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isLogged) SproutLeaf else SproutPaper)
                                                .border(1.2.dp, if (isLogged) SproutLeaf else SproutLine, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isLogged) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
    }
}

@Composable
private fun ClinicalMealCard(
    slot: String,
    meal: AiMeal,
    onNavigate: (String) -> Unit
) {
    val (slotEmoji, slotColor, slotBg) = when (slot.lowercase()) {
        "breakfast" -> Triple("🍳", Color(0xFFC07010), Color(0xFFFFF6E5))
        "lunch" -> Triple("🥗", SproutLeafDark, SproutLeafLight)
        "dinner" -> Triple("🍲", SproutTomato, SproutTomatoLight)
        else -> Triple("🥑", Color(0xFF4A7C59), Color(0xFFEBF5EE))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SproutPaperCard)
            .border(1.3.dp, SproutLine, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: Slot pill with emoji + Calorie and Prep Time badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(slotBg)
                            .border(1.dp, slotColor.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(slotEmoji, fontSize = 13.sp)
                            Text(
                                text = slot.uppercase(),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = slotColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Calories pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(SproutPaper)
                            .border(1.dp, SproutLine.copy(alpha = 0.7f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "🔥 ${meal.calories} kcal",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInk
                        )
                    }

                    // Prep time pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(SproutPaper)
                            .border(1.dp, SproutLine.copy(alpha = 0.7f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "⏱ ${meal.prepTimeMin}m",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SproutInkSoft
                        )
                    }
                }
            }

            // Meal Name
            Text(
                text = meal.name,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutInk,
                    lineHeight = 26.sp
                )
            )

            // Portion Details
            Text(
                text = meal.portionDetails,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = SproutInkSoft
            )

            // Produce Tags linking to Atlas Guides
            if (meal.produceIngredients.isNotEmpty()) {
                val tagScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tagScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    meal.produceIngredients.forEach { ing ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(SproutPaper)
                                .border(1.dp, SproutLeaf.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                                .clickable { onNavigate(ing) }
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🌿", fontSize = 11.sp)
                                Text(
                                    text = ing,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SproutLeafDark
                                )
                            }
                        }
                    }
                }
            }

            // Visual Segmented Macro Bar & Clean Metrics
            MacroVisualStrip(
                protein = meal.protein,
                carbs = meal.carbs,
                fats = meal.fats,
                fiber = meal.fiber
            )

            // Phytonutrient / Botanical Highlight Callout
            if (meal.nutrients.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SproutPaper)
                        .border(1.dp, SproutLine.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✨", fontSize = 13.sp)
                        Text(
                            text = meal.nutrients,
                            fontSize = 13.5.sp,
                            color = SproutLeafDark,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroVisualStrip(
    protein: Int,
    carbs: Int,
    fats: Int,
    fiber: Int,
    modifier: Modifier = Modifier
) {
    val totalMacroCalories = (protein * 4) + (carbs * 4) + (fats * 9).coerceAtLeast(1)
    val safeTotal = if (totalMacroCalories > 0) totalMacroCalories.toFloat() else 1f
    val proteinPct = ((protein * 4 / safeTotal) * 100).toInt()
    val carbsPct = ((carbs * 4 / safeTotal) * 100).toInt()
    val fatsPct = (100 - proteinPct - carbsPct).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SproutPaper)
            .border(1.dp, SproutLine.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Continuous Multi-Color Macro Ratio Progress Track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color(0xFFE2E8DF))
        ) {
            val pWeight = proteinPct.coerceAtLeast(4).toFloat()
            val cWeight = carbsPct.coerceAtLeast(4).toFloat()
            val fWeight = fatsPct.coerceAtLeast(4).toFloat()

            Box(
                modifier = Modifier
                    .weight(pWeight)
                    .fillMaxHeight()
                    .background(SproutTomato)
            )
            Box(
                modifier = Modifier
                    .weight(cWeight)
                    .fillMaxHeight()
                    .background(SproutCitrus)
            )
            Box(
                modifier = Modifier
                    .weight(fWeight)
                    .fillMaxHeight()
                    .background(SproutLeafDark)
            )
        }

        // 4 Clean, Scannable Macro Pills in a single responsive horizontal row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Protein
            MacroMiniBadge(
                dotColor = SproutTomato,
                label = "Protein",
                value = "${protein}g",
                subtext = "$proteinPct%"
            )
            // Carbs
            MacroMiniBadge(
                dotColor = SproutCitrus,
                label = "Carbs",
                value = "${carbs}g",
                subtext = "$carbsPct%"
            )
            // Fats
            MacroMiniBadge(
                dotColor = SproutLeafDark,
                label = "Fats",
                value = "${fats}g",
                subtext = "$fatsPct%"
            )
            // Fiber
            MacroMiniBadge(
                dotColor = SproutInkSoft,
                label = "Fiber",
                value = "${fiber}g",
                subtext = "Gut"
            )
        }
    }
}

@Composable
private fun MacroMiniBadge(
    dotColor: Color,
    label: String,
    value: String,
    subtext: String
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SproutInkSoft
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SproutInk
            )
            Text(
                text = subtext,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = dotColor
            )
        }
    }
}

@Composable
private fun MacroPointsList(
    title: String = "DAILY MACRONUTRIENT TARGETS & PERCENTAGES",
    protein: Int,
    carbs: Int,
    fats: Int,
    fiber: Int,
    modifier: Modifier = Modifier
) {
    MacroVisualStrip(
        protein = protein,
        carbs = carbs,
        fats = fats,
        fiber = fiber,
        modifier = modifier
    )
}

@Composable
private fun LabSegmentTab(
    title: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    accentColor: Color = SproutTomato,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SproutInk else Color.Transparent,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.2.dp, SproutInk) else null,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) accentColor else SproutInkSoft
            ) {
                icon()
            }
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selected) SproutPaper else SproutInkSoft,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.5.sp
                )
            )
        }
    }
}

// Decodes a sampled bitmap from Uri safely
private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(stream, null, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { stream2 ->
                BitmapFactory.decodeStream(stream2, null, options)
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

// Generates an illustrative test bitmap for preset produce specimens
private fun createSampleProduceBitmap(name: String): Bitmap {
    val size = 300
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Warm background
    paint.color = android.graphics.Color.parseColor("#FAF6E9")
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

    val lower = name.lowercase()
    // Produce circle base
    paint.color = when {
        lower.contains("spoiled") || lower.contains("mold") -> android.graphics.Color.parseColor("#7A5555")
        lower.contains("avocado") -> android.graphics.Color.parseColor("#2F4F2F")
        lower.contains("banana") -> android.graphics.Color.parseColor("#C49A28")
        lower.contains("apple") -> android.graphics.Color.parseColor("#C42B2B")
        lower.contains("mango") -> android.graphics.Color.parseColor("#ECA328")
        lower.contains("dragon") -> android.graphics.Color.parseColor("#D81B60")
        lower.contains("guava") -> android.graphics.Color.parseColor("#C9D97A")
        lower.contains("tomato") -> android.graphics.Color.parseColor("#D44D2B")
        lower.contains("broccoli") -> android.graphics.Color.parseColor("#3C643C")
        lower.contains("strawberry") -> android.graphics.Color.parseColor("#C82828")
        else -> android.graphics.Color.parseColor("#7EA04D")
    }
    canvas.drawCircle(size / 2f, size / 2f, 100f, paint)

    // Mold speckles or spots simulation
    if (lower.contains("spoiled") || lower.contains("mold")) {
        paint.color = android.graphics.Color.parseColor("#EAEAEA")
        canvas.drawCircle(size / 2f - 30f, size / 2f - 30f, 22f, paint)
        canvas.drawCircle(size / 2f + 25f, size / 2f - 15f, 16f, paint)
        canvas.drawCircle(size / 2f, size / 2f + 35f, 20f, paint)
    } else if (lower.contains("banana")) {
        paint.color = android.graphics.Color.parseColor("#5A3E1B")
        canvas.drawCircle(size / 2f - 35f, size / 2f - 20f, 8f, paint)
        canvas.drawCircle(size / 2f + 30f, size / 2f + 10f, 10f, paint)
        canvas.drawCircle(size / 2f - 10f, size / 2f + 30f, 7f, paint)
    }

    // Highlight & Text
    paint.color = if (lower.contains("spoiled")) android.graphics.Color.parseColor("#FFFFFF") else android.graphics.Color.parseColor("#233022")
    paint.textSize = 20f
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText(name, size / 2f, size / 2f + 8f, paint)

    return bitmap
}
