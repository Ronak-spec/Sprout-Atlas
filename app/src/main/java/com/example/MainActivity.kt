package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ai.GeminiService
import com.example.data.datasource.ProduceCatalog
import com.example.data.firebase.AuthState
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirebaseFirestoreService
import com.example.data.repository.SproutAtlasRepository
import com.example.data.subscription.SubscriptionService
import com.example.ui.components.SproutBottomBar
import com.example.ui.components.SproutTopBar
import com.example.ui.dialogs.AccountSyncDialog
import com.example.ui.dialogs.QuickTourDialog
import com.example.ui.dialogs.QuizDialog
import com.example.ui.dialogs.SproutPaywallDialog
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.GuideDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LabsScreen
import com.example.ui.screens.ViewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SproutPaper

class MainActivity : ComponentActivity() {

    private val repository by lazy { SproutAtlasRepository(applicationContext) }
    private val geminiService = GeminiService()
    private val authService by lazy { FirebaseAuthService(applicationContext) }
    private val firestoreService by lazy { FirebaseFirestoreService(applicationContext) }
    private val subscriptionService by lazy { SubscriptionService(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SproutAtlasApp(
                    repository = repository,
                    geminiService = geminiService,
                    authService = authService,
                    firestoreService = firestoreService,
                    subscriptionService = subscriptionService
                )
            }
        }
    }
}

@Composable
fun SproutAtlasApp(
    repository: SproutAtlasRepository,
    geminiService: GeminiService,
    authService: FirebaseAuthService,
    firestoreService: FirebaseFirestoreService,
    subscriptionService: SubscriptionService
) {
    var currentTab by remember { mutableStateOf("home") } // "home", "explore", "labs", "view"
    var activeGuideId by remember { mutableStateOf<Int?>(null) }
    var exploreInitialCategory by remember { mutableStateOf<String?>(null) }
    var exploreInitialQuery by remember { mutableStateOf<String?>(null) }
    var showQuizDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showTourDialog by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }

    val hasSeenQuickTour by repository.hasSeenQuickTour.collectAsState()
    val quizStreak by repository.quizStreak.collectAsState()
    val todayQuiz = remember { repository.getTodayQuiz() }
    val authState by authService.authState.collectAsState()
    val isAuthenticated = authState is AuthState.Authenticated
    val subState by subscriptionService.subscriptionState.collectAsState()

    // Trigger quick tour on first-time app launch
    LaunchedEffect(hasSeenQuickTour) {
        if (!hasSeenQuickTour) {
            showTourDialog = true
        }
    }

    // Automatically open paywall on app launch after 2 to 3 days (48+ hrs) for free users
    LaunchedEffect(subState.isPro) {
        if (!showTourDialog && subscriptionService.shouldAutoOpenPaywallOnLaunch()) {
            subscriptionService.recordAutoPaywallShown()
            showPaywallDialog = true
        }
    }

    // Automatically attach Firestore listener when authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val user = (authState as AuthState.Authenticated).user
            firestoreService.attachUserListener(user.uid, repository)
            subscriptionService.activateTrialOnSignIn(user.email ?: user.uid)
        } else {
            firestoreService.detachUserListener()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        topBar = {
            SproutTopBar(
                onLogoClick = {
                    activeGuideId = null
                    currentTab = "home"
                },
                onNotificationClick = {
                    showQuizDialog = true
                },
                onAccountClick = {
                    showAccountDialog = true
                },
                onProClick = {
                    showPaywallDialog = true
                },
                isPro = subState.isPro,
                showBack = activeGuideId != null,
                onBack = if (activeGuideId != null) {
                    { activeGuideId = null }
                } else null,
                quizStreak = quizStreak,
                isAuthenticated = isAuthenticated,
                userProfile = (authState as? AuthState.Authenticated)?.user
            )
        },
        bottomBar = {
            if (activeGuideId == null) {
                SproutBottomBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        currentTab = it
                        exploreInitialCategory = null
                        exploreInitialQuery = null
                    }
                )
            }
        },
        containerColor = SproutPaper
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SproutPaper)
        ) {
            if (activeGuideId != null) {
                GuideDetailScreen(
                    produceId = activeGuideId!!,
                    repository = repository,
                    subscriptionService = subscriptionService,
                    onOpenPaywall = { showPaywallDialog = true },
                    onBack = { activeGuideId = null },
                    onOpenQuiz = { showQuizDialog = true },
                    onNavigateToGuide = { id -> activeGuideId = id }
                )
            } else {
                when (currentTab) {
                    "home" -> {
                        HomeScreen(
                            onNavigateToExplore = { filterOrQuery ->
                                if (filterOrQuery != null && ProduceCatalog.categories.contains(filterOrQuery)) {
                                    exploreInitialCategory = filterOrQuery
                                    exploreInitialQuery = null
                                } else {
                                    exploreInitialQuery = filterOrQuery
                                    exploreInitialCategory = null
                                }
                                currentTab = "explore"
                            },
                            onNavigateToGuide = { id ->
                                activeGuideId = id
                            },
                            onOpenQuiz = {
                                showQuizDialog = true
                            },
                            repository = repository
                        )
                    }

                    "explore" -> {
                        ExploreScreen(
                            repository = repository,
                            initialCategory = exploreInitialCategory,
                            initialQuery = exploreInitialQuery,
                            onNavigateToGuide = { id ->
                                activeGuideId = id
                            }
                        )
                    }

                    "labs" -> {
                        LabsScreen(
                            repository = repository,
                            geminiService = geminiService,
                            subscriptionService = subscriptionService,
                            onOpenPaywall = {
                                showPaywallDialog = true
                            },
                            onNavigateToGuideByName = { name ->
                                val matched = ProduceCatalog.allItems.find {
                                    it.name.equals(name, ignoreCase = true) ||
                                            it.name.contains(name, ignoreCase = true)
                                }
                                if (matched != null) {
                                    activeGuideId = matched.id
                                } else {
                                    currentTab = "explore"
                                    exploreInitialQuery = name
                                }
                            }
                        )
                    }

                    "view" -> {
                        ViewScreen(
                            repository = repository,
                            onNavigateToGuide = { id ->
                                activeGuideId = id
                            },
                            onNavigateToExplore = {
                                currentTab = "explore"
                            }
                        )
                    }
                }
            }

            if (showQuizDialog) {
                QuizDialog(
                    question = todayQuiz,
                    currentStreak = quizStreak,
                    onDismiss = { showQuizDialog = false },
                    onCorrectAnswer = { repository.markTodayQuizCompleted() },
                    onNavigateToGuide = { id ->
                        activeGuideId = id
                    }
                )
            }

            if (showAccountDialog) {
                AccountSyncDialog(
                    authService = authService,
                    firestoreService = firestoreService,
                    repository = repository,
                    subscriptionService = subscriptionService,
                    onOpenPaywall = {
                        showAccountDialog = false
                        showPaywallDialog = true
                    },
                    onDismiss = { showAccountDialog = false },
                    onReplayTour = {
                        showAccountDialog = false
                        showTourDialog = true
                    }
                )
            }

            if (showPaywallDialog) {
                SproutPaywallDialog(
                    subscriptionService = subscriptionService,
                    authService = authService,
                    firestoreService = firestoreService,
                    onDismiss = { showPaywallDialog = false }
                )
            }

            if (showTourDialog) {
                QuickTourDialog(
                    onDismiss = {
                        showTourDialog = false
                        repository.setHasSeenQuickTour(true)
                    }
                )
            }
        }
    }
}
}
