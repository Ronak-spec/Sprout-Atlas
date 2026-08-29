package com.example.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.AuthState
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirebaseFirestoreService
import com.example.data.firebase.SproutUserProfile
import com.example.data.subscription.PlanOption
import com.example.data.subscription.SproutSubscriptionState
import com.example.data.subscription.SubscriptionService
import com.example.ui.components.ProduceDoodle
import com.revenuecat.purchases.PackageType
import kotlinx.coroutines.launch

// Botanical Theme Colors exactly matching design
val SproutPaperBg = Color(0xFFFAF6E9)
val SproutPaperCard = Color(0xFFF2ECD8)
val SproutCardSelected = Color(0xFFEAF2E6)
val SproutInkText = Color(0xFF233022)
val SproutInkSoftText = Color(0xFF5B6A54)
val SproutLeafGreen = Color(0xFF3F7D4C)
val SproutLeafDarkGreen = Color(0xFF2C5936)
val SproutTomatoRed = Color(0xFFD6482F)
val SproutCitrusYellow = Color(0xFFE79B1F)
val SproutBerryPurple = Color(0xFF6E3B6E)
val SproutSoilBrown = Color(0xFF6B4A30)

private enum class PaywallScreenState {
    PLANS,
    SIGN_IN_FLOW,
    SUCCESS_CONFIRMATION
}

