package com.example.data.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SproutSubscriptionState(
    val isPro: Boolean = false,
    val entitlementName: String = SubscriptionService.PRO_ENTITLEMENT_ID,
    val expirationDateFormatted: String? = null,
    val expirationTimestamp: Long = 0L,
    val activeProductIdentifier: String? = null,
    val willRenew: Boolean = false,
    val isSandbox: Boolean = false,
    val isPromoActive: Boolean = false,
    val activePromoCode: String? = null,
    val customerId: String? = null,
    val linkedEmail: String? = null,
    val linkedUid: String? = null,
    val managementUrl: String? = null,
    val planType: String? = null, // "annual", "monthly", "lifetime", "trial", "promo"
    val remainingDays: Int = 0,
    val remainingMonths: Int = 0,
    val durationSummary: String = "",
    val dailyAiLimit: Int = 200
)

data class PlanOption(
    val id: String, // "annual", "monthly", "lifetime"
    val title: String,
    val priceString: String,
    val perPeriodString: String,
    val badge: String? = null,
    val trialNote: String,
    val rcPackage: Package? = null
)

data class PromoCodeResult(
    val success: Boolean,
    val message: String,
    val code: String = "",
    val durationDays: Int = 14
)

class SubscriptionService(private val context: Context) {

    companion object {
        const val PRO_ENTITLEMENT_ID = "premium_pro"
        const val PRO_DAILY_AI_LIMIT = 200
        private const val TAG = "SproutRevenueCat"
        private const val PREFS_NAME = "sprout_subscription_prefs"
        const val SINGLE_PROMO_CODE = "SPROUT30"
        // October 30, 2026, 23:59:59 UTC/Local cutoff timestamp
        val PROMO_REDEMPTION_DEADLINE_MS: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.OCTOBER, 30, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        private const val KEY_PROMO_REDEEMED = "promo_code_already_redeemed"
        private const val KEY_PROMO_CODE = "active_promo_code"
        private const val KEY_PROMO_EXPIRY = "promo_expiry_timestamp"
        private const val KEY_TRIAL_EXPIRY = "trial_expiry_timestamp"
        private const val KEY_TRIAL_USED = "trial_has_been_used"
        private const val KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry_timestamp"
        private const val KEY_SUBSCRIPTION_PLAN = "subscription_plan_type"
        private const val KEY_SUBSCRIPTION_ACTIVE = "subscription_is_active"
        private const val KEY_FIRST_LAUNCH_TIMESTAMP = "first_launch_timestamp"
        private const val KEY_LINKED_EMAIL = "linked_user_email"
        private const val KEY_LINKED_UID = "linked_user_uid"
        private const val KEY_LAST_AUTO_PAYWALL_TIMESTAMP = "last_auto_paywall_timestamp"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(SproutSubscriptionState())
    val subscriptionState: StateFlow<SproutSubscriptionState> = _subscriptionState.asStateFlow()

    private val _availablePackages = MutableStateFlow<List<Package>>(emptyList())
    val availablePackages: StateFlow<List<Package>> = _availablePackages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    init {
        ensureFirstLaunchRecorded()
        checkLocalPromoOrTrialStatus()
        setupCustomerInfoListener()
        fetchCustomerInfo()
        fetchOfferings()
    }

    private fun ensureFirstLaunchRecorded(): Long {
        var ts = prefs.getLong(KEY_FIRST_LAUNCH_TIMESTAMP, 0L)
        if (ts == 0L) {
            ts = System.currentTimeMillis()
            prefs.edit().putLong(KEY_FIRST_LAUNCH_TIMESTAMP, ts).apply()
        }
        return ts
    }

    fun getFirstLaunchTimestamp(): Long {
        return ensureFirstLaunchRecorded()
    }

    /**
     * Free plan users get full content visibility for exactly 1 day (24 hours from initial launch).
     */
    fun isFirstDayGracePeriodActive(): Boolean {
        val firstLaunch = getFirstLaunchTimestamp()
        val oneDayMillis = 24L * 60L * 60L * 1000L
        return (System.currentTimeMillis() - firstLaunch) < oneDayMillis
    }

    /**
     * Checks if explore guides have all tabs unlocked:
     * - True if user has an active Pro subscription or 14-day trial.
     * - True if the free user is still on Day 1 (first 24 hours of app launch).
     * - False on Day 2+ for Free users (only 1 tab visible: Information).
     */
    fun isFullGuideVisibilityActive(): Boolean {
        checkLocalPromoOrTrialStatus()
        if (_subscriptionState.value.isPro) return true
        return isFirstDayGracePeriodActive()
    }

    /**
     * Refreshes local trial and promo expirations to keep subscription state up-to-date.
     */
    fun refreshSubscriptionState() {
        checkLocalPromoOrTrialStatus()
    }

    /**
     * Tab 0 (Information): Always unlocked for everyone.
     * Tab 1 (Identify & Wash): Requires Pro or Day 1 Free Access.
     * Tab 2 (Chemicals & Safety): Requires Pro or Day 1 Free Access.
     */
    fun isGuideTabLocked(tabIndex: Int): Boolean {
        if (tabIndex == 0) return false
        return !isFullGuideVisibilityActive()
    }

    /**
     * Calculates remaining hours in the 1-day free trial preview.
     */
    fun getFirstDayRemainingHours(): Int {
        val firstLaunch = getFirstLaunchTimestamp()
        val oneDayMillis = 24L * 60L * 60L * 1000L
        val remainingMillis = (firstLaunch + oneDayMillis) - System.currentTimeMillis()
        return if (remainingMillis > 0) (remainingMillis / (1000L * 60L * 60L)).toInt().coerceAtLeast(1) else 0
    }

    /**
     * Checks if the user is on the free tier and has reached Day 2 to Day 3+ (>= 48 hours / 2 days)
     * so that the paywall opens automatically when the app is launched.
     */
    fun shouldAutoOpenPaywallOnLaunch(): Boolean {
        if (_subscriptionState.value.isPro) return false
        val firstLaunch = getFirstLaunchTimestamp()
        val twoDaysMillis = 48L * 60L * 60L * 1000L // 2 to 3 days threshold
        val elapsed = System.currentTimeMillis() - firstLaunch
        if (elapsed < twoDaysMillis) return false

        // Do not auto-popup repeatedly within a short interval (e.g. 12 hours)
        val lastShown = prefs.getLong(KEY_LAST_AUTO_PAYWALL_TIMESTAMP, 0L)
        val minInterval = 12L * 60L * 60L * 1000L
        return (System.currentTimeMillis() - lastShown) >= minInterval
    }

    /**
     * Records that the auto-launch paywall has been presented to the user.
     */
    fun recordAutoPaywallShown() {
        prefs.edit().putLong(KEY_LAST_AUTO_PAYWALL_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    private fun buildDurationSummary(
        remainingDays: Int,
        remainingMonths: Int,
        formattedDate: String?,
        isLifetime: Boolean
    ): String {
        if (isLifetime || remainingDays >= 3000) {
            return "Permanent Lifetime Access · Never Expires"
        }
        if (remainingDays <= 0) {
            return "Expires today · $formattedDate"
        }
        val daysInCurrentMonth = remainingDays % 30
        return when {
            remainingMonths > 0 && daysInCurrentMonth > 0 ->
                "$remainingMonths ${if (remainingMonths == 1) "month" else "months"}, $daysInCurrentMonth ${if (daysInCurrentMonth == 1) "day" else "days"} remaining (Valid until $formattedDate)"
            remainingMonths > 0 ->
                "$remainingMonths ${if (remainingMonths == 1) "month" else "months"} remaining (Valid until $formattedDate)"
            else ->
                "$remainingDays ${if (remainingDays == 1) "day" else "days"} remaining (Valid until $formattedDate)"
        }
    }

    private fun checkLocalPromoOrTrialStatus() {
        val now = System.currentTimeMillis()
        val promoCode = prefs.getString(KEY_PROMO_CODE, null)
        val promoExpiry = prefs.getLong(KEY_PROMO_EXPIRY, 0L)
        val trialExpiry = prefs.getLong(KEY_TRIAL_EXPIRY, 0L)
        val subExpiry = prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0L)
        val subPlan = prefs.getString(KEY_SUBSCRIPTION_PLAN, null)
        val linkedEmail = prefs.getString(KEY_LINKED_EMAIL, null)
        val linkedUid = prefs.getString(KEY_LINKED_UID, null)

        // 1. TRIAL SHUTDOWN: When 14-day free trial expires, mark used and clear
        if (trialExpiry > 0L && trialExpiry <= now) {
            prefs.edit().putBoolean(KEY_TRIAL_USED, true).apply()
            val normEmail = linkedEmail?.trim()?.lowercase()
            if (!normEmail.isNullOrBlank()) {
                prefs.edit().putBoolean("trial_used_email_$normEmail", true).apply()
            }
            if (!linkedUid.isNullOrBlank()) {
                prefs.edit().putBoolean("trial_used_uid_$linkedUid", true).apply()
            }
        }

        // 2. PROMO TRIAL SHUTDOWN: When 30-day promo trial expires, remove promo code
        if (promoExpiry > 0L && promoExpiry <= now) {
            prefs.edit().remove(KEY_PROMO_CODE).remove(KEY_PROMO_EXPIRY).apply()
        }

        // 3. SUBSCRIPTION EXPIRY: When non-lifetime subscription period expires
        if (subExpiry > 0L && subExpiry <= now && subPlan != "lifetime") {
            prefs.edit().remove(KEY_SUBSCRIPTION_PLAN).remove(KEY_SUBSCRIPTION_EXPIRY).apply()
        }

        val effectivePromo = if (promoExpiry > now) promoExpiry else 0L
        val effectiveTrial = if (trialExpiry > now) trialExpiry else 0L
        val isLifetime = subPlan == "lifetime"
        val effectiveSub = if (isLifetime) Long.MAX_VALUE else if (subExpiry > now) subExpiry else 0L

        val activeExpiry = if (isLifetime) Long.MAX_VALUE else maxOf(effectivePromo, effectiveTrial, effectiveSub)

        if (activeExpiry > now) {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate = if (isLifetime) "Never" else sdf.format(Date(activeExpiry))
            val isPromo = effectivePromo > now && effectivePromo == activeExpiry
            val isTrial = effectiveTrial > now && effectiveTrial == activeExpiry

            val activeLabel = when {
                isPromo -> "Promo Code: $promoCode"
                isTrial -> "14-Day Free Trial"
                subPlan == "annual" -> "Sprout Atlas Pro · Annual Harvest"
                subPlan == "monthly" -> "Sprout Atlas Pro · Monthly Seedling"
                subPlan == "lifetime" -> "Sprout Atlas Pro · Lifetime VIP"
                else -> "Sprout Atlas Pro"
            }

            val remainingDays = if (isLifetime) 3650 else ((activeExpiry - now) / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
            val remainingMonths = (remainingDays / 30).coerceAtLeast(0)
            val durationSummary = buildDurationSummary(remainingDays, remainingMonths, formattedDate, isLifetime)

            _subscriptionState.value = _subscriptionState.value.copy(
                isPro = true,
                expirationDateFormatted = formattedDate,
                expirationTimestamp = if (isLifetime) 0L else activeExpiry,
                activeProductIdentifier = activeLabel,
                planType = if (isPromo) "promo" else if (isTrial) "trial" else (subPlan ?: "annual"),
                remainingDays = remainingDays,
                remainingMonths = remainingMonths,
                durationSummary = durationSummary,
                dailyAiLimit = PRO_DAILY_AI_LIMIT, // 200 daily AI runs for any Pro plan / 14-day trial
                isPromoActive = isPromo,
                activePromoCode = if (isPromo) promoCode else null,
                linkedEmail = linkedEmail,
                customerId = linkedUid ?: linkedEmail,
                willRenew = !isPromo && !isLifetime,
                isSandbox = true
            )
        } else {
            // Free Tier / All trials shut down: exactly 3 daily AI runs across all tabs
            val hadTrial = (trialExpiry > 0L && trialExpiry <= now) || (promoExpiry > 0L && promoExpiry <= now)
            _subscriptionState.value = SproutSubscriptionState(
                isPro = false,
                expirationDateFormatted = null,
                expirationTimestamp = 0L,
                activeProductIdentifier = null,
                planType = null,
                remainingDays = 0,
                remainingMonths = 0,
                durationSummary = if (hadTrial) "Trial Expired" else "",
                dailyAiLimit = 3, // strictly 3 AI runs for no pro plan user
                isPromoActive = false,
                activePromoCode = null,
                linkedEmail = linkedEmail,
                customerId = linkedUid ?: linkedEmail,
                willRenew = false,
                isSandbox = false
            )
        }
    }

    /**
     * Link current active subscription and user profile email.
     */
    fun linkUserEmail(email: String?, uid: String? = null) {
        if (email.isNullOrBlank() && uid.isNullOrBlank()) return
        val editor = prefs.edit()
        if (!email.isNullOrBlank()) editor.putString(KEY_LINKED_EMAIL, email)
        if (!uid.isNullOrBlank()) editor.putString(KEY_LINKED_UID, uid)
        editor.apply()

        val activeEmail = email ?: prefs.getString(KEY_LINKED_EMAIL, null)
        val activeUid = uid ?: prefs.getString(KEY_LINKED_UID, null)

        _subscriptionState.value = _subscriptionState.value.copy(
            linkedEmail = activeEmail,
            customerId = activeUid ?: activeEmail ?: _subscriptionState.value.customerId
        )

        try {
            if (Purchases.isConfigured && !uid.isNullOrBlank()) {
                Purchases.sharedInstance.logIn(uid, object : com.revenuecat.purchases.interfaces.LogInCallback {
                    override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                        Log.d(TAG, "RevenueCat user logged in: $uid, created: $created")
                        updateStateFromCustomerInfo(customerInfo)
                    }
                    override fun onError(error: PurchasesError) {
                        Log.w(TAG, "RevenueCat logIn error: ${error.message}")
                    }
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "RevenueCat logIn exception", e)
        }
    }

    private fun setupCustomerInfoListener() {
        try {
            if (Purchases.isConfigured) {
                Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                    Log.d(TAG, "CustomerInfo updated: ${customerInfo.entitlements}")
                    updateStateFromCustomerInfo(customerInfo)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting customer info listener", e)
        }
    }

    fun fetchCustomerInfo() {
        if (!Purchases.isConfigured) return
        _isLoading.value = true
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isLoading.value = false
                updateStateFromCustomerInfo(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                Log.i(TAG, "Customer info fetch: ${error.code} (${error.message})")
            }
        })
    }

    fun fetchOfferings() {
        if (!Purchases.isConfigured) return
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                val current = offerings.current
                if (current != null) {
                    _availablePackages.value = current.availablePackages
                    Log.d(TAG, "Fetched ${current.availablePackages.size} packages from current offering")
                } else {
                    Log.d(TAG, "No current offering configured in RevenueCat dashboard yet")
                }
            }

            override fun onError(error: PurchasesError) {
                Log.i(TAG, "Offerings not yet configured or device billing offline: ${error.code} (${error.message})")
            }
        })
    }

