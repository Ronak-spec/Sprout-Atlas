package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class SproutUserProfile(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false
)

sealed class AuthState {
    object Initializing : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: SproutUserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class FirebaseAuthService(private val context: Context) {

    private val tag = "FirebaseAuthService"

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                Log.w(tag, "FirebaseApp is not initialized. Please ensure google-services.json is present.")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to get FirebaseAuth instance", e)
            null
        }
    }

    private val credentialManager by lazy { CredentialManager.create(context) }

    private val prefs = context.getSharedPreferences("sprout_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentAuth()
    }

    fun checkCurrentAuth() {
        try {
            val current = auth?.currentUser
            if (current != null) {
                _authState.value = AuthState.Authenticated(
                    SproutUserProfile(
                        uid = current.uid,
                        displayName = current.displayName ?: "Botanical Explorer",
                        email = current.email,
                        photoUrl = current.photoUrl?.toString()
                    )
                )
            } else {
                // Check locally saved session
                val savedUid = prefs.getString("auth_user_uid", null)
                val savedName = prefs.getString("auth_user_name", null)
                val savedEmail = prefs.getString("auth_user_email", null)
                if (!savedUid.isNullOrBlank()) {
                    _authState.value = AuthState.Authenticated(
                        SproutUserProfile(
                            uid = savedUid,
                            displayName = savedName ?: "Botanical Explorer",
                            email = savedEmail,
                            photoUrl = null
                        )
                    )
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error checking auth status", e)
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun signInQuickBotanist(name: String = "Botanical Explorer", email: String? = null): Result<SproutUserProfile> {
        return try {
            val firebaseAuth = auth
            val userProfile: SproutUserProfile = if (firebaseAuth != null) {
                try {
                    val anonResult = firebaseAuth.signInAnonymously().await()
                    val user = anonResult.user
                    SproutUserProfile(
                        uid = user?.uid ?: "botanist-${System.currentTimeMillis()}",
                        displayName = name.ifBlank { "Botanical Explorer" },
                        email = email ?: user?.email ?: "explorer@sprout.atlas",
                        photoUrl = null
                    )
                } catch (e: Exception) {
                    Log.w(tag, "Firebase anonymous signin failed, using local persistent account", e)
                    SproutUserProfile(
                        uid = "botanist-${System.currentTimeMillis()}",
                        displayName = name.ifBlank { "Botanical Explorer" },
                        email = email ?: "explorer@sprout.atlas",
                        photoUrl = null
                    )
                }
            } else {
                SproutUserProfile(
                    uid = "botanist-${System.currentTimeMillis()}",
                    displayName = name.ifBlank { "Botanical Explorer" },
                    email = email ?: "explorer@sprout.atlas",
                    photoUrl = null
                )
            }

            prefs.edit()
                .putString("auth_user_uid", userProfile.uid)
                .putString("auth_user_name", userProfile.displayName)
                .putString("auth_user_email", userProfile.email)
                .apply()

            _authState.value = AuthState.Authenticated(userProfile)
            Result.success(userProfile)
        } catch (e: Exception) {
            Log.e(tag, "Quick sign-in error", e)
            val err = e.localizedMessage ?: "Failed to create botanist profile"
            _authState.value = AuthState.Error(err)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<SproutUserProfile> {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            // Local fallback
            return signInQuickBotanist(name = email.substringBefore("@"), email = email)
        }
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                val profile = SproutUserProfile(
                    uid = user.uid,
                    displayName = user.displayName ?: email.substringBefore("@"),
                    email = user.email ?: email,
                    photoUrl = user.photoUrl?.toString()
                )
                prefs.edit()
                    .putString("auth_user_uid", profile.uid)
                    .putString("auth_user_name", profile.displayName)
                    .putString("auth_user_email", profile.email)
                    .apply()
                _authState.value = AuthState.Authenticated(profile)
                Result.success(profile)
            } else {
                val err = "Sign-in returned null user"
                _authState.value = AuthState.Error(err)
                Result.failure(IllegalStateException(err))
            }
        } catch (e: Exception) {
            Log.e(tag, "Email sign-in failed", e)
            val err = e.localizedMessage ?: "Invalid email or password"
            _authState.value = AuthState.Error(err)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<SproutUserProfile> {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return signInQuickBotanist(name = displayName, email = email)
        }
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                val profile = SproutUserProfile(
                    uid = user.uid,
                    displayName = displayName.ifBlank { email.substringBefore("@") },
                    email = user.email ?: email,
                    photoUrl = null
                )
                prefs.edit()
                    .putString("auth_user_uid", profile.uid)
                    .putString("auth_user_name", profile.displayName)
                    .putString("auth_user_email", profile.email)
                    .apply()
                _authState.value = AuthState.Authenticated(profile)
                Result.success(profile)
            } else {
                val err = "Sign-up returned null user"
                _authState.value = AuthState.Error(err)
                Result.failure(IllegalStateException(err))
            }
        } catch (e: Exception) {
            Log.e(tag, "Email sign-up failed", e)
            val err = e.localizedMessage ?: "Registration failed. Try a different password (6+ characters)."
            _authState.value = AuthState.Error(err)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(webClientIdOverride: String? = null): Result<SproutUserProfile> {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            val errorMsg = "Firebase is not configured. Using Quick Botanist Profile."
            return signInQuickBotanist("Google Botanist")
        }

        val clientId = when {
            !webClientIdOverride.isNullOrBlank() -> webClientIdOverride.trim()
            try { BuildConfig::class.java.getField("WEB_CLIENT_ID").get(null) as? String } catch (_: Exception) { null }?.isNotBlank() == true -> {
                val found = try { BuildConfig::class.java.getField("WEB_CLIENT_ID").get(null) as String } catch (_: Exception) { "" }
                if (found.startsWith("YOUR_")) "" else found
            }
            else -> ""
        }

        if (clientId.isBlank()) {
            val errorMsg = "Google OAuth Web Client ID is not configured. Please use Email Login below!"
            _authState.value = AuthState.Error(errorMsg)
            return Result.failure(IllegalArgumentException(errorMsg))
        }

        return try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(clientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    val profile = SproutUserProfile(
                        uid = user.uid,
                        displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Botanical Explorer",
                        email = user.email ?: googleIdTokenCredential.id,
                        photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString()
                    )
                    prefs.edit()
                        .putString("auth_user_uid", profile.uid)
                        .putString("auth_user_name", profile.displayName)
                        .putString("auth_user_email", profile.email)
                        .apply()
                    _authState.value = AuthState.Authenticated(profile)
                    Result.success(profile)
                } else {
                    val err = "Authentication returned null user"
                    _authState.value = AuthState.Error(err)
                    Result.failure(IllegalStateException(err))
                }
            } else {
                val err = "Unexpected credential type: ${credential.type}"
                _authState.value = AuthState.Error(err)
                Result.failure(IllegalStateException(err))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "User cancelled Google Sign-in dialog")
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-in failed", e)
            val err = "Google Sign-in unavailable on this device (${e.localizedMessage ?: "No accounts registered"}). Please use Email Login to connect."
            _authState.value = AuthState.Error(err)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            prefs.edit().clear().apply()
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            Log.e(tag, "Error signing out", e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth?.currentUser
}