@Composable
fun SproutPaywallDialog(
    subscriptionService: SubscriptionService,
    authService: FirebaseAuthService? = null,
    firestoreService: FirebaseFirestoreService? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    val subState by subscriptionService.subscriptionState.collectAsState()
    val availablePackages by subscriptionService.availablePackages.collectAsState()
    val isLoading by subscriptionService.isLoading.collectAsState()
    val authState = authService?.authState?.collectAsState()?.value
    val authenticatedUser = (authState as? AuthState.Authenticated)?.user

    var isCloudTrialChecked by remember { mutableStateOf(false) }
    LaunchedEffect(authenticatedUser) {
        if (authenticatedUser != null && !isCloudTrialChecked) {
            val cloudTrialUsed = firestoreService?.checkCloudTrialUsed(authenticatedUser.uid, authenticatedUser.email) ?: false
            if (cloudTrialUsed) {
                subscriptionService.markTrialUsed(authenticatedUser.email, authenticatedUser.uid)
            }
            isCloudTrialChecked = true
        }
    }

    val hasUsedTrial = subscriptionService.hasUsedFreeTrial(authenticatedUser?.email, authenticatedUser?.uid)
    var currentScreen by remember { mutableStateOf(PaywallScreenState.PLANS) }
    // Selected tier: "yearly" (Bloom), "monthly" (Seedling), "lifetime" (Evergreen), "trial" (Germinate)
    var selectedPlanId by remember { mutableStateOf(if (hasUsedTrial) "yearly" else "yearly") }

    // Ensure trial cannot be selected if already used
    LaunchedEffect(hasUsedTrial) {
        if (hasUsedTrial && selectedPlanId == "trial") {
            selectedPlanId = "yearly"
        }
    }

    // Dynamic RevenueCat packages if loaded
    val annualPkg = availablePackages.firstOrNull { it.packageType == PackageType.ANNUAL }
        ?: availablePackages.firstOrNull { it.identifier.contains("year", true) || it.identifier.contains("annual", true) }
    val monthlyPkg = availablePackages.firstOrNull { it.packageType == PackageType.MONTHLY }
        ?: availablePackages.firstOrNull { it.identifier.contains("month", true) }
    val lifetimePkg = availablePackages.firstOrNull { it.packageType == PackageType.LIFETIME }
        ?: availablePackages.firstOrNull { it.identifier.contains("life", true) }

    val annualPrice = annualPkg?.product?.price?.formatted ?: "$19.99"
    val monthlyPrice = monthlyPkg?.product?.price?.formatted ?: "$3.99"
    val lifetimePrice = lifetimePkg?.product?.price?.formatted ?: "$49.99"

    // Activated email state for confirmation
    var savedSubscriptionEmail by remember { mutableStateOf<String?>(null) }
    var activatedExpiryDate by remember { mutableStateOf<String?>(null) }

    // Promo code dialog state
    var showPromoDialog by remember { mutableStateOf(false) }

    // Smooth entry state & RevenueCat custom paywall impression tracking
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
        subscriptionService.trackCustomPaywallImpression("sprout_botanical_paywall")
    }

    fun executeSubscriptionActivation(userEmail: String?, userUid: String?) {
        if (activity == null) return
        val targetEmail = userEmail ?: subState.linkedEmail ?: "app.ronak.1311@gmail.com"
        val targetUid = userUid ?: authService?.let {
            (it.authState.value as? AuthState.Authenticated)?.user?.uid
        } ?: "sprout-user-${System.currentTimeMillis()}"

        // Save email link in subscription service
        subscriptionService.linkUserEmail(targetEmail, targetUid)

        if (selectedPlanId == "trial") {
            if (subscriptionService.hasUsedFreeTrial(targetEmail, targetUid)) {
                Toast.makeText(context, "14-day Free Trial has already been used for this account. Please select a subscription plan.", Toast.LENGTH_LONG).show()
                selectedPlanId = "yearly"
                return
            }
            // Activate 14-day trial for this signed-in user
            subscriptionService.activate14DayTrial(targetEmail, targetUid)
            val exp = subscriptionService.subscriptionState.value.expirationDateFormatted
            val durationSummary = subscriptionService.subscriptionState.value.durationSummary
            savedSubscriptionEmail = targetEmail
            activatedExpiryDate = exp
            coroutineScope.launch {
                firestoreService?.saveSubscriptionToCloud(
                    uid = targetUid,
                    email = targetEmail,
                    planType = "trial",
                    isPro = true,
                    expirationDate = exp,
                    isTrial = true,
                    dailyAiLimit = SubscriptionService.PRO_DAILY_AI_LIMIT,
                    durationSummary = durationSummary
                )
            }
            currentScreen = PaywallScreenState.SUCCESS_CONFIRMATION
            return
        }

        val pkg = when (selectedPlanId) {
            "yearly", "annual" -> annualPkg
            "monthly" -> monthlyPkg
            "lifetime" -> lifetimePkg
            else -> null
        }

        if (pkg != null) {
            // Trigger RevenueCat Google Play / Google Pay purchase flow
            subscriptionService.purchasePackage(
                activity = activity,
                pkg = pkg,
                onSuccess = { customerInfo ->
                    val exp = customerInfo.entitlements[SubscriptionService.PRO_ENTITLEMENT_ID]?.expirationDate?.toString()
                    val durationSummary = subscriptionService.subscriptionState.value.durationSummary
                    savedSubscriptionEmail = targetEmail
                    activatedExpiryDate = exp
                    coroutineScope.launch {
                        firestoreService?.saveSubscriptionToCloud(
                            uid = targetUid,
                            email = targetEmail,
                            planType = selectedPlanId,
                            isPro = true,
                            expirationDate = exp,
                            isTrial = false,
                            dailyAiLimit = SubscriptionService.PRO_DAILY_AI_LIMIT,
                            durationSummary = durationSummary
                        )
                    }
                    currentScreen = PaywallScreenState.SUCCESS_CONFIRMATION
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Fallback direct plan purchase with Google Pay / Local simulation
            subscriptionService.purchaseDirectPlan(
                activity = activity,
                planType = selectedPlanId,
                onSuccess = {
                    val exp = subscriptionService.subscriptionState.value.expirationDateFormatted
                    val durationSummary = subscriptionService.subscriptionState.value.durationSummary
                    savedSubscriptionEmail = targetEmail
                    activatedExpiryDate = exp
                    coroutineScope.launch {
                        firestoreService?.saveSubscriptionToCloud(
                            uid = targetUid,
                            email = targetEmail,
                            planType = selectedPlanId,
                            isPro = true,
                            expirationDate = exp,
                            isTrial = false,
                            dailyAiLimit = SubscriptionService.PRO_DAILY_AI_LIMIT,
                            durationSummary = durationSummary
                        )
                    }
                    currentScreen = PaywallScreenState.SUCCESS_CONFIRMATION
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 160 },
                    animationSpec = spring(dampingRatio = 0.76f, stiffness = 380f)
                ) + scaleIn(
                    initialScale = 0.90f,
                    animationSpec = spring(dampingRatio = 0.74f, stiffness = 340f)
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(
                    targetOffsetY = { 100 },
                    animationSpec = tween(180)
                ) + scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(180)
                ) + fadeOut(tween(180))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp)
                        .fillMaxHeight(0.96f)
                        .clip(RoundedCornerShape(32.dp))
                        .border(1.6.dp, SproutInkText.copy(alpha = 0.18f), RoundedCornerShape(32.dp))
                        .shadow(elevation = 28.dp, shape = RoundedCornerShape(32.dp)),
                    color = SproutPaperBg
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> (width * 0.4f).toInt() } + scaleIn(initialScale = 0.94f) + fadeIn(tween(200)))
                                    .togetherWith(slideOutHorizontally { width -> -(width * 0.4f).toInt() } + scaleOut(targetScale = 0.94f) + fadeOut(tween(180)))
                            } else {
                                (slideInHorizontally { width -> -(width * 0.4f).toInt() } + scaleIn(initialScale = 0.94f) + fadeIn(tween(200)))
                                    .togetherWith(slideOutHorizontally { width -> (width * 0.4f).toInt() } + scaleOut(targetScale = 0.94f) + fadeOut(tween(180)))
                            }
                        },
                        label = "PaywallContentTransition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            PaywallScreenState.PLANS -> {
                                BotanicalPaywallMainView(
                                    selectedPlanId = selectedPlanId,
                                    onSelectPlan = { selectedPlanId = it },
                                    annualPrice = annualPrice,
                                    monthlyPrice = monthlyPrice,
                                    lifetimePrice = lifetimePrice,
                                    hasUsedTrial = hasUsedTrial,
                                    isLoading = isLoading,
                                    onCtaClick = {
                                        val authenticatedUser = (authState as? AuthState.Authenticated)?.user
                                        if (authenticatedUser != null) {
                                            executeSubscriptionActivation(authenticatedUser.email, authenticatedUser.uid)
                                        } else {
                                            // User must be signed in before taking a trial or buying a subscription
                                            currentScreen = PaywallScreenState.SIGN_IN_FLOW
                                        }
                                    },
                                    onRestorePurchases = {
                                        subscriptionService.restorePurchases(
                                            onSuccess = { active ->
                                                if (active) {
                                                    Toast.makeText(context, "Pro subscription restored!", Toast.LENGTH_SHORT).show()
                                                    onDismiss()
                                                } else {
                                                    Toast.makeText(context, "No active subscription found in Google Play.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    onOpenPromo = { showPromoDialog = true },
                                    onSimulateSandboxPlan = { sandboxPlan ->
                                        selectedPlanId = sandboxPlan
                                        val authenticatedUser = (authState as? AuthState.Authenticated)?.user
                                        executeSubscriptionActivation(authenticatedUser?.email, authenticatedUser?.uid)
                                    },
                                    onDismiss = onDismiss
                                )
                            }
                            PaywallScreenState.SIGN_IN_FLOW -> {
                                PaywallAuthStepView(
                                    selectedPlanId = selectedPlanId,
                                    authService = authService,
                                    onBack = { currentScreen = PaywallScreenState.PLANS },
                                    onAuthSuccess = { profile ->
                                        executeSubscriptionActivation(profile.email, profile.uid)
                                    }
                                )
                            }
                            PaywallScreenState.SUCCESS_CONFIRMATION -> {
                                PaywallSuccessConfirmationView(
                                    planId = selectedPlanId,
                                    savedEmail = savedSubscriptionEmail ?: "your account",
                                    expiryDate = activatedExpiryDate,
                                    onDone = onDismiss
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPromoDialog) {
        var promoCodeInput by remember { mutableStateOf("") }
        val authenticatedUser = (authState as? AuthState.Authenticated)?.user

        AlertDialog(
            onDismissRequest = { showPromoDialog = false },
            title = {
                Text(
                    text = "Redeem Monthly Promo Code",
                    style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SproutInkText)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter the 30-day monthly promotional trial code. Usable only until October 30, 2026:",
                        fontSize = 13.sp,
                        color = SproutInkSoftText
                    )
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it.uppercase().trim() },
                        placeholder = { Text("SPROUT30") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "• Grants 30 days (1 monthly trial) of Sprout Atlas Pro\n• Valid for one-time activation until October 30, 2026\n• Includes 500 daily AI scans & all botanical guides",
                        fontSize = 11.sp,
                        color = SproutInkSoftText,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = authenticatedUser?.email ?: subState.linkedEmail
                        val uid = authenticatedUser?.uid ?: subState.linkedUid
                        val res = subscriptionService.redeemPromoCode(promoCodeInput, email, uid)
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                        if (res.success) {
                            val activeEmail = email ?: "user_${System.currentTimeMillis()}@sproutatlas.app"
                            val activeUid = uid ?: "sprout_uid_${System.currentTimeMillis()}"
                            val currentSub = subscriptionService.subscriptionState.value
                            coroutineScope.launch {
                                firestoreService?.saveSubscriptionToCloud(
                                    uid = activeUid,
                                    email = activeEmail,
                                    planType = "promo_${promoCodeInput.lowercase().trim()}",
                                    isPro = true,
                                    expirationDate = currentSub.expirationDateFormatted,
                                    isTrial = true,
                                    dailyAiLimit = SubscriptionService.PRO_DAILY_AI_LIMIT,
                                    durationSummary = currentSub.durationSummary
                                )
                            }
                            showPromoDialog = false
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SproutLeafGreen)
                ) {
                    Text("Redeem 30 Days", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoDialog = false }) {
                    Text("Cancel", color = SproutInkSoftText)
                }
            },
            containerColor = SproutPaperBg
        )
    }
}

/**
 * Botanical Paywall Main Layout (Faithfully implementing the HTML P1 spec)
 */
@Composable
private fun BotanicalPaywallMainView(
    selectedPlanId: String,
    onSelectPlan: (String) -> Unit,
    annualPrice: String,
    monthlyPrice: String,
    lifetimePrice: String,
    hasUsedTrial: Boolean = false,
    isLoading: Boolean,
    onCtaClick: () -> Unit,
    onRestorePurchases: () -> Unit,
    onOpenPromo: () -> Unit,
    onSimulateSandboxPlan: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SproutAtlasLogoIcon(modifier = Modifier.size(24.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SproutInkText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFamily = FontFamily.Serif)) {
                            append("Sprout ")
                        }
                        withStyle(SpanStyle(color = SproutLeafGreen, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, fontSize = 16.sp, fontFamily = FontFamily.Serif)) {
                            append("Atlas")
                        }
                    }
                )
            }

            // Close button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SproutPaperCard)
                    .border(1.3.dp, SproutInkText.copy(alpha = 0.18f), CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = SproutInkSoftText,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Hero section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(1.dp)
                            .background(SproutLeafDarkGreen)
                    )
                    Text(
                        text = "SPROUT ATLAS PREMIUM",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutLeafDarkGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SproutInkText, fontWeight = FontWeight.Bold, fontSize = 23.sp, fontFamily = FontFamily.Serif)) {
                            append("Pick how far you want to ")
                        }
                        withStyle(SpanStyle(color = SproutTomatoRed, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 23.sp, fontFamily = FontFamily.Serif)) {
                            append("grow.")
                        }
                    },
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Signature Visual: Growth Stage Strip
            GrowthStageStrip(selectedPlanId = selectedPlanId)

            Spacer(modifier = Modifier.height(16.dp))

            // Tier List Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                // Tier 1: Seedling (Monthly)
                PricingTierCard(
                    id = "monthly",
                    name = "Seedling",
                    subtitle = "Monthly · cancel anytime, no lock-in",
                    priceAmt = monthlyPrice,
                    pricePer = "/ month",
                    strikePrice = null,
                    badgeText = null,
                    badgeColor = null,
                    iconType = PlantStageType.SEEDLING,
                    isSelected = selectedPlanId == "monthly",
                    onSelect = { onSelectPlan("monthly") }
                )

                // Tier 2: Bloom (Yearly / Best Value)
                PricingTierCard(
                    id = "yearly",
                    name = "Bloom",
                    subtitle = if (!hasUsedTrial) "14-day Free Trial included · $19.99/yr (~$1.66/mo)" else "Billed annually · $19.99/yr (~$1.66/mo)",
                    priceAmt = annualPrice,
                    pricePer = "/ month",
                    strikePrice = "$5.99",
                    badgeText = if (!hasUsedTrial) "14-Day Free Trial" else "Best Value",
                    badgeColor = if (!hasUsedTrial) SproutTomatoRed else SproutLeafGreen,
                    iconType = PlantStageType.BLOOM,
                    isSelected = selectedPlanId == "yearly",
                    onSelect = { onSelectPlan("yearly") }
                )

                // Tier 3: Evergreen (Lifetime)
                PricingTierCard(
                    id = "lifetime",
                    name = "Evergreen",
                    subtitle = "One-time payment",
                    priceAmt = lifetimePrice,
                    pricePer = "once",
                    strikePrice = null,
                    badgeText = "Own it forever",
                    badgeColor = SproutBerryPurple,
                    iconType = PlantStageType.EVERGREEN,
                    isSelected = selectedPlanId == "lifetime",
                    onSelect = { onSelectPlan("lifetime") }
                )

                // If user has not yet used free trial, show Germinate trial option
                if (!hasUsedTrial) {
                    // OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(SproutInkText.copy(alpha = 0.16f)))
                        Text(
                            text = "OR, NOT READY TO COMMIT?",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.8.sp,
                                letterSpacing = 0.8.sp,
                                color = SproutInkSoftText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(SproutInkText.copy(alpha = 0.16f)))
                    }

                    // Germinate: 14-day Free Trial Option
                    GerminateTrialCard(
                        isSelected = selectedPlanId == "trial",
                        onSelect = { onSelectPlan("trial") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // What grows with you section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EVERY PLAN INCLUDES",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInkSoftText
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "What grows with you",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutInkText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Perk List
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    PerkRow(text = "All 500+ guides — nutrients, treatments, botanical facts, unlocked")
                    PerkRow(text = "Up to 500 Nutrition Tutor, Scanner, and Meal Planner uses per day")
                    PerkRow(text = "All 5 dietary archetypes with clinical safety checks")
                    PerkRow(text = "Ad-free, always")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Teaser Row (Dashed Berry border)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SproutPaperCard,
                    border = BorderStroke(1.3.dp, SproutBerryPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SparkleIcon(modifier = Modifier.size(18.dp), tint = SproutBerryPurple)
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = SproutBerryPurple, fontWeight = FontWeight.Bold)) {
                                    append("You're looking at Sprout Atlas Pro. ")
                                }
                                withStyle(SpanStyle(color = SproutInkSoftText)) {
                                    append("A separate Premium tier, with even more, is coming soon")
                                }
                            },
                            style = TextStyle(fontSize = 11.5.sp, lineHeight = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trust Stamps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StampItem(icon = Icons.Outlined.Shield, label = "Secure checkout")
                    Spacer(modifier = Modifier.width(28.dp))
                    StampItem(icon = Icons.Default.Refresh, label = "Cancel anytime")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Restore purchase",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SproutInkSoftText,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { onRestorePurchases() }
                    )
                    Text("  ·  ", color = SproutInkSoftText, fontSize = 10.sp)
                    Text(
                        text = "Redeem code",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SproutLeafGreen,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { onOpenPromo() }
                    )
                    Text("  ·  ", color = SproutInkSoftText, fontSize = 10.sp)
                    Text(
                        text = "Privacy",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SproutInkSoftText,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic Disclaimer
                Text(
                    text = if (selectedPlanId == "trial") {
                        "No payment is collected now. After 14 days, Germinate ends and you'll be asked to choose Seedling, Bloom, or Evergreen to keep your access."
                    } else {
                        "You are charged today for the selected plan. Subscriptions auto-renew until canceled. Lifetime is a one-time, non-recurring purchase."
                    },
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.2.sp,
                        color = SproutInkSoftText,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Sticky Bottom CTA Bar
        StickyCtaBar(
            selectedPlanId = selectedPlanId,
            annualPrice = annualPrice,
            monthlyPrice = monthlyPrice,
            lifetimePrice = lifetimePrice,
            hasUsedTrial = hasUsedTrial,
            isLoading = isLoading,
            onCtaClick = onCtaClick
        )
    }
}