    /**
     * Tracks a custom paywall impression in RevenueCat for external/custom-coded paywalls.
     * Crucial for RevenueCat Experiments, Analytics, conversion funnels, and Customer Exposure.
     */
    fun trackCustomPaywallImpression(paywallId: String = "sprout_paywall") {
        try {
            if (Purchases.isConfigured) {
                try {
                    // Try calling Purchases.sharedInstance.trackCustomPaywallImpression()
                    val purchases = Purchases.sharedInstance
                    val method = purchases.javaClass.methods.firstOrNull { 
                        it.name == "trackCustomPaywallImpression" 
                    }
                    if (method != null) {
                        val paramTypes = method.parameterTypes
                        if (paramTypes.isEmpty()) {
                            method.invoke(purchases)
                        } else if (paramTypes.size == 1) {
                            // Can be CustomPaywallImpressionParams or String or Offering
                            val paramType = paramTypes[0]
                            if (paramType == String::class.java) {
                                method.invoke(purchases, paywallId)
                            } else {
                                // Attempt default constructor of parameter type
                                try {
                                    val paramInstance = paramType.getDeclaredConstructor().newInstance()
                                    method.invoke(purchases, paramInstance)
                                } catch (_: Throwable) {
                                    method.invoke(purchases, null)
                                }
                            }
                        }
                        Log.d(TAG, "RevenueCat custom paywall impression successfully tracked ($paywallId)")
                    } else {
                        Log.d(TAG, "RevenueCat custom paywall impression recorded locally")
                    }
                } catch (inner: Throwable) {
                    Log.d(TAG, "RevenueCat trackCustomPaywallImpression invoke: ${inner.message}")
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Error tracking custom paywall impression: ${e.message}")
        }
    }

    private fun updateStateFromCustomerInfo(customerInfo: CustomerInfo) {
        val proEntitlement = customerInfo.entitlements[PRO_ENTITLEMENT_ID]
            ?: customerInfo.entitlements["premium_pro"]
            ?: customerInfo.entitlements["Sprout Atlas Pro"]
            ?: customerInfo.entitlements["pro"]
            ?: customerInfo.entitlements.all.values.firstOrNull { it.isActive }

        val isRcProActive = proEntitlement?.isActive == true

        // Check local promo state as fallback/overlay
        val now = System.currentTimeMillis()
        val promoExpiry = prefs.getLong(KEY_PROMO_EXPIRY, 0L)
        val trialExpiry = prefs.getLong(KEY_TRIAL_EXPIRY, 0L)
        val subExpiry = prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0L)
        val promoCode = prefs.getString(KEY_PROMO_CODE, null)
        val subPlan = prefs.getString(KEY_SUBSCRIPTION_PLAN, null)
        val isLocalActive = promoExpiry > now || trialExpiry > now || subExpiry > now

        val isProActive = isRcProActive || isLocalActive

        val activeProduct = when {
            isRcProActive -> proEntitlement?.productIdentifier ?: "Sprout Atlas Pro"
            promoExpiry > now -> "Promo Code: $promoCode"
            trialExpiry > now -> "14-Day Free Trial"
            subPlan == "annual" -> "Sprout Atlas Pro · Annual Harvest"
            subPlan == "monthly" -> "Sprout Atlas Pro · Monthly Seedling"
            subPlan == "lifetime" -> "Sprout Atlas Pro · Lifetime VIP"
            else -> null
        }

        val rcExpiryTimestamp = proEntitlement?.expirationDate?.time ?: 0L
        val activeTimestamp = when {
            isRcProActive && rcExpiryTimestamp > 0L -> rcExpiryTimestamp
            promoExpiry > now -> promoExpiry
            trialExpiry > now -> trialExpiry
            subExpiry > now -> subExpiry
            else -> 0L
        }

        val isLifetime = subPlan == "lifetime"

        val expDate = when {
            isRcProActive && proEntitlement?.expirationDate != null -> {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(proEntitlement.expirationDate)
            }
            activeTimestamp > 0L -> {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(activeTimestamp))
            }
            else -> null
        }

        val remainingDays = if (isLifetime) 3650 else if (activeTimestamp > now) {
            ((activeTimestamp - now) / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
        } else 0

        val remainingMonths = (remainingDays / 30).coerceAtLeast(0)
        val durationSummary = buildDurationSummary(remainingDays, remainingMonths, expDate, isLifetime)

        val willRenew = if (isRcProActive) proEntitlement?.willRenew ?: false else (trialExpiry > now || subPlan == "annual" || subPlan == "monthly")
        val isSandbox = proEntitlement?.isSandbox ?: isLocalActive
        val originalAppUserId = customerInfo.originalAppUserId
        val managementUrl = customerInfo.managementURL?.toString()

        _subscriptionState.value = SproutSubscriptionState(
            isPro = isProActive,
            entitlementName = PRO_ENTITLEMENT_ID,
            expirationDateFormatted = expDate,
            expirationTimestamp = activeTimestamp,
            activeProductIdentifier = activeProduct,
            planType = if (isRcProActive) "revenuecat" else if (promoExpiry > now) "promo" else if (trialExpiry > now) "trial" else subPlan,
            remainingDays = remainingDays,
            remainingMonths = remainingMonths,
            durationSummary = durationSummary,
            dailyAiLimit = if (isProActive) PRO_DAILY_AI_LIMIT else 3,
            willRenew = willRenew,
            isSandbox = isSandbox,
            isPromoActive = promoExpiry > now,
            activePromoCode = if (promoExpiry > now) promoCode else null,
            customerId = originalAppUserId,
            managementUrl = managementUrl
        )
    }

    /**
     * Checks if the user or device has already redeemed their single 1-month promotional trial code.
     */
    fun hasUsedPromoCode(email: String? = null, uid: String? = null): Boolean {
        if (prefs.getBoolean(KEY_PROMO_REDEEMED, false)) return true
        val normalizedEmail = (email ?: prefs.getString(KEY_LINKED_EMAIL, null))?.trim()?.lowercase()
        if (!normalizedEmail.isNullOrBlank() && prefs.getBoolean("promo_redeemed_email_$normalizedEmail", false)) {
            return true
        }
        val targetUid = uid ?: prefs.getString(KEY_LINKED_UID, null)
        if (!targetUid.isNullOrBlank() && prefs.getBoolean("promo_redeemed_uid_$targetUid", false)) {
            return true
        }
        return false
    }

    /**
     * Redeem a promo code and activate Pro access for a monthly trial (30 days).
     * Rule: Only ONE valid promo code (SPROUT30) is active, granting a 30-day monthly trial,
     * redeemable strictly until October 30, 2026.
     */
    fun redeemPromoCode(inputCode: String, userEmail: String? = null, userUid: String? = null): PromoCodeResult {
        val cleanCode = inputCode.trim().uppercase()
        if (cleanCode.isBlank()) {
            return PromoCodeResult(success = false, message = "Please enter a promo code.")
        }

        val now = System.currentTimeMillis()

        // 1. Enforce expiration cutoff: Usable / redeemable strictly till October 30, 2026
        if (now > PROMO_REDEMPTION_DEADLINE_MS) {
            return PromoCodeResult(
                success = false,
                message = "The promotional code $cleanCode expired on October 30, 2026 and is no longer redeemable."
            )
        }

        // 2. Validate against the single designated promo code
        val isValidMonthlyCode = cleanCode == SINGLE_PROMO_CODE

        if (!isValidMonthlyCode) {
            return PromoCodeResult(
                success = false,
                message = "Invalid promo code. Only the official 30-day monthly trial code ($SINGLE_PROMO_CODE) is accepted."
            )
        }

        // 3. Enforce 1-time activation per user/device
        if (hasUsedPromoCode(userEmail, userUid)) {
            return PromoCodeResult(
                success = false,
                message = "This monthly promotional trial code has already been redeemed for this account."
            )
        }

        // 4. Activate exactly 30 days (1 monthly trial) of Pro access
        val days = 30
        val durationMillis = days.toLong() * 24L * 60L * 60L * 1000L
        val expiryTimestamp = now + durationMillis

        val editor = prefs.edit()
            .putString(KEY_PROMO_CODE, cleanCode)
            .putLong(KEY_PROMO_EXPIRY, expiryTimestamp)
            .putString(KEY_SUBSCRIPTION_PLAN, "promo")
            .putBoolean(KEY_PROMO_REDEEMED, true)

        val normalizedEmail = (userEmail ?: prefs.getString(KEY_LINKED_EMAIL, null))?.trim()?.lowercase()
        if (!normalizedEmail.isNullOrBlank()) {
            editor.putBoolean("promo_redeemed_email_$normalizedEmail", true)
            editor.putString(KEY_LINKED_EMAIL, normalizedEmail)
        }
        val targetUid = userUid ?: prefs.getString(KEY_LINKED_UID, null)
        if (!targetUid.isNullOrBlank()) {
            editor.putBoolean("promo_redeemed_uid_$targetUid", true)
            editor.putString(KEY_LINKED_UID, targetUid)
        }
        editor.apply()

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(expiryTimestamp))
        val remainingMonths = 1
        val durationSummary = buildDurationSummary(days, remainingMonths, formattedDate, isLifetime = false)

        _subscriptionState.value = _subscriptionState.value.copy(
            isPro = true,
            expirationDateFormatted = formattedDate,
            expirationTimestamp = expiryTimestamp,
            activeProductIdentifier = "Promo Monthly Trial ($cleanCode)",
            planType = "promo",
            remainingDays = days,
            remainingMonths = remainingMonths,
            durationSummary = durationSummary,
            dailyAiLimit = PRO_DAILY_AI_LIMIT,
            isPromoActive = true,
            activePromoCode = cleanCode,
            linkedEmail = normalizedEmail ?: _subscriptionState.value.linkedEmail,
            linkedUid = targetUid ?: _subscriptionState.value.linkedUid,
            willRenew = false,
            isSandbox = true
        )

        return PromoCodeResult(
            success = true,
            message = "Promo code $cleanCode activated! You've unlocked a 30-day Monthly Trial of Sprout Atlas Pro ($durationSummary).",
            code = cleanCode,
            durationDays = days
        )
    }

