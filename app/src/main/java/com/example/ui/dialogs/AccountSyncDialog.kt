package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.firebase.AuthState
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirebaseFirestoreService
import com.example.data.repository.SproutAtlasRepository
import com.example.ui.components.EyebrowHeader
import com.example.data.subscription.SubscriptionService
import com.example.ui.components.ProduceDoodle
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AccountSyncDialog(
    authService: FirebaseAuthService,
    firestoreService: FirebaseFirestoreService,
    repository: SproutAtlasRepository,
    subscriptionService: SubscriptionService? = null,
    onOpenPaywall: () -> Unit = {},
    onDismiss: () -> Unit,
    onReplayTour: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val authState by authService.authState.collectAsState()
    val subState by (subscriptionService?.subscriptionState ?: remember { kotlinx.coroutines.flow.MutableStateFlow(com.example.data.subscription.SproutSubscriptionState()) }).collectAsState()
    var isSigningIn by remember { mutableStateOf(false) }
    var customClientIdInput by remember { mutableStateOf("") }
    var showClientIdConfig by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, SproutInk, RoundedCornerShape(24.dp)),
            color = SproutPaper
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EyebrowHeader(text = if (authState is AuthState.Authenticated) "Account Profile" else "Account & Sign-In")
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("account_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SproutInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val state = authState) {
                    is AuthState.Authenticated -> {
                        val user = state.user
                        // User Profile Box
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SproutPaperCard)
                                .border(1.5.dp, SproutLeaf, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = user.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                ProduceDoodle(
                                    symbolId = "d-sprout",
                                    archetype = "leafy",
                                    name = "User",
                                    size = 40.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.displayName ?: "Sprout Explorer",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInk
                            )
                        )

                        if (!user.email.isNullOrBlank()) {
                            Text(
                                text = user.email,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = SproutInkSoft
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // PRO MEMBERSHIP STATUS CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (subState.isPro) Color(0xFFEAF2E6) else SproutPaperCard)
                                .border(1.5.dp, if (subState.isPro) SproutLeaf else SproutLine, RoundedCornerShape(16.dp))
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
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (subState.isPro) Icons.Default.Check else Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = if (subState.isPro) SproutLeafDark else SproutInk,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (subState.isPro) "Sprout Atlas Pro · Active" else "Free Plan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (subState.isPro) SproutLeafDark else SproutInk
                                        )
                                    }

                                    if (!subState.isPro) {
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onOpenPaywall()
                                            },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFA5182F),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Upgrade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (subState.isPro) {
                                    val durationText = subState.durationSummary.ifBlank {
                                        if (!subState.expirationDateFormatted.isNullOrBlank()) "Valid until ${subState.expirationDateFormatted}" else "Active Pro Member"
                                    }
                                    val planName = subState.activeProductIdentifier ?: "Sprout Atlas Pro"
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = "Plan: $planName",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SproutInk
                                        )
                                        Text(
                                            text = "⏳ $durationText",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SproutLeafDark
                                        )
                                        Text(
                                            text = "⚡ 500 daily AI Nutritionist queries, 500 vision scans & all 5 meal planners unlocked.",
                                            fontSize = 11.sp,
                                            color = SproutInkSoft,
                                            lineHeight = 14.5.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { subscriptionService?.openCustomerCenter(context) },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Manage in Google Play", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SproutLeafDark)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Unlock all 5 meal planners, 500 daily AI tutor questions & scanner pathology. Includes 14-Day Free Trial.",
                                        fontSize = 11.sp,
                                        color = SproutInkMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // App Guide & Feature Walkthrough button
                        Button(
                            onClick = {
                                onReplayTour()
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SproutPaperCard,
                                contentColor = SproutInk
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_profile_replay_tour")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "App Guide & Tour",
                                    tint = SproutLeafDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "App Guide & Feature Tour",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = SproutInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sign Out button
                        OutlinedButton(
                            onClick = {
                                authService.signOut()
                                firestoreService.detachUserListener()
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SproutTomato
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutTomato.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_sign_out")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Sign Out",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Sign Out",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    else -> {
                        // Unauthenticated or Error
                        var authTab by remember { mutableStateOf(0) } // 0: Quick / Google, 1: Email
                        var emailInput by remember { mutableStateOf("") }
                        var passwordInput by remember { mutableStateOf("") }
                        var nameInput by remember { mutableStateOf("") }
                        var isRegisterMode by remember { mutableStateOf(false) }

                        ProduceDoodle(
                            symbolId = "d-sprout",
                            archetype = "leafy",
                            name = "Sprout Cloud",
                            size = 64.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sync Your Field Atlas",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutInk
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Backup your 500+ saved guides, rainbow meal logs, quiz streak, and camera scans seamlessly.",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = SproutInkSoft,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        if (state is AuthState.Error) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SproutCautionBg)
                                    .border(1.dp, SproutCautionBorder, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = state.message,
                                    style = TextStyle(
                                        fontSize = 11.5.sp,
                                        color = SproutCautionText,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auth Option Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(100.dp))
                                .background(SproutPaperCard)
                                .border(1.dp, SproutLine.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (authTab == 0) SproutInk else Color.Transparent)
                                    .clickable { authTab = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "1-Tap / Google",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (authTab == 0) SproutPaper else SproutInkSoft
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (authTab == 1) SproutInk else Color.Transparent)
                                    .clickable { authTab = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Email / Pass",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (authTab == 1) SproutPaper else SproutInkSoft
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (authTab == 0) {
                            // Quick 1-Tap Botanist Sign-In
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isSigningIn = true
                                        val result = authService.signInQuickBotanist("Botanical Explorer")
                                        isSigningIn = false
                                        if (result.isSuccess) {
                                            val user = result.getOrThrow()
                                            firestoreService.attachUserListener(user.uid, repository)
                                            firestoreService.syncRepositoryToCloud(user.uid, repository)
                                            subscriptionService?.linkUserEmail(user.email, user.uid)
                                            val activated = subscriptionService?.activateTrialOnSignIn(user.email ?: user.uid) == true
                                            if (activated) {
                                                android.widget.Toast.makeText(context, "Welcome! 14-Day Free Pro Trial Activated 🌱", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SproutLeaf,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_quick_signin")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isSigningIn) "Signing in..." else "1-Tap Instant Sign-In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Google Sign In Button
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isSigningIn = true
                                        val override = if (customClientIdInput.isNotBlank()) customClientIdInput.trim() else null
                                        val result = authService.signInWithGoogle(override)
                                        isSigningIn = false
                                        if (result.isSuccess) {
                                            val user = result.getOrThrow()
                                            firestoreService.attachUserListener(user.uid, repository)
                                            firestoreService.syncRepositoryToCloud(user.uid, repository)
                                            subscriptionService?.linkUserEmail(user.email, user.uid)
                                            val activated = subscriptionService?.activateTrialOnSignIn(user.email ?: user.uid) == true
                                            if (activated) {
                                                android.widget.Toast.makeText(context, "Welcome! 14-Day Free Pro Trial Activated 🌱", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SproutInk
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.4.dp, SproutInk),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_google_signin")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Google 'G' letter icon badge
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(SproutTomato),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    Text(
                                        text = "Sign in with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        } else {
                            // Email & Password Fields
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isRegisterMode) {
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        label = { Text("Display Name", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email", fontSize = 12.sp) },
                                    placeholder = { Text("you@domain.com", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (emailInput.isBlank() || passwordInput.isBlank()) return@Button
                                        coroutineScope.launch {
                                            isSigningIn = true
                                            val res = if (isRegisterMode) {
                                                authService.signUpWithEmail(emailInput, passwordInput, nameInput)
                                            } else {
                                                authService.signInWithEmail(emailInput, passwordInput)
                                            }
                                            isSigningIn = false
                                            if (res.isSuccess) {
                                                val user = res.getOrThrow()
                                                firestoreService.attachUserListener(user.uid, repository)
                                                firestoreService.syncRepositoryToCloud(user.uid, repository)
                                                subscriptionService?.linkUserEmail(user.email, user.uid)
                                                val activated = subscriptionService?.activateTrialOnSignIn(user.email ?: user.uid) == true
                                                if (activated) {
                                                    android.widget.Toast.makeText(context, "Welcome! 14-Day Free Pro Trial Activated 🌱", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isSigningIn && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SproutInk,
                                        contentColor = SproutPaper
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                ) {
                                    Text(
                                        text = if (isSigningIn) "Processing..." else if (isRegisterMode) "Create Account" else "Log In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }

                                TextButton(
                                    onClick = { isRegisterMode = !isRegisterMode },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        text = if (isRegisterMode) "Already have an account? Log in" else "Need an account? Register here",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SproutLeafDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // App Guide & Feature Walkthrough button for guests / unauthenticated users
                        OutlinedButton(
                            onClick = {
                                onReplayTour()
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SproutInk
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, SproutLine),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_unauth_replay_tour")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "App Guide & Tour",
                                    tint = SproutLeafDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "App Guide & Feature Tour",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SproutInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Client ID configuration toggle for developer testing
                        TextButton(
                            onClick = { showClientIdConfig = !showClientIdConfig }
                        ) {
                            Text(
                                text = if (showClientIdConfig) "Hide Web Client ID" else "Web Client ID Config (Optional)",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SproutInkMuted
                                )
                            )
                        }

                        AnimatedVisibility(visible = showClientIdConfig) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = customClientIdInput,
                                    onValueChange = { customClientIdInput = it },
                                    label = { Text("Web Client ID", fontSize = 11.sp) },
                                    placeholder = { Text("xxxx.apps.googleusercontent.com", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