/**
 * Continuous Soil Growth Stage Strip
 */
@Composable
private fun GrowthStageStrip(selectedPlanId: String) {
    val activeIndex = when (selectedPlanId) {
        "monthly" -> 0
        "yearly" -> 1
        "lifetime" -> 2
        else -> -1 // trial has no stage selected
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Soil Dashed Baseline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height - 18.dp.toPx()
            drawLine(
                color = SproutSoilBrown.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f),
                cap = StrokeCap.Round
            )
        }

        // Three plant stages positioned across the strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Stage 1: Seedling (~30% width)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                PlantStageDrawing(
                    type = PlantStageType.SEEDLING,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SEEDLING",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        fontWeight = if (activeIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (activeIndex == 0) SproutLeafDarkGreen else SproutInkSoftText
                    )
                )
            }

            // Stage 2: Bloom (~30% width)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                PlantStageDrawing(
                    type = PlantStageType.BLOOM,
                    modifier = Modifier.size(68.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "BLOOM",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        fontWeight = if (activeIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (activeIndex == 1) SproutLeafDarkGreen else SproutInkSoftText
                    )
                )
            }

            // Stage 3: Evergreen (~30% width)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                PlantStageDrawing(
                    type = PlantStageType.EVERGREEN,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "EVERGREEN",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        fontWeight = if (activeIndex == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (activeIndex == 2) SproutLeafDarkGreen else SproutInkSoftText
                    )
                )
            }
        }
    }
}