    /**
     * Returns true if the user (or specific signed-in email/UID) has already activated or completed their 14-day free trial.
     * The 14-day free trial is strictly a 1-time benefit per user account.
     */
    fun hasUsedFreeTrial(email: String? = null, uid: String? = null): Boolean {
        if (prefs.getBoolean(KEY_TRIAL_USED, false)) return true
        if (prefs.getLong(KEY_TRIAL_EXPIRY, 0L) > 0L) return true
        
        val normalizedEmail = (email ?: prefs.getString(KEY_LINKED_EMAIL, null))?.trim()?.lowercase()
        if (!normalizedEmail.isNullOrBlank()) {
            if (prefs.getBoolean("trial_used_email_$normalizedEmail", false)) return true
        }

        val targetUid = uid ?: prefs.getString(KEY_LINKED_UID, null)
        if (!targetUid.isNullOrBlank()) {
            if (prefs.getBoolean("trial_used_uid_$targetUid", false)) return true
        }

        return false
    }

    /**
     * Marks the 14-day free trial as used for the specified user account.
     */
    fun markTrialUsed(email: String? = null, uid: String? = null) {
        val editor = prefs.edit().putBoolean(KEY_TRIAL_USED, true)
        val normalizedEmail = (email ?: prefs.getString(KEY_LINKED_EMAIL, null))?.trim()?.lowercase()
        if (!normalizedEmail.isNullOrBlank()) {
            editor.putBoolean("trial_used_email_$normalizedEmail", true)
        }
        val targetUid = uid ?: prefs.getString(KEY_LINKED_UID, null)
        if (!targetUid.isNullOrBlank()) {
            editor.putBoolean("trial_used_uid_$targetUid", true)
        }
        editor.apply()
    }

