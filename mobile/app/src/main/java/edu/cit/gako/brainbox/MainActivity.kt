package edu.cit.gako.brainbox

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import edu.cit.gako.brainbox.app.BrainBoxApp
import edu.cit.gako.brainbox.app.BrainBoxAppController
import edu.cit.gako.brainbox.data.BrainBoxRepository
import edu.cit.gako.brainbox.network.RetrofitClient
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.ui.theme.BrainboxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var controller: BrainBoxAppController
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)
        val repository = BrainBoxRepository(RetrofitClient.apiService, sessionManager)
        controller = BrainBoxAppController(repository, lifecycleScope, ::showMessage)
        credentialManager = CredentialManager.create(this)

        setContent {
            BrainboxTheme {
                BrainBoxApp(
                    state = controller.state,
                    onLogin = controller::handleLogin,
                    onGoogleLogin = { token ->
                        if (token == "__TRIGGER_GOOGLE_SIGN_IN__") {
                            launchGoogleSignIn()
                        } else {
                            controller.handleGoogleLogin(token)
                        }
                    },
                    onRegister = controller::handleRegister,
                    onSendResetCode = controller::handleSendResetCode,
                    onVerifyResetCode = controller::handleVerifyResetCode,
                    onResetPassword = controller::handleResetPassword,
                    onAuthStageChange = controller::handleAuthStageChange,
                    onTabSelected = controller::handleTabSelected,
                    onOpenNotebook = controller::handleOpenNotebook,
                    onCloseNotebookEditor = controller::handleCloseNotebookEditor,
                    onOpenQuiz = controller::handleOpenQuiz,
                    onOpenFlashcardDeck = controller::handleOpenFlashcardDeck,
                    onNotebookEditorLoadingStarted = controller::handleNotebookEditorLoadingStarted,
                    onNotebookEditorReady = controller::handleNotebookEditorReady,
                    onNotebookEditorError = controller::handleNotebookEditorError,
                    onEmbeddedSessionCleared = controller::handleEmbeddedSessionCleared,
                    onExitStudySession = controller::handleExitStudySession,
                    onRecordQuizAttempt = controller::handleRecordQuizAttempt,
                    onRecordFlashcardAttempt = controller::handleRecordFlashcardAttempt,
                    onRefreshHome = controller::refreshHome,
                    onLogout = controller::handleLogout,
                    onFeatureRequest = ::showMessage
                )
            }
        }

        controller.bootstrap()
    }

    private fun launchGoogleSignIn() {
        lifecycleScope.launch {
            val idToken = getGoogleIdToken() ?: return@launch
            controller.handleGoogleLogin(idToken)
        }
    }

    private suspend fun getGoogleIdToken(): String? {
        return try {
            val result = credentialManager.getCredential(
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build(),
                context = this@MainActivity
            )
            extractIdToken(result.credential)
        } catch (_: GetCredentialCancellationException) {
            null
        } catch (_: NoCredentialException) {
            showMessage("No Google account found on this device.")
            null
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Sign-in failed: ${e.message}", e)
            showMessage("Google Sign-In failed. Please try again.")
            null
        }
    }

    private fun extractIdToken(credential: Credential): String? {
        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            showMessage("Unexpected credential type. Try signing in with your username instead.")
            null
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