/**
 * Pricing Tier Card with Radio and Badges
 */
@Composable
private fun PricingTierCard(
    id: String,
    name: String,
    subtitle: String,
    priceAmt: String,
    pricePer: String,
    strikePrice: String?,
    badgeText: String?,
    badgeColor: Color?,
    iconType: PlantStageType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val transition = updateTransition(targetState = isSelected, label = "TierSelectionTransition")
    val bgColor by transition.animateColor(label = "CardBgColor") { selected ->
        if (selected) SproutCardSelected else SproutPaperCard
    }
    val borderColor by transition.animateColor(label = "CardBorderColor") { selected ->
        if (selected) SproutLeafGreen else SproutInkText.copy(alpha = 0.16f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(if (isSelected) 1.8.dp else 1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon thumbnail box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SproutPaperBg),
                contentAlignment = Alignment.Center
            ) {
                PlantStageDrawing(
                    type = iconType,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Copy
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = name,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SproutInkText
                        )
                    )
                    if (badgeText != null && badgeColor != null) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = badgeColor
                        ) {
                            Text(
                                text = badgeText,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SproutInkSoftText,
                        lineHeight = 13.sp
                    )
                )
            }

            // Price Column
            Column(horizontalAlignment = Alignment.End) {
                if (strikePrice != null) {
                    Text(
                        text = strikePrice,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = SproutInkSoftText.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
                Text(
                    text = priceAmt,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SproutInkText
                    )
                )
                Text(
                    text = pricePer,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = SproutInkSoftText
                    )
                )
            }

            // Custom Radio Button
            Box(
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SproutLeafGreen else Color.Transparent)
                    .border(2.dp, if (isSelected) SproutLeafGreen else SproutInkText.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Germinate 14-day Free Trial Card
 */
@Composable
private fun GerminateTrialCard(
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val bgColor = if (isSelected) SproutCardSelected else SproutPaperCard
    val borderColor = if (isSelected) SproutLeafGreen else SproutLeafGreen.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(
                border = BorderStroke(1.6.dp, borderColor),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon thumbnail box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SproutPaperBg),
                contentAlignment = Alignment.Center
            ) {
                PlantStageDrawing(
                    type = PlantStageType.GERMINATE,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Copy
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Germinate",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SproutInkText
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SproutCitrusYellow
                    ) {
                        Text(
                            text = "NO PAYMENT NOW",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInkText
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "14-day free trial · pick a plan after",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SproutInkSoftText,
                        lineHeight = 13.sp
                    )
                )
            }

            // Custom Radio Button
            Box(
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SproutLeafGreen else Color.Transparent)
                    .border(2.dp, if (isSelected) SproutLeafGreen else SproutInkText.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Sticky Bottom CTA with Dynamic Price & Google Play / Billing Trigger
 */
@Composable
private fun StickyCtaBar(
    selectedPlanId: String,
    annualPrice: String,
    monthlyPrice: String,
    lifetimePrice: String,
    hasUsedTrial: Boolean = false,
    isLoading: Boolean,
    onCtaClick: () -> Unit
) {
    val isTrial = selectedPlanId == "trial"
    val isLifetime = selectedPlanId == "lifetime"

    val amtText = when (selectedPlanId) {
        "yearly" -> annualPrice
        "monthly" -> monthlyPrice
        "lifetime" -> lifetimePrice
        else -> "Free"
    }

    val perText = when (selectedPlanId) {
        "yearly", "monthly" -> "/ month"
        "lifetime" -> "once"
        else -> "for 14 days"
    }

    val trialBadgeText = when (selectedPlanId) {
        "yearly" -> if (!hasUsedTrial) "14 days free" else "Best Value"
        "monthly" -> "Billed monthly"
        "lifetime" -> "One-time"
        else -> "No card charge yet"
    }

    val ctaLabel = when (selectedPlanId) {
        "yearly" -> if (!hasUsedTrial) "Start 14-day trial" else "Subscribe to Bloom"
        "monthly" -> "Subscribe to Seedling"
        "lifetime" -> "Buy Evergreen access"
        else -> "Start 14-day trial"
    }

    Surface(
        color = SproutPaperBg.copy(alpha = 0.97f),
        border = BorderStroke(1.2.dp, SproutInkText.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = amtText,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SproutInkText
                        )
                    )
                    Text(
                        text = perText,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = SproutInkSoftText
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = trialBadgeText,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutLeafDarkGreen
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CTA Button with botanical gradient & shadow
            Button(
                onClick = onCtaClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(100.dp),
                        spotColor = SproutLeafGreen.copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4C8F58),
                                SproutLeafGreen,
                                SproutLeafDarkGreen
                            )
                        ),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .testTag("btn_sprout_paywall_cta")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        SeedIcon(modifier = Modifier.size(16.dp), tint = Color.White)
                        Text(
                            text = ctaLabel,
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single Feature Bullet
 */
@Composable
private fun PerkRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SproutLeafGreen,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = SproutInkText
            )
        )
    }
}