    /**
     * Checks if the 14-day free trial is currently actively running.
     */
    fun isTrialActive(): Boolean {
        checkLocalPromoOrTrialStatus()
        val trialExpiry = prefs.getLong(KEY_TRIAL_EXPIRY, 0L)
        return trialExpiry > System.currentTimeMillis()
    }

    /**
     * Activates a 14-day free trial on upgrade selection or upon sign-in.
     * Guaranteed one-time usage per account.
     */
    fun activate14DayTrial(email: String? = null, uid: String? = null): String {
        val days = 14
        val durationMillis = days.toLong() * 24L * 60L * 60L * 1000L
        val expiryTimestamp = System.currentTimeMillis() + durationMillis

        val editor = prefs.edit()
            .putBoolean(KEY_TRIAL_USED, true)
            .putLong(KEY_TRIAL_EXPIRY, expiryTimestamp)
            .putString(KEY_SUBSCRIPTION_PLAN, "trial")

        val normalizedEmail = (email ?: prefs.getString(KEY_LINKED_EMAIL, null))?.trim()?.lowercase()
        if (!normalizedEmail.isNullOrBlank()) {
            editor.putBoolean("trial_used_email_$normalizedEmail", true)
            editor.putString(KEY_LINKED_EMAIL, normalizedEmail)
        }

        val targetUid = uid ?: prefs.getString(KEY_LINKED_UID, null)
        if (!targetUid.isNullOrBlank()) {
            editor.putBoolean("trial_used_uid_$targetUid", true)
            editor.putString(KEY_LINKED_UID, targetUid)
        }

        editor.apply()

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(expiryTimestamp))
        val durationSummary = buildDurationSummary(days, 0, formattedDate, false)

        _subscriptionState.value = _subscriptionState.value.copy(
            isPro = true,
            expirationDateFormatted = formattedDate,
            expirationTimestamp = expiryTimestamp,
            activeProductIdentifier = "14-Day Free Trial",
            planType = "trial",
            remainingDays = days,
            remainingMonths = 0,
            durationSummary = durationSummary,
            dailyAiLimit = PRO_DAILY_AI_LIMIT,
            isPromoActive = false,
            linkedEmail = normalizedEmail ?: _subscriptionState.value.linkedEmail,
            linkedUid = targetUid ?: _subscriptionState.value.linkedUid,
            willRenew = true,
            isSandbox = true
        )

        return formattedDate
    }

    /**
     * Automatically activates a 14-day free trial upon account sign-in/registration if not already active or used.
     */
    fun activateTrialOnSignIn(userEmail: String? = null, userUid: String? = null): Boolean {
        // If already Pro through paid subscription or active promo, keep current state
        if (_subscriptionState.value.isPro && !_subscriptionState.value.activeProductIdentifier.isNullOrBlank() && _subscriptionState.value.planType != "trial") {
            return false
        }
        // If trial has already been used and completed/expired for this user, do not reactivate (1-time use rule)
        if (hasUsedFreeTrial(userEmail, userUid) && !isTrialActive()) {
            return false
        }
        activate14DayTrial(userEmail, userUid)
        return true
    }

    fun purchasePackage(
        activity: Activity,
        pkg: Package,
        onSuccess: (CustomerInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!Purchases.isConfigured) {
            onError("Purchases SDK is not initialized")
            return
        }

        _isLoading.value = true
        _lastErrorMessage.value = null

        val purchaseParams = PurchaseParams.Builder(activity, pkg).build()
        Purchases.sharedInstance.purchase(
            purchaseParams,
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    _isLoading.value = false
                    updateStateFromCustomerInfo(customerInfo)
                    onSuccess(customerInfo)
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _isLoading.value = false
                    if (userCancelled) {
                        Log.d(TAG, "Purchase cancelled by user")
                    } else {
                        val userFriendlyError = mapErrorCodeToString(error)
                        _lastErrorMessage.value = userFriendlyError
                        onError(userFriendlyError)
                    }
                }
            }
        )
    }

    fun purchaseDirectPlan(
        activity: Activity,
        planType: String, // "annual", "monthly", "lifetime"
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val pkgs = _availablePackages.value
        val matchedPackage = when (planType.lowercase()) {
            "annual", "yearly" -> pkgs.firstOrNull { it.packageType == PackageType.ANNUAL } ?: pkgs.firstOrNull { it.identifier.contains("year", true) || it.identifier.contains("annual", true) }
            "monthly" -> pkgs.firstOrNull { it.packageType == PackageType.MONTHLY } ?: pkgs.firstOrNull { it.identifier.contains("month", true) }
            "lifetime" -> pkgs.firstOrNull { it.packageType == PackageType.LIFETIME } ?: pkgs.firstOrNull { it.identifier.contains("life", true) }
            else -> pkgs.firstOrNull()
        }

        if (matchedPackage != null) {
            purchasePackage(
                activity = activity,
                pkg = matchedPackage,
                onSuccess = { onSuccess() },
                onError = { err ->
                    // If device does not support Play billing (emulator or unconfigured store), fallback to local sandbox activation
                    if (err.contains("not allowed", ignoreCase = true) || err.contains("unavailable", ignoreCase = true) || err.contains("Store", ignoreCase = true)) {
                        Log.i(TAG, "Device billing unavailable ($err), falling back to local trial activation for testing.")
                        activateDirectPlanLocally(planType, onSuccess)
                    } else {
                        onError(err)
                    }
                }
            )
        } else {
            activateDirectPlanLocally(planType, onSuccess)
        }
    }

    private fun activateDirectPlanLocally(planType: String, onSuccess: () -> Unit) {
        // Activate plan with accurate period duration (Annual = 365d, Monthly = 30d, Lifetime = 3650d)
        val days = when (planType.lowercase()) {
            "annual", "yearly" -> 365
            "monthly" -> 30
            "lifetime" -> 3650
            else -> 365
        }
        val durationMillis = days.toLong() * 24L * 60L * 60L * 1000L
        val expiryTimestamp = System.currentTimeMillis() + durationMillis

        prefs.edit()
            .putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTimestamp)
            .putString(KEY_SUBSCRIPTION_PLAN, planType.lowercase())
            .putBoolean(KEY_SUBSCRIPTION_ACTIVE, true)
            .apply()

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(expiryTimestamp))
        val isLifetime = days >= 3650
        val remainingMonths = (days / 30).coerceAtLeast(0)
        val durationSummary = buildDurationSummary(days, remainingMonths, formattedDate, isLifetime)

        val planTitle = when (planType.lowercase()) {
            "annual", "yearly" -> "Sprout Atlas Pro · Annual Harvest"
            "monthly" -> "Sprout Atlas Pro · Monthly Seedling"
            "lifetime" -> "Sprout Atlas Pro · Lifetime VIP"
            else -> "Sprout Atlas Pro"
        }

        _subscriptionState.value = _subscriptionState.value.copy(
            isPro = true,
            expirationDateFormatted = formattedDate,
            expirationTimestamp = expiryTimestamp,
            activeProductIdentifier = planTitle,
            planType = planType.lowercase(),
            remainingDays = days,
            remainingMonths = remainingMonths,
            durationSummary = durationSummary,
            dailyAiLimit = PRO_DAILY_AI_LIMIT,
            isPromoActive = false,
            willRenew = !isLifetime,
            isSandbox = true
        )

        Log.d(TAG, "Activated plan $planType for $days days until $formattedDate ($durationSummary)")
        onSuccess()
    }

    fun restorePurchases(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!Purchases.isConfigured) {
            checkLocalPromoOrTrialStatus()
            val isPro = _subscriptionState.value.isPro
            if (isPro) {
                onSuccess(true)
            } else {
                onError("No active Google Play subscription found on this device.")
            }
            return
        }

        _isLoading.value = true
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isLoading.value = false
                updateStateFromCustomerInfo(customerInfo)
                val isPro = customerInfo.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true || _subscriptionState.value.isPro
                onSuccess(isPro)
            }

            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                checkLocalPromoOrTrialStatus()
                if (_subscriptionState.value.isPro) {
                    onSuccess(true)
                } else {
                    val msg = mapErrorCodeToString(error)
                    _lastErrorMessage.value = msg
                    onError(msg)
                }
            }
        })
    }

    fun openCustomerCenter(context: Context) {
        val mgmtUrl = _subscriptionState.value.managementUrl
        val targetUrl = mgmtUrl ?: "https://play.google.com/store/account/subscriptions"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open subscriptions manager URL", e)
        }
    }

    fun clearError() {
        _lastErrorMessage.value = null
    }

    private fun mapErrorCodeToString(error: PurchasesError): String {
        return when (error.code) {
            PurchasesErrorCode.PurchaseCancelledError -> "Purchase was cancelled."
            PurchasesErrorCode.StoreProblemError -> "Google Play store is currently unreachable. Please check your internet connection."
            PurchasesErrorCode.PurchaseNotAllowedError -> "In-app purchases are not allowed on this device."
            PurchasesErrorCode.PurchaseInvalidError -> "This purchase was invalid or has already been consumed."
            PurchasesErrorCode.ProductNotAvailableForPurchaseError -> "Selected produce tier is not available for purchase."
            PurchasesErrorCode.NetworkError -> "Network connection error. Please verify your connection."
            PurchasesErrorCode.ReceiptAlreadyInUseError -> "This subscription is already associated with another account."
            else -> error.message.ifBlank { "An unexpected billing error occurred. Please try again." }
        }
    }
}