/**
 * Trust Stamp Item
 */
@Composable
private fun StampItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .border(1.4.dp, SproutInkText.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SproutLeafGreen,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = SproutInkSoftText
            )
        )
    }
}

enum class PlantStageType {
    SEEDLING,
    BLOOM,
    EVERGREEN,
    GERMINATE
}

/**
 * Pure Jetpack Compose Botanical Drawings corresponding to SVG defs in HTML
 */
@Composable
fun PlantStageDrawing(
    type: PlantStageType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (type) {
            PlantStageType.SEEDLING -> {
                // Soil stem
                val stemPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.9f)
                    lineTo(w * 0.5f, h * 0.5f)
                }
                drawPath(stemPath, color = SproutSoilBrown, style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))

                // Left leaf
                val leftLeaf = Path().apply {
                    moveTo(w * 0.5f, h * 0.6f)
                    cubicTo(w * 0.38f, h * 0.56f, w * 0.28f, h * 0.48f, w * 0.25f, h * 0.34f)
                    cubicTo(w * 0.42f, h * 0.36f, w * 0.51f, h * 0.45f, w * 0.52f, h * 0.58f)
                    close()
                }
                drawPath(leftLeaf, color = SproutLeafGreen)
                drawPath(leftLeaf, color = SproutInkText, style = Stroke(width = w * 0.035f))

                // Right leaf
                val rightLeaf = Path().apply {
                    moveTo(w * 0.5f, h * 0.54f)
                    cubicTo(w * 0.60f, h * 0.50f, w * 0.68f, h * 0.42f, w * 0.70f, h * 0.32f)
                    cubicTo(w * 0.55f, h * 0.34f, w * 0.48f, h * 0.42f, w * 0.48f, h * 0.54f)
                    close()
                }
                drawPath(rightLeaf, color = SproutLeafGreen)
                drawPath(rightLeaf, color = SproutInkText, style = Stroke(width = w * 0.035f))
            }
            PlantStageType.BLOOM -> {
                // Soil stem
                val stemPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.9f)
                    lineTo(w * 0.5f, h * 0.42f)
                }
                drawPath(stemPath, color = SproutSoilBrown, style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))

                // Leaves
                val leftLeaf = Path().apply {
                    moveTo(w * 0.5f, h * 0.62f)
                    cubicTo(w * 0.34f, h * 0.58f, w * 0.24f, h * 0.45f, w * 0.22f, h * 0.28f)
                    cubicTo(w * 0.42f, h * 0.30f, w * 0.50f, h * 0.40f, w * 0.51f, h * 0.60f)
                    close()
                }
                drawPath(leftLeaf, color = SproutLeafGreen)
                drawPath(leftLeaf, color = SproutInkText, style = Stroke(width = w * 0.035f))

                val rightLeaf = Path().apply {
                    moveTo(w * 0.5f, h * 0.56f)
                    cubicTo(w * 0.66f, h * 0.50f, w * 0.76f, h * 0.38f, w * 0.78f, h * 0.22f)
                    cubicTo(w * 0.60f, h * 0.25f, w * 0.51f, h * 0.35f, w * 0.49f, h * 0.54f)
                    close()
                }
                drawPath(rightLeaf, color = SproutLeafGreen)
                drawPath(rightLeaf, color = SproutInkText, style = Stroke(width = w * 0.035f))

                // Central Citrus Blossom
                drawCircle(color = SproutCitrusYellow, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.22f))
                drawCircle(color = SproutInkText, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.22f), style = Stroke(width = w * 0.03f))

                // Tomato flower bud
                drawCircle(color = SproutTomatoRed, radius = w * 0.08f, center = Offset(w * 0.36f, h * 0.28f))
                drawCircle(color = SproutInkText, radius = w * 0.08f, center = Offset(w * 0.36f, h * 0.28f), style = Stroke(width = w * 0.025f))

                // Berry flower bud
                drawCircle(color = SproutBerryPurple, radius = w * 0.08f, center = Offset(w * 0.64f, h * 0.28f))
                drawCircle(color = SproutInkText, radius = w * 0.08f, center = Offset(w * 0.64f, h * 0.28f), style = Stroke(width = w * 0.025f))
            }
            PlantStageType.EVERGREEN -> {
                // Trunk
                val trunk = Path().apply {
                    moveTo(w * 0.5f, h * 0.92f)
                    lineTo(w * 0.5f, h * 0.60f)
                }
                drawPath(trunk, color = SproutSoilBrown, style = Stroke(width = w * 0.07f, cap = StrokeCap.Round))

                // Tree Crown
                val crown = Path().apply {
                    moveTo(w * 0.5f, h * 0.08f)
                    cubicTo(w * 0.28f, h * 0.30f, w * 0.18f, h * 0.44f, w * 0.18f, h * 0.58f)
                    cubicTo(w * 0.18f, h * 0.70f, w * 0.34f, h * 0.72f, w * 0.5f, h * 0.64f)
                    cubicTo(w * 0.66f, h * 0.72f, w * 0.82f, h * 0.70f, w * 0.82f, h * 0.58f)
                    cubicTo(w * 0.82f, h * 0.44f, w * 0.72f, h * 0.30f, w * 0.5f, h * 0.08f)
                    close()
                }
                drawPath(crown, color = SproutLeafGreen)
                drawPath(crown, color = SproutInkText, style = Stroke(width = w * 0.035f))

                // Inner crown shadow
                val innerCrown = Path().apply {
                    moveTo(w * 0.5f, h * 0.24f)
                    cubicTo(w * 0.36f, h * 0.38f, w * 0.30f, h * 0.48f, w * 0.30f, h * 0.54f)
                    cubicTo(w * 0.30f, h * 0.60f, w * 0.40f, h * 0.62f, w * 0.5f, h * 0.56f)
                    cubicTo(w * 0.60f, h * 0.62f, w * 0.70f, h * 0.60f, w * 0.70f, h * 0.54f)
                    cubicTo(w * 0.70f, h * 0.48f, w * 0.64f, h * 0.38f, w * 0.5f, h * 0.24f)
                    close()
                }
                drawPath(innerCrown, color = SproutLeafDarkGreen.copy(alpha = 0.55f))

                // Little Fruit Ornaments
                drawCircle(color = SproutTomatoRed, radius = w * 0.045f, center = Offset(w * 0.38f, h * 0.46f))
                drawCircle(color = SproutCitrusYellow, radius = w * 0.045f, center = Offset(w * 0.62f, h * 0.40f))
                drawCircle(color = SproutBerryPurple, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.56f))
            }
            PlantStageType.GERMINATE -> {
                // Soil mound
                drawOval(
                    color = SproutSoilBrown.copy(alpha = 0.3f),
                    topLeft = Offset(w * 0.22f, h * 0.64f),
                    size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.22f)
                )

                // Sprout stem
                val stem = Path().apply {
                    moveTo(w * 0.5f, h * 0.72f)
                    cubicTo(w * 0.48f, h * 0.58f, w * 0.50f, h * 0.48f, w * 0.50f, h * 0.38f)
                }
                drawPath(stem, color = SproutInkText, style = Stroke(width = w * 0.04f, cap = StrokeCap.Round))

                // Small emergent leaf
                val smallLeaf = Path().apply {
                    moveTo(w * 0.5f, h * 0.46f)
                    cubicTo(w * 0.40f, h * 0.45f, w * 0.34f, h * 0.38f, w * 0.32f, h * 0.28f)
                    cubicTo(w * 0.46f, h * 0.29f, w * 0.51f, h * 0.35f, w * 0.52f, h * 0.45f)
                    close()
                }
                drawPath(smallLeaf, color = SproutLeafGreen)
                drawPath(smallLeaf, color = SproutInkText, style = Stroke(width = w * 0.03f))

                // Dashed seed root
                drawOval(
                    color = SproutInkText,
                    topLeft = Offset(w * 0.38f, h * 0.74f),
                    size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.14f),
                    style = Stroke(width = w * 0.03f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)))
                )
            }
        }
    }
}

@Composable
fun SproutAtlasLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val stem = Path().apply {
            moveTo(w * 0.5f, h * 0.88f)
            cubicTo(w * 0.48f, h * 0.65f, w * 0.5f, h * 0.48f, w * 0.5f, h * 0.30f)
        }
        drawPath(stem, color = SproutInkText, style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))

        val leftLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.46f)
            cubicTo(w * 0.34f, h * 0.44f, w * 0.22f, h * 0.32f, w * 0.20f, h * 0.14f)
            cubicTo(w * 0.40f, h * 0.15f, w * 0.51f, h * 0.26f, w * 0.52f, h * 0.44f)
            close()
        }
        drawPath(leftLeaf, color = SproutLeafGreen)
        drawPath(leftLeaf, color = SproutInkText, style = Stroke(width = w * 0.04f))

        val rightLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.40f)
            cubicTo(w * 0.64f, h * 0.36f, w * 0.74f, h * 0.24f, w * 0.76f, h * 0.08f)
            cubicTo(w * 0.58f, h * 0.10f, w * 0.49f, h * 0.20f, w * 0.47f, h * 0.38f)
            close()
        }
        drawPath(rightLeaf, color = SproutLeafGreen)
        drawPath(rightLeaf, color = SproutInkText, style = Stroke(width = w * 0.04f))
    }
}

@Composable
fun SeedIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.3f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.55f)
        )
    }
}

@Composable
fun SparkleIcon(modifier: Modifier = Modifier, tint: Color = SproutBerryPurple) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val star = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.58f, h * 0.40f)
            lineTo(w * 0.90f, h * 0.50f)
            lineTo(w * 0.58f, h * 0.60f)
            lineTo(w * 0.50f, h * 0.92f)
            lineTo(w * 0.42f, h * 0.60f)
            lineTo(w * 0.10f, h * 0.50f)
            lineTo(w * 0.42f, h * 0.40f)
            close()
        }
        drawPath(star, color = tint)
    }
}

/**
 * Sign In step required before trial activation or subscription checkout
 */
@Composable
private fun PaywallAuthStepView(
    selectedPlanId: String,
    authService: FirebaseAuthService?,
    onBack: () -> Unit,
    onAuthSuccess: (SproutUserProfile) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSigningIn by remember { mutableStateOf(false) }

    val planTitle = when (selectedPlanId) {
        "yearly", "annual" -> "Bloom · Annual (14-Day Free Trial)"
        "monthly" -> "Seedling · Monthly"
        "lifetime" -> "Evergreen · Lifetime VIP"
        else -> "Germinate · 14-Day Free Trial"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SproutInkText
                )
            }
            Text(
                text = "Account Sign-In Required",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SproutInkText
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SproutLeafGreen.copy(alpha = 0.14f))
                .border(1.5.dp, SproutLeafGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = SproutLeafDarkGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Sign in to activate $planTitle",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SproutInkText,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please sign in with Google so your free trial, subscription entitlements, and plant diagnosis history can be safely stored and synced across all your devices.",
            style = TextStyle(
                fontSize = 12.5.sp,
                color = SproutInkSoftText,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (authService == null) {
                    Toast.makeText(context, "Authentication service initializing...", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSigningIn = true
                coroutineScope.launch {
                    val result = authService.signInWithGoogle()
                    isSigningIn = false
                    result.onSuccess { profile ->
                        Toast.makeText(context, "Signed in as ${profile.email}", Toast.LENGTH_SHORT).show()
                        onAuthSuccess(profile)
                    }.onFailure { err ->
                        Toast.makeText(context, "Sign-in cancelled or failed: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SproutLeafDarkGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isSigningIn) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Sign In with Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "100% secure via Google Credential Manager",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                color = SproutInkSoftText
            )
        )
    }
}

/**
 * Success Confirmation Screen
 */
@Composable
private fun PaywallSuccessConfirmationView(
    planId: String,
    savedEmail: String,
    expiryDate: String?,
    onDone: () -> Unit
) {
    val planTitle = when (planId) {
        "yearly", "annual" -> "Bloom · Annual Harvest"
        "monthly" -> "Seedling · Monthly"
        "lifetime" -> "Evergreen · Lifetime VIP"
        else -> "Germinate · 14-Day Free Trial"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = SproutLeafDarkGreen
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SproutCitrusYellow, modifier = Modifier.size(14.dp))
                Text(
                    text = "PRO ACTIVATED",
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(SproutLeafGreen.copy(alpha = 0.16f))
                .border(1.6.dp, SproutLeafGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            PlantStageDrawing(
                type = when (planId) {
                    "monthly" -> PlantStageType.SEEDLING
                    "lifetime" -> PlantStageType.EVERGREEN
                    "trial" -> PlantStageType.GERMINATE
                    else -> PlantStageType.BLOOM
                },
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Welcome to Sprout Atlas Pro!",
            style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = SproutInkText)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your access is active and synced to your cloud account.",
            style = TextStyle(fontSize = 13.sp, color = SproutInkSoftText, textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SproutCardSelected,
            border = BorderStroke(1.2.dp, SproutLeafGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Linked Email:", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SproutInkSoftText))
                    Text(savedEmail, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = SproutInkText))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Plan Tier:", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SproutInkSoftText))
                    Text(planTitle, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = SproutLeafDarkGreen))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily AI Quota:", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SproutInkSoftText))
                    Text("500 Queries / Day", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = SproutLeafDarkGreen))
                }
                if (expiryDate != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Until:", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SproutInkSoftText))
                        Text(expiryDate, style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = SproutInkText))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SproutLeafDarkGreen),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Start Exploring Pro Guides 🌱", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
